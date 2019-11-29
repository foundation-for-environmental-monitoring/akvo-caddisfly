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

package org.akvo.caddisfly.preference;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.helper.ApkHelper;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class TestingPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_testing);
    }

    private void setBackgroundColor(View view) {
        if (AppPreferences.isTestMode()) {
            view.setBackgroundColor(Color.rgb(255, 165, 0));
        } else {
            view.setBackgroundColor(Color.rgb(255, 240, 220));
        }
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setBackgroundColor(view);

        Preference testModeOnPreference = findPreference(getString(R.string.testModeOnKey));
        if (testModeOnPreference != null) {
            testModeOnPreference.setOnPreferenceClickListener(preference -> {
                setBackgroundColor(view);
                return true;
            });
        }

        (new Handler()).postDelayed(() -> {
            Preference nextUpdateCheckPreference = findPreference(getString(R.string.nextUpdateCheckKey));
            if (nextUpdateCheckPreference != null) {
                if (!ApkHelper.isNonStoreVersion(getActivity())) {
                    long nextUpdateTime = PreferencesUtil.getLong(getActivity(), ConstantKey.NEXT_UPDATE_CHECK);
                    String dateString = DateFormat.format("dd/MMM/yyyy hh:mm", new Date(nextUpdateTime)).toString();
                    nextUpdateCheckPreference.setSummary(dateString);
                } else {
                    nextUpdateCheckPreference.setSummary("Not installed from Play store");
                }
            }
        }, 200);
    }
}
