/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.sensor.turbidity;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.snackbar.Snackbar;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.SoundPoolPlayer;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.liquid.LiquidTimeLapsePreferenceFragment;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class TimeLapseActivity extends BaseActivity {

    private static final String TAG = "TimeLapseActivity";

    private static final int PERMISSION_ALL = 1;
    private static final int INITIAL_DELAY = 25000;
//    private static final float SNACK_BAR_LINE_SPACING = 1.4f;
//
//    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
//        @Override
//        public void onManagerConnected(int status) {
//            switch (status) {
//                case LoaderCallbackInterface.SUCCESS:
//                    break;
//                default:
//                    super.onManagerConnected(status);
//                    break;
//            }
//        }
//    };
    private CoordinatorLayout coordinatorLayout;
    private SoundPoolPlayer sound;
    private View layoutWait;
    private LinearLayout layoutDetails;
    private TextView textSampleCount;
    private TextView textCountdown;
    private Calendar futureDate;
    private Runnable runnable;
    private Handler handler;
    private String mUuid;
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            sound.playShortResource(R.raw.beep);

            mUuid = getIntent().getDataString();

            int delayMinute;
            int numberOfSamples;

            File folder = FileHelper.getFilesDir(FileHelper.FileType.IMAGE,
                    intent.getStringExtra("savePath"));

            CaddisflyApp.getApp().loadTestConfigurationByUuid(mUuid);
            final TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

            delayMinute = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                    testInfo.getShortCode() + "_IntervalMinutes", "1"));

            numberOfSamples = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                    testInfo.getShortCode() + "_NumberOfSamples", "1"));

            File[] files = folder.listFiles();
            if (files != null) {
                if (files.length >= numberOfSamples) {
                    TurbidityConfig.stopRepeatingAlarm(context, mUuid);
                    finish();
                } else {
                    textSampleCount.setText(String.format(Locale.getDefault(), "%s: %d of %d",
                            "Samples done", files.length, numberOfSamples));
                    futureDate = Calendar.getInstance();
                    futureDate.add(Calendar.MINUTE, delayMinute);
                }
            }
        }
    };

    private boolean showTimer = true;

    private void startCountdownTimer() {
        showTimer = true;
        handler = new Handler();
        handler.removeCallbacks(runnable);
        runnable = new Runnable() {
            @Override
            public void run() {
                if (showTimer) {
                    handler.postDelayed(this, 1000);
                    try {
                        Calendar currentDate = Calendar.getInstance();
                        if (futureDate != null && !currentDate.after(futureDate)) {
                            long diff = futureDate.getTimeInMillis() - currentDate.getTimeInMillis();
                            long days = diff / (24 * 60 * 60 * 1000);
                            diff -= days * (24 * 60 * 60 * 1000);
                            long hours = diff / (60 * 60 * 1000);
                            diff -= hours * (60 * 60 * 1000);
                            long minutes = diff / (60 * 1000);
                            diff -= minutes * (60 * 1000);
                            long seconds = diff / 1000;
                            textCountdown.setText(String.format(Locale.getDefault(),
                                    "%02d:%02d:%02d", hours, minutes, seconds));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_lapse);

        mUuid = getIntent().getDataString();

        sound = new SoundPoolPlayer(this);

        layoutWait = findViewById(R.id.layoutWait);
        layoutDetails = findViewById(R.id.layoutDetails);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        Fragment fragment;
        Bundle bundle = new Bundle();

        final TextView textTitle = findViewById(R.id.textTitle);

        CaddisflyApp.getApp().loadTestConfigurationByUuid(mUuid);
        final TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        switch (testInfo.getId()) {
            case SensorConstants.FLUORIDE_ID:
                fragment = new LiquidTimeLapsePreferenceFragment();
                break;
            default:
                fragment = new TimeLapsePreferenceFragment();
                break;
        }

        bundle.putString("uuid", mUuid);
        fragment.setArguments(bundle);

        textTitle.setText(testInfo.getName());
        setTitle(testInfo.getName());

        getFragmentManager().beginTransaction()
                .add(R.id.layoutContent4, fragment)
                .commit();

        LinearLayout layoutTitleBar = findViewById(R.id.layoutTitleBar);
        layoutTitleBar.setVisibility(View.GONE);

        layoutWait.setVisibility(View.VISIBLE);
        layoutDetails.setVisibility(View.GONE);

//        if (TurbidityConfig.isAlarmRunning(this, mUuid)) {
//            Log.e("TimeLapse", "Already Running Alarm");
//        }

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-event-name"));

        textSampleCount = findViewById(R.id.textSampleCount);

        final Activity activity = this;

        Button buttonStart = findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if (AppPreferences.useExternalCamera()) {
                    permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                }

                if (!ApiUtil.hasPermissions(activity, permissions)) {
                    ActivityCompat.requestPermissions(activity, permissions, PERMISSION_ALL);
                } else {
                    startTest();
                }
            }
        });
        textCountdown = findViewById(R.id.textCountdown);
    }

    private void startTest() {

        final TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        layoutWait.setVisibility(View.GONE);
        layoutDetails.setVisibility(View.VISIBLE);

        Calendar startDate = Calendar.getInstance();

        String details = "";
        String testId = "";
        if (testInfo.getShortCode().equals("colif")) {

            testId = PreferencesUtil.getString(this, getString(R.string.colif_TestIdKey), "");
            String phoneNumber = PreferencesUtil.getString(this, getString(R.string.colif_PhoneIdKey), "");
            String chamber = PreferencesUtil.getString(this, getString(R.string.colif_chamberVersionKey), "");
            String media = PreferencesUtil.getString(this, getString(R.string.colif_brothMediumKey), "");
            String volume = PreferencesUtil.getString(this, getString(R.string.colif_volumeKey), "");
            String description = PreferencesUtil.getString(this, getString(R.string.colif_testDescriptionKey), "");

            details = "_" + Build.MODEL.replace("_", "-") + "-"
                    + phoneNumber + "_"
                    + chamber + "_"
                    + media + "_"
                    + volume + "ml_"
                    + description;

            if (testId.isEmpty() || phoneNumber.isEmpty() || chamber.isEmpty() || media.isEmpty() || volume.isEmpty() || description.isEmpty()) {
                AlertUtil.showAlert(this, R.string.required, "All fields must be filled", R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }, null, null);
                return;
            }
        }

        File folder = FileHelper.getFilesDir(FileHelper.FileType.IMAGE, testInfo.getName());
        if (folder.exists()) {
            FileUtil.deleteRecursive(folder);
        }

        PreferencesUtil.setString(this, R.string.turbiditySavePathKey,
                testInfo.getName() + File.separator + testId + "_"
                        + new SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(startDate.getTime()) + details);

        TurbidityConfig.setRepeatingAlarm(this, INITIAL_DELAY, testInfo.getId());

        String date = new SimpleDateFormat("dd MMM yyy HH:mm", Locale.US).format(startDate.getTime());
        ((TextView) findViewById(R.id.textSubtitle))
                .setText(String.format(Locale.getDefault(), "%s %s", "Started", date));

        futureDate = Calendar.getInstance();
        futureDate.add(Calendar.MILLISECOND, INITIAL_DELAY);

        TextView textInterval = findViewById(R.id.textInterval);
        int interval = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                testInfo.getShortCode() + "_IntervalMinutes", "1"));

        textInterval.setText(String.format(Locale.getDefault(), "Every %d minutes", interval));

        startCountdownTimer();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        final Activity activity = this;
        if (requestCode == PERMISSION_ALL) {
            // If request is cancelled, the result arrays are empty.
            boolean granted = false;
            for (int grantResult : grantResults) {
                if (grantResult != PERMISSION_GRANTED) {
                    granted = false;
                    break;
                } else {
                    granted = true;
                }
            }
            if (granted) {
                startTest();
            } else {
                String message = getString(R.string.cameraAndStoragePermissions);
                if (AppPreferences.useExternalCamera()) {
                    message = getString(R.string.storagePermission);
                }
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
                        .setAction("SETTINGS", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ApiUtil.startInstalledAppDetailsActivity(activity);
                            }
                        });

                TypedValue typedValue = new TypedValue();
                getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);

                snackbar.setActionTextColor(typedValue.data);
                snackbar.show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        showTimer = false;
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        startCountdownTimer();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showTimer = false;
        handler.removeCallbacks(runnable);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        TurbidityConfig.stopRepeatingAlarm(this, mUuid);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        showTimer = false;
        handler.removeCallbacks(runnable);

        Toast.makeText(this, "Test cancelled", Toast.LENGTH_LONG).show();

        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        TurbidityConfig.stopRepeatingAlarm(this, mUuid);
    }
}
