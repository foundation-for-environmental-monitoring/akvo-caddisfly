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

package org.akvo.caddisfly.sensor.chamber;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.dao.CalibrationDao;
import org.akvo.caddisfly.diagnostic.DiagnosticResultDialog;
import org.akvo.caddisfly.diagnostic.DiagnosticSwatchActivity;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.entity.CalibrationDetail;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.SoundUtil;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ConfigDownloader;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.NetUtil;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.akvo.caddisfly.viewmodel.TestInfoViewModel;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.ffem.tryout.DiagnosticSendDialogFragment;
import timber.log.Timber;

import static org.akvo.caddisfly.common.ConstantKey.IS_INTERNAL;
import static org.akvo.caddisfly.helper.CameraHelper.getMaxSupportedMegaPixelsByCamera;

public class ChamberTestActivity extends BaseActivity implements
        BaseRunTest.OnResultListener,
        CalibrationItemFragment.OnCalibrationSelectedListener,
        SaveCalibrationDialogFragment.OnCalibrationDetailsSavedListener,
        SelectDilutionFragment.OnDilutionSelectedListener,
        EditCustomDilution.OnCustomDilutionListener,
        DiagnosticSendDialogFragment.OnDetailsSavedListener,
        DiagnosticResultDialog.OnDismissed {

    private static final String TWO_SENTENCE_FORMAT = "%s%n%n%s";
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    private RunTest runTestFragment;
    private CalibrationItemFragment calibrationItemFragment;
    private FragmentManager fragmentManager;
    private TestInfo testInfo;
    private boolean cameraIsOk = false;
    private int currentDilution = 1;
    private AlertDialog alertDialogToBeDestroyed;
    private boolean testStarted;
    private int retryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chamber_test);

        fragmentManager = getSupportFragmentManager();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("data-sent-to-dash"));

        // Add list fragment if this is first creation
        if (savedInstanceState == null) {

            testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);
            if (testInfo == null) {
                finish();
                return;
            }

            if (testInfo.getCameraAbove()) {
                runTestFragment = ChamberBelowFragment.newInstance(testInfo);
            } else {
                runTestFragment = ChamberAboveFragment.newInstance(testInfo);
            }

            if (getIntent().getBooleanExtra(ConstantKey.RUN_TEST, false)) {
                start();
            } else {
                setTitle(R.string.calibration);
                boolean isInternal = getIntent().getBooleanExtra(IS_INTERNAL, true);
                calibrationItemFragment = CalibrationItemFragment.newInstance(testInfo, isInternal);
                goToFragment(calibrationItemFragment);
            }
        }
    }

    private void goToFragment(Fragment fragment) {
        if (fragmentManager.getFragments().size() > 0) {
            fragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragment_container, fragment).commit();
        } else {
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment).commit();
        }

        if (fragment instanceof SelectDilutionFragment) {
            testStarted = true;
        } else if (fragment != runTestFragment) {
            testStarted = false;
        }

        invalidateOptionsMenu();
    }

    private void start() {

        retryCount = 0;

        if (testInfo.getDilutions().size() > 0) {
            Fragment selectDilutionFragment = SelectDilutionFragment.newInstance(testInfo);
            goToFragment(selectDilutionFragment);
        } else {
            runTest();
        }

        setTitle(R.string.analyze);

        invalidateOptionsMenu();

    }

    private void runTest() {
        if (cameraIsOk) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    && AppConfig.USE_SCREEN_PINNING) {
                startLockTask();
            }

            if (fragmentManager.getFragments().size() > 0 && retryCount > 0) {

                for (int i = 0; i < fragmentManager.getFragments().size(); i++) {
                    fragmentManager.popBackStackImmediate();
                }

//                for (Fragment fragment : fragmentManager.getFragments()) {
//                    fragmentManager.beginTransaction().remove(fragment).commit();
//                }

                if (testInfo.getCameraAbove()) {
                    runTestFragment = ChamberBelowFragment.newInstance(testInfo);
                } else {
                    runTestFragment = ChamberAboveFragment.newInstance(testInfo);
                }
            }

            runTestFragment.setDilution(currentDilution);
            runTestFragment.setRetryCount(retryCount);
            goToFragment((Fragment) runTestFragment);

            testStarted = true;

        } else {
            checkCameraMegaPixel();
        }

        invalidateOptionsMenu();
    }

    @SuppressWarnings("unused")
    public void runTestClick(View view) {
        if (runTestFragment != null) {
            runTestFragment.setCalibration(null);
        }
        start();
    }

    @Override
    public void onCalibrationSelected(Calibration item) {

        CalibrationDetail calibrationDetail = CaddisflyApp.getApp().getDb()
                .calibrationDao().getCalibrationDetails(testInfo.getUuid());

        if (calibrationDetail == null) {
            showEditCalibrationDetailsDialog(true);
        } else {
            long milliseconds = calibrationDetail.expiry;
            //Show edit calibration details dialog if required
            if (milliseconds <= new Date().getTime()) {
                showEditCalibrationDetailsDialog(true);
            } else {
                (new Handler()).postDelayed(() -> {
                    runTestFragment.setCalibration(item);
                    setTitle(R.string.calibrate);
                    runTest();
//                    invalidateOptionsMenu();
                }, 150);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isAppInLockTaskMode()) {
            if (((Fragment) runTestFragment).isVisible()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    showLockTaskEscapeMessage();
                } else {
                    Toast.makeText(this, R.string.screen_pinned, Toast.LENGTH_SHORT).show();
                }
            } else {
                stopScreenPinning();
            }
        } else {
            if (!fragmentManager.popBackStackImmediate()) {
                super.onBackPressed();
            }
            refreshTitle();
            testStarted = false;
            invalidateOptionsMenu();
        }
    }

    private void refreshTitle() {
        if (fragmentManager.getBackStackEntryCount() == 0) {
            if (getIntent().getBooleanExtra(ConstantKey.RUN_TEST, false)) {
                setTitle(R.string.analyze);
            } else {
                setTitle(R.string.calibration);
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEditCalibration(View view) {
        showEditCalibrationDetailsDialog(true);
    }

    private void showEditCalibrationDetailsDialog(boolean isEdit) {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SaveCalibrationDialogFragment saveCalibrationDialogFragment =
                SaveCalibrationDialogFragment.newInstance(testInfo, isEdit);
        saveCalibrationDialogFragment.show(ft, "saveCalibrationDialog");
    }

    @Override
    public void onCalibrationDetailsSaved() {
        loadDetails();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (AppPreferences.isDiagnosticMode() && !testStarted
                && (calibrationItemFragment != null && calibrationItemFragment.isVisible())) {
            getMenuInflater().inflate(R.menu.menu_calibrate_dev, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuGraph:
                final Intent graphIntent = new Intent(this, CalibrationGraphActivity.class);
                graphIntent.putExtra(ConstantKey.TEST_INFO, testInfo);
                startActivity(graphIntent);
                return true;
            case R.id.actionSwatches:
                final Intent intent = new Intent(this, DiagnosticSwatchActivity.class);
                intent.putExtra(ConstantKey.TEST_INFO, testInfo);
                startActivity(intent);
                return true;
            case R.id.menuLoad:
                loadCalibrationFromFile(this);
                return true;
            case R.id.menuSave:
                showEditCalibrationDetailsDialog(false);
                return true;
            case android.R.id.home:
                if (isAppInLockTaskMode()) {
                    if (((Fragment) runTestFragment).isVisible()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            showLockTaskEscapeMessage();
                        } else {
                            Toast.makeText(this, R.string.screen_pinned, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        stopScreenPinning();
                    }
                } else {
                    stopScreenPinning();
                    releaseResources();
                    if (!fragmentManager.popBackStackImmediate()) {
                        super.onBackPressed();
                    }
                    refreshTitle();
                    testStarted = false;
                    invalidateOptionsMenu();
                }
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadDetails() {

        List<Calibration> calibrations = CaddisflyApp.getApp().getDb()
                .calibrationDao().getAll(testInfo.getUuid());

        testInfo.setCalibrations(calibrations);
        if (calibrationItemFragment != null) {
            calibrationItemFragment.setAdapter(testInfo);
        }

        final TestInfoViewModel model =
                ViewModelProviders.of(this).get(TestInfoViewModel.class);

        model.setTest(testInfo);

        calibrationItemFragment.loadDetails();
    }

    /**
     * Load the calibrated swatches from the calibration text file.
     */
    private void loadCalibrationFromFile(@NonNull final Context context) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.loadCalibration);

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, R.layout.row_text);

            final File path = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION, testInfo.getUuid());

            File[] listFilesTemp = null;
            if (path.exists() && path.isDirectory()) {
                listFilesTemp = path.listFiles();
            }

            final File[] listFiles = listFilesTemp;
            if (listFiles != null && listFiles.length > 0) {
                Arrays.sort(listFiles);

                for (File listFile : listFiles) {
                    arrayAdapter.add(listFile.getName());
                }

                builder.setNegativeButton(R.string.cancel,
                        (dialog, which) -> dialog.dismiss()
                );

                builder.setAdapter(arrayAdapter,
                        (dialog, which) -> {
                            String fileName = listFiles[which].getName();
                            try {
                                SwatchHelper.loadCalibrationFromFile(testInfo, fileName);
                                loadDetails();

                                Toast toast = Toast.makeText(this,
                                        String.format(getString(R.string.calibrationLoaded), fileName),
                                        Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.BOTTOM, 0, 200);
                                toast.show();

                            } catch (Exception ex) {
                                AlertUtil.showError(context, R.string.error, getString(R.string.errorLoadingFile),
                                        null, R.string.ok,
                                        (dialog1, which1) -> dialog1.dismiss(), null, null);
                            }
                        }
                );

                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(dialogInterface -> {
                    final ListView listView = alertDialog.getListView();
                    listView.setOnItemLongClickListener((adapterView, view, i, l) -> {
                        final int position = i;

                        AlertUtil.askQuestion(context, R.string.delete,
                                R.string.deleteConfirm, R.string.delete, R.string.cancel, true,
                                (dialogInterface1, i1) -> {
                                    String fileName = listFiles[position].getName();
                                    FileUtil.deleteFile(path, fileName);
                                    ArrayAdapter listAdapter = (ArrayAdapter) listView.getAdapter();
                                    //noinspection unchecked
                                    listAdapter.remove(listAdapter.getItem(position));
                                    alertDialog.dismiss();
                                    Toast.makeText(context, R.string.deleted, Toast.LENGTH_SHORT).show();
                                }, null);
                        return true;
                    });

                });
                alertDialog.show();
            } else {
                AlertUtil.showMessage(context, R.string.notFound, R.string.loadFilesNotAvailable);
            }
        } catch (ActivityNotFoundException ignored) {
            // do nothing
        }
    }

    @Override
    public void onResult(ArrayList<ResultDetail> resultDetails, Calibration calibration, int cancelled) {

        ColorInfo colorInfo = new ColorInfo(SwatchHelper.getAverageColor(resultDetails), 0);
        ResultDetail resultDetail = SwatchHelper.analyzeColor(testInfo.getSwatches().size(),
                colorInfo, testInfo.getSwatches());

        resultDetail.setBitmap(resultDetails.get(resultDetails.size() - 1).getBitmap());
        resultDetail.setCroppedBitmap(resultDetails.get(resultDetails.size() - 1).getCroppedBitmap());

        if (calibration == null) {

            int dilution = resultDetails.get(0).getDilution();

            Result result = testInfo.getResults().get(0);

            double value = resultDetail.getResult();

            if (value > -1) {

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }

                result.setResult(value, dilution, testInfo.getMaxDilution());
                resultDetail.setResult(result.getResultValue());

                if (result.highLevelsFound() && testInfo.getDilution() != testInfo.getMaxDilution()) {
                    SoundUtil.playShortResource(this, R.raw.beep_long);
                } else {
                    SoundUtil.playShortResource(this, R.raw.done);
                }

                boolean isInternal = getIntent().getBooleanExtra(IS_INTERNAL, true);

                fragmentManager.popBackStack();
                fragmentManager
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.fragment_container,
                                ChamberResultFragment.newInstance(testInfo, isInternal), null).commit();

                testInfo.setResultDetail(resultDetail);

                if (AppPreferences.getShowDebugInfo()) {
                    showDiagnosticResultDialog(false, resultDetail, resultDetails, false);
                }

            } else {

                releaseResources();

                setResult(Activity.RESULT_CANCELED);

                stopScreenPinning();

                fragmentManager.popBackStack();

                if (testInfo.getDilutions().size() > 0) {
                    fragmentManager.popBackStack();
                }

                if (AppPreferences.getShowDebugInfo()) {

                    SoundUtil.playShortResource(this, R.raw.err);

                    showDiagnosticResultDialog(true, resultDetail, resultDetails, false);

                } else {

                    showError(String.format(TWO_SENTENCE_FORMAT, getString(R.string.errorTestFailed),
                            getString(R.string.checkChamberPlacement)),
                            resultDetails.get(resultDetails.size() - 1).getCroppedBitmap());
                }
            }
        } else {

            int color = SwatchHelper.getAverageColor(resultDetails);

            if (color == Color.TRANSPARENT) {

                if (AppPreferences.getShowDebugInfo()) {
                    showDiagnosticResultDialog(true, resultDetail, resultDetails, true);
                }

                showError(String.format(TWO_SENTENCE_FORMAT, getString(R.string.couldNotCalibrate),
                        getString(R.string.checkChamberPlacement)),
                        resultDetails.get(resultDetails.size() - 1).getCroppedBitmap());
            } else {

                CalibrationDao dao = CaddisflyApp.getApp().getDb().calibrationDao();
                calibration.color = color;
                calibration.date = new Date().getTime();
                if (AppPreferences.isDiagnosticMode()) {

                    calibration.image = UUID.randomUUID().toString() + ".png";
                    // Save photo taken during the test
                    FileUtil.writeBitmapToExternalStorage(resultDetails.get(resultDetails.size() - 1).getBitmap(),
                            FileHelper.FileType.DIAGNOSTIC_IMAGE, calibration.image);

                    calibration.croppedImage = UUID.randomUUID().toString() + ".png";
                    // Save photo taken during the test
                    FileUtil.writeBitmapToExternalStorage(resultDetails.get(resultDetails.size() - 1).getCroppedBitmap(),
                            FileHelper.FileType.DIAGNOSTIC_IMAGE, calibration.croppedImage);
                }
                dao.insert(calibration);
                CalibrationFile.saveCalibratedData(this, testInfo, calibration, color);
                loadDetails();

                SoundUtil.playShortResource(this, R.raw.done);

                if (AppPreferences.getShowDebugInfo()) {
                    showDiagnosticResultDialog(false, resultDetail, resultDetails, true);
                }

                showCalibrationDialog(calibration);

            }

            stopScreenPinning();
            fragmentManager.popBackStackImmediate();
        }

        invalidateOptionsMenu();
    }

    private void stopScreenPinning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                stopLockTask();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * In diagnostic mode show the diagnostic results dialog.
     *
     * @param testFailed    if test has failed then dialog knows to show the retry button
     * @param resultDetail  the result shown to the user
     * @param resultDetails the result details
     * @param isCalibration is this a calibration result
     */
    private void showDiagnosticResultDialog(boolean testFailed, ResultDetail resultDetail,
                                            ArrayList<ResultDetail> resultDetails, boolean isCalibration) {
        DialogFragment resultFragment = DiagnosticResultDialog.newInstance(
                testFailed, retryCount, resultDetail, resultDetails, isCalibration);
        final android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();

        android.app.Fragment prev = getFragmentManager().findFragmentByTag("gridDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        resultFragment.setCancelable(false);
        resultFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        resultFragment.show(ft, "gridDialog");
    }

    /**
     * In diagnostic mode show the diagnostic results dialog.
     *
     * @param calibration the calibration details shown to the user
     */
    private void showCalibrationDialog(Calibration calibration) {
        DialogFragment resultFragment = CalibrationResultDialog.newInstance(
                calibration, testInfo.getDecimalPlaces(), testInfo.getResults().get(0).getUnit());
        final android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();

        android.app.Fragment prev = getFragmentManager().findFragmentByTag("calibrationDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        resultFragment.setCancelable(false);
        resultFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        resultFragment.show(ft, "calibrationDialog");
    }

    /**
     * Create result json to send back.
     */
    @SuppressWarnings("unused")
    public void onClickAcceptResult(View view) {
        sendResult();
    }

    private void sendResult() {
        Intent resultIntent = new Intent();
        SparseArray<String> results = new SparseArray<>();

        for (int i = 0; i < testInfo.getResults().size(); i++) {
            Result result = testInfo.getResults().get(i);

            String testName = result.getName().replace(" ", "_");
            if (testInfo.getNameSuffix() != null && !testInfo.getNameSuffix().isEmpty()) {
                testName += "_" + testInfo.getNameSuffix().replace(" ", "_");
            }

            resultIntent.putExtra(testName
                    + testInfo.getResultSuffix(), result.getResult());

            resultIntent.putExtra(testName
                    + "_" + SensorConstants.DILUTION
                    + testInfo.getResultSuffix(), testInfo.getDilution());

            resultIntent.putExtra(
                    result.getName().replace(" ", "_")
                            + "_" + SensorConstants.UNIT + testInfo.getResultSuffix(),
                    testInfo.getResults().get(0).getUnit());

            if (i == 0) {
                resultIntent.putExtra(SensorConstants.VALUE, result.getResult());
            }

            results.append(result.getId(), result.getResult());
        }

        JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo, results, null, -1, null);
        resultIntent.putExtra(SensorConstants.RESULT_JSON, resultJson.toString());

        setResult(Activity.RESULT_OK, resultIntent);

        stopScreenPinning();

        finish();
    }

    /**
     * Show an error message dialog.
     *
     * @param message the message to be displayed
     * @param bitmap  any bitmap image to displayed along with error message
     */
    private void showError(String message, final Bitmap bitmap) {

        stopScreenPinning();

        SoundUtil.playShortResource(this, R.raw.err);

        releaseResources();

        if (retryCount < 1) {
            alertDialogToBeDestroyed = AlertUtil.showError(this, R.string.error, message, bitmap, R.string.retry,
                    (dialogInterface, i) -> {

                        retryCount++;

                        if (getIntent().getBooleanExtra(ConstantKey.RUN_TEST, false)) {
                            runTest();
                        } else {
                            start();
                        }
                    },
                    (dialogInterface, i) -> {
                        stopScreenPinning();
                        dialogInterface.dismiss();
                        releaseResources();
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                    }, null
            );
        } else {
            alertDialogToBeDestroyed = AlertUtil.showError(this, R.string.error, message, bitmap, R.string.ok,
                    null,
                    (dialogInterface, i) -> {
                        stopScreenPinning();
                        dialogInterface.dismiss();
                        releaseResources();
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                    }, null
            );
        }
    }

    private void releaseResources() {
        if (alertDialogToBeDestroyed != null) {
            alertDialogToBeDestroyed.dismiss();
        }
    }

    /**
     * Navigate back to the dilution selection screen if re-testing.
     */
    @SuppressWarnings("unused")
    public void onTestWithDilution(View view) {

        stopScreenPinning();

        if (!fragmentManager.popBackStackImmediate("dilution", 0)) {
            super.onBackPressed();
        }

        invalidateOptionsMenu();
    }

    @Override
    public void onDilutionSelected(int dilution) {
        currentDilution = dilution;
        runTest();
    }

    @Override
    public void onCustomDilution(Integer dilution) {
        currentDilution = dilution;
        runTest();
    }

    private void checkCameraMegaPixel() {

        cameraIsOk = true;
        if (PreferencesUtil.getBoolean(this, R.string.showMinMegaPixelDialogKey, true)) {
            try {

                if (getMaxSupportedMegaPixelsByCamera(this) < Constants.MIN_CAMERA_MEGA_PIXELS) {

                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                    View checkBoxView = View.inflate(this, R.layout.dialog_message, null);
                    CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);
                    checkBox.setOnCheckedChangeListener((buttonView, isChecked)
                            -> PreferencesUtil.setBoolean(getBaseContext(),
                            R.string.showMinMegaPixelDialogKey, !isChecked));

                    androidx.appcompat.app.AlertDialog.Builder builder
                            = new androidx.appcompat.app.AlertDialog.Builder(this);

                    builder.setTitle(R.string.warning);
                    builder.setMessage(R.string.camera_not_good)
                            .setView(checkBoxView)
                            .setCancelable(false)
                            .setPositiveButton(R.string.continue_anyway, (dialog, id) -> runTest())
                            .setNegativeButton(R.string.stop_test, (dialog, id) -> {
                                dialog.dismiss();
                                cameraIsOk = false;
                                finish();
                            }).show();

                } else {
                    runTest();
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        } else {
            runTest();
        }
    }

    public void sendToServerClick(View view) {
        stopScreenPinning();
        try {
            ConfigDownloader.sendDataToCloudDatabase(this, testInfo, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendTestResultClick(View view) {
        if (!NetUtil.isNetworkAvailable(this)) {
            Toast.makeText(this,
                    "No data connection. Please connect to the internet and try again.", Toast.LENGTH_LONG).show();
        } else {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            DiagnosticSendDialogFragment diagnosticSendDialogFragment =
                    DiagnosticSendDialogFragment.newInstance();
            diagnosticSendDialogFragment.show(ft, "sendDialog");
        }
        stopScreenPinning();
    }

    @Override
    public void onDetailsSaved(String comment) {
        ConfigDownloader.sendDataToCloudDatabase(this, testInfo, comment);
    }

    private boolean isAppInLockTaskMode() {
        ActivityManager activityManager;

        activityManager = (ActivityManager)
                this.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return activityManager.getLockTaskModeState()
                        != ActivityManager.LOCK_TASK_MODE_NONE;
            }

            //noinspection deprecation
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    && activityManager.isInLockTaskMode();
        }

        return false;
    }

    @Override
    public void onDismissed(boolean retry) {
        testStarted = false;
        invalidateOptionsMenu();
        if (retry) {
            if (retryCount < 1) {
                retryCount++;
                runTest();
            } else {
                setResult(Activity.RESULT_CANCELED);

                stopScreenPinning();

                finish();
            }
        } else {
            sendResult();

            finish();
        }
    }
}
