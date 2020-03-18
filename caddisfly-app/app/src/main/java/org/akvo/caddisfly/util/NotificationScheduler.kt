/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */
package org.akvo.caddisfly.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.akvo.caddisfly.R
import org.akvo.caddisfly.updater.NotificationCancelReceiver
import org.akvo.caddisfly.updater.UpdateAppReceiver

object NotificationScheduler {
    private const val EXTRA_NOTIFICATION_ID = "android.intent.extra.NOTIFICATION_ID"
    private const val DAILY_REMINDER_REQUEST_CODE = 100
    fun showNotification(context: Context, title: String?, content: String?) {
        val updateIntent = Intent(context, UpdateAppReceiver::class.java)
        updateIntent.action = DAILY_REMINDER_REQUEST_CODE.toString()
        updateIntent.putExtra(EXTRA_NOTIFICATION_ID, DAILY_REMINDER_REQUEST_CODE)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, updateIntent, 0)
        val snoozeIntent = Intent(context, NotificationCancelReceiver::class.java)
        snoozeIntent.action = DAILY_REMINDER_REQUEST_CODE.toString()
        snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, DAILY_REMINDER_REQUEST_CODE)
        val snoozePendingIntent = PendingIntent.getBroadcast(context, 0, snoozeIntent, 0)
        val builder = NotificationCompat.Builder(context)
        val notification = builder.setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
                .setColor(ContextCompat.getColor(context, R.color.accentColor))
                .addAction(0, "Later", snoozePendingIntent)
                .addAction(0, "Update", pendingIntent)
                .setContentIntent(pendingIntent).build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(DAILY_REMINDER_REQUEST_CODE, notification)
    }
}