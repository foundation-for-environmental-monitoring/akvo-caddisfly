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

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_about.*
import org.akvo.caddisfly.R
import org.akvo.caddisfly.app.CaddisflyApp
import org.akvo.caddisfly.helper.ApkHelper.isTestDevice
import org.akvo.caddisfly.preference.AppPreferences
import org.akvo.caddisfly.viewmodel.TestListViewModel

/**
 * Activity to display info about the app.
 */
class AboutActivity : BaseActivity() {
    private var clickCount = 0
    private var dialog: NoticesDialogFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        textVersion.text = CaddisflyApp.getAppVersion(AppPreferences.isDiagnosticMode())
        setTitle(R.string.about)
    }

    /**
     * Displays legal information.
     */
    fun onSoftwareNoticesClick(@Suppress("UNUSED_PARAMETER") view: View?) {
        if (!isTestDevice(this)) {
            dialog = NoticesDialogFragment.newInstance()
            dialog?.show(supportFragmentManager, "NoticesDialog")
        }
    }

    /**
     * Disables diagnostic mode.
     */
    fun disableDiagnosticsMode(@Suppress("UNUSED_PARAMETER") view: View?) {
        Toast.makeText(this, getString(R.string.diagnosticModeDisabled),
                Toast.LENGTH_SHORT).show()
        AppPreferences.disableDiagnosticMode()
        switchLayoutForDiagnosticOrUserMode()
        changeActionBarStyleBasedOnCurrentMode()
        clearTests()
    }

    private fun clearTests() {
        val viewModel = ViewModelProvider(this).get(TestListViewModel::class.java)
        viewModel.clearTests()
    }

    /**
     * Turn on diagnostic mode if user clicks on version section CHANGE_MODE_MIN_CLICKS times.
     */
    fun switchToDiagnosticMode(@Suppress("UNUSED_PARAMETER") view: View?) {
        if (!AppPreferences.isDiagnosticMode()) {
            clickCount++
            if (clickCount >= CHANGE_MODE_MIN_CLICKS) {
                clickCount = 0
                Toast.makeText(this, getString(
                        R.string.diagnosticModeEnabled), Toast.LENGTH_SHORT).show()
                AppPreferences.enableDiagnosticMode()
                changeActionBarStyleBasedOnCurrentMode()
                switchLayoutForDiagnosticOrUserMode()
                // clear and reload all the tests as diagnostic mode includes experimental tests
                clearTests()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        switchLayoutForDiagnosticOrUserMode()
    }

    /**
     * Show the diagnostic mode layout.
     */
    private fun switchLayoutForDiagnosticOrUserMode() {
        if (AppPreferences.isDiagnosticMode()) {
            findViewById<View>(R.id.layoutDiagnostics).visibility = View.VISIBLE
        } else {
            if (findViewById<View>(R.id.layoutDiagnostics).visibility == View.VISIBLE) {
                findViewById<View>(R.id.layoutDiagnostics).visibility = View.GONE
            }
        }
    }

    fun onHomeClick(@Suppress("UNUSED_PARAMETER") view: View) {
        dialog?.dismiss()
    }

    companion object {
        private const val CHANGE_MODE_MIN_CLICKS = 10
    }
}