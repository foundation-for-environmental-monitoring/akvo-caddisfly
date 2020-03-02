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
package org.akvo.caddisfly.diagnostic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.preference.AppPreferences
import org.akvo.caddisfly.sensor.chamber.BaseRunTest
import org.akvo.caddisfly.sensor.chamber.RunTest

class ChamberPreviewFragment : BaseRunTest(), RunTest {
    override fun initializeTest() {
        super.initializeTest()
        binding?.imageIllustration?.visibility = View.GONE
        binding?.circleView?.visibility = View.GONE
        if (!cameraStarted) {
            setupCamera()
            turnFlashOn()
            cameraStarted = true
            binding?.startCaptureButton?.visibility = View.VISIBLE
            binding?.startCaptureButton?.setOnClickListener {
                stopPreview()
                turnFlashOff()
                binding?.startCaptureButton?.visibility = View.GONE
                pictureCount = AppPreferences.getSamplingTimes() - 1
                startRepeatingTask()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding?.root
    }

    companion object {
        /**
         * Instance of fragment.
         *
         * @param testInfo the test info
         * @return the fragment
         */
        fun newInstance(testInfo: TestInfo?): ChamberPreviewFragment {
            val fragment = ChamberPreviewFragment()
            val args = Bundle()
            args.putParcelable(ConstantKey.TEST_INFO, testInfo)
            fragment.arguments = args
            return fragment
        }
    }
}