package com.serdun.online.isolate_tester.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.serdun.online.isolate_tester.PDelegateBackgroundRegisterFlutterApi
import com.serdun.online.isolate_tester.R
import io.flutter.FlutterInjector
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor.DartCallback
import io.flutter.view.FlutterCallbackInformation
import java.util.concurrent.atomic.AtomicBoolean

class TestForegroundCallService : Service() {
    private var isManuallyStopped = false
    private var isRunning: AtomicBoolean = AtomicBoolean(false)
    private val notificationId = 1234
    private var mainHandler: Handler? = null



    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        mainHandler = Handler(Looper.getMainLooper())
        startForegroundService()
    }

    override fun onDestroy() {
        stopForeground(true)
        isRunning.set(false)
        backgroundEngine?.serviceControlSurface?.detachFromService()

        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundService() {
        createNotificationChannel()
        val packageName = applicationContext.packageName
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_CANCEL_CURRENT
        }

        val notification = buildNotification()
        val notificationManager = NotificationManagerCompat.from(applicationContext)

        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            try {
                notificationManager.notify(notificationId, notification)
            } catch (e: SecurityException) {
                Log.e(TAG, "Notification permission denied", e)
            }
        } else {
            Log.e(TAG, "Notifications are disabled")
        }

        ServiceCompat.startForeground(
            this, notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun buildNotification(): Notification {
        val notificationBuilder =
            Notification.Builder(applicationContext, NOTIFICATION_CHANNEL).apply {
                setSmallIcon(R.drawable.launch_background)
                setContentTitle("VoIP Service Active")
                setContentText("Maintaining connection for incoming VoIP calls.")
                setAutoCancel(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setCategory(Notification.CATEGORY_MISSED_CALL)
                }
            }
        val notification = notificationBuilder.build()
        notification.flags = notification.flags or NotificationCompat.FLAG_INSISTENT
        return notification
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channelId = NOTIFICATION_CHANNEL
        val channelName = "VoIP Calls"
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        channel.description = "Notifications for maintaining VoIP call connections"
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            TestForegroundCallServiceEnums.Launch.action -> runService()
            TestForegroundCallServiceEnums.WakeUp.action -> wakeUp()
            TestForegroundCallServiceEnums.Teardown.action -> tearDown()

        }
        return START_NOT_STICKY
    }

    private fun wakeUp() {
        runService()

        pDelegateBackgroundRegisterFlutterApi?.onWakeUpBackgroundHandler(IsolateUserCallbackHandler!!) {
            println("triggerCall: $it")
        }
    }

    private fun tearDown() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        stopSelf()
    }


    @SuppressLint("WakelockTimeout")
    private fun runService() {
        if (isRunning.get() || (backgroundEngine != null && !backgroundEngine!!.dartExecutor.isExecutingDart)) {
            Log.v(TAG, "Service already running, using existing service")
            return
        }

        Log.v(TAG, "Starting Flutter engine for background service")
        getLock(applicationContext)?.acquire()

        if (backgroundEngine == null) {
            if (IsolatePluginCallbackHandler != null) {
                startBackgroundIsolate(this, IsolatePluginCallbackHandler!!)

                pDelegateBackgroundRegisterFlutterApi =
                    PDelegateBackgroundRegisterFlutterApi(backgroundEngine!!.dartExecutor.binaryMessenger);

            } else {
                Log.e(TAG, "No callback handle found in preferences")
            }
        } else {
            backgroundEngine!!.serviceControlSurface.attachToService(this, null, true)
        }

        isRunning.set(true)
    }

    private fun startBackgroundIsolate(context: Context, callbackHandle: Long) {
        val flutterLoader = FlutterInjector.instance().flutterLoader()
        if (!flutterLoader.initialized()) {
            flutterLoader.startInitialization(context.applicationContext)
        }
        flutterLoader.ensureInitializationComplete(context.applicationContext, null)

        backgroundEngine = FlutterEngine(context.applicationContext)

        val callbackInformation = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)
        if (callbackInformation != null) {
            val dartCallback = DartCallback(
                context.assets, flutterLoader.findAppBundlePath(), callbackInformation
            )
            backgroundEngine!!.dartExecutor.executeDartCallback(dartCallback)
            backgroundEngine!!.serviceControlSurface.attachToService(this, null, true)
        } else {
            Log.e(TAG, "Invalid callback handle: $callbackHandle")
        }
    }


    companion object {

        private const val NOTIFICATION_CHANNEL = "FOREGROUND_CALL_NOTIFICATION_CHANNEL_ID"
        private const val TAG = "BackgroundService"
        private val LOCK_NAME = (TestForegroundCallService::class.java.name + ".Lock")

        private var backgroundEngine: FlutterEngine? = null
        private var pDelegateBackgroundRegisterFlutterApi: PDelegateBackgroundRegisterFlutterApi? = null;

        var IsolatePluginCallbackHandler: Long? = null
        var IsolateUserCallbackHandler: Long? = null

        @RequiresApi(Build.VERSION_CODES.O)
        private fun communicate(context: Context, action: TestForegroundCallServiceEnums, bundle: Bundle?) {
            val intent = Intent(context, TestForegroundCallService::class.java)
            intent.action = action.action
            bundle?.let { intent.putExtras(it) }
            context.startForegroundService(intent)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun launch(context: Context) {
            communicate(context, TestForegroundCallServiceEnums.Launch, null)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun wakeUp(context: Context) {
            communicate(context, TestForegroundCallServiceEnums.WakeUp, null)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun tearDown(context: Context) {
            communicate(context, TestForegroundCallServiceEnums.Teardown, null)
        }

        @Volatile
        private var lockStatic: PowerManager.WakeLock? = null

        @Synchronized
        fun getLock(context: Context): PowerManager.WakeLock? {
            if (lockStatic == null) {
                val mgr = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME)
                lockStatic?.setReferenceCounted(true)
            }
            return lockStatic
        }
    }
}