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

package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.util.ListViewUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class LiquidTimeLapsePreferenceFragment extends PreferenceFragment {

    private static final int MAX_SAMPLE_NUMBER = 50;
    private ListView list;

    public LiquidTimeLapsePreferenceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_fluoride);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.card_row, container, false);

        final EditTextPreference sampleIntervalPreference =
                (EditTextPreference) findPreference(getString(R.string.fluor_IntervalMinutesKey));

        if (sampleIntervalPreference != null) {

            sampleIntervalPreference.setSummary(String.format("Every %s minutes", sampleIntervalPreference.getText()));

            sampleIntervalPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {

                        if (Integer.parseInt(String.valueOf(newValue)) > 360) {
                            newValue = 360;
                        }

                        if (Integer.parseInt(String.valueOf(newValue)) < 2) {
                            newValue = 2;
                        }
                    } catch (Exception e) {
                        newValue = 2;
                    }

                    sampleIntervalPreference.setText(String.valueOf(newValue));
                    sampleIntervalPreference.setSummary(String.format("Every %s minutes", newValue));
                    return false;
                }
            });
        }

        final EditTextPreference samplesPreference =
                (EditTextPreference) findPreference(getString(R.string.fluor_NumberOfSamplesKey));
        if (samplesPreference != null) {
            samplesPreference.setSummary(samplesPreference.getText());

            samplesPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        if (Integer.parseInt(String.valueOf(newValue)) > MAX_SAMPLE_NUMBER) {
                            newValue = MAX_SAMPLE_NUMBER;
                        }

                        if (Integer.parseInt(String.valueOf(newValue)) < 1) {
                            newValue = 1;
                        }
                    } catch (NumberFormatException e) {
                        newValue = 1;
                    }

                    samplesPreference.setText(String.valueOf(newValue));
                    samplesPreference.setSummary(String.valueOf(newValue));
                    return false;
                }
            });
        }

        final Preference rgbPreference =
                findPreference(getString(R.string.ledRgbKey));
        if (rgbPreference != null) {
            String rgb = PreferencesUtil.getString(getActivity(),
                    CaddisflyApp.getApp().getCurrentTestInfo().getId(),
                    R.string.ledRgbKey, "15,15,15");

            rgbPreference.setSummary(rgb);
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = view.findViewById(android.R.id.list);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListViewUtil.setListViewHeightBasedOnChildren(list, 0);
    }
}
