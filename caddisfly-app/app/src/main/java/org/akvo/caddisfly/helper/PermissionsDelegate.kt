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
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionsDelegate(private val activity: Activity) {
    fun hasPermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            val permissionCheckResult = ContextCompat.checkSelfPermission(
                activity, permission
            )
            if (permissionCheckResult != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun requestPermissions(permissions: Array<String>?) {
        ActivityCompat.requestPermissions(
                activity,
                permissions!!,
                REQUEST_CODE
        )
    }

    fun requestPermissions(permissions: Array<String>?, requestCode: Int) {
        ActivityCompat.requestPermissions(
                activity,
                permissions!!,
                requestCode
        )
    }

    fun resultGranted(grantResults: IntArray): Boolean {
        if (grantResults.isEmpty()) {
            return false
        }
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    companion object {
        private const val REQUEST_CODE = 100
    }

}