package org.akvo.caddisfly.updater

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationCancelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0))
    }

    companion object {
        private const val EXTRA_NOTIFICATION_ID = "android.intent.extra.NOTIFICATION_ID"
    }
}