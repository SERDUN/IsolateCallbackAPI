import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.serdun.online.isolate_tester.PDelegateBackgroundRegisterFlutterApi

class TestForegroundCallServiceReceiver(
    private val api: PDelegateBackgroundRegisterFlutterApi,
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action) {
            TelephonyServiceType.triggertCall.action -> triggerCall(intent.extras)
        }
    }

    private fun triggerCall(extras: Bundle?) {
        extras?.let {
            api.onWakeUpBackgroundHandler(extras.getLong("handler")) {
                println("triggerCall: $it")
            }
        }
    }

    companion object {
        private const val TAG = "TelephonyBackgroundCallkeepReceiver"
    }
}


enum class TelephonyServiceType {
    triggertCall;

    val action: String
        get() = name
}
