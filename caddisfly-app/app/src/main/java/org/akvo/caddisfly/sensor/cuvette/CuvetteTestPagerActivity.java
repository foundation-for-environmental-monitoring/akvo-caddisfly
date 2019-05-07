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

package org.akvo.caddisfly.sensor.cuvette;

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
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.dao.CalibrationDao;
import org.akvo.caddisfly.databinding.FragmentInstructionBinding;
import org.akvo.caddisfly.diagnostic.DiagnosticResultDialog;
import org.akvo.caddisfly.diagnostic.DiagnosticSwatchActivity;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.SoundUtil;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.Instruction;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.chamber.BaseRunTest;
import org.akvo.caddisfly.sensor.chamber.CalibrationFile;
import org.akvo.caddisfly.sensor.chamber.CalibrationGraphActivity;
import org.akvo.caddisfly.sensor.chamber.CalibrationResultDialog;
import org.akvo.caddisfly.sensor.chamber.ChamberAboveFragment;
import org.akvo.caddisfly.sensor.chamber.ChamberBelowFragment;
import org.akvo.caddisfly.sensor.chamber.EditCustomDilution;
import org.akvo.caddisfly.sensor.chamber.ResultFragment;
import org.akvo.caddisfly.sensor.chamber.RunTest;
import org.akvo.caddisfly.sensor.chamber.SelectDilutionFragment;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ConfigDownloader;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.NetUtil;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.akvo.caddisfly.viewmodel.TestInfoViewModel;
import org.akvo.caddisfly.widget.ButtonType;
import org.akvo.caddisfly.widget.CustomViewPager;
import org.akvo.caddisfly.widget.PageIndicatorView;
import org.akvo.caddisfly.widget.SwipeDirection;
import org.jetbrains.annotations.NotNull;
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

public class CuvetteTestPagerActivity extends BaseActivity implements
        BaseRunTest.OnResultListener,
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
    SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
    TextView imagePageRight;
    private RunTest runTestFragment;
    private SelectDilutionFragment selectDilutionFragment;
    private ResultFragment resultFragment;
    private FragmentManager fragmentManager;
    private TestInfo testInfo;
    private boolean cameraIsOk = false;
    private int currentDilution = 0;
    private AlertDialog alertDialogToBeDestroyed;
    private boolean testStarted;
    private int dilutionPageNumber;
    private int resultPageNumber;
    private int testPageNumber;
    private int totalPageCount;
    private ArrayList<Instruction> instructions = new ArrayList<>();
    private CustomViewPager viewPager;
    private PageIndicatorView pagerIndicator;
    private RelativeLayout footerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chamber_test_pager);

        viewPager = findViewById(R.id.viewPager);
        pagerIndicator = findViewById(R.id.pager_indicator);
        footerLayout = findViewById(R.id.layout_footer);

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

            boolean isInternal = getIntent().getBooleanExtra(IS_INTERNAL, true);
            resultFragment = ResultFragment.newInstance(testInfo, isInternal);

            if (getIntent().getBooleanExtra(ConstantKey.RUN_TEST, false)) {
                start();
            } else {
                setTitle(R.string.calibration);
//                boolean isInternal = getIntent().getBooleanExtra(IS_INTERNAL, true);
            }
        }

        setupInstructions();

        pagerIndicator.showDots(true);

        imagePageRight = findViewById(R.id.image_pageRight);
        imagePageRight.setOnClickListener(view -> pageNext());

        TextView imagePageLeft = findViewById(R.id.image_pageLeft);
        imagePageLeft.setOnClickListener(view -> pageBack());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == testPageNumber) {
                    runTestFragment.start();
                } else {
                    runTestFragment.stop();
                }
//                else if (position > photoPageNumber) {
//                    if (!resultPhotoFragment.isValid()) {
//                        viewPager.setCurrentItem(photoPageNumber);
//                    }
//                } else if (result1PageNumber != -1 && position > result1PageNumber) {
//                    if (!result1Fragment.isValid(true)) {
//                        viewPager.setCurrentItem(result1PageNumber);
//                    }
//                } else if (photo1PageNumber != -1 && position > photo1PageNumber) {
//                    if (!result1PhotoFragment.isValid()) {
//                        viewPager.setCurrentItem(photo1PageNumber);
//                    }
//                }
            }

            @Override
            public void onPageSelected(int position) {
                pagerIndicator.setActiveIndex(position);

                if (position < 1) {
                    imagePageLeft.setVisibility(View.INVISIBLE);
                } else {
                    imagePageLeft.setVisibility(View.VISIBLE);
                }

                showHideFooter();

//                if (position == resultPageNumber) {
//                    showWaitingView();
//                } else {
//                    showInstructionsView();
//                    onInstructionFinish(testInfo.getInstructions().size() - position);
//                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
//                    if (viewPager.getCurrentItem() == resultPageNumber &&
//                            !resultFragment.isValid(false)) {
//                        resultFragment.showSoftKeyboard();
//                    } else if (viewPager.getCurrentItem() == result1PageNumber &&
//                            !result1Fragment.isValid(false)) {
//                        if (result1Fragment != null) {
//                            result1Fragment.showSoftKeyboard();
//                        }
//                    } else {
//                        resultFragment.hideSoftKeyboard();
//                        if (result1Fragment != null) {
//                            result1Fragment.hideSoftKeyboard();
//                        }
//                    }
                }
            }
        });

        viewPager.setAdapter(mSectionsPagerAdapter);

        showHideFooter();
    }

