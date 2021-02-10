package org.akvo.caddisfly.updater

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Objects.requireNonNull(intent.action) == "android.intent.action.BOOT_COMPLETED") {
//            UpdateCheck.setNextUpdateCheck(context, -1)
        }
    }
}