package org.akvo.caddisfly.updater

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri

class UpdateAppReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0))
        context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
        val marketUrl = Uri.parse("market://details?id=" + context.packageName)
        val storeIntent = Intent(Intent.ACTION_VIEW, marketUrl)
        storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(storeIntent)
    }

    companion object {
        private const val EXTRA_NOTIFICATION_ID = "android.intent.extra.NOTIFICATION_ID"
    }
}