//    private void goToFragment(Fragment fragment) {
//        if (fragmentManager.getFragments().size() > 0) {
//            fragmentManager.beginTransaction()
//                    .addToBackStack(null)
//                    .replace(R.id.fragment_container, fragment).commit();
//        } else {
//            fragmentManager.beginTransaction()
//                    .add(R.id.fragment_container, fragment).commit();
//        }
//
//        if (fragment instanceof SelectDilutionFragment) {
//            testStarted = true;
//        } else if (fragment != runTestFragment) {
//            testStarted = false;
//        }
//
//        invalidateOptionsMenu();
//    }

    private void start() {

        if (testInfo.getDilutions().size() > 0) {
            selectDilutionFragment = SelectDilutionFragment.newInstance(testInfo);
//            goToFragment(selectDilutionFragment);
        } else {
            runTest();
        }

        setTitle(testInfo.getName());

        invalidateOptionsMenu();

    }

    private void runTest() {
        if (cameraIsOk) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    && AppConfig.USE_SCREEN_PINNING) {
                startLockTask();
            }

            runTestFragment.setDilution(currentDilution);
            pageNext();
//            goToFragment((Fragment) runTestFragment);

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
            if (viewPager.getCurrentItem() == 0) {
                if (!fragmentManager.popBackStackImmediate()) {
                    super.onBackPressed();
                }
            } else {
                pageBack();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (AppPreferences.isDiagnosticMode() && !testStarted) {
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
        final TestInfoViewModel model =
                ViewModelProviders.of(this).get(TestInfoViewModel.class);

        model.setTest(testInfo);

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

        if (cancelled == Activity.RESULT_CANCELED) {
            stopScreenPinning();
            pageBack();
            return;
        }

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

                testInfo.setResultDetail(resultDetail);

                resultFragment.setInfo(testInfo);

                pageNext();

//                fragmentManager.popBackStack();
//                fragmentManager
//                        .beginTransaction()
//                        .addToBackStack(null)
//                        .replace(R.id.fragment_container,
//                                ResultFragment.newInstance(testInfo, isInternal), null).commit();

                if (AppPreferences.getShowDebugInfo()) {
                    showDiagnosticResultDialog(false, resultDetail, resultDetails, false);
                }

            } else {

                if (AppPreferences.getShowDebugInfo()) {

                    SoundUtil.playShortResource(this, R.raw.err);

                    releaseResources();

                    setResult(Activity.RESULT_CANCELED);

                    stopScreenPinning();

                    fragmentManager.popBackStack();
                    if (testInfo.getDilutions().size() > 0) {
                        fragmentManager.popBackStack();
                    }

                    showDiagnosticResultDialog(true, resultDetail, resultDetails, false);

                } else {

                    fragmentManager.popBackStack();

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
                testFailed, resultDetail, resultDetails, isCalibration);
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
    public void onAcceptResultClick(View view) {
        submitResult();
    }

    public void submitResult() {
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

        alertDialogToBeDestroyed = AlertUtil.showError(this, R.string.error, message, bitmap, R.string.retry,
                (dialogInterface, i) -> {
                    stopScreenPinning();
                    if (getIntent().getBooleanExtra(ConstantKey.RUN_TEST, false)) {
                        start();
                    } else {
                        runTest();
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
        setupInstructions();
        pageNext();
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
    public void onDismissed() {
        testStarted = false;
        invalidateOptionsMenu();
    }

    private void pageNext() {
        viewPager.setCurrentItem(Math.min(totalPageCount, viewPager.getCurrentItem() + 1));
    }

    private void pageBack() {
        viewPager.setCurrentItem(Math.max(0, viewPager.getCurrentItem() - 1));
    }

    public void onStartClick(View view) {
        runTest();
    }

    private void setupInstructions() {
        int instructionIndex = 0;
        instructions.clear();
        for (int i = 0; i < testInfo.getInstructions().size(); i++) {
            if (currentDilution == 1 && i > 0 && i < 8) {
                continue;
            }
            Instruction instruction;
            try {
                instruction = testInfo.getInstructions().get(i).clone();
                if (instruction != null) {
                    if (instruction.section.get(0).contains("<test>")) {
                        testPageNumber = instructionIndex;
                    } else if (instruction.section.get(0).contains("<result>")) {
                        resultPageNumber = instructionIndex;
                    } else if (instruction.section.get(0).contains("<dilution>")) {
                        dilutionPageNumber = instructionIndex;
                    } else {
                        instruction.section.set(0, instructionIndex + ". " + instruction.section.get(0));
                    }
                }
                instructions.add(instruction);

                instructionIndex++;

            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        totalPageCount = instructionIndex;
        pagerIndicator.setPageCount(totalPageCount - 2);
        pagerIndicator.setVisibility(View.GONE);
        pagerIndicator.invalidate();
        pagerIndicator.setVisibility(View.VISIBLE);

        viewPager.setAdapter(mSectionsPagerAdapter);
    }

    private void showHideFooter() {
        imagePageRight.setVisibility(View.VISIBLE);
        if (viewPager.getCurrentItem() == testPageNumber) {
            footerLayout.setVisibility(View.GONE);
            viewPager.setAllowedSwipeDirection(SwipeDirection.left);
        } else if (viewPager.getCurrentItem() == dilutionPageNumber) {
            footerLayout.setVisibility(View.GONE);
            viewPager.setAllowedSwipeDirection(SwipeDirection.none);
        } else if (viewPager.getCurrentItem() == testPageNumber - 1) {
            imagePageRight.setVisibility(View.INVISIBLE);
            viewPager.setAllowedSwipeDirection(SwipeDirection.left);
        } else if (viewPager.getCurrentItem() == resultPageNumber) {
            footerLayout.setVisibility(View.GONE);
            viewPager.setAllowedSwipeDirection(SwipeDirection.none);
        } else if (viewPager.getCurrentItem() == totalPageCount - 1) {
            footerLayout.setVisibility(View.GONE);
            viewPager.setAllowedSwipeDirection(SwipeDirection.left);
        } else {
            footerLayout.setVisibility(View.VISIBLE);
            viewPager.setAllowedSwipeDirection(SwipeDirection.all);
        }
    }

    public void onClickNext(View view) {
        pageNext();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_SHOW_OK = "show_ok";
        FragmentInstructionBinding fragmentInstructionBinding;
        Instruction instruction;
        private ButtonType showOk;

        /**
         * Returns a new instance of this fragment for the given section number.
         *
         * @param instruction The information to to display
         * @return The instance
         */
        static PlaceholderFragment newInstance(Instruction instruction, ButtonType button) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putParcelable(ARG_SECTION_NUMBER, instruction);
            args.putSerializable(ARG_SHOW_OK, button);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            fragmentInstructionBinding = DataBindingUtil.inflate(inflater,
                    R.layout.fragment_instruction, container, false);

            if (getArguments() != null) {
                instruction = getArguments().getParcelable(ARG_SECTION_NUMBER);
                showOk = (ButtonType) getArguments().getSerializable(ARG_SHOW_OK);
                fragmentInstructionBinding.setInstruction(instruction);
            }

            View view = fragmentInstructionBinding.getRoot();

            if (showOk == ButtonType.NEXT) {
                view.findViewById(R.id.buttonStart).setVisibility(View.VISIBLE);
            } else if (showOk == ButtonType.ACCEPT) {
                view.findViewById(R.id.buttonAcceptResult).setVisibility(View.VISIBLE);
            }
            return view;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            if (position == testPageNumber) {
                return (Fragment) runTestFragment;
            } else if (position == resultPageNumber) {
                return resultFragment;
            } else if (position == testPageNumber - 1) {
                return PlaceholderFragment.newInstance(
                        instructions.get(position), ButtonType.NEXT);
            } else if (position == resultPageNumber + 1) {
                return PlaceholderFragment.newInstance(
                        instructions.get(position), ButtonType.ACCEPT);
            } else if (position == dilutionPageNumber) {
                return selectDilutionFragment;
            } else {
                return PlaceholderFragment.newInstance(
                        instructions.get(position), ButtonType.NONE);
            }
        }

        @Override
        public int getCount() {
            return totalPageCount;
        }
    }
}
