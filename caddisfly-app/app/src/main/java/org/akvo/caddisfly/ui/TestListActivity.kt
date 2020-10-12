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
package org.akvo.caddisfly.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.common.ConstantKey.IS_INTERNAL
import org.akvo.caddisfly.helper.CameraHelper
import org.akvo.caddisfly.helper.ErrorMessages.alertCalibrationIncomplete
import org.akvo.caddisfly.helper.ErrorMessages.alertCouldNotLoadConfig
import org.akvo.caddisfly.helper.PermissionsDelegate
import org.akvo.caddisfly.helper.SwatchHelper.isSwatchListValid
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.model.TestSampleType
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.preference.AppPreferences.useExternalCamera
import org.akvo.caddisfly.repository.TestConfigRepository
import org.akvo.caddisfly.sensor.chamber.ChamberTestActivity
import org.akvo.caddisfly.ui.TestListFragment.Companion.newInstance
import org.akvo.caddisfly.ui.TestListFragment.OnListFragmentInteractionListener
import org.akvo.caddisfly.util.AlertUtil

class TestListActivity : BaseActivity(), OnListFragmentInteractionListener {
    private val permissionsDelegate = PermissionsDelegate(this)
    private val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var testInfo: TestInfo? = null
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissionsDelegate.resultGranted(grantResults)) {
            startTest()
        } else {
            val message: String = if (requestCode == REQUEST_SYNC_PERMISSION) {
                getString(R.string.storage_permission)
            } else {
                getString(R.string.camera_storage_permissions)
            }
            AlertUtil.showSettingsSnackbar(this,
                    window.decorView.rootView, message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ViewDataBinding>(this, R.layout.activity_test_list)
        setTitle(R.string.select_test)

        // Add list fragment if this is first creation
        if (savedInstanceState == null) {
            val testType = intent.getSerializableExtra(ConstantKey.TYPE) as TestType
            val sampleType = intent
                .getSerializableExtra(ConstantKey.SAMPLE_TYPE_KEY) as TestSampleType
            val fragment = newInstance(testType, sampleType)
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment, TestListFragment.TAG).commit()
        }
    }

    /**
     * Shows the detail fragment.
     */
    private fun navigateToTestDetails() {
        if (testInfo!!.subtype === TestType.CHAMBER_TEST) {
            if (permissionsDelegate.hasPermissions(permissions)) {
                startTest()
            } else {
                permissionsDelegate.requestPermissions(permissions)
            }
        } else {
            startTest()
        }
    }

    private fun startTest() {
        testInfo = TestConfigRepository().getTestInfo(testInfo!!.uuid)
        if (testInfo == null || testInfo!!.isGroup) {
            return
        }
        if (testInfo!!.results!!.isEmpty()) {
            alertCouldNotLoadConfig(this)
            return
        }
        val runTest = intent.getBooleanExtra(ConstantKey.RUN_TEST, false)
        if (testInfo!!.subtype === TestType.CHAMBER_TEST && !runTest) {
            startCalibration()
        } else {
            if (testInfo!!.subtype === TestType.CHAMBER_TEST && !isSwatchListValid(testInfo)) {
                alertCalibrationIncomplete(
                    this, testInfo!!,
                    isInternal = false, finishActivity = false
                )
                return
            }
            val intent = Intent(this, TestActivity::class.java)
            intent.putExtra(ConstantKey.TEST_INFO, testInfo)
            intent.putExtra(IS_INTERNAL, true)
            if (runTest) {
                intent.putExtra(ConstantKey.RUN_TEST, true)
            }
            startActivity(intent)
        }
    }

    private fun startCalibration() {
        //Only start the colorimetry calibration if the device has a camera flash
        if (useExternalCamera()
                || CameraHelper.hasFeatureCameraFlash(this,
                R.string.cannot_calibrate, R.string.ok, null
            )
        ) {
            val intent: Intent = if (testInfo!!.results!![0].colors.size > 0) {
                Intent(this, ChamberTestActivity::class.java)
            } else {
                alertCouldNotLoadConfig(this)
                return
            }
            intent.putExtra(ConstantKey.TEST_INFO, testInfo)
            startActivity(intent)
        }
    }

    override fun onListFragmentInteraction(testInfo: TestInfo?) {
        this.testInfo = testInfo
        navigateToTestDetails()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val REQUEST_SYNC_PERMISSION = 101
    }
}