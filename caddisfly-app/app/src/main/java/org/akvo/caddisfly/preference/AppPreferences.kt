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

package org.akvo.caddisfly.preference

import android.hardware.Camera
import android.util.Pair
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.R
import org.akvo.caddisfly.app.CaddisflyApp.Companion.app
import org.akvo.caddisfly.common.ChamberTestConfig
import org.akvo.caddisfly.util.PreferencesUtil
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

/**
 * Static functions to get or set values of various preferences.
 */
object AppPreferences {

    @JvmStatic
    fun isDiagnosticMode(): Boolean {
        return PreferencesUtil.getBoolean(
            app?.applicationContext!!,
            R.string.diagnosticModeKey,
            false
        )
    }

    fun enableDiagnosticMode() {
        PreferencesUtil.setBoolean(app?.applicationContext!!, R.string.diagnosticModeKey, true)
    }

    fun disableDiagnosticMode() {
        PreferencesUtil.setBoolean(app?.applicationContext!!, R.string.diagnosticModeKey, false)
        PreferencesUtil.setBoolean(app?.applicationContext!!, R.string.testModeOnKey, false)
        PreferencesUtil.setBoolean(app?.applicationContext!!, R.string.dummyResultKey, false)
    }

    /**
     * The number of photos to take during the test.
     *
     * @return number of samples to take
     */
    val samplingTimes: Int
        get() {
            val samplingTimes: Int = if (isDiagnosticMode()) {
                PreferencesUtil.getString(
                    app?.applicationContext!!,
                    R.string.samplingsTimeKey,
                    java.lang.String.valueOf(ChamberTestConfig.SAMPLING_COUNT_DEFAULT)
                )!!.toInt()
            } else {
                ChamberTestConfig.SAMPLING_COUNT_DEFAULT
            }
            //Add skip count as the first few samples may not be valid
            return samplingTimes + ChamberTestConfig.SKIP_SAMPLING_COUNT
        }

    /**
     * The color distance tolerance for when matching colors.
     *
     * @return the tolerance value
     */
    @JvmStatic
    val colorDistanceTolerance: Int
        get() = if (isDiagnosticMode()) {
            PreferencesUtil.getString(
                app?.applicationContext!!,
                R.string.colorDistanceToleranceKey,
                java.lang.String.valueOf(ChamberTestConfig.MAX_COLOR_DISTANCE_RGB)
            )!!.toInt()
        } else {
            ChamberTestConfig.MAX_COLOR_DISTANCE_RGB
        }

    /**
     * The color distance tolerance for when matching colors.
     *
     * @return the tolerance value
     */
    val averagingColorDistanceTolerance: Int
        get() = try {
            if (isDiagnosticMode()) {
                PreferencesUtil.getString(
                    app?.applicationContext!!,
                    R.string.colorAverageDistanceToleranceKey,
                    java.lang.String.valueOf(ChamberTestConfig.MAX_COLOR_DISTANCE_CALIBRATION)
                )!!.toInt()
            } else {
                ChamberTestConfig.MAX_COLOR_DISTANCE_CALIBRATION
            }
        } catch (e: NullPointerException) {
            ChamberTestConfig.MAX_COLOR_DISTANCE_CALIBRATION
        }

    @JvmStatic
    val isSoundOn: Boolean
        get() = !isDiagnosticMode() || PreferencesUtil.getBoolean(
            app?.applicationContext!!,
            R.string.soundOnKey,
            true
        )

    @JvmStatic
    val showDebugInfo: Boolean
        get() = (isDiagnosticMode()
                && PreferencesUtil.getBoolean(
            app?.applicationContext!!,
            R.string.showDebugMessagesKey,
            false
        ))

    @JvmStatic
    val isTestMode: Boolean
        get() = (isDiagnosticMode()
                && PreferencesUtil.getBoolean(
            app?.applicationContext!!,
            R.string.testModeOnKey,
            false
        ))

    fun returnDummyResults(): Boolean {
        return (isDiagnosticMode()
                && PreferencesUtil.getBoolean(
            app?.applicationContext!!,
            R.string.dummyResultKey,
            false
        ))
    }

    @JvmStatic
    fun useExternalCamera(): Boolean {
        return PreferencesUtil.getBoolean(
            app?.applicationContext!!,
            R.string.useExternalCameraKey,
            false
        )
    }

    @JvmStatic
    fun ignoreTimeDelays(): Boolean {
        return (isDiagnosticMode()
                && PreferencesUtil.getBoolean(
            app?.applicationContext!!,
            R.string.ignoreTimeDelaysKey,
            false
        ))
    }

//    fun useMaxZoom(): Boolean {
//        return (isDiagnosticMode
//                && PreferencesUtil.getBoolean(app, R.string.maxZoomKey, false))
//    }

    @JvmStatic
    val cameraZoom: Int
        get() = if (isDiagnosticMode()) {
            PreferencesUtil.getInt(
                app?.applicationContext!!,
                R.string.cameraZoomPercentKey, 0
            )
        } else {
            0
        }

    @JvmStatic
    val cameraResolution: Pair<Int, Int>
        get() {
            val res = Pair(640, 480)
            return try {
                if (isDiagnosticMode()) {
                    val resolution = PreferencesUtil.getString(
                        app?.applicationContext!!,
                        R.string.cameraResolutionKey, "640-480"
                    )
                    val resolutions = resolution?.split("-")!!.toTypedArray()
                    val widthTemp = resolutions[0].toInt()
                    val heightTemp = resolutions[1].toInt()
                    val width = max(heightTemp, widthTemp)
                    val height = min(heightTemp, widthTemp)
                    Pair(width, height)
                } else {
                    res
                }
            } catch (e: Exception) {
                res
            }
        }

    @JvmStatic
    val cameraCenterOffset: Int
        get() = if (isDiagnosticMode()) {
            PreferencesUtil.getInt(
                app?.applicationContext!!,
                R.string.cameraCenterOffsetKey, 0
            )
        } else {
            0
        }

    @JvmStatic
    fun getCameraFocusMode(focusModes: List<String?>): String {
        var focusMode = ""
        if (isDiagnosticMode()) {
            focusMode = PreferencesUtil.getString(
                app?.applicationContext!!,
                R.string.cameraFocusKey, Camera.Parameters.FOCUS_MODE_INFINITY
            )!!
        }
        return when {
            focusModes.contains(focusMode) -> {
                focusMode
            }
            focusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED) -> {
                Camera.Parameters.FOCUS_MODE_FIXED
            }
            focusModes.contains(Camera.Parameters.FOCUS_MODE_INFINITY) -> {
                Camera.Parameters.FOCUS_MODE_INFINITY
            }
            focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) -> {
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            }
            focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO) -> {
                Camera.Parameters.FOCUS_MODE_AUTO
            }
            else -> {
                ""
            }
        }
    }

    fun isAppUpdateCheckRequired(): Boolean {
        if (BuildConfig.TEST_RUNNING.get()) {
            return true
        }
        val lastCheck = PreferencesUtil.getLong(app, "lastUpdateCheck")
        return TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().timeInMillis - lastCheck) > 0
    }
}