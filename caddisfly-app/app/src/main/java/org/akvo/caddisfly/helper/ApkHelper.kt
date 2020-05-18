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
package org.akvo.caddisfly.helper

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.R
import java.util.*

/**
 * Installation related utility methods.
 */
object ApkHelper {
    /**
     * Checks if app version has expired and if so displays an expiry message and closes activity.
     *
     * @param activity The activity
     * @return True if the app has expired
     */
    @JvmStatic
    fun isAppVersionExpired(activity: Activity): Boolean {
        if (BuildConfig.BUILD_TYPE.equals("release", ignoreCase = true) &&
                isNonStoreVersion(activity)) {
            val marketUrl = Uri.parse("market://details?id=" + activity.packageName)
            val appExpiryDate = GregorianCalendar.getInstance()
            appExpiryDate.time = BuildConfig.BUILD_TIME
            appExpiryDate.add(Calendar.DAY_OF_YEAR, 15)
            if (GregorianCalendar().after(appExpiryDate)) {
                val message = String.format(
                    "%s%n%n%s", activity.getString(R.string.version_has_expired),
                    activity.getString(R.string.uninstall_install_from_store)
                )
                val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
                builder.setTitle(R.string.version_expired)
                        .setMessage(message)
                        .setCancelable(false)
                builder.setPositiveButton(R.string.ok) { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                    activity.startActivity(Intent(Intent.ACTION_VIEW, marketUrl))
                    activity.finish()
                }
                val alertDialog = builder.create()
                alertDialog.show()
                return true
            }
        }
        return false
    }

    /**
     * Checks if the app was installed from the app store or from an install file.
     * source: http://stackoverflow.com/questions/37539949/detect-if-an-app-is-installed-from-play-store
     *
     * @param context The context
     * @return True if app was not installed from the store
     */
    @JvmStatic
    fun isNonStoreVersion(context: Context): Boolean { // Valid installer package names
        val validInstallers: List<String> = ArrayList(
                listOf("com.android.vending", "com.google.android.feedback"))
        try { // The package name of the app that has installed the app
            val installer = context.packageManager.getInstallerPackageName(context.packageName)
            // true if the app has been downloaded from Play Store
            return installer == null || !validInstallers.contains(installer)
        } catch (ignored: Exception) { // do nothing
        }
        return true
    }
}