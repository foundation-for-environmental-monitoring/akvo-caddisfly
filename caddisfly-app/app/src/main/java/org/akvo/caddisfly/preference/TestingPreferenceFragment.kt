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
import android.text.format.DateFormat
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.helper.ApkHelper.isNonStoreVersion
import org.akvo.caddisfly.util.PreferencesUtil
import java.util.*

class TestingPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_testing)
    }

    private fun setBackgroundColor(view: View) {
        if (AppPreferences.isTestMode) {
            view.setBackgroundColor(Color.rgb(255, 165, 0))
        } else {
            view.setBackgroundColor(Color.rgb(255, 240, 220))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBackgroundColor(view)
        val testModeOnPreference = findPreference<Preference>(getString(R.string.testModeOnKey))
        if (testModeOnPreference != null) {
            testModeOnPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                setBackgroundColor(view)
                true
            }
        }
        val nextUpdateCheckPreference =
            findPreference<Preference>(getString(R.string.nextUpdateCheckKey))
        if (nextUpdateCheckPreference != null) {
            if (!isNonStoreVersion(requireContext())) {
                val nextUpdateTime =
                    PreferencesUtil.getLong(activity, ConstantKey.NEXT_UPDATE_CHECK)
                val dateString =
                    DateFormat.format("dd/MMM/yyyy hh:mm", Date(nextUpdateTime)).toString()
                nextUpdateCheckPreference.summary = dateString
            } else {
                nextUpdateCheckPreference.summary = "Not installed from Play store"
            }
        }
    }
}