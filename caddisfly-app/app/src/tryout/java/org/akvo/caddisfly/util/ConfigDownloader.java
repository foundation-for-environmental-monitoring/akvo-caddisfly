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

package org.akvo.caddisfly.util;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.common.AppConstants;
import org.akvo.caddisfly.dao.CalibrationDao;
import org.akvo.caddisfly.diagnostic.ConfigTask;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.entity.CalibrationDetail;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.ui.TestListActivity;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import timber.log.Timber;

import static org.akvo.caddisfly.helper.CameraHelper.getMaxSupportedMegaPixelsByCamera;

public class ConfigDownloader {

    /**
     * Download latest version of the experimental config file.
     *
     * @param activity          the activity
     * @param configSyncHandler the callback
     */
    public static void syncExperimentalConfig(Activity activity,
                                              TestListActivity.SyncCallbackInterface configSyncHandler) {
        if (NetUtil.isNetworkAvailable(activity)) {
            Date todayDate = Calendar.getInstance().getTime();
            ConfigTask configTask = new ConfigTask(activity, configSyncHandler);

            configTask.execute(AppConfig.EXPERIMENT_TESTS_URL + "?" + todayDate.getTime(),
                    FileHelper.FileType.EXP_CONFIG.toString());

            final TestListViewModel viewModel =
                    ViewModelProviders.of((FragmentActivity) activity).get(TestListViewModel.class);

            viewModel.clearTests();

        } else {
            Toast.makeText(activity,
                    "No data connection. Please connect to the internet and try again.", Toast.LENGTH_LONG).show();
        }
    }

//    /**
//     * Download latest version of the experimental config file.
//     *
//     * @param activity the activity
//     */
//    public static void syncFfemExperimentalConfig(Activity activity) {
//
//        if (NetUtil.isNetworkAvailable(activity)) {
//            Date todayDate = Calendar.getInstance().getTime();
//            ConfigTask configTask = new ConfigTask(activity, null);
//
//            configTask.execute(AppConfig.EXPERIMENT_TESTS_FFEM_URL + "?" + todayDate.getTime(),
//                    FileHelper.FileType.FFEM_EXP_CONFIG.toString());
//
//        }
//    }

    private static int stringToInteger(String value) {

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void sendDataToCloudDatabase(Context context, TestInfo testInfo, String comment) {

        ProgressDialog pd;

        File folder = new File(FileUtil.getFilesStorageDir(CaddisflyApp.getApp(), false)
                + File.separator + AppConstants.APP_FOLDER + File.separator + "qa");

        String deviceId = FileUtil.loadTextFromFile(new File(folder, "deviceId"));

        if (stringToInteger(deviceId) < 1) {

            deviceId = PreferencesUtil.getString(context, R.string.deviceIdKey, "0");

            if (stringToInteger(deviceId) < 1) {
                deviceId = String.valueOf(Calendar.getInstance().getTimeInMillis());
            }

            FileUtil.saveToFile(folder, "deviceId", deviceId);
        }

        if (!NetUtil.isNetworkAvailable(context)) {
            Toast.makeText(context,
                    "No data connection. Please connect to the internet and try again.", Toast.LENGTH_LONG).show();
            return;
        }

        CalibrationDao dao = CaddisflyApp.getApp().getDb().calibrationDao();

        CalibrationDetail calibrationDetail = dao.getCalibrationDetails(testInfo.getUuid());
        String calibrationFile = calibrationDetail.fileName;

        final File path = FileHelper.getFilesDir(FileHelper.FileType.DIAGNOSTIC_IMAGE);

        FirebaseApp.initializeApp(context);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        pd = new ProgressDialog(context);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();

        String finalDeviceId = deviceId;
        new Thread(() -> {

            boolean isSending = false;

            ResultDetail result = testInfo.getResultDetail();
            if (result == null) {

                for (Calibration calibration : testInfo.getCalibrations()) {

                    if (calibration.image != null && calibration.croppedImage != null) {

                        File imagePath = new File(path, calibration.image);
                        File croppedImagePath = new File(path, calibration.croppedImage);

                        if (!imagePath.exists() || !croppedImagePath.exists()) {
                            continue;
                        }

                        isSending = true;

                        sendFile(context, testInfo.getUuid(), comment, pd, finalDeviceId, db, storageReference,
                                imagePath, croppedImagePath, calibration, calibrationFile, new Date(calibration.date));
                    }
                }
            } else {

                isSending = true;

                result.setImage(UUID.randomUUID().toString() + ".png");
                result.setCroppedImage(UUID.randomUUID().toString() + ".png");

                // Save photo taken during the test
                FileUtil.writeBitmapToExternalStorage(result.getBitmap(),
                        FileHelper.FileType.DIAGNOSTIC_IMAGE, result.getImage());

                // Save photo taken during the test
                FileUtil.writeBitmapToExternalStorage(result.getCroppedBitmap(),
                        FileHelper.FileType.DIAGNOSTIC_IMAGE, result.getCroppedImage());

                File imagePath = new File(path, result.getImage());
                File croppedImagePath = new File(path, result.getCroppedImage());

                Calibration calibration = new Calibration();
                calibration.value = result.getResult();
                calibration.color = result.getColor();
                calibration.image = result.getImage();
                calibration.quality = result.getQuality();
                calibration.centerOffset = AppPreferences.getCameraCenterOffset();
                calibration.resWidth = result.getBitmap().getWidth();
                calibration.resHeight = result.getBitmap().getHeight();
                calibration.zoom = AppPreferences.getCameraZoom();
                calibration.croppedImage = result.getCroppedImage();

                sendFile(context, testInfo.getUuid(), comment, pd, finalDeviceId, db, storageReference,
                        imagePath, croppedImagePath, calibration, calibrationFile, new Date());
            }

            if (!isSending) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context,
                        "No data to send", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private static void sendFile(Context context, String uuid, String comment, ProgressDialog pd,
                                 String deviceId, FirebaseFirestore db, StorageReference storageReference,
                                 File imagePath, File croppedImagePath, Calibration calibration,
                                 String calibrationFileName, Date date) {
        Uri file = Uri.fromFile(imagePath);
        StorageReference storageReference1 = storageReference.child("calibration-images/" + calibration.image);

        storageReference1.putFile(file).addOnSuccessListener(taskSnapshot ->
                storageReference1.getDownloadUrl().addOnSuccessListener(uri -> {

                    //noinspection ResultOfMethodCallIgnored
                    imagePath.delete();

                    Uri file2 = Uri.fromFile(croppedImagePath);
                    StorageReference storageReference2 = storageReference.child("calibration-images/" + calibration.croppedImage);

                    storageReference2.putFile(file2).addOnSuccessListener(taskSnapshot2 ->
                            storageReference2.getDownloadUrl().addOnSuccessListener(uri2 -> {

                                //noinspection ResultOfMethodCallIgnored
                                croppedImagePath.delete();

                                // Get a URL to the uploaded content
                                // Create a new calibration with a first and last name
                                Map<String, Object> cal = new HashMap<>();
                                cal.put("deviceId", deviceId);
                                cal.put("id", uuid);
                                cal.put("value", calibration.value);
                                cal.put("color", calibration.color);
                                cal.put("quality", calibration.quality);
                                cal.put("zoom", calibration.zoom);
                                cal.put("resWidth", calibration.resWidth);
                                cal.put("resHeight", calibration.resHeight);
                                cal.put("centerOffset", calibration.centerOffset);
                                cal.put("r", Color.red(calibration.color));
                                cal.put("g", Color.green(calibration.color));
                                cal.put("b", Color.blue(calibration.color));
                                cal.put("calibration", calibrationFileName);
                                cal.put("date", date);
                                cal.put("comment", comment);
                                cal.put("version", BuildConfig.VERSION_CODE);
                                cal.put("manufacturer", Build.MANUFACTURER);
                                cal.put("device", Build.MODEL);
                                cal.put("product", Build.PRODUCT);
                                cal.put("os", "Android - " + Build.VERSION.RELEASE + " ("
                                        + Build.VERSION.SDK_INT + ")");
                                cal.put("megaPixel", getMaxSupportedMegaPixelsByCamera(context));
                                cal.put("appName", context.getString(R.string.app_name));
                                cal.put("appVersion", CaddisflyApp.getAppVersion(true));

                                if (uri != null) {
                                    cal.put("image", uri.toString());
                                }
                                if (uri2 != null) {
                                    cal.put("croppedImage", uri2.toString());
                                }

                                db.collection(uuid)
                                        .document(String.valueOf(deviceId + "-" + date.getTime()))
                                        .set(cal)
                                        .addOnSuccessListener(documentReference -> {
                                            if (pd.isShowing()) {
                                                pd.dismiss();
                                            }

                                            Toast.makeText(context, "Data sent", Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent("data-sent-to-dash");
                                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                                        })
                                        .addOnFailureListener(e -> {
                                            if (pd.isShowing()) {
                                                pd.dismiss();
                                            }

                                            Toast.makeText(context,
                                                    "Unable to send. Check connection", Toast.LENGTH_SHORT).show();

                                            Timber.w("Error adding document");
                                        });

                            }))
                            .addOnFailureListener(exception -> {
                                if (pd.isShowing()) {
                                    pd.dismiss();
                                }
                                Toast.makeText(context,
                                        "Unable to send. Check connection", Toast.LENGTH_SHORT).show();
                            });

                }))
                .addOnFailureListener(exception -> {
                    if (pd.isShowing()) {
                        pd.dismiss();
                    }

                    Toast.makeText(context,
                            "Unable to send. Check connection", Toast.LENGTH_SHORT).show();
                });

    }
}
