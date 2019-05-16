package org.akvo.caddisfly.util;

import android.content.Context;
import android.hardware.Camera;
import android.preference.ListPreference;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

import static org.akvo.caddisfly.util.ApiUtil.getCameraInstance;

public class ResolutionListPreference extends ListPreference {
    public ResolutionListPreference(Context context, AttributeSet attrs) {
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
                    items.add(String.valueOf(size.width) + " x " + String.valueOf(size.height));
                    values.add(String.valueOf(size.width) + "-" + String.valueOf(size.height));
                }

                setEntries(items.toArray(new String[0]));
                setEntryValues(values.toArray(new String[0]));
                setValueIndex(initializeIndex());
            } finally {
                camera.release();
            }
        }
    }

    public ResolutionListPreference(Context context) {
        this(context, null);
    }

    private int initializeIndex() {
        return 0;
    }
}