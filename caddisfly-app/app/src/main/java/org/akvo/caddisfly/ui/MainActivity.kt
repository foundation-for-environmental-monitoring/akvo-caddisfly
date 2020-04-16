package org.akvo.caddisfly.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.R
import org.akvo.caddisfly.app.CaddisflyApp
import org.akvo.caddisfly.common.NavigationController
import org.akvo.caddisfly.helper.ApkHelper
import org.akvo.caddisfly.helper.FileHelper
import org.akvo.caddisfly.helper.PermissionsDelegate
import org.akvo.caddisfly.model.TestSampleType
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.preference.AppPreferences
import org.akvo.caddisfly.preference.SettingsActivity
import org.akvo.caddisfly.util.AlertUtil
import org.akvo.caddisfly.util.PreferencesUtil
import java.lang.ref.WeakReference
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

const val STORAGE_PERMISSION_WATER = 1
const val STORAGE_PERMISSION_SOIL = 2

class MainActivity : BaseActivity() {
    private val refreshHandler = WeakRefHandler(this)
    private val permissionsDelegate = PermissionsDelegate(this)
    private val storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var navigationController: NavigationController? = null
    private var runTest = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CaddisflyApp.app?.setAppLanguage(null, false, null)
        navigationController = NavigationController(this)
        setContentView(R.layout.activity_main)
        setTitle(R.string.appName)
        try {
            if (BuildConfig.BUILD_TYPE.equals("release", ignoreCase = true) &&
                    ApkHelper.isNonStoreVersion(this)) {
                val appExpiryDate = GregorianCalendar.getInstance()
                appExpiryDate.time = BuildConfig.BUILD_TIME
                appExpiryDate.add(Calendar.DAY_OF_YEAR, 15)
                val df: DateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.US)
                textVersionExpiry?.text = String.format("Version expiry: %s", df.format(appExpiryDate.time))
                textVersionExpiry?.visibility = View.VISIBLE
            } else {
                if (ApkHelper.isNonStoreVersion(this)) {
                    textVersionExpiry?.text = CaddisflyApp.getAppVersion(true)
                    textVersionExpiry?.visibility = View.VISIBLE
                }
            }
            // If app has expired then close this activity
            ApkHelper.isAppVersionExpired(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        buttonSettings?.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivityForResult(intent, 100)
        }
    }

    /**
     * Show the diagnostic mode layout.
     */
    private fun switchLayoutForDiagnosticOrUserMode() {
        if (AppPreferences.isDiagnosticMode()) {
            textDiagnostics.visibility = View.VISIBLE
        } else {
            textDiagnostics.visibility = View.GONE
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        }
    }

    override fun onResume() {
        super.onResume()
        switchLayoutForDiagnosticOrUserMode()
        CaddisflyApp.app?.setAppLanguage(null, false, refreshHandler)
        if (PreferencesUtil.getBoolean(this, R.string.themeChangedKey, false)) {
            PreferencesUtil.setBoolean(this, R.string.themeChangedKey, false)
            refreshHandler.sendEmptyMessage(0)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissionsDelegate.resultGranted(grantResults)) {
            when (requestCode) {
                STORAGE_PERMISSION_WATER -> if (runTest) {
                    startTest(TestSampleType.WATER)
                }
                STORAGE_PERMISSION_SOIL -> if (runTest) {
                    startTest(TestSampleType.SOIL)
                }
            }
        } else {
            val message = ""
            when (requestCode) {
                STORAGE_PERMISSION_WATER, STORAGE_PERMISSION_SOIL -> {
                }
            }
            AlertUtil.showSettingsSnackbar(this,
                    window.decorView.rootView, message)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && PreferencesUtil.getBoolean(this, R.string.refreshKey, false)) {
            PreferencesUtil.setBoolean(this, R.string.refreshKey, false)
            recreate()
        }
    }

    fun onRunTestClick(@Suppress("UNUSED_PARAMETER") view: View?) {
        runTest = true
        if (permissionsDelegate.hasPermissions(storagePermission)) {
            startTest(TestSampleType.ALL)
        } else {
            if (BuildConfig.APPLICATION_ID.contains("soil")) {
                permissionsDelegate.requestPermissions(storagePermission, STORAGE_PERMISSION_SOIL)
            } else {
                permissionsDelegate.requestPermissions(storagePermission, STORAGE_PERMISSION_WATER)
            }
        }
    }

    private fun startTest(testSampleType: TestSampleType) {
        FileHelper.migrateFolders()
        navigationController!!.navigateToTestType(TestType.CHAMBER_TEST, testSampleType, runTest)
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