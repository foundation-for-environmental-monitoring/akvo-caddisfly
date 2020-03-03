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
@file:Suppress("DEPRECATION")

package org.akvo.caddisfly.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import android.provider.Settings
import timber.log.Timber
import java.util.*

/**
 * Utility functions for api related actions.
 */
object ApiUtil {
    private const val PREF_UNIQUE_ID = "PREF_UNIQUE_ID"
    private var uniqueID: String? = null

    @JvmStatic
    val cameraInstance: Camera?
        get() {
            var c: Camera? = null
            try {
                c = Camera.open()
            } catch (e: Exception) {
                Timber.e(e)
            }
            return c
        }

    /**
     * Checks if the device has a camera flash.
     *
     * @param context the context
     * @return true if camera flash is available
     */
    @JvmStatic
    fun hasCameraFlash(context: Context, camera: Camera): Boolean {
        var hasFlash = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        try {
            val p: Camera.Parameters
            if (hasFlash) {
                p = camera.parameters
                try {
                    if (p.supportedFlashModes == null) {
                        hasFlash = false
                    } else {
                        if (p.supportedFlashModes.size == 1 && p.supportedFlashModes[0] == "off") {
                            hasFlash = false
                        }
                    }
                } catch (ignored: Exception) { // do nothing
                }
            }
        } finally {
            camera.release()
        }
        return hasFlash
    }

    /**
     * Gets an unique id for installation.
     *
     * @return the unique id
     */
    @Synchronized
    fun getInstallationId(context: Context): String? {
        if (uniqueID == null) {
            val sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE)
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null)
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString()
                val editor = sharedPrefs.edit()
                editor.putString(PREF_UNIQUE_ID, uniqueID)
                editor.apply()
            }
        }
        return uniqueID
    }

    @JvmStatic
    fun startInstalledAppDetailsActivity(context: Activity?) {
        if (context == null) {
            return
        }
        val i = Intent()
        i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        i.addCategory(Intent.CATEGORY_DEFAULT)
        i.data = Uri.parse("package:" + context.packageName)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        context.startActivity(i)
    }

    @JvmStatic
    fun getAppVersionCode(context: Context): Long {
        var versionCode = 0L
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
            } else {
                @Suppress("DEPRECATION")
                versionCode = context.packageManager.getPackageInfo(context.packageName, 0).versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return versionCode
    }
}