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

package org.akvo.caddisfly.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.common.NavigationController;
import org.akvo.caddisfly.databinding.ActivityMainBinding;
import org.akvo.caddisfly.helper.ApkHelper;
import org.akvo.caddisfly.helper.ErrorMessages;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.PermissionsDelegate;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestSampleType;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.preference.SettingsActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.akvo.caddisfly.viewmodel.TestListViewModel;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import static org.akvo.caddisfly.common.AppConfig.GET_STARTED_URL;
import static org.akvo.caddisfly.model.TestType.CHAMBER_TEST;

public class MainActivity extends BaseActivity {

    private final int STORAGE_PERMISSION_WATER = 1;
    private final int STORAGE_PERMISSION_SOIL = 2;

    private final WeakRefHandler refreshHandler = new WeakRefHandler(this);
    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private final String[] storagePermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private NavigationController navigationController;

    private boolean runTest = false;
    private ActivityMainBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CaddisflyApp.getApp().setAppLanguage(null, false, null);

        navigationController = new NavigationController(this);

        b = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setTitle(R.string.appName);

        try {
            if (AppConfig.APP_EXPIRY && ApkHelper.isNonStoreVersion(this)) {
                final GregorianCalendar appExpiryDate = new GregorianCalendar(AppConfig.APP_EXPIRY_YEAR,
                        AppConfig.APP_EXPIRY_MONTH - 1, AppConfig.APP_EXPIRY_DAY);

                DateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
                b.textVersionExpiry.setText(String.format("Version expiry: %s", df.format(appExpiryDate.getTime())));

                b.textVersionExpiry.setVisibility(View.VISIBLE);
            } else {
                if (BuildConfig.showExperimentalTests) {
                    b.textVersionExpiry.setText(CaddisflyApp.getAppVersion(true));
                    b.textVersionExpiry.setVisibility(View.VISIBLE);
                }
            }

            // If app has expired then close this activity
            ApkHelper.isAppVersionExpired(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Show the diagnostic mode layout.
     */
    private void switchLayoutForDiagnosticOrUserMode() {
        if (AppPreferences.isDiagnosticMode()) {
            b.textDiagnostics.setVisibility(View.VISIBLE);
        } else {
            b.textDiagnostics.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        switchLayoutForDiagnosticOrUserMode();

        CaddisflyApp.getApp().setAppLanguage(null, false, refreshHandler);

        if (PreferencesUtil.getBoolean(this, R.string.themeChangedKey, false)) {
            PreferencesUtil.setBoolean(this, R.string.themeChangedKey, false);
            refreshHandler.sendEmptyMessage(0);
        }
    }

    public void onDisableDiagnosticsClick(View view) {

        Toast.makeText(getBaseContext(), getString(R.string.diagnosticModeDisabled),
                Toast.LENGTH_SHORT).show();

        AppPreferences.disableDiagnosticMode();

        switchLayoutForDiagnosticOrUserMode();

        changeActionBarStyleBasedOnCurrentMode();

        final TestListViewModel viewModel =
                ViewModelProviders.of(this).get(TestListViewModel.class);

        viewModel.clearTests();
    }

    public void onStripTestsClick(View view) {
        navigationController.navigateToTestType(TestType.STRIP_TEST, TestSampleType.ALL, true);
    }

    public void onSensorsClick(View view) {
        boolean hasOtg = getBaseContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
        if (hasOtg) {
            navigationController.navigateToTestType(TestType.SENSOR, TestSampleType.ALL, true);
        } else {
            ErrorMessages.alertFeatureNotSupported(this, false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissionsDelegate.resultGranted(requestCode, grantResults)) {
            switch (requestCode) {
                case STORAGE_PERMISSION_WATER:
                    if (runTest) {
                        startTest(TestSampleType.WATER);
                    } else {
                        startCalibrate(TestSampleType.WATER);
                    }
                    break;
                case STORAGE_PERMISSION_SOIL:
                    if (runTest) {
                        startTest(TestSampleType.SOIL);
                    } else {
                        startCalibrate(TestSampleType.SOIL);
                    }
                    break;
            }
        } else {
            String message = "";
            switch (requestCode) {
                case STORAGE_PERMISSION_WATER:
                case STORAGE_PERMISSION_SOIL:
            }
            AlertUtil.showSettingsSnackbar(this,
                    getWindow().getDecorView().getRootView(), message);
        }
    }

    private void startCalibrate(TestSampleType testSampleType) {
        FileHelper.migrateFolders();
        navigationController.navigateToTestType(CHAMBER_TEST, testSampleType, false);
    }

    public void onSettingsClick(View view) {
        final Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && PreferencesUtil.getBoolean(this, R.string.refreshKey, false)) {
            PreferencesUtil.setBoolean(this, R.string.refreshKey, false);
            this.recreate();
        }
    }

    public void onColiformsClick(View view) {
        final TestListViewModel viewModel =
                ViewModelProviders.of(this).get(TestListViewModel.class);

        TestInfo testInfo = viewModel.getTestInfo(Constants.COLIFORM_ID);

        final Intent intent = new Intent(this, TestActivity.class);
        intent.putExtra(ConstantKey.TEST_INFO, testInfo);
        startActivity(intent);
    }

    public void onCalibrateSoilClick(View view) {
        runTest = false;
        if (permissionsDelegate.hasPermissions(storagePermission)) {
            startCalibrate(TestSampleType.SOIL);
        } else {
            permissionsDelegate.requestPermissions(storagePermission, STORAGE_PERMISSION_SOIL);
        }
    }

    public void onCalibrateWaterClick(View view) {
        if (permissionsDelegate.hasPermissions(storagePermission)) {
            startCalibrate(TestSampleType.WATER);
        } else {
            permissionsDelegate.requestPermissions(storagePermission, STORAGE_PERMISSION_WATER);
        }
    }

    public void onCalibrateClick(View view) {
        if (permissionsDelegate.hasPermissions(storagePermission)) {
            startCalibrate(TestSampleType.ALL);
        } else {
            //noinspection ConstantConditions
            if (BuildConfig.APPLICATION_ID.contains("soil")) {
                permissionsDelegate.requestPermissions(storagePermission, STORAGE_PERMISSION_SOIL);
            } else {
                permissionsDelegate.requestPermissions(storagePermission, STORAGE_PERMISSION_WATER);
            }
        }
    }

    public void onGetStartedClicked(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GET_STARTED_URL));
        startActivity(browserIntent);
    }

    public void onRunTestClick(View view) {
        runTest = true;
        if (permissionsDelegate.hasPermissions(storagePermission)) {
            startTest(TestSampleType.ALL);
        } else {
            //noinspection ConstantConditions
            if (BuildConfig.APPLICATION_ID.contains("soil")) {
                permissionsDelegate.requestPermissions(storagePermission, STORAGE_PERMISSION_SOIL);
            } else {
                permissionsDelegate.requestPermissions(storagePermission, STORAGE_PERMISSION_WATER);
            }
        }
    }

    private void startTest(TestSampleType testSampleType) {
        FileHelper.migrateFolders();
        navigationController.navigateToTestType(CHAMBER_TEST, testSampleType, runTest);
    }

    /**
     * Handler to restart the app after language has been changed.
     */
    private static class WeakRefHandler extends Handler {
        private final WeakReference<Activity> ref;

        WeakRefHandler(Activity ref) {
            this.ref = new WeakReference<>(ref);
        }

        @Override
        public void handleMessage(@NotNull Message msg) {
            Activity f = ref.get();
            if (f != null) {
                f.recreate();
            }
        }
    }
}

