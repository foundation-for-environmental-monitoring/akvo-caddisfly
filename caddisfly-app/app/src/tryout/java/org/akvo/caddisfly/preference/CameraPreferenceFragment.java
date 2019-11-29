package org.akvo.caddisfly.preference;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import org.akvo.caddisfly.R;

public class CameraPreferenceFragment extends PreferenceFragmentCompat {

//    private RecyclerView list;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_camera);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setBackgroundColor(Color.rgb(255, 240, 220));
//        list = view.findViewById(R.id.recycler_view);

        (new Handler()).postDelayed(() -> {

//            setupZoomPreference();
//
//            setupOffsetPreference();

//            setupCameraPreference();

        }, 200);
    }

//    private void setupOffsetPreference() {
//        final SeekBarPreference seekBarPreference =
//                findPreference(getString(R.string.cameraCenterOffsetKey));
//
//        if (seekBarPreference != null) {
//            int offset = PreferenceManager.getDefaultSharedPreferences(this.getActivity())
//                    .getInt(getString(R.string.cameraCenterOffsetKey), 0);
//
//            seekBarPreference.setSummary(String.valueOf(offset));
//
//            seekBarPreference.setOnPreferenceChangeListener((preference, newValue) -> {
//                seekBarPreference.setSummary(String.valueOf(newValue));
//                return false;
//            });
//        }
//    }
//
//    private void setupZoomPreference() {
//
//        final SeekBarPreference seekBarPreference =
//                findPreference(getString(R.string.cameraZoomPercentKey));
//
//        if (seekBarPreference != null) {
//            int zoomValue = PreferenceManager.getDefaultSharedPreferences(this.getActivity())
//                    .getInt(getString(R.string.cameraZoomPercentKey), 0);
//
//            Camera camera = getCameraInstance();
//            if (camera != null) {
//                try {
//                    Camera.Parameters parameters;
//                    parameters = camera.getParameters();
//                    seekBarPreference.setMax(parameters.getMaxZoom());
//
//                    zoomValue = Math.min(zoomValue, parameters.getMaxZoom());
//                } finally {
//                    camera.release();
//                }
//            }
//
//            seekBarPreference.setSummary(String.valueOf(zoomValue));
//
//            seekBarPreference.setOnPreferenceChangeListener((preference, newValue) -> {
//                seekBarPreference.setSummary(String.valueOf(newValue));
//                return false;
//            });
//        }
//    }

//    private void setupCameraPreference() {
//        final ResolutionListPreference resolutionListPreference =
//                findPreference(getString(R.string.cameraResolutionKey));
//
//        if (resolutionListPreference != null) {
//
//            String resolution = PreferenceManager.getDefaultSharedPreferences(this.getActivity())
//                    .getString(getString(R.string.cameraResolutionKey), "640-480");
//
//            resolutionListPreference.setSummary(resolution);
//            resolutionListPreference.setValue(resolution);
//            if (resolutionListPreference.getEntries() == null) {
//                resolutionListPreference.setShouldDisableView(true);
//                resolutionListPreference.setEnabled(false);
//            } else {
//                resolutionListPreference.setOnPreferenceChangeListener((preference, newValue) -> {
//                    resolutionListPreference.setValue(newValue.toString());
//                    resolutionListPreference.setSummary(String.valueOf(newValue));
//                    PreferencesUtil.setString(getActivity(), R.string.cameraResolutionKey, newValue.toString());
//                    return false;
//                });
//            }
//        }
//    }

//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        list = view.findViewById(android.R.id.list);
//        (new Handler()).postDelayed(() -> ListViewUtil
//                .setListViewHeightBasedOnChildren(list, 0), 200);
//    }

}
