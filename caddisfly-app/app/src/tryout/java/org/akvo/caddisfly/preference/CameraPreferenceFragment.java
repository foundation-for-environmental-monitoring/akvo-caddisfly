package org.akvo.caddisfly.preference;

import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.ListViewUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import io.ffem.tryout.ResolutionListPreference;

import static org.akvo.caddisfly.util.ApiUtil.getCameraInstance;

public class CameraPreferenceFragment extends PreferenceFragment {

    private ListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_camera);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.card_row, container, false);

        view.setBackgroundColor(Color.rgb(255, 240, 220));

        return view;
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

            resolutionListPreference.setSummary(resolution);
            resolutionListPreference.setValue(resolution);
            if (resolutionListPreference.getEntries() == null) {
                resolutionListPreference.setShouldDisableView(true);
                resolutionListPreference.setEnabled(false);
            } else {
                resolutionListPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    resolutionListPreference.setValue(newValue.toString());
                    resolutionListPreference.setSummary(String.valueOf(newValue));
                    PreferencesUtil.setString(getActivity(), R.string.cameraResolutionKey, newValue.toString());
                    return false;
                });
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = view.findViewById(android.R.id.list);

        (new Handler()).postDelayed(() -> {

            setupZoomPreference();

            setupOffsetPreference();

            setupCameraPreference();

            ListViewUtil.setListViewHeightBasedOnChildren(list, 0);

        }, 200);
    }
}
