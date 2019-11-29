package io.ffem.tryout;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import androidx.preference.ListPreference;

import java.util.ArrayList;
import java.util.List;

import static org.akvo.caddisfly.util.ApiUtil.getCameraInstance;

public class ResolutionListPreference extends ListPreference {
    private ResolutionListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        Camera camera = getCameraInstance();
        if (camera != null) {
            try {
                Camera.Parameters parameters;
                parameters = camera.getParameters();

                List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();

                List<String> items = new ArrayList<>();
                List<String> values = new ArrayList<>();

                parameters.setPictureSize(
                        supportedPictureSizes.get(supportedPictureSizes.size() - 1).width,
                        supportedPictureSizes.get(supportedPictureSizes.size() - 1).height);

                for (Camera.Size size : supportedPictureSizes) {
                    items.add(size.width + " x " + size.height);
                    values.add(size.width + "-" + size.height);
                }

                setEntries(items.toArray(new String[0]));
                setEntryValues(values.toArray(new String[0]));
                setValueIndex(initializeIndex());
            } finally {
                camera.release();
            }
        }
    }

    private int initializeIndex() {
        return 0;
    }
}