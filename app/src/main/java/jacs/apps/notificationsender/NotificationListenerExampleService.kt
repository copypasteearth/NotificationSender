package jacs.apps.notificationsender

import android.app.Notification
import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import su.litvak.chromecast.api.v2.*


class NotificationListenerExampleService : NotificationListenerService() {
    private var mPreviousNotificationKey: String? = null
    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (TextUtils.isEmpty(mPreviousNotificationKey) || !TextUtils.isEmpty(
                mPreviousNotificationKey
            ) && !sbn.getKey().equals(mPreviousNotificationKey)
        ) {
            mPreviousNotificationKey = sbn.key
            Log.i("NotifyService", "got notification");
            var extras = sbn.notification.extras
            var text = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE)
                .toString() + " " + sbn.getNotification().extras.getCharSequence(
                Notification.EXTRA_TEXT
            ).toString()
            var textLength = text.length
            text = text.replace(" ", "+")
            val url =
                "https://translate.google.com/translate_tts?ie=UTF-8&q=$text&tl=en-us&ttsspeed=1&total=1&idx=0&client=tw-ob&textlen=$textLength&tk=272861.189055"
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    val ipAddress = SharedPreferencesHelper.getDeviceIPAddress(applicationContext)
                    if (ipAddress != "none") {
                        val chromecast = ChromeCast(ipAddress)
                        val status: Status = chromecast.status
                        if (chromecast.isAppAvailable("CC1AD845") && !status.isAppRunning("CC1AD845")) {
                            val app: Application = chromecast.launchApp("CC1AD845")
                        }
                        chromecast.registerListener(ChromeCastSpontaneousEventListener {
                            val data = it.data
                            if(data is MediaStatus){
                                Log.d("listener", data.toString())
                                if(data.idleReason == MediaStatus.IdleReason.FINISHED){
                                    Log.d("listener", "everything stopped and shutdown")
                                    chromecast.stopApp()
                                    chromecast.disconnect()

                                }
                            }
                        })
                        chromecast.load(url)

                    }

                }
            }

            Log.d(
                "notification",
                sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE)
                    .toString() + " " + sbn.getNotification().extras.getCharSequence(
                    Notification.EXTRA_TEXT
                ).toString()
            )
            // Implement what you want here
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Implement what you want here
    }
}