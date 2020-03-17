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
package org.akvo.caddisfly.sensor.chamber

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.SparseArray
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemLongClickListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.R
import org.akvo.caddisfly.app.CaddisflyApp.Companion.db
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.common.ConstantKey.IS_INTERNAL
import org.akvo.caddisfly.common.Constants
import org.akvo.caddisfly.common.SensorConstants
import org.akvo.caddisfly.diagnostic.DiagnosticResultDialog.Companion.newInstance
import org.akvo.caddisfly.diagnostic.DiagnosticResultDialog.OnDismissed
import org.akvo.caddisfly.diagnostic.DiagnosticSwatchActivity
import org.akvo.caddisfly.entity.Calibration
import org.akvo.caddisfly.helper.CameraHelper
import org.akvo.caddisfly.helper.FileHelper
import org.akvo.caddisfly.helper.FileType
import org.akvo.caddisfly.helper.SoundUtil.playShortResource
import org.akvo.caddisfly.helper.SwatchHelper.analyzeColor
import org.akvo.caddisfly.helper.SwatchHelper.getAverageColor
import org.akvo.caddisfly.helper.SwatchHelper.loadCalibrationFromFile
import org.akvo.caddisfly.helper.TestConfigHelper.getJsonResult
import org.akvo.caddisfly.model.ColorInfo
import org.akvo.caddisfly.model.ResultDetail
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.preference.AppPreferences
import org.akvo.caddisfly.sensor.chamber.BaseRunTest.OnResultListener
import org.akvo.caddisfly.sensor.chamber.CalibrationItemFragment.OnCalibrationSelectedListener
import org.akvo.caddisfly.sensor.chamber.CalibrationResultDialog.Companion.newInstance
import org.akvo.caddisfly.sensor.chamber.EditCustomDilution.OnCustomDilutionListener
import org.akvo.caddisfly.sensor.chamber.SaveCalibrationDialogFragment.OnCalibrationDetailsSavedListener
import org.akvo.caddisfly.sensor.chamber.SelectDilutionFragment.OnDilutionSelectedListener
import org.akvo.caddisfly.ui.BaseActivity
import org.akvo.caddisfly.util.AlertUtil
import org.akvo.caddisfly.util.FileUtil
import org.akvo.caddisfly.util.PreferencesUtil
import org.akvo.caddisfly.viewmodel.TestInfoViewModel
import timber.log.Timber
import java.io.File
import java.util.*

