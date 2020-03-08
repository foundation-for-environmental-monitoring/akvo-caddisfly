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

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Implements SensorEventListener for receiving notifications from the SensorManager when
 * sensor values have changed.
 *
 *
 * http://stackoverflow.com/questions/2317428/android-i-want-to-shake-it
 */
class ShakeDetector // Constructor that sets the shake listener
(// OnShakeListener that will be notified when the shake is detected
        private val mShakeListener: OnShakeListener, // OnShakeListener that will be notified when the no shake is detected
        private val mNoShakeListener: OnNoShakeListener) : SensorEventListener {
    // Arrays to store gravity and linear acceleration values
    private val mGravity = floatArrayOf(0.0f, 0.0f, 0.0f)
    private val mLinearAcceleration = floatArrayOf(0.0f, 0.0f, 0.0f)

    // Minimum acceleration needed to count as a shake movement
    private var minShakeAcceleration = 5.0

    // Maximum time (in milliseconds) for the whole shake to occur
    private var maxShakeDuration = 0

    // Start time for the shake detection
    private var startTime: Long = 0
    private var noShakeStartTime: Long = 0

    // Counter for shake movements
    private var moveCount = 0
    private var previousNoShake: Long = 0
    override fun onSensorChanged(event: SensorEvent) {
        setCurrentAcceleration(event)
        val maxLinearAcceleration = maxCurrentLinearAcceleration

        //stackoverflow.com/questions/11175599/how-to-measure-the-tilt-of-the-phone-in-xy-plane-
        // using-accelerometer-in-android/15149421#15149421
        val g: FloatArray = event.values.clone()
        val normal = sqrt(g[0] * g[0] + g[1] * g[1] + (g[2] * g[2]).toDouble()).toFloat()

        // Normalize the accelerometer vector
        g[0] = g[0] / normal
        g[1] = g[1] / normal
        g[2] = g[2] / normal
        val inclination = Math.toDegrees(acos(g[2].toDouble())).roundToInt()

        // check inclination to detect if the phone is placed face down on a flat surface
        if (inclination > MIN_INCLINATION) {
            synchronized(this) {
                val nowNoShake = System.currentTimeMillis()
                if (abs(maxLinearAcceleration) < MAX_SHAKE_ACCELERATION) {
                    val elapsedNoShakeTime = nowNoShake - noShakeStartTime
                    if (elapsedNoShakeTime > maxShakeDuration) {
                        noShakeStartTime = nowNoShake
                        if (System.currentTimeMillis() - previousNoShake > MIN_NO_SHAKE_DURATION) {
                            previousNoShake = System.currentTimeMillis()
                            mNoShakeListener.onNoShake()
                        }
                    }
                } else {
                    noShakeStartTime = nowNoShake
                }
            }
        }
        synchronized(this) {

            // Check if the acceleration is greater than our minimum threshold
            if (maxLinearAcceleration > minShakeAcceleration) {
                val now = System.currentTimeMillis()

                // Set the startTime if it was reset to zero
                if (startTime == 0L) {
                    startTime = now
                }
                val elapsedTime = now - startTime

                // Check if we're still in the shake window we defined
                if (elapsedTime > maxShakeDuration) {
                    // Too much time has passed. Start over!
                    resetShakeDetection()
                } else {
                    // Keep track of all the movements
                    moveCount++

                    // Check if enough movements have been made to qualify as a shake
                    if (moveCount > MIN_MOVEMENTS) {
                        // Reset for the next one!
                        resetShakeDetection()

                        // It's a shake! Notify the listener.
                        mShakeListener.onShake()
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Nothing to do here
    }

    private fun setCurrentAcceleration(event: SensorEvent) {
        // BEGIN SECTION from Android dev site. This code accounts for
        // gravity using a high-pass filter
        val alpha = 0.8f

        // Gravity components of x, y, and z acceleration
        mGravity[X] = alpha * mGravity[X] + (1 - alpha) * event.values[X]
        mGravity[Y] = alpha * mGravity[Y] + (1 - alpha) * event.values[Y]
        mGravity[Z] = alpha * mGravity[Z] + (1 - alpha) * event.values[Z]

        // Linear acceleration along the x, y, and z axes (gravity effects removed)
        mLinearAcceleration[X] = event.values[X] - mGravity[X]
        mLinearAcceleration[Y] = event.values[Y] - mGravity[Y]
        mLinearAcceleration[Z] = event.values[Z] - mGravity[Z]

        // END SECTION from Android developer site
    }

    // Start by setting the value to the x value

    // Check if the y value is greater

    // Check if the z value is greater

    // Return the greatest value
    private val maxCurrentLinearAcceleration: Float
        get() {
            // Start by setting the value to the x value
            var maxLinearAcceleration = mLinearAcceleration[X]

            // Check if the y value is greater
            if (mLinearAcceleration[Y] > maxLinearAcceleration) {
                maxLinearAcceleration = mLinearAcceleration[Y]
            }

            // Check if the z value is greater
            if (mLinearAcceleration[Z] > maxLinearAcceleration) {
                maxLinearAcceleration = mLinearAcceleration[Z]
            }

            // Return the greatest value
            return maxLinearAcceleration
        }

    private fun resetShakeDetection() {
        startTime = 0
        moveCount = 0
    }

    fun setMinShakeAcceleration(minShakeAcceleration: Int) {
        synchronized(this) { this.minShakeAcceleration = minShakeAcceleration.toDouble() }
    }

    fun setMaxShakeDuration(maxShakeDuration: Int) {
        synchronized(this) { this.maxShakeDuration = maxShakeDuration }
    }

    /**
     * If the device has been shaken.
     */
    interface OnShakeListener {
        fun onShake()
    }

    /**
     * If the device is still for a while.
     */
    interface OnNoShakeListener {
        fun onNoShake()
    }

    companion object {
        // Max to determine if the phone is not moving
        private const val MAX_SHAKE_ACCELERATION = 0.35f

        // Minimum number of movements to register a shake
        private const val MIN_MOVEMENTS = 10

        // Indexes for x, y, and z values
        private const val X = 0
        private const val Y = 1
        private const val Z = 2
        private const val MIN_INCLINATION = 172
        private const val MIN_NO_SHAKE_DURATION = 400
    }

}