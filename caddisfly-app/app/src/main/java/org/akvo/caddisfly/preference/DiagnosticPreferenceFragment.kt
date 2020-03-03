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
package org.akvo.caddisfly.preference

import android.graphics.Color
import android.os.Bundle

import android.view.View
import android.widget.ListView
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.ChamberTestConfig
import org.akvo.caddisfly.util.ListViewUtil

class DiagnosticPreferenceFragment : PreferenceFragmentCompat() {
    private var list: ListView? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_diagnostic)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list = view.findViewById(android.R.id.list)
        view.setBackgroundColor(Color.rgb(255, 240, 220))
        setupSampleTimesPreference()
        setupDistancePreference()
        setupAverageDistancePreference()
    }

    private fun setupSampleTimesPreference() {
        val sampleTimesPreference = findPreference<Preference>(getString(R.string.samplingsTimeKey)) as EditTextPreference
        sampleTimesPreference.summary = sampleTimesPreference.text
        sampleTimesPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
            var value = newValue
            try {
                if (value.toString().toInt() > ChamberTestConfig.SAMPLING_COUNT_DEFAULT) {
                    value = ChamberTestConfig.SAMPLING_COUNT_DEFAULT
                }
                if (value.toString().toInt() < 1) {
                    value = 1
                }
            } catch (e: Exception) {
                value = ChamberTestConfig.SAMPLING_COUNT_DEFAULT
            }
            sampleTimesPreference.text = value.toString()
            sampleTimesPreference.summary = value.toString()
            false
        }
    }

    private fun setupDistancePreference() {
        val distancePreference = findPreference<Preference>(getString(R.string.colorDistanceToleranceKey)) as EditTextPreference
        distancePreference.summary = distancePreference.text
        distancePreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
            var value = newValue
            try {
                if (value.toString().toInt() > MAX_TOLERANCE) {
                    value = MAX_TOLERANCE
                }
                if (value.toString().toInt() < 1) {
                    value = 1
                }
            } catch (e: Exception) {
                value = ChamberTestConfig.MAX_COLOR_DISTANCE_RGB
            }
            distancePreference.text = value.toString()
            distancePreference.summary = value.toString()
            false
        }
    }

    private fun setupAverageDistancePreference() {
        val distancePreference = findPreference<Preference>(getString(R.string.colorAverageDistanceToleranceKey)) as EditTextPreference
        distancePreference.summary = distancePreference.text
        distancePreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
            var value = newValue
            try {
                if (value.toString().toInt() > MAX_TOLERANCE) {
                    value = MAX_TOLERANCE
                }
                if (value.toString().toInt() < 1) {
                    value = 1
                }
            } catch (e: Exception) {
                value = ChamberTestConfig.MAX_COLOR_DISTANCE_CALIBRATION
            }
            distancePreference.text = value.toString()
            distancePreference.summary = value.toString()
            false
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ListViewUtil.setListViewHeightBasedOnChildren(list, 0)
    }

    companion object {
        private const val MAX_TOLERANCE = 399
    }
}