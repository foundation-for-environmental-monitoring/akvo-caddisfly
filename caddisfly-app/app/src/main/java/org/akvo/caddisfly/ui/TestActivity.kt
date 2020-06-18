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
@file:Suppress("DEPRECATION")

package org.akvo.caddisfly.ui

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.SparseArray
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.R
import org.akvo.caddisfly.app.CaddisflyApp.Companion.app
import org.akvo.caddisfly.app.CaddisflyApp.Companion.db
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.common.Constants
import org.akvo.caddisfly.common.SensorConstants
import org.akvo.caddisfly.helper.ApkHelper.isAppVersionExpired
import org.akvo.caddisfly.helper.CameraHelper
import org.akvo.caddisfly.helper.ErrorMessages
import org.akvo.caddisfly.helper.FileHelper
import org.akvo.caddisfly.helper.PermissionsDelegate
import org.akvo.caddisfly.helper.SwatchHelper.isSwatchListValid
import org.akvo.caddisfly.helper.TestConfigHelper.getJsonResult
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.preference.AppPreferences
import org.akvo.caddisfly.sensor.chamber.ChamberTestActivity
import org.akvo.caddisfly.sensor.striptest.ui.StripTestActivity
import org.akvo.caddisfly.sensor.titration.TitrationTestActivity
import org.akvo.caddisfly.util.AlertUtil
import org.akvo.caddisfly.util.PreferencesUtil
import org.akvo.caddisfly.util.toLocalString
import org.akvo.caddisfly.viewmodel.TestListViewModel
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*
import java.util.regex.Pattern

class TestActivity : BaseActivity() {
    companion object {
        private const val REQUEST_TEST = 1
        private const val MESSAGE_TWO_LINE_FORMAT = "%s%n%n%s"

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private val handler = WeakRefHandler(this)
    private val permissionsDelegate = PermissionsDelegate(this)
    private val storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val permissions =
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var testInfo: TestInfo? = null
    private var cameraIsOk = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        val fragmentManager = supportFragmentManager
        // Add list fragment if this is first creation
        if (savedInstanceState == null) {
            testInfo = intent.getParcelableExtra(ConstantKey.TEST_INFO)
            if (testInfo != null) {
                val runTest = intent.getBooleanExtra(ConstantKey.RUN_TEST, false)
                if (runTest) {
                    startTest()
                } else {
                    fragmentManager.beginTransaction()
                        .replace(
                            R.id.fragment_container, TestInfoFragment.getInstance(testInfo),
                            TestActivity::class.java.simpleName
                        ).commit()
                }
            }
        }
        val intent = intent
        if (BuildConfig.APPLICATION_ID == intent.action) {
            getTestSelectedByExternalApp(fragmentManager, intent)
        }
    }

