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
package org.akvo.caddisfly.updater

import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import org.akvo.caddisfly.R
import org.akvo.caddisfly.updater.UpdateCheck.setNextUpdateCheck
import org.akvo.caddisfly.util.ApiUtil.getAppVersionCode
import org.akvo.caddisfly.util.NotificationScheduler
import org.akvo.caddisfly.util.PreferencesUtil.getInt
import org.akvo.caddisfly.util.PreferencesUtil.setInt
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL

internal class UpdateCheckTask(context: Context) : AsyncTask<String?, String?, String?>() {
    private val contextRef: WeakReference<Context> = WeakReference(context)
    override fun doInBackground(vararg params: String?): String? {
        val context = contextRef.get()
        var versionCode = 0
        try {
            versionCode = context!!.packageManager
                    .getPackageInfo(context.packageName, 0).versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        if (getInt(contextRef.get(), "serverVersionCode", 0) < versionCode) {
            var connection: HttpURLConnection? = null
            var reader: BufferedReader? = null
            try {
                val url = URL(params[0])
                connection = url.openConnection() as HttpURLConnection
                connection.connect()
                val stream = connection.inputStream
                reader = BufferedReader(InputStreamReader(stream))
                val buffer = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    buffer.append(line).append("\n")
                }
                return buffer.toString()
            } catch (e: IOException) {
                e.printStackTrace()
                setNextUpdateCheck(context!!, AlarmManager.INTERVAL_HALF_HOUR)
            } finally {
                connection?.disconnect()
                try {
                    reader?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        val context = contextRef.get()
        val versionCode = getAppVersionCode(context!!)
        val serverVersion: Int
        try {
            if (result != null) {
                serverVersion = result.trim { it <= ' ' }.toInt()
                if (serverVersion > versionCode) {
                    setInt(context, "serverVersionCode", serverVersion)
                }
            } else {
                serverVersion = getInt(context, "serverVersionCode", 0)
            }
            if (serverVersion > versionCode) {
                NotificationScheduler.showNotification(context,
                        context.getString(R.string.appName),
                    context.getString(R.string.update_available)
                )
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
    }

}