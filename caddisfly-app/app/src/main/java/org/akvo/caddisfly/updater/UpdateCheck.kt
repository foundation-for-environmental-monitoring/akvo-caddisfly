package org.akvo.caddisfly.updater

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.helper.ApkHelper.isNonStoreVersion
import org.akvo.caddisfly.util.PreferencesUtil.getLong
import org.akvo.caddisfly.util.PreferencesUtil.setLong
import kotlin.math.max

object UpdateCheck {
    /**
     * Setup alarm manager to check for app updates.
     *
     * @param context  the Context
     * @param interval wait time before next check
     */
    @JvmStatic
    fun setNextUpdateCheck(context: Context, interval: Long) {
        if (!isNonStoreVersion(context)) {
            if (interval > -1) {
                setLong(context, ConstantKey.NEXT_UPDATE_CHECK,
                        System.currentTimeMillis() + interval)
            }
            val alarmIntent = PendingIntent.getService(context, 0,
                    Intent(context, AlarmService::class.java), 0)
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val nextUpdateTime = max(getLong(context, ConstantKey.NEXT_UPDATE_CHECK),
                    System.currentTimeMillis() + 10000)
            manager.setInexactRepeating(AlarmManager.RTC, nextUpdateTime,
                    AlarmManager.INTERVAL_DAY, alarmIntent)
        }
    }
}