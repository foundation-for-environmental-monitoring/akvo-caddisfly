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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ChamberTestConfig;
import org.akvo.caddisfly.helper.PermissionsDelegate;
import org.akvo.caddisfly.util.AlertUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class DiagnosticPreferenceFragment extends PreferenceFragmentCompat {

    private static final int MAX_TOLERANCE = 399;
    private ListView list;
    private PermissionsDelegate permissionsDelegate;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_diagnostic);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        permissionsDelegate = new PermissionsDelegate(getActivity());

        setupSampleTimesPreference();

        setupDistancePreference();

        setupAverageDistancePreference();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setBackgroundColor(view);
    }

    private void setBackgroundColor(View view) {
        view.setBackgroundColor(Color.rgb(255, 240, 220));
    }

    private void setupSampleTimesPreference() {
        final EditTextPreference sampleTimesPreference =
                findPreference(getString(R.string.samplingsTimeKey));
        if (sampleTimesPreference != null) {

            sampleTimesPreference.setSummary(sampleTimesPreference.getText());

            sampleTimesPreference.setOnPreferenceChangeListener((preference, newValue) -> {

                Object value = newValue;
                try {

                    if (Integer.parseInt(String.valueOf(value)) > ChamberTestConfig.SAMPLING_COUNT_DEFAULT) {
                        value = ChamberTestConfig.SAMPLING_COUNT_DEFAULT;
                    }

                    if (Integer.parseInt(String.valueOf(value)) < 1) {
                        value = 1;
                    }

                } catch (Exception e) {
                    value = ChamberTestConfig.SAMPLING_COUNT_DEFAULT;
                }
                sampleTimesPreference.setText(String.valueOf(value));
                sampleTimesPreference.setSummary(String.valueOf(value));
                return false;
            });
        }
    }

    private void setupDistancePreference() {
        final EditTextPreference distancePreference =
                findPreference(getString(R.string.colorDistanceToleranceKey));
        if (distancePreference != null) {
            distancePreference.setSummary(distancePreference.getText());

            distancePreference.setOnPreferenceChangeListener((preference, newValue) -> {

                Object value = newValue;
                try {
                    if (Integer.parseInt(String.valueOf(value)) > MAX_TOLERANCE) {
                        value = MAX_TOLERANCE;
                    }

                    if (Integer.parseInt(String.valueOf(value)) < 1) {
                        value = 1;
                    }

                } catch (Exception e) {
                    value = ChamberTestConfig.MAX_COLOR_DISTANCE_RGB;
                }
                distancePreference.setText(String.valueOf(value));
                distancePreference.setSummary(String.valueOf(value));
                return false;
            });
        }
    }

    private void setupAverageDistancePreference() {
        final EditTextPreference distancePreference =
                findPreference(getString(R.string.colorAverageDistanceToleranceKey));
        if (distancePreference != null) {
            distancePreference.setSummary(distancePreference.getText());

            distancePreference.setOnPreferenceChangeListener((preference, newValue) -> {

                Object value = newValue;
                try {
                    if (Integer.parseInt(String.valueOf(value)) > MAX_TOLERANCE) {
                        value = MAX_TOLERANCE;
                    }

                    if (Integer.parseInt(String.valueOf(value)) < 1) {
                        value = 1;
                    }

                } catch (Exception e) {
                    value = ChamberTestConfig.MAX_COLOR_DISTANCE_CALIBRATION;
                }
                distancePreference.setText(String.valueOf(value));
                distancePreference.setSummary(String.valueOf(value));
                return false;
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (!permissionsDelegate.resultGranted(requestCode, grantResults)) {
            AlertUtil.showSettingsSnackbar(getActivity(),
                    getActivity().getWindow().getDecorView().getRootView(),
                    getString(R.string.location_permission));
        }
    }
}
