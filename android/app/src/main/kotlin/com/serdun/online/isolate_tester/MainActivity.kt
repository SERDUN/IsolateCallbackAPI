package com.serdun.online.isolate_tester

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.serdun.online.isolate_tester.services.TestForegroundCallService
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity : FlutterActivity(), PHostIsolateApi {

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        PHostIsolateApi.setUp(flutterEngine.dartExecutor.binaryMessenger, this)
    }

    override fun registerBackgroundMessageHandler(
        userCallbackHandle: Long,
        bgCallbackHandle: Long,
        callback: (Result<Unit>) -> Unit
    ) {
        TestForegroundCallService.IsolateUserCallbackHandler = userCallbackHandle;
        TestForegroundCallService.IsolatePluginCallbackHandler = bgCallbackHandle;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            TestForegroundCallService.launch(this)
        };
        Toast.makeText(this, "registerBackgroundMessageHandler", Toast.LENGTH_SHORT).show()
    }

    override fun wakeUpBackgroundHandler(callback: (Result<Unit>) -> Unit) {
        Toast.makeText(this, "wakeUpBackgroundHandler", Toast.LENGTH_SHORT).show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            TestForegroundCallService.wakeUp(this)
        }
    }

    override fun tearDownBackgroundHandler(callback: (Result<Unit>) -> Unit) {
        Toast.makeText(this, "tearDownBackgroundHandler", Toast.LENGTH_SHORT).show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            TestForegroundCallService.tearDown(this)
        }
    }

    override fun requestPermissions(callback: (Result<Unit>) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 (API level 33) and above, request notification permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted
                callback(Result.success(Unit))
            } else {
                // Request the POST_NOTIFICATIONS permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            // For lower Android versions, no need to request notification permissions
            callback(Result.success(Unit))
        }
    }

    // Handling the result of permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}