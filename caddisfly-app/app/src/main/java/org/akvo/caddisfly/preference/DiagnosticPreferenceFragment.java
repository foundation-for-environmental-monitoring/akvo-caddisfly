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

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ChamberTestConfig;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.diagnostic.ChamberPreviewActivity;
import org.akvo.caddisfly.helper.CameraHelper;
import org.akvo.caddisfly.helper.PermissionsDelegate;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ListViewUtil;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class DiagnosticPreferenceFragment extends PreferenceFragment {

    private static final int MAX_TOLERANCE = 399;
    private ListView list;
    private PermissionsDelegate permissionsDelegate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_diagnostic);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.card_row, container, false);

        permissionsDelegate = new PermissionsDelegate(getActivity());

        rootView.setBackgroundColor(Color.rgb(255, 240, 220));

        setupSampleTimesPreference();

        setupDistancePreference();

        setupAverageDistancePreference();

        return rootView;
    }

    private void setupCameraPreviewPreference() {
        final Preference cameraPreviewPreference = findPreference("cameraPreview");
        if (cameraPreviewPreference != null) {
            cameraPreviewPreference.setOnPreferenceClickListener(preference -> {
                if (getFragmentManager().findFragmentByTag("diagnosticPreviewFragment") == null) {

                    String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    if (AppPreferences.useExternalCamera()) {
                        permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    }

                    if (permissionsDelegate.hasPermissions(permissions)) {
                        startPreview();
                    } else {
                        permissionsDelegate.requestPermissions(permissions);
                    }
                }
                return true;
            });
        }
    }

    private void startPreview() {
        if (isCameraAvailable()) {

            final TestListViewModel viewModel =
                    ViewModelProviders.of((FragmentActivity) getActivity()).get(TestListViewModel.class);
            TestInfo testInfo;
            try {
                testInfo = viewModel.getTestInfo(Constants.FLUORIDE_ID);
                Intent intent = new Intent(getActivity(), ChamberPreviewActivity.class);
                intent.putExtra(ConstantKey.RUN_TEST, true);
                intent.putExtra(ConstantKey.TEST_INFO, testInfo);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("deprecation")
    private boolean isCameraAvailable() {
        Camera camera = null;
        try {
            camera = CameraHelper.getCamera(getActivity(), (dialogInterface, i) -> dialogInterface.dismiss());

            if (camera != null) {
                return true;
            }

        } finally {
            if (camera != null) {
                camera.release();
            }
        }
        return false;
    }

    private void setupSampleTimesPreference() {
        final EditTextPreference sampleTimesPreference =
                (EditTextPreference) findPreference(getString(R.string.samplingsTimeKey));
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
                (EditTextPreference) findPreference(getString(R.string.colorDistanceToleranceKey));
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
                (EditTextPreference) findPreference(getString(R.string.colorAverageDistanceToleranceKey));
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupCameraPreviewPreference();

        list = view.findViewById(android.R.id.list);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListViewUtil.setListViewHeightBasedOnChildren(list, 0);
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