    private fun getTestSelectedByExternalApp(fragmentManager: FragmentManager, intent: Intent) {
        app!!.setAppLanguage(
            intent.getStringExtra(SensorConstants.LANGUAGE), true, handler
        )
        val uuid = intent.getStringExtra(SensorConstants.TEST_ID)
        if (uuid != null) {
            val viewModel = ViewModelProvider(this).get(TestListViewModel::class.java)
            testInfo = viewModel.getTestInfo(uuid)
            if (testInfo != null && intent.extras != null) {
                for (i in intent.extras!!.keySet().indices) {
                    val code = Objects.requireNonNull<Array<Any>>(
                        intent.extras!!.keySet().toTypedArray()
                    )[i].toString()
                    if (code != SensorConstants.TEST_ID) {
                        val pattern = Pattern.compile("_(\\d*?)$")
                        val matcher = pattern.matcher(code)
                        if (matcher.find()) {
                            testInfo!!.resultSuffix = matcher.group(0)
                        } else if (code.contains("_x")) {
                            testInfo!!.resultSuffix = code.substring(code.indexOf("_x"))
                        }
                    }
                }
            }
        }
        if (testInfo == null) {
            setTitle(R.string.notFound)
            alertTestTypeNotSupported()
        } else {
            val fragment = TestInfoFragment.getInstance(testInfo)
            fragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment, TestActivity::class.java.simpleName)
                .commit()
        }
    }

    override fun onStart() {
        super.onStart()
        // Stop if the app version has expired
        if (isAppVersionExpired(this)) {
            return
        }
        if (testInfo != null) {
            title = testInfo!!.name!!.toLocalString()
        }
    }

    /**
     * Start the test.
     *
     * @param view the View
     */
    fun onStartTestClick(@Suppress("UNUSED_PARAMETER") view: View?) { // if app was launched in debug mode then send dummy results without running test
        if (AppPreferences.returnDummyResults() ||
            intent.getBooleanExtra(SensorConstants.DEBUG_MODE, false)
        ) {
            sendDummyResultForDebugging()
            return
        }
        val checkPermissions = permissions
        if (permissionsDelegate.hasPermissions(checkPermissions)) {
            startTest()
        } else {
            permissionsDelegate.requestPermissions(checkPermissions)
        }
    }

    /**
     * Create dummy results to send when in debug mode
     */
    private fun sendDummyResultForDebugging() {
        val resultIntent = Intent()
        val results = SparseArray<String>()
        var maxDilution = testInfo!!.maxDilution
        if (maxDilution == -1) {
            maxDilution = 15
        }
        for (i in testInfo!!.results!!.indices) {
            val result = testInfo!!.results!![i]
            val random = Random()
            var maxValue = 100.0
            if (result.colors.isNotEmpty()) {
                maxValue = result.colors[result.colors.size - 1].value!!
            }
            val dilution = random.nextInt(maxDilution) + 1
            result.setResult(
                random.nextDouble() * maxValue,
                dilution, maxDilution
            )
            var testName = result.name.replace(" ", "_")
            if (testInfo!!.nameSuffix != null && testInfo!!.nameSuffix!!.isNotEmpty()) {
                testName += "_" + testInfo!!.nameSuffix!!.replace(" ", "_")
            }
            resultIntent.putExtra(
                testName
                        + testInfo!!.resultSuffix, result.result
            )
            resultIntent.putExtra(
                testName
                        + "_" + SensorConstants.DILUTION
                        + testInfo!!.resultSuffix, dilution
            )
            resultIntent.putExtra(
                testName
                        + "_" + SensorConstants.UNIT + testInfo!!.resultSuffix,
                testInfo!!.results!![0].unit
            )
            if (i == 0) {
                resultIntent.putExtra(SensorConstants.VALUE, result.result)
            }
            results.append(result.id, result.result)
        }
        val resultJson = getJsonResult(
            testInfo!!, results,
            null, -1, null
        )
        resultIntent.putExtra(SensorConstants.RESULT_JSON, resultJson.toString())
        val pd = ProgressDialog(this)
        pd.setMessage("Sending dummy result...")
        pd.setCancelable(false)
        pd.show()
        setResult(Activity.RESULT_OK, resultIntent)
        Handler().postDelayed({
            pd.dismiss()
            finish()
        }, 3000)
    }

    private fun startTest() {
        if (permissionsDelegate.hasPermissions(storagePermissions)) {
            FileHelper.migrateFolders()
        }
        if (testInfo != null) {
            if (testInfo!!.subtype == TestType.CHAMBER_TEST) {
                if (!isSwatchListValid(testInfo)) {
                    ErrorMessages.alertCalibrationIncomplete(
                        this, testInfo!!,
                        isInternal = false, finishActivity = true
                    )
                    return
                }
                val calibrationDetail =
                    db?.calibrationDao()!!.getCalibrationDetails(testInfo!!.uuid)
                if (calibrationDetail != null) {
                    val milliseconds = calibrationDetail.expiry
                    if (milliseconds > 0 && milliseconds <= Date().time) {
                        ErrorMessages.alertCalibrationExpired(this)
                        return
                    }
                }
            }
            when (testInfo!!.subtype) {
                TestType.CHAMBER_TEST -> startChamberTest()
                TestType.STRIP_TEST -> if (cameraIsOk) {
                    startStripTest()
                } else {
                    checkCameraMegaPixel()
                }
                TestType.TITRATION -> startTitrationTest()
                else -> {
                }
            }
        }
    }

    private fun startTitrationTest() {
        val intent = Intent(this, TitrationTestActivity::class.java)
        intent.putExtra(ConstantKey.TEST_INFO, testInfo)
        startActivityForResult(intent, REQUEST_TEST)
    }

    private fun startChamberTest() { //Only start the colorimetry calibration if the device has a camera flash
        if (AppPreferences.useExternalCamera()
            || CameraHelper.hasFeatureCameraFlash(
                this,
                R.string.cannot_start_test, R.string.ok, null
            )
        ) {
            if (!isSwatchListValid(testInfo)) {
                ErrorMessages.alertCalibrationIncomplete(
                    this, testInfo!!,
                    isInternal = false, finishActivity = true
                )
                return
            }
            val intent = Intent(this, ChamberTestActivity::class.java)
            intent.putExtra(ConstantKey.RUN_TEST, true)
            intent.putExtra(ConstantKey.TEST_INFO, testInfo)
            startActivityForResult(intent, REQUEST_TEST)
        }
    }

    private fun startStripTest() {
        val intent = Intent(this, StripTestActivity::class.java)
        intent.putExtra(ConstantKey.TEST_INFO, testInfo)
        startActivityForResult(intent, REQUEST_TEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TEST && resultCode == Activity.RESULT_OK) { //return the test result to the external app
            val intent = Intent(data)
            //            if (AppConfig.EXTERNAL_APP_ACTION.equals(intent.getAction())
//                    && data.hasExtra(SensorConstants.RESPONSE_COMPAT)) {
//                //if survey from old version server then don't send json response
//                intent.putExtra(SensorConstants.RESPONSE, data.getStringExtra(SensorConstants.RESPONSE_COMPAT));
//                intent.putExtra(SensorConstants.VALUE, data.getStringExtra(SensorConstants.RESPONSE_COMPAT));
//            } else {
//                intent.putExtra(SensorConstants.RESPONSE, data.getStringExtra(SensorConstants.RESPONSE));
//                if (testInfo.getHasImage() && mCallerExpectsImageInResult) {
//                    intent.putExtra(ConstantJsonKey.IMAGE, data.getStringExtra(ConstantKey.IMAGE));
//                }
//            }
            this.setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            finish()
        }
    }

    /**
     * Show Instructions for the test.
     *
     * @param view the View
     */
    fun onInstructionsClick(@Suppress("UNUSED_PARAMETER") view: View?) {
        val instructionFragment = InstructionFragment.getInstance(testInfo)
        supportFragmentManager
            .beginTransaction()
            .addToBackStack("instructions")
            .replace(
                R.id.fragment_container,
                instructionFragment, null
            ).commit()
    }

    /**
     * Navigate to clicked link.
     *
     * @param view the View
     */
    fun onSiteLinkClick(@Suppress("UNUSED_PARAMETER") view: View?) {
        var url = testInfo!!.brandUrl
        if (url != null) {
            if (!url.contains("http://")) {
                url = "http://$url"
            }
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }
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
                        PreferencesUtil.setBoolean(
                            baseContext,
                            R.string.showMinMegaPixelDialogKey, !isChecked
                        )
                    }
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.warning)
                    builder.setMessage(R.string.camera_not_good)
                        .setView(checkBoxView)
                        .setCancelable(false)
                        .setPositiveButton(R.string.continue_anyway) { _: DialogInterface?, _: Int -> startTest() }
                        .setNegativeButton(R.string.stop_test) { dialog: DialogInterface, _: Int ->
                            dialog.dismiss()
                            cameraIsOk = false
                            finish()
                        }.show()
                } else {
                    startTest()
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        } else {
            startTest()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissionsDelegate.resultGranted(grantResults)) {
            FileHelper.migrateFolders()
            startTest()
        } else {
            val message = getString(R.string.camera_storage_permissions)
            AlertUtil.showSettingsSnackbar(
                this,
                window.decorView.rootView, message
            )
        }
    }

    /**
     * Alert displayed when an unsupported contaminant test type was requested.
     */
    private fun alertTestTypeNotSupported() {
        var message = getString(R.string.error_test_not_available)
        message = String.format(
            MESSAGE_TWO_LINE_FORMAT,
            message,
            getString(R.string.please_contact_support)
        )
        AlertUtil.showAlert(
            this, R.string.cannot_start_test, message,
            R.string.ok,
            DialogInterface.OnClickListener { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
                finish()
            }, null,
            DialogInterface.OnCancelListener { dialogInterface: DialogInterface ->
                dialogInterface.dismiss()
                finish()
            }
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Handler to restart the app after language has been changed.
     */
    private class WeakRefHandler internal constructor(ref: Activity) : Handler() {
        private val ref: WeakReference<Activity> = WeakReference(ref)
        override fun handleMessage(msg: Message) {
            val f = ref.get()
            f?.recreate()
        }

    }
}