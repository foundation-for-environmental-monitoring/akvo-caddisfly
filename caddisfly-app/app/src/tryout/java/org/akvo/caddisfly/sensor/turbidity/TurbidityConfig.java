package org.akvo.caddisfly.sensor.turbidity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.text.format.DateUtils;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.util.PreferencesUtil;

final class TurbidityConfig {
    static final String ACTION_ALARM_RECEIVER = "ACTION_ALARM_RECEIVER";
    private static final int INTENT_REQUEST_CODE = 1000;

    private TurbidityConfig() {
    }

    static void setRepeatingAlarm(Context context, int initialDelay, String uuid) {

        int mDelayMinute = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                R.string.colif_intervalMinutesKey, "1"));

        PendingIntent pendingIntent = getPendingIntent(context, PendingIntent.FLAG_CANCEL_CURRENT, uuid);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long delay = (mDelayMinute * DateUtils.MINUTE_IN_MILLIS);

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (initialDelay > 0) {
                    delay = initialDelay;
                }
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + delay, pendingIntent);
            } else {
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + initialDelay, delay, pendingIntent);
            }
        }
    }

    private static PendingIntent getPendingIntent(Context context, int flag, String uuid) {
        Intent intent = new Intent(context, TurbidityStartReceiver.class);
        intent.setAction(TurbidityConfig.ACTION_ALARM_RECEIVER);
        String savePath = PreferencesUtil.getString(context, R.string.turbiditySavePathKey, "");
        intent.putExtra(ConstantKey.SAVE_FOLDER, savePath);
        intent.putExtra("uuid", uuid);

        return PendingIntent.getBroadcast(context, TurbidityConfig.INTENT_REQUEST_CODE, intent, flag);
    }

//    public static boolean isAlarmRunning(Context context, String uuid) {
//        return getPendingIntent(context, PendingIntent.FLAG_NO_CREATE, uuid) != null;
//    }

    static void stopRepeatingAlarm(Context context, String uuid) {
        PendingIntent pendingIntent = getPendingIntent(context, PendingIntent.FLAG_NO_CREATE, uuid);
        if (pendingIntent != null) {
            pendingIntent.cancel();
        }
    }
}
