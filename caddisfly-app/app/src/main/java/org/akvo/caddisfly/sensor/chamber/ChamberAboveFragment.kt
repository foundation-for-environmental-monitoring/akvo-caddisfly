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
package org.akvo.caddisfly.sensor.chamber

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.helper.ShakeDetector
import org.akvo.caddisfly.helper.ShakeDetector.OnNoShakeListener
import org.akvo.caddisfly.helper.ShakeDetector.OnShakeListener
import org.akvo.caddisfly.model.TestInfo

class ChamberAboveFragment : BaseRunTest(), RunTest {
    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private var mShakeDetector: ShakeDetector? = null
    private var mIgnoreShake = false
    private var mWaitingForStillness = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activity != null) {
            //Set up the shake detector
            mSensorManager = activity!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            if (mSensorManager != null) {
                mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            }
            mShakeDetector = ShakeDetector(object : OnShakeListener {
                override fun onShake() {
                    if (mIgnoreShake || mWaitingForStillness) {
                        return
                    }
                    if (activity!!.isDestroyed) {
                        return
                    }
                    mWaitingForStillness = true
                    showError(String.format(TWO_SENTENCE_FORMAT, getString(R.string.errorTestInterrupted),
                            getString(R.string.doNotMoveDeviceDuringTest)), null, activity!!)
                }
            }, object : OnNoShakeListener {
                override fun onNoShake() {
                    if (mWaitingForStillness) {
                        mWaitingForStillness = false
                        dismissShakeAndStartTest()
                    }
                }
            })
            mSensorManager!!.unregisterListener(mShakeDetector)
            mShakeDetector!!.setMinShakeAcceleration(5)
            mShakeDetector!!.setMaxShakeDuration(MAX_SHAKE_DURATION)
        }
    }

    override fun initializeTest() {
        super.initializeTest()
        pictureCount = 0
        binding!!.imageIllustration.visibility = View.VISIBLE
        mSensorManager!!.unregisterListener(mShakeDetector)
        mIgnoreShake = false
        mWaitingForStillness = true
    }

    override fun waitForStillness() {
        mSensorManager!!.registerListener(mShakeDetector, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI)
    }

    private fun dismissShakeAndStartTest() {
        mSensorManager!!.unregisterListener(mShakeDetector)
        binding!!.cameraView.visibility = View.VISIBLE
        binding!!.circleView.visibility = View.VISIBLE
        binding!!.imageIllustration.visibility = View.GONE
        startTest()
    }

    override fun startTest() {
        if (!cameraStarted) {
            mShakeDetector!!.setMinShakeAcceleration(1)
            mShakeDetector!!.setMaxShakeDuration(MAX_SHAKE_DURATION)
            mSensorManager!!.registerListener(mShakeDetector, mAccelerometer,
                    SensorManager.SENSOR_DELAY_UI)
        }
        super.startTest()
        stopPreview()
    }

    override fun releaseResources() {
        mSensorManager!!.unregisterListener(mShakeDetector)
        super.releaseResources()
    }

    companion object {
        private const val MAX_SHAKE_DURATION = 2000
        private const val TWO_SENTENCE_FORMAT = "%s%n%n%s"

        /**
         * Get the instance.
         *
         * @param testInfo The test info
         * @return the instance
         */
        fun newInstance(testInfo: TestInfo?): ChamberAboveFragment {
            val fragment = ChamberAboveFragment()
            val args = Bundle()
            args.putParcelable(ConstantKey.TEST_INFO, testInfo)
            fragment.arguments = args
            return fragment
        }
    }
}