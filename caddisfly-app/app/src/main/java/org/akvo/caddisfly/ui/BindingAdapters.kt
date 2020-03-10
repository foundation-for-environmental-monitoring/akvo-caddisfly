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

import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import org.akvo.caddisfly.R
import org.akvo.caddisfly.app.CaddisflyApp.Companion.db
import org.akvo.caddisfly.helper.SwatchHelper.isCalibrationComplete
import org.akvo.caddisfly.helper.SwatchHelper.isSwatchListValid
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.preference.AppPreferences.isDiagnosticMode
import java.util.*

object BindingAdapters {
    @JvmStatic
    @BindingAdapter("background")
    fun setBackground(view: View, @Suppress("UNUSED_PARAMETER") dummy: String?) {
        if (isDiagnosticMode()) {
            view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.diagnostic))
        } else {
            val typedValue = TypedValue()
            view.context.theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
            val color = typedValue.data
            view.setBackgroundColor(color)
        }
    }

    /**
     * Validate the calibration.
     *
     * @param view     the view
     * @param testInfo the test
     */
    @JvmStatic
    @BindingAdapter("checkValidity")
    fun validateCalibration(view: TextView, testInfo: TestInfo) {
        val context = view.context
        val calibrationDetail = db?.calibrationDao()!!.getCalibrationDetails(testInfo.uuid)
        if (calibrationDetail != null) {
            val milliseconds = calibrationDetail.expiry
            if (milliseconds > 0 && milliseconds <= Date().time) {
                view.text = String.format("%s. %s", context.getString(R.string.expired),
                        context.getString(R.string.calibrateWithNewReagent))
                view.visibility = View.VISIBLE
                return
            }
        }
        if (isCalibrationComplete(testInfo.swatches)
                && !isSwatchListValid(testInfo)) {
            //Display error if calibration is completed but invalid
            view.text = String.format("%s. %s",
                    context.getString(R.string.calibrationIsInvalid),
                    context.getString(R.string.tryRecalibrating))
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }
}