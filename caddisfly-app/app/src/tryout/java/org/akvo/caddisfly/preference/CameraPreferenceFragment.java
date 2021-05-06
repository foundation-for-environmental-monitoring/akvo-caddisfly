package org.akvo.caddisfly.preference;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.diagnostic.ChamberPreviewActivity;
import org.akvo.caddisfly.helper.CameraHelper;
import org.akvo.caddisfly.helper.PermissionsDelegate;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

public class CameraPreferenceFragment extends PreferenceFragmentCompat {

    //    private RecyclerView list;
    private PermissionsDelegate permissionsDelegate;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_camera);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setBackgroundColor(Color.rgb(255, 240, 220));
//        list = view.findViewById(R.id.recycler_view);

//                (new Handler()).postDelayed(() -> {

//            setupZoomPreference();
//
//            setupOffsetPreference();

//            setupCameraPreference();

//        }, 200);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        permissionsDelegate = new PermissionsDelegate(getActivity());

        setupCameraPreviewPreference();

        final Preference preference = findPreference(requireContext().getString(R.string.useCameraAboveModeKey));
        if (preference != null) {
            preference.setOnPreferenceChangeListener((preference1, newValue) -> {
                final Preference cameraPreviewPreference = findPreference("cameraPreview");
                String testName = PreferencesUtil.getString(requireContext(), "lastSelectedTestName", "Fluoride");
                if (cameraPreviewPreference != null) {
                    if (Boolean.parseBoolean(newValue.toString())) {
                        cameraPreviewPreference.setSummary("Parameter: " + testName);
                    } else {
                        cameraPreviewPreference.setSummary("Camera above mode only");
                    }
                }
                return true;
            });
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void setupCameraPreviewPreference() {
        final Preference cameraPreviewPreference = findPreference("cameraPreview");
        String testName = PreferencesUtil.getString(requireContext(), "lastSelectedTestName", "Fluoride");
        if (cameraPreviewPreference != null) {
            if (AppPreferences.useCameraAboveMode()) {
                cameraPreviewPreference.setSummary("Parameter: " + testName);
            } else {
                cameraPreviewPreference.setSummary("Camera above mode only");
            }
            cameraPreviewPreference.setOnPreferenceClickListener(preference -> {
                if (getFragmentManager().findFragmentByTag("diagnosticPreviewFragment") == null) {

                    String[] permissions = {Manifest.permission.CAMERA};
                    if (AppPreferences.useExternalCamera()) {
                        startPreview();
                    } else if (permissionsDelegate.hasPermissions(permissions)) {
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
                    ViewModelProviders.of(getActivity()).get(TestListViewModel.class);
            TestInfo testInfo;
            try {
                testInfo = viewModel.getTestInfo(PreferencesUtil.getString(requireContext(),
                        "lastSelectedTestId", Constants.FLUORIDE_ID));
                Intent intent = new Intent(getActivity(), ChamberPreviewActivity.class);
                intent.putExtra(ConstantKey.RUN_TEST, true);
                intent.putExtra(ConstantKey.TEST_INFO, testInfo);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

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
}
