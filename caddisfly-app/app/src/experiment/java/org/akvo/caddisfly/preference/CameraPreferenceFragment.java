package org.akvo.caddisfly.preference;

import android.Manifest;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.diagnostic.ChamberPreviewActivity;
import org.akvo.caddisfly.helper.CameraHelper;
import org.akvo.caddisfly.helper.PermissionsDelegate;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ListViewUtil;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

import io.ffem.experiment.ResolutionListPreference;

import static org.akvo.caddisfly.util.ApiUtil.getCameraInstance;

public class CameraPreferenceFragment extends PreferenceFragment {

    private PermissionsDelegate permissionsDelegate;
    private ListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_camera);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.card_row, container, false);

        permissionsDelegate = new PermissionsDelegate(getActivity());

        view.setBackgroundColor(Color.rgb(255, 240, 220));

        setupCameraPreviewPreference();

        setupZoomPreference();

        setupOffsetPreference();

        setupCameraPreference();

        return view;
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
            TestInfo testInfo = viewModel.getTestInfo(Constants.FLUORIDE_ID);

            Intent intent = new Intent(getActivity(), ChamberPreviewActivity.class);
            intent.putExtra(ConstantKey.RUN_TEST, true);
            intent.putExtra(ConstantKey.TEST_INFO, testInfo);
            startActivity(intent);
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

    private void setupOffsetPreference() {
        final SeekBarPreference seekBarPreference =
                (SeekBarPreference) findPreference(getString(R.string.cameraCenterOffsetKey));

        if (seekBarPreference != null) {
            int offset = PreferenceManager.getDefaultSharedPreferences(this.getActivity())
                    .getInt(getString(R.string.cameraCenterOffsetKey), 0);

            seekBarPreference.setSummary(String.valueOf(offset));

            seekBarPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                seekBarPreference.setSummary(String.valueOf(newValue));
                return false;
            });
        }
    }

    private void setupZoomPreference() {

        final SeekBarPreference seekBarPreference =
                (SeekBarPreference) findPreference(getString(R.string.cameraZoomPercentKey));

        if (seekBarPreference != null) {
            int zoomValue = PreferenceManager.getDefaultSharedPreferences(this.getActivity())
                    .getInt(getString(R.string.cameraZoomPercentKey), 0);

            Camera camera = getCameraInstance();
            if (camera != null) {
                try {
                    Camera.Parameters parameters;
                    parameters = camera.getParameters();
                    seekBarPreference.setMax(parameters.getMaxZoom());

                    zoomValue = Math.min(zoomValue, parameters.getMaxZoom());
                } finally {
                    camera.release();
                }
            }

            seekBarPreference.setSummary(String.valueOf(zoomValue));

            seekBarPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                seekBarPreference.setSummary(String.valueOf(newValue));
                return false;
            });
        }
    }

    private void setupCameraPreference() {
        final ResolutionListPreference resolutionListPreference =
                (ResolutionListPreference) findPreference(getString(R.string.cameraResolutionKey));

        if (resolutionListPreference != null) {
            String resolution = PreferenceManager.getDefaultSharedPreferences(this.getActivity())
                    .getString(getString(R.string.cameraResolutionKey), "640-480");

            resolutionListPreference.setSummary(String.valueOf(resolution));

            resolutionListPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                resolutionListPreference.setSummary(String.valueOf(newValue));
                PreferencesUtil.setString(getActivity(), R.string.cameraResolutionKey, newValue.toString());
                return false;
            });
        }
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
