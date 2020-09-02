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
package org.akvo.caddisfly.diagnostic

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.diagnostic.DiagnosticResultDialog.OnDismissed
import org.akvo.caddisfly.entity.Calibration
import org.akvo.caddisfly.helper.SoundUtil.playShortResource
import org.akvo.caddisfly.helper.SwatchHelper.analyzeColor
import org.akvo.caddisfly.helper.SwatchHelper.getAverageColor
import org.akvo.caddisfly.model.ColorInfo
import org.akvo.caddisfly.model.ResultDetail
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.preference.AppPreferences
import org.akvo.caddisfly.sensor.chamber.BaseRunTest.OnResultListener
import org.akvo.caddisfly.sensor.chamber.RunTest
import org.akvo.caddisfly.ui.BaseActivity
import org.akvo.caddisfly.util.AlertUtil
import java.util.*

class ChamberPreviewActivity : BaseActivity(), OnResultListener, OnDismissed {
    private var runTestFragment: RunTest? = null
    private var fragmentManager: FragmentManager? = null
    private var testInfo: TestInfo? = null
    private var alertDialogToBeDestroyed: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chamber_test)
        fragmentManager = supportFragmentManager
        // Add list fragment if this is first creation
        if (savedInstanceState == null) {
            testInfo = intent.getParcelableExtra(ConstantKey.TEST_INFO)
            if (testInfo == null) {
                finish()
                return
            }
            testInfo!!.cameraAbove = true
            runTestFragment = ChamberPreviewFragment.newInstance(testInfo)
            start()
        }
    }

    private fun goToFragment(fragment: Fragment?) {
        if (fragmentManager!!.fragments.size > 0) {
            fragmentManager!!.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.fragment_container, fragment!!).commit()
        } else {
            fragmentManager!!.beginTransaction()
                .add(R.id.fragment_container, fragment!!).commit()
        }
        invalidateOptionsMenu()
    }

    private fun start() {
        runTest()
        setTitle(R.string.cameraPreview)
    }

    private fun runTest() {
        goToFragment(runTestFragment as Fragment?)
    }

    fun runTestClick(@Suppress("UNUSED_PARAMETER") view: View?) {
        runTestFragment!!.setCalibration(null)
        start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResult(resultDetails: ArrayList<ResultDetail>, calibration: Calibration?) {
        val colorInfo = ColorInfo(
            getAverageColor(resultDetails),
            resultDetails[resultDetails.size - 1].quality
        )
        val resultDetail = analyzeColor(
            testInfo!!.swatches.size,
            colorInfo, testInfo!!.swatches
        )
        resultDetail.bitmap = resultDetails[resultDetails.size - 1].bitmap
        resultDetail.croppedBitmap = resultDetails[resultDetails.size - 1].croppedBitmap
        if (calibration == null) {
            val dilution = resultDetails[0].dilution
            val value = resultDetail.result
            if (value > -1) {
                if (supportActionBar != null) {
                    supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                }
                val result = testInfo!!.results!![0]
                result.setResult(value, dilution, testInfo!!.maxDilution)
                if (result.highLevelsFound() && testInfo!!.dilution != testInfo!!.maxDilution) {
                    playShortResource(this, R.raw.beep_long)
                } else {
                    playShortResource(this, R.raw.done)
                }
                fragmentManager!!.beginTransaction()
                    .remove((runTestFragment as Fragment?)!!)
                    .commit()
                showDiagnosticResultDialog(false, resultDetail, resultDetails, false)
                testInfo!!.resultDetail = resultDetail
            } else {
                if (AppPreferences.showDebugInfo) {
                    playShortResource(this, R.raw.err)
                    releaseResources()
                    setResult(Activity.RESULT_CANCELED)
                    fragmentManager!!.beginTransaction()
                        .remove((runTestFragment as Fragment?)!!)
                        .commit()
                    showDiagnosticResultDialog(true, resultDetail, resultDetails, false)
                } else {
                    fragmentManager!!.beginTransaction()
                        .remove((runTestFragment as Fragment?)!!)
                        .commit()
                    showError(
                        String.format(
                            TWO_SENTENCE_FORMAT, getString(R.string.error_test_failed),
                            getString(R.string.check_chamber_placement)
                        ),
                        resultDetails[resultDetails.size - 1].croppedBitmap
                    )
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
    private fun showDiagnosticResultDialog(
        testFailed: Boolean, resultDetail: ResultDetail,
        resultDetails: ArrayList<ResultDetail>,
        @Suppress("SameParameterValue") isCalibration: Boolean
    ) {
        val resultFragment: DialogFragment = DiagnosticResultDialog.newInstance(
            testFailed, resultDetail, resultDetails, isCalibration
        )
        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag("gridDialog")
        if (prev != null) {
            ft.remove(prev)
        }
        resultFragment.isCancelable = false
        resultFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog)
        resultFragment.show(ft, "gridDialog")
    }

    /**
     * Show an error message dialog.
     *
     * @param message the message to be displayed
     * @param bitmap  any bitmap image to displayed along with error message
     */
    private fun showError(message: String, bitmap: Bitmap?) {
        playShortResource(this, R.raw.err)
        releaseResources()
        alertDialogToBeDestroyed = AlertUtil.showError(
            this, R.string.error, message, bitmap, R.string.retry,
            { _: DialogInterface?, _: Int ->
                if (intent.getBooleanExtra(ConstantKey.RUN_TEST, false)) {
                    start()
                } else {
                    runTest()
                }
            },
            { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
                releaseResources()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }, null
        )
    }

    private fun releaseResources() {
        if (alertDialogToBeDestroyed != null) {
            alertDialogToBeDestroyed!!.dismiss()
        }
    }

    override fun onDismissed() {
        finish()
    }

    companion object {
        private const val TWO_SENTENCE_FORMAT = "%s%n%n%s"
    }
}