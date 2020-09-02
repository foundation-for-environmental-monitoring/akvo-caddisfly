@file:Suppress("DEPRECATION")

package org.akvo.caddisfly.preference

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.R
import org.akvo.caddisfly.app.CaddisflyApp.Companion.getAppVersion
import org.akvo.caddisfly.common.NavigationController
import org.akvo.caddisfly.helper.FileHelper
import org.akvo.caddisfly.helper.PermissionsDelegate
import org.akvo.caddisfly.helper.SwatchHelper.generateCalibrationFile
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.model.TestSampleType
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.preference.OtherPreferenceFragment.GenerateMessageAsyncTask.ExampleAsyncTaskListener
import org.akvo.caddisfly.ui.AboutActivity
import org.akvo.caddisfly.ui.STORAGE_PERMISSION_SOIL
import org.akvo.caddisfly.ui.STORAGE_PERMISSION_WATER
import org.akvo.caddisfly.viewmodel.TestListViewModel
import java.lang.ref.WeakReference

class OtherPreferenceFragment : PreferenceFragmentCompat() {
    private lateinit var permissionsDelegate: PermissionsDelegate
    private val storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var navigationController: NavigationController? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_other)
        navigationController = NavigationController(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionsDelegate = PermissionsDelegate(requireActivity())

        val calibratePreference = findPreference<Preference>("calibrate")
        if (calibratePreference != null) {
            calibratePreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                if (permissionsDelegate.hasPermissions(storagePermission)) {
                    startCalibrate()
                } else {
                    if (BuildConfig.APPLICATION_ID.contains("soil")) {
                        permissionsDelegate.requestPermissions(
                            storagePermission,
                            STORAGE_PERMISSION_SOIL
                        )
                    } else {
                        permissionsDelegate.requestPermissions(
                            storagePermission,
                            STORAGE_PERMISSION_WATER
                        )
                    }
                }
                true
            }
        }

        val aboutPreference = findPreference<Preference>("about")
        if (aboutPreference != null) {
            aboutPreference.summary = getAppVersion(AppPreferences.isDiagnosticMode())
            aboutPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val intent = Intent(activity, AboutActivity::class.java)
                activity?.startActivity(intent)
                true
            }
        }

        val emailSupportPreference = findPreference<Preference>("emailSupport")
        if (emailSupportPreference != null) {
            emailSupportPreference.setSummary(R.string.send_details_to_support)
            emailSupportPreference.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    message.setLength(0)
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle(R.string.email_support)
                    builder.setMessage(
                        getString(R.string.if_you_need_assistance) + "\n\n" +
                                getString(R.string.select_email_app)
                    )
                        .setCancelable(false)
                        .setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                        .setPositiveButton(R.string.create_support_email) { dialog: DialogInterface, _: Int ->
                            dialog.dismiss()
                            val progressDialog = ProgressDialog(
                                activity,
                                android.R.style.Theme_DeviceDefault_Light_Dialog
                            )
                            // START AsyncTask
                            val generateMessageAsyncTask = GenerateMessageAsyncTask(this)
                            val exampleAsyncTaskListener = object : ExampleAsyncTaskListener {
                                override fun onExampleAsyncTaskFinished(value: Int?) {
                                    if (progressDialog.isShowing) {
                                        progressDialog.dismiss()
                                    }
                                    sendEmail(requireContext(), message.toString())
                                }
                            }
                            generateMessageAsyncTask.setListener(exampleAsyncTaskListener)
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                            progressDialog.isIndeterminate = true
                            progressDialog.setTitle(R.string.creating_message)
                            progressDialog.setMessage(getString(R.string.just_a_moment))
                            progressDialog.setCancelable(false)
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && progressDialog.window != null) {
                                progressDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                            }
                            progressDialog.show()
                            generateMessageAsyncTask.execute()
                        }.show()
                    true
                }
        }
    }

    private fun startCalibrate() {
        FileHelper.migrateFolders()
        navigationController!!.navigateToTestType(TestType.CHAMBER_TEST, TestSampleType.ALL, false)
    }

    private fun sendEmail(context: Context, message: String) {
        try {
            val email = "devices@ternup.com"
            val subject = "Support request"
            val intent = Intent(Intent.ACTION_VIEW)
            val data = Uri.parse("mailto:?to=$email&subject=$subject&body=$message")
            intent.data = data
            startActivity(intent)
        } catch (t: Throwable) {
            Toast.makeText(context, "Request failed try again: $t", Toast.LENGTH_LONG).show()
        }
    }

    internal class GenerateMessageAsyncTask(fragment: OtherPreferenceFragment) :
        AsyncTask<Void?, Void?, Int?>() {
        private var listener: ExampleAsyncTaskListener? = null
        private val activityReference: WeakReference<OtherPreferenceFragment> =
            WeakReference(fragment)
        private lateinit var viewModel: TestListViewModel
        private lateinit var testList: List<TestInfo>

        override fun onPreExecute() {
            super.onPreExecute()
            val context = (activityReference.get()?.activity as FragmentActivity)
            viewModel = ViewModelProvider(context).get(TestListViewModel::class.java)
            testList = viewModel.getTests(TestType.CHAMBER_TEST, TestSampleType.ALL)
        }

        override fun doInBackground(vararg params: Void?): Int? {
            for (testInfo in testList) {
                if (testInfo.isGroup) {
                    continue
                }
                val testInfo1 = viewModel.getTestInfo(testInfo.uuid)
                var calibrated = false
                if (testInfo1 != null) {
                    for (calibration in testInfo1.calibrations) {
                        if (calibration.color != Color.TRANSPARENT &&
                            calibration.color != Color.BLACK
                        ) {
                            calibrated = true
                            break
                        }
                    }
                    if (calibrated) {
                        message.append(
                            generateCalibrationFile(
                                activityReference.get()!!.activity,
                                testInfo1, false
                            )
                        )
                        message.append("\n")
                        message.append("-------------------------------------------------")
                        message.append("\n")
                    }
                }
            }
            if (message.toString().isEmpty()) {
                message.append("No calibrations found")
            }
            return null
        }

        override fun onPostExecute(value: Int?) {
            super.onPostExecute(value)
            if (listener != null) {
                listener!!.onExampleAsyncTaskFinished(value)
            }
        }

        fun setListener(listener: ExampleAsyncTaskListener) {
            this.listener = listener
        }

        internal interface ExampleAsyncTaskListener {
            fun onExampleAsyncTaskFinished(value: Int?)
        }
    }

    companion object {
        private val message = StringBuilder()
    }
}