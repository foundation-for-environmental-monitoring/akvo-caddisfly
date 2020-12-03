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
/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.akvo.caddisfly.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.databinding.FragmentTestDetailBinding
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.preference.AppPreferences
import org.akvo.caddisfly.viewmodel.TestInfoViewModel

class TestInfoFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View { // Inflate this data b layout
        val b: FragmentTestDetailBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_test_detail, container, false
        )
        val model = ViewModelProvider(this).get(TestInfoViewModel::class.java)
        if (arguments != null) {
            val testInfo: TestInfo? = requireArguments().getParcelable(ConstantKey.TEST_INFO)
            if (testInfo != null) {
                if (AppPreferences.isDiagnosticMode()) {
                    b.swatchView.visibility = View.VISIBLE
                    b.swatchView.setTestInfo(testInfo)
                }
                model.setTest(testInfo)
                b.testInfoViewModel = model
                b.testInfo = testInfo
                if (testInfo.instructions == null || testInfo.instructions!!.isEmpty()) {
                    b.buttonInstructions.visibility = View.GONE
                }
            }
        }
        return b.root
    }

    companion object {
        /**
         * Creates test fragment for specific test.
         */
        fun getInstance(testInfo: TestInfo?): TestInfoFragment {
            val fragment = TestInfoFragment()
            val args = Bundle()
            args.putParcelable(ConstantKey.TEST_INFO, testInfo)
            fragment.arguments = args
            return fragment
        }
    }
}