class ChamberTestActivity : BaseActivity(), OnResultListener, OnCalibrationSelectedListener,
        OnCalibrationDetailsSavedListener, OnDilutionSelectedListener,
        OnCustomDilutionListener, OnDismissed {
    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            finish()
        }
    }
    private var runTestFragment: RunTest? = null
    private var calibrationItemFragment: CalibrationItemFragment? = null
    private var fragmentManager: FragmentManager? = null
    private lateinit var testInfo: TestInfo
    private var cameraIsOk = false
    private var currentDilution = 1
    private var alertDialogToBeDestroyed: AlertDialog? = null
    private var testStarted = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chamber_test)
        fragmentManager = supportFragmentManager
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                IntentFilter("data-sent-to-dash"))
        // Add list fragment if this is first creation
        if (savedInstanceState == null) {
            try {
                testInfo = intent.getParcelableExtra(ConstantKey.TEST_INFO)!!
            } catch (e: Exception) {
                finish()
                return
            }
            runTestFragment = ChamberAboveFragment.newInstance(testInfo)

            if (intent.getBooleanExtra(ConstantKey.RUN_TEST, false)) {
                start()
            } else {
                setTitle(R.string.calibration)
                val isInternal = intent.getBooleanExtra(IS_INTERNAL, true)
                calibrationItemFragment = CalibrationItemFragment.newInstance(testInfo, isInternal)
                goToFragment(calibrationItemFragment)
            }
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
        if (fragment is SelectDilutionFragment) {
            testStarted = true
        } else if (fragment !== runTestFragment) {
            testStarted = false
        }
        invalidateOptionsMenu()
    }

    private fun start() {
        if (testInfo.dilutions.isNotEmpty()) {
            val selectDilutionFragment: Fragment = SelectDilutionFragment.newInstance(testInfo)
            goToFragment(selectDilutionFragment)
        } else {
            runTest()
        }
        setTitle(R.string.analyze)
        invalidateOptionsMenu()
    }

    private fun runTest() {
        if (cameraIsOk) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    && !BuildConfig.TEST_RUNNING) {
                startLockTask()
            }
            runTestFragment!!.setDilution(currentDilution)
            goToFragment(runTestFragment as Fragment?)
            testStarted = true
        } else {
            checkCameraMegaPixel()
        }
        invalidateOptionsMenu()
    }

    fun runTestClick(@Suppress("UNUSED_PARAMETER") view: View?) {
        if (runTestFragment != null) {
            runTestFragment!!.setCalibration(null)
        }
        start()
    }

    override fun onCalibrationSelected(item: Calibration?) {
        val calibrationDetail = db?.calibrationDao()!!.getCalibrationDetails(testInfo.uuid)
        if (calibrationDetail == null) {
            showEditCalibrationDetailsDialog(true)
        } else {
            val milliseconds = calibrationDetail.expiry
            //Show edit calibration details dialog if required
            if (milliseconds <= Date().time) {
                showEditCalibrationDetailsDialog(true)
            } else {
                Handler().postDelayed({
                    runTestFragment!!.setCalibration(item)
                    setTitle(R.string.calibrate)
                    runTest()
                }, 150)
            }
        }
    }

    override fun onBackPressed() {
        if (isAppInLockTaskMode) {
            if ((runTestFragment as Fragment?)!!.isVisible) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    showLockTaskEscapeMessage()
                } else {
                    Toast.makeText(this, R.string.screen_pinned, Toast.LENGTH_SHORT).show()
                }
            } else {
                stopScreenPinning()
            }
        } else {
            if (!fragmentManager!!.popBackStackImmediate()) {
                super.onBackPressed()
            }
            refreshTitle()
            testStarted = false
            invalidateOptionsMenu()
        }
    }

    private fun refreshTitle() {
        if (fragmentManager!!.backStackEntryCount == 0) {
            if (intent.getBooleanExtra(ConstantKey.RUN_TEST, false)) {
                setTitle(R.string.analyze)
            } else {
                setTitle(R.string.calibration)
            }
        }
    }

    fun onEditCalibration(@Suppress("UNUSED_PARAMETER") view: View?) {
        showEditCalibrationDetailsDialog(true)
    }

    private fun showEditCalibrationDetailsDialog(isEdit: Boolean) {
        val ft = supportFragmentManager.beginTransaction()
        val saveCalibrationDialogFragment = SaveCalibrationDialogFragment.newInstance(testInfo, isEdit)
        saveCalibrationDialogFragment.show(ft, "saveCalibrationDialog")
    }

    override fun onCalibrationDetailsSaved() {
        loadDetails()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (AppPreferences.isDiagnosticMode() && !testStarted
                && calibrationItemFragment != null && calibrationItemFragment!!.isVisible) {
            menuInflater.inflate(R.menu.menu_calibrate_dev, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuGraph -> {
                val graphIntent = Intent(this, CalibrationGraphActivity::class.java)
                graphIntent.putExtra(ConstantKey.TEST_INFO, testInfo)
                startActivity(graphIntent)
                return true
            }
            R.id.actionSwatches -> {
                val intent = Intent(this, DiagnosticSwatchActivity::class.java)
                intent.putExtra(ConstantKey.TEST_INFO, testInfo)
                startActivity(intent)
                return true
            }
            R.id.menuLoad -> {
                loadCalibrationFromFile(this)
                return true
            }
            R.id.menuSave -> {
                showEditCalibrationDetailsDialog(false)
                return true
            }
            android.R.id.home -> {
                if (isAppInLockTaskMode) {
                    if ((runTestFragment as Fragment?)!!.isVisible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            showLockTaskEscapeMessage()
                        } else {
                            Toast.makeText(this, R.string.screen_pinned, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        stopScreenPinning()
                    }
                } else {
                    stopScreenPinning()
                    releaseResources()
                    if (!fragmentManager!!.popBackStackImmediate()) {
                        super.onBackPressed()
                    }
                    refreshTitle()
                    testStarted = false
                    invalidateOptionsMenu()
                }
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadDetails() {
        val calibrations = db?.calibrationDao()!!.getAll(testInfo.uuid)
        if (calibrations != null) {
            testInfo.calibrations = calibrations
        }
        if (calibrationItemFragment != null) {
            calibrationItemFragment!!.setAdapter(testInfo)
        }
        val model = ViewModelProvider(this).get(TestInfoViewModel::class.java)
        model.setTest(testInfo)
        calibrationItemFragment!!.loadDetails()
    }

    /**
     * Load the calibrated swatches from the calibration text file.
     */
    private fun loadCalibrationFromFile(context: Context) {
        try {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.loadCalibration)
            val arrayAdapter = ArrayAdapter<String>(context, R.layout.row_text)
            val path = FileHelper.getFilesDir(FileType.CALIBRATION, testInfo.uuid!!)
            var listFilesTemp: Array<File>? = null
            if (path.exists() && path.isDirectory) {
                listFilesTemp = path.listFiles()
            }
            val listFiles = listFilesTemp
            if (listFiles != null && listFiles.isNotEmpty()) {
                Arrays.sort(listFiles)
                for (listFile in listFiles) {
                    arrayAdapter.add(listFile.name)
                }
                builder.setNegativeButton(R.string.cancel
                ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                builder.setAdapter(arrayAdapter
                ) { _: DialogInterface?, which: Int ->
                    val fileName = listFiles[which].name
                    try {
                        loadCalibrationFromFile(testInfo, fileName)
                        loadDetails()
                    } catch (ex: Exception) {
                        AlertUtil.showError(context, R.string.error, getString(R.string.errorLoadingFile),
                                null, R.string.ok,
                                DialogInterface.OnClickListener { dialog1: DialogInterface, _: Int -> dialog1.dismiss() }, null, null)
                    }
                }
                val alertDialog = builder.create()
                alertDialog.setOnShowListener {
                    val listView = alertDialog.listView
                    listView.onItemLongClickListener = OnItemLongClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
                        AlertUtil.askQuestion(context, R.string.delete,
                                R.string.deleteConfirm, R.string.delete, R.string.cancel, true,
                                DialogInterface.OnClickListener { _: DialogInterface?, _: Int ->
                                    val fileName = listFiles[i].name
                                    FileUtil.deleteFile(path, fileName)
                                    @Suppress("UNCHECKED_CAST")
                                    val listAdapter = listView.adapter as ArrayAdapter<Any>
                                    listAdapter.remove(listAdapter.getItem(i))
                                    alertDialog.dismiss()
                                    Toast.makeText(context, R.string.deleted, Toast.LENGTH_SHORT).show()
                                }, null)
                        true
                    }
                }
                alertDialog.show()
            } else {
                AlertUtil.showMessage(context, R.string.notFound, R.string.loadFilesNotAvailable)
            }
        } catch (ignored: ActivityNotFoundException) { // do nothing
        }
    }

    override fun onResult(resultDetails: ArrayList<ResultDetail>, calibration: Calibration?) {
        val colorInfo = ColorInfo(getAverageColor(resultDetails), 0)
        val resultDetail = analyzeColor(testInfo.swatches.size,
                colorInfo, testInfo.swatches)
        resultDetail.bitmap = resultDetails[resultDetails.size - 1].bitmap
        resultDetail.croppedBitmap = resultDetails[resultDetails.size - 1].croppedBitmap
        if (calibration == null) {
            val dilution = resultDetails[0].dilution
            val result = testInfo.results!![0]
            val value = resultDetail.result
            if (value > -1) {
                if (supportActionBar != null) {
                    supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                }
                result.setResult(value, dilution, testInfo.maxDilution)
                resultDetail.result = result.resultValue
                if (result.highLevelsFound() && testInfo.dilution != testInfo.maxDilution) {
                    playShortResource(this, R.raw.beep_long)
                } else {
                    playShortResource(this, R.raw.done)
                }
                val isInternal = intent.getBooleanExtra(IS_INTERNAL, true)
                fragmentManager!!.popBackStack()
                fragmentManager!!
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.fragment_container,
                                ResultFragment.newInstance(testInfo, isInternal), null).commit()
                testInfo.resultDetail = resultDetail
                if (AppPreferences.showDebugInfo) {
                    showDiagnosticResultDialog(false, resultDetail, resultDetails, false)
                }
            } else {
                if (AppPreferences.showDebugInfo) {
                    playShortResource(this, R.raw.err)
                    releaseResources()
                    setResult(Activity.RESULT_CANCELED)
                    stopScreenPinning()
                    fragmentManager!!.popBackStack()
                    if (testInfo.dilutions.isNotEmpty()) {
                        fragmentManager!!.popBackStack()
                    }
                    showDiagnosticResultDialog(true, resultDetail, resultDetails, false)
                } else {
                    fragmentManager!!.popBackStack()
                    showError(String.format(TWO_SENTENCE_FORMAT, getString(R.string.errorTestFailed),
                            getString(R.string.checkChamberPlacement)),
                            resultDetails[resultDetails.size - 1].croppedBitmap)
                }
            }
        } else {
            val color = getAverageColor(resultDetails)
            if (color == Color.TRANSPARENT) {
                if (AppPreferences.showDebugInfo) {
                    showDiagnosticResultDialog(true, resultDetail, resultDetails, true)
                }
                showError(String.format(TWO_SENTENCE_FORMAT, getString(R.string.couldNotCalibrate),
                        getString(R.string.checkChamberPlacement)),
                        resultDetails[resultDetails.size - 1].croppedBitmap)
            } else {
                val dao = db!!.calibrationDao()
                calibration.color = color
                calibration.date = Date().time
                if (AppPreferences.isDiagnosticMode()) {
                    calibration.image = UUID.randomUUID().toString() + ".png"
                    // Save photo taken during the test
                    FileUtil.writeBitmapToExternalStorage(resultDetails[resultDetails.size - 1].bitmap,
                            FileType.DIAGNOSTIC_IMAGE, calibration.image!!)
                    calibration.croppedImage = UUID.randomUUID().toString() + ".png"
                    // Save photo taken during the test
                    FileUtil.writeBitmapToExternalStorage(resultDetails[resultDetails.size - 1].croppedBitmap,
                            FileType.DIAGNOSTIC_IMAGE, calibration.croppedImage!!)
                }
                dao!!.insert(calibration)
                CalibrationFile.saveCalibratedData(this, testInfo, calibration, color)
                loadDetails()
                playShortResource(this, R.raw.done)
                if (AppPreferences.showDebugInfo) {
                    showDiagnosticResultDialog(false, resultDetail, resultDetails, true)
                }
                showCalibrationDialog(calibration)
            }
            stopScreenPinning()
            fragmentManager!!.popBackStackImmediate()
        }
        invalidateOptionsMenu()
    }

    private fun stopScreenPinning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                stopLockTask()
            } catch (ignored: Exception) {
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
    private fun showDiagnosticResultDialog(testFailed: Boolean, resultDetail: ResultDetail,
                                           resultDetails: ArrayList<ResultDetail>, isCalibration: Boolean) {
        val resultFragment = newInstance(
                testFailed, resultDetail, resultDetails, isCalibration)
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
     * In diagnostic mode show the diagnostic results dialog.
     *
     * @param calibration the calibration details shown to the user
     */
    private fun showCalibrationDialog(calibration: Calibration) {
        val resultFragment = newInstance(
                calibration, testInfo.decimalPlaces, testInfo.results!![0].unit)
        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag("calibrationDialog")
        if (prev != null) {
            ft.remove(prev)
        }
        resultFragment.isCancelable = false
        resultFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog)
        resultFragment.show(ft, "calibrationDialog")
    }

    /**
     * Create result json to send back.
     */
    fun onClickAcceptResult(@Suppress("UNUSED_PARAMETER") view: View?) {
        val resultIntent = Intent()
        val results = SparseArray<String>()
        for (i in testInfo.results!!.indices) {
            val result = testInfo.results!![i]
            var testName = result.name?.replace(" ", "_")
            if (testInfo.nameSuffix != null && testInfo.nameSuffix!!.isNotEmpty()) {
                testName += "_" + testInfo.nameSuffix!!.replace(" ", "_")
            }
            resultIntent.putExtra(testName
                    + testInfo.resultSuffix, result.result)
            resultIntent.putExtra(testName
                    + "_" + SensorConstants.DILUTION
                    + testInfo.resultSuffix, testInfo.dilution)
            resultIntent.putExtra(
                    result.name?.replace(" ", "_")
                            + "_" + SensorConstants.UNIT + testInfo.resultSuffix,
                    testInfo.results!![0].unit)
            if (i == 0) {
                resultIntent.putExtra(SensorConstants.VALUE, result.result)
            }
            results.append(result.id, result.result)
        }
        val resultJson = getJsonResult(testInfo, results, null, -1, null)
        resultIntent.putExtra(SensorConstants.RESULT_JSON, resultJson.toString())
        setResult(Activity.RESULT_OK, resultIntent)
        stopScreenPinning()
        finish()
    }

    /**
     * Show an error message dialog.
     *
     * @param message the message to be displayed
     * @param bitmap  any bitmap image to displayed along with error message
     */
    private fun showError(message: String, bitmap: Bitmap?) {
        stopScreenPinning()
        playShortResource(this, R.raw.err)
        releaseResources()
        alertDialogToBeDestroyed = AlertUtil.showError(this, R.string.error, message, bitmap, R.string.retry,
                DialogInterface.OnClickListener { _: DialogInterface?, _: Int ->
                    stopScreenPinning()
                    if (intent.getBooleanExtra(ConstantKey.RUN_TEST, false)) {
                        start()
                    } else {
                        runTest()
                    }
                },
                DialogInterface.OnClickListener { dialogInterface: DialogInterface, _: Int ->
                    stopScreenPinning()
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

    /**
     * Navigate back to the dilution selection screen if re-testing.
     */
    fun onTestWithDilution(@Suppress("UNUSED_PARAMETER") view: View?) {
        stopScreenPinning()
        if (!fragmentManager!!.popBackStackImmediate("dilution", 0)) {
            super.onBackPressed()
        }
        invalidateOptionsMenu()
    }

    override fun onDilutionSelected(dilution: Int) {
        currentDilution = dilution
        runTest()
    }

    override fun onCustomDilution(value: Int) {
        currentDilution = value
        runTest()
    }

    private fun checkCameraMegaPixel() {
        cameraIsOk = true
        if (PreferencesUtil.getBoolean(this, R.string.showMinMegaPixelDialogKey, true)) {
            try {
                if (CameraHelper.getMaxSupportedMegaPixelsByCamera(this) < Constants.MIN_CAMERA_MEGA_PIXELS) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    val checkBoxView = View.inflate(this, R.layout.dialog_message, null)
                    val checkBox = checkBoxView.findViewById<CheckBox>(R.id.checkbox)
                    checkBox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                        PreferencesUtil.setBoolean(baseContext,
                                R.string.showMinMegaPixelDialogKey, !isChecked)
                    }
                    val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                    builder.setTitle(R.string.warning)
                    builder.setMessage(R.string.camera_not_good)
                            .setView(checkBoxView)
                            .setCancelable(false)
                            .setPositiveButton(R.string.continue_anyway) { _: DialogInterface?, _: Int -> runTest() }
                            .setNegativeButton(R.string.stop_test) { dialog: DialogInterface, _: Int ->
                                dialog.dismiss()
                                cameraIsOk = false
                                finish()
                            }.show()
                } else {
                    runTest()
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        } else {
            runTest()
        }
    }

    private val isAppInLockTaskMode: Boolean
        get() {
            val activityManager: ActivityManager =
                    this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            @Suppress("DEPRECATION")
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                (activityManager.lockTaskModeState
                        != ActivityManager.LOCK_TASK_MODE_NONE)
            } else Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    && activityManager.isInLockTaskMode
        }

    override fun onDismissed() {
        testStarted = false
        invalidateOptionsMenu()
    }

    companion object {
        private const val TWO_SENTENCE_FORMAT = "%s%n%n%s"
    }
}