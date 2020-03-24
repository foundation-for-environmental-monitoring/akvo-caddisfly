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

package org.akvo.caddisfly.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.Camera
import androidx.annotation.StringRes
import org.akvo.caddisfly.R
import org.akvo.caddisfly.util.AlertUtil.showAlert
import org.akvo.caddisfly.util.AlertUtil.showError
import org.akvo.caddisfly.util.ApiUtil.cameraInstance
import org.akvo.caddisfly.util.ApiUtil.hasCameraFlash
import org.akvo.caddisfly.util.PreferencesUtil.containsKey
import org.akvo.caddisfly.util.PreferencesUtil.getBoolean
import org.akvo.caddisfly.util.PreferencesUtil.getInt
import org.akvo.caddisfly.util.PreferencesUtil.setBoolean
import org.akvo.caddisfly.util.PreferencesUtil.setInt
import timber.log.Timber
import kotlin.math.ceil

object CameraHelper {
    private const val ONE_MILLION = 1000000f
    private var hasCameraFlash = false

    /**
     * Check if the camera is available.
     *
     * @param context         the context
     * @param onClickListener positive button listener
     * @return true if camera flash exists otherwise false
     */
    private fun getCamera(context: Context,
                          onClickListener: DialogInterface.OnClickListener?): Camera? {
        val camera = cameraInstance
        if (hasFeatureBackCamera(context, onClickListener) && camera == null) {
            val message = String.format("%s%n%n%s",
                    context.getString(R.string.cannotUseCamera),
                    context.getString(R.string.tryRestarting))
            showError(context, R.string.cameraBusy,
                    message, null, R.string.ok, onClickListener, null, null)
            return null
        }
        return camera
    }

    @SuppressLint("UnsupportedChromeOsCameraSystemFeature")
    private fun hasFeatureBackCamera(context: Context,
                                     onClickListener: DialogInterface.OnClickListener?): Boolean {
        val packageManager = context.packageManager
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            showAlert(context, R.string.cameraNotAvailable,
                    R.string.cameraRequired,
                    R.string.ok, onClickListener, null, null)
            return false
        }
        return true
    }

    /**
     * Check if the device has a camera flash.
     *
     * @param context         the context
     * @param onClickListener positive button listener
     * @return true if camera flash exists otherwise false
     */
    fun hasFeatureCameraFlash(context: Context, @StringRes errorTitle: Int,
                              @StringRes buttonText: Int,
                              onClickListener: DialogInterface.OnClickListener?): Boolean {
        if (containsKey(context, R.string.hasCameraFlashKey)) {
            hasCameraFlash = getBoolean(context, R.string.hasCameraFlashKey, false)
        } else {
            val camera = getCamera(context, onClickListener)
            try {
                if (camera != null) {
                    hasCameraFlash = hasCameraFlash(context, camera)
                    setBoolean(context, R.string.hasCameraFlashKey, hasCameraFlash)
                }
            } finally {
                camera?.release()
            }
        }
        if (!hasCameraFlash) {
            showAlert(context, errorTitle,
                    R.string.errorCameraFlashRequired,
                    buttonText, onClickListener, null, null)
        }
        return hasCameraFlash
    }

    fun getMaxSupportedMegaPixelsByCamera(context: Context?): Int {
        var cameraMegaPixels = 0
        if (containsKey(context!!, R.string.cameraMegaPixelsKey)) {
            cameraMegaPixels = getInt(context, R.string.cameraMegaPixelsKey, 0)
        } else {
            val camera = cameraInstance
            try {

                // make sure the camera is not in use
                if (camera != null) {
                    val allParams = camera.parameters
                    for (pictureSize in allParams.supportedPictureSizes) {
                        val sizeInMegaPixel = ceil(pictureSize.width * pictureSize.height / ONE_MILLION.toDouble()).toInt()
                        if (sizeInMegaPixel > cameraMegaPixels) {
                            cameraMegaPixels = sizeInMegaPixel
                        }
                    }
                }
                setInt(context, R.string.cameraMegaPixelsKey, cameraMegaPixels)
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                camera?.release()
            }
        }
        return cameraMegaPixels
    }
}