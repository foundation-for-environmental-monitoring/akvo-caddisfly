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

import android.hardware.Camera;
import android.util.Pair;
import android.util.Patterns;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.ChamberTestConfig;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.util.List;

/**
 * Static functions to get or set values of various preferences.
 */
public final class AppPreferences {

    // Phone models where the camera does not work in torch mode
    private static final String[] phoneModels = {"M1803E6I", "REDMI NOTE 3"};

    private AppPreferences() {
    }

    public static boolean isDiagnosticMode() {
        return PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.diagnosticModeKey, false);
    }

    public static void enableDiagnosticMode() {
        PreferencesUtil.setBoolean(CaddisflyApp.getApp(), R.string.diagnosticModeKey, true);
    }

    public static void disableDiagnosticMode() {
        PreferencesUtil.setBoolean(CaddisflyApp.getApp(), R.string.diagnosticModeKey, false);
        PreferencesUtil.setBoolean(CaddisflyApp.getApp(), R.string.testModeOnKey, false);
        PreferencesUtil.setBoolean(CaddisflyApp.getApp(), R.string.dummyResultKey, false);
    }

    /**
     * The number of photos to take during the test.
     *
     * @return number of samples to take
     */
    public static int getSamplingTimes() {
        int samplingTimes;
        if (isDiagnosticMode()) {
            samplingTimes = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                    R.string.samplingsTimeKey, String.valueOf(ChamberTestConfig.SAMPLING_COUNT_DEFAULT)));
        } else {
            samplingTimes = ChamberTestConfig.SAMPLING_COUNT_DEFAULT;
        }
        //Add skip count as the first few samples may not be valid
        return samplingTimes + ChamberTestConfig.SKIP_SAMPLING_COUNT;
    }

    /**
     * The color distance tolerance for when matching colors.
     *
     * @return the tolerance value
     */
    public static int getColorDistanceTolerance() {
        if (isDiagnosticMode()) {
            return Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                    R.string.colorDistanceToleranceKey,
                    String.valueOf(ChamberTestConfig.MAX_COLOR_DISTANCE_RGB)));
        } else {
            return ChamberTestConfig.MAX_COLOR_DISTANCE_RGB;
        }
    }

    /**
     * The color distance tolerance for when matching colors.
     *
     * @return the tolerance value
     */
    public static int getAveragingColorDistanceTolerance() {
        try {
            if (isDiagnosticMode()) {
                return Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                        R.string.colorAverageDistanceToleranceKey,
                        String.valueOf(ChamberTestConfig.MAX_COLOR_DISTANCE_CALIBRATION)));
            } else {
                return ChamberTestConfig.MAX_COLOR_DISTANCE_CALIBRATION;
            }
        } catch (NullPointerException e) {
            return ChamberTestConfig.MAX_COLOR_DISTANCE_CALIBRATION;
        }
    }

    public static boolean isSoundOn() {
        return !isDiagnosticMode() || PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.soundOnKey, true);
    }

    public static boolean getShowDebugInfo() {
        return isDiagnosticMode()
                && PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.showDebugMessagesKey, false);
    }

    public static boolean isTestMode() {
        return isDiagnosticMode()
                && PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.testModeOnKey, false);
    }

    public static boolean returnDummyResults() {
        return isDiagnosticMode()
                && PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.dummyResultKey, false);
    }

    public static boolean useExternalCamera() {
        return PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.useExternalCameraKey, false);
    }

    public static boolean ignoreTimeDelays() {
        return isDiagnosticMode()
                && PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.ignoreTimeDelaysKey, false);
    }

    public static boolean useCameraAboveMode() {
        return isDiagnosticMode()
                && PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.useCameraAboveModeKey, false);
    }

    public static boolean useMaxZoom() {
        return isDiagnosticMode()
                && PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.maxZoomKey, false);
    }

    public static String getNotificationEmails() {
        String emails = PreferencesUtil.getString(CaddisflyApp.getApp(), R.string.colif_emails, "");
        String[] emailArray = emails.split("\n");
        StringBuilder emailList = new StringBuilder();
        for (String email : emailArray) {
            email = email.trim();
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (!emailList.toString().isEmpty()) {
                    emailList.append(",");
                }
                emailList.append(email);
            }
        }
        return emailList.toString();
    }

    public static int getCameraZoom() {

        return 0;

//        if (isDiagnosticMode()) {
//            return PreferencesUtil.getInt(CaddisflyApp.getApp(),
//                    R.string.cameraZoomPercentKey, 0);
//        } else {
//            return 0;
//        }
    }

    public static Pair<Integer, Integer> getCameraResolution() {
        Pair<Integer, Integer> res = new Pair<>(640, 480);
        try {
            if (isDiagnosticMode()) {
                // todo: fix this...
//                String resolution = PreferencesUtil.getString(CaddisflyApp.getApp(),
//                        R.string.cameraResolutionKey, "640-480");

                String resolution = "640-480";

                String[] resolutions = resolution.split("-");
                int widthTemp = Integer.parseInt(resolutions[0]);
                int heightTemp = Integer.parseInt(resolutions[1]);
                int width = Math.max(heightTemp, widthTemp);
                int height = Math.min(heightTemp, widthTemp);

                return new Pair<>(width, height);
            } else {
                return res;
            }
        } catch (Exception e) {
            return res;
        }
    }

    public static int getCameraCenterOffset() {
        return 0;
//        if (isDiagnosticMode()) {
//            return PreferencesUtil.getInt(CaddisflyApp.getApp(),
//                    R.string.cameraCenterOffsetKey, 0);
//        } else {
//            return 0;
//        }
    }

    public static String getCameraFocusMode(List<String> focusModes) {
        String focusMode = "";
        if (isDiagnosticMode()) {
            focusMode = PreferencesUtil.getString(CaddisflyApp.getApp(),
                    R.string.cameraFocusKey, Camera.Parameters.FOCUS_MODE_INFINITY);
        }

        if (focusModes.contains(focusMode)) {
            return focusMode;
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            return Camera.Parameters.FOCUS_MODE_FIXED;
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            return Camera.Parameters.FOCUS_MODE_INFINITY;
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            return Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            return Camera.Parameters.FOCUS_MODE_AUTO;
        } else {
            return "";
        }
    }

    public static String getFlashMode() {
        //        if (flashMode.equals(Camera.Parameters.FLASH_MODE_TORCH)) {
//            for (String model : phoneModels) {
//                if (Build.MODEL.toUpperCase().contains(model)) {
//                    return Camera.Parameters.FLASH_MODE_ON;
//                }
//            }
//        } else {
//            return flashMode;
//        }
        return PreferencesUtil.getString(CaddisflyApp.getApp(),
                R.string.flashModePrefKey, Camera.Parameters.FLASH_MODE_TORCH);
    }
}
