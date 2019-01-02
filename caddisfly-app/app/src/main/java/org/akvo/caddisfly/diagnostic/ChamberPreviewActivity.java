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

package org.akvo.caddisfly.diagnostic;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.helper.SoundUtil;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.chamber.BaseRunTest;
import org.akvo.caddisfly.sensor.chamber.RunTest;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.AlertUtil;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class ChamberPreviewActivity extends BaseActivity implements
        BaseRunTest.OnResultListener,
        DiagnosticResultDialog.OnDismissed {

    private static final String TWO_SENTENCE_FORMAT = "%s%n%n%s";

    private RunTest runTestFragment;
    private FragmentManager fragmentManager;
    private TestInfo testInfo;
    private AlertDialog alertDialogToBeDestroyed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chamber_test);

        fragmentManager = getSupportFragmentManager();

        // Add list fragment if this is first creation
        if (savedInstanceState == null) {

            testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);
            if (testInfo == null) {
                finish();
                return;
            }

            testInfo.setCameraAbove(true);

            runTestFragment = ChamberPreviewFragment.newInstance(testInfo);

            start();
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

        invalidateOptionsMenu();
    }

    private void start() {

        runTest();

        setTitle(R.string.cameraPreview);

    }

    private void runTest() {
        goToFragment((Fragment) runTestFragment);
    }

    @SuppressWarnings("unused")
    public void runTestClick(View view) {
        runTestFragment.setCalibration(null);
        start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResult(ArrayList<ResultDetail> resultDetails,
                         ArrayList<ResultDetail> oneStepResults, Calibration calibration) {

        ColorInfo colorInfo = new ColorInfo(SwatchHelper.getAverageColor(resultDetails),
                resultDetails.get(resultDetails.size() - 1).getQuality());
        ResultDetail resultDetail = SwatchHelper.analyzeColor(testInfo.getSwatches().size(),
                colorInfo, testInfo.getSwatches());

        resultDetail.setBitmap(resultDetails.get(resultDetails.size() - 1).getBitmap());
        resultDetail.setCroppedBitmap(resultDetails.get(resultDetails.size() - 1).getCroppedBitmap());

        if (calibration == null) {

            int dilution = resultDetails.get(0).getDilution();

            double value = resultDetail.getResult();

            if (value > -1) {

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }

                Result result = testInfo.getResults().get(0);
                result.setResult(value, dilution, testInfo.getMaxDilution());

                if (result.highLevelsFound() && testInfo.getDilution() != testInfo.getMaxDilution()) {
                    SoundUtil.playShortResource(this, R.raw.beep_long);
                } else {
                    SoundUtil.playShortResource(this, R.raw.done);
                }

                fragmentManager.beginTransaction()
                        .remove((Fragment) runTestFragment)
                        .commit();

                showDiagnosticResultDialog(false, resultDetail, resultDetails, false);

                testInfo.setResultDetail(resultDetail);

            } else {

                if (AppPreferences.getShowDebugInfo()) {

                    SoundUtil.playShortResource(this, R.raw.err);

                    releaseResources();

                    setResult(Activity.RESULT_CANCELED);

                    fragmentManager.beginTransaction()
                            .remove((Fragment) runTestFragment)
                            .commit();

                    showDiagnosticResultDialog(true, resultDetail, resultDetails, false);

                } else {

                    fragmentManager.beginTransaction()
                            .remove((Fragment) runTestFragment)
                            .commit();

                    showError(String.format(TWO_SENTENCE_FORMAT, getString(R.string.errorTestFailed),
                            getString(R.string.checkChamberPlacement)),
                            resultDetails.get(resultDetails.size() - 1).getCroppedBitmap());
                }
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
                testFailed, 0, resultDetail, resultDetail, resultDetails, isCalibration);
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
     * Show an error message dialog.
     *
     * @param message the message to be displayed
     * @param bitmap  any bitmap image to displayed along with error message
     */
    private void showError(String message, final Bitmap bitmap) {

        SoundUtil.playShortResource(this, R.raw.err);

        releaseResources();

        alertDialogToBeDestroyed = AlertUtil.showError(this, R.string.error, message, bitmap, R.string.retry,
                (dialogInterface, i) -> {
                    if (getIntent().getBooleanExtra(ConstantKey.RUN_TEST, false)) {
                        start();
                    } else {
                        runTest();
                    }
                },
                (dialogInterface, i) -> {
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

    @Override
    public void onDismissed(boolean retry) {
        finish();
    }
}
