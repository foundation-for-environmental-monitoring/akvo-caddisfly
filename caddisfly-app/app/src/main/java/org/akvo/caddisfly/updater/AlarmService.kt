package org.akvo.caddisfly.updater

import android.app.AlarmManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.akvo.caddisfly.common.AppConfig
import org.akvo.caddisfly.helper.ApkHelper.isNonStoreVersion
import org.akvo.caddisfly.util.NetUtil.isNetworkAvailable
import java.util.*

class AlarmService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (!isNonStoreVersion(this)) {
            if (isNetworkAvailable(this)) {
                UpdateCheck.setNextUpdateCheck(this, AlarmManager.INTERVAL_DAY)
                val updateCheckTask = UpdateCheckTask(this)
                val todayDate = Calendar.getInstance().time
                updateCheckTask.execute(AppConfig.UPDATE_CHECK_URL + "?" + todayDate.time)
            } else {
                UpdateCheck.setNextUpdateCheck(this, AlarmManager.INTERVAL_HALF_HOUR)
            }
        }
        return START_NOT_STICKY
    }
}