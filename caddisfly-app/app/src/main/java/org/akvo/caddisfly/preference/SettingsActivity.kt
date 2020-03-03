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
package org.akvo.caddisfly.preference

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import org.akvo.caddisfly.R
import org.akvo.caddisfly.app.CaddisflyApp.Companion.app
import org.akvo.caddisfly.ui.BaseActivity
import org.akvo.caddisfly.util.PreferencesUtil
import org.akvo.caddisfly.viewmodel.TestListViewModel

class SettingsActivity : BaseActivity(), OnSharedPreferenceChangeListener {
    private var mScrollView: ScrollView? = null
    private var mScrollPosition = 0
    private fun removeAllFragments() {
        findViewById<View>(R.id.layoutGeneral).visibility = View.GONE
        findViewById<View>(R.id.layoutDiagnostics).visibility = View.GONE
        findViewById<View>(R.id.layoutDiagnosticsOptions).visibility = View.GONE
        findViewById<View>(R.id.layoutDebugging).visibility = View.GONE
        findViewById<View>(R.id.layoutCamera).visibility = View.GONE
        findViewById<View>(R.id.layoutTesting).visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActivity()
    }

//    public override fun onRestart() {
//        super.onRestart()
//        setupActivity()
//    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(this.applicationContext)
                .registerOnSharedPreferenceChangeListener(this)
    }

    private fun setupActivity() {
        setTitle(R.string.settings)
        setContentView(R.layout.activity_settings)
        supportFragmentManager.beginTransaction()
                .replace(R.id.layoutOther, OtherPreferenceFragment())
                .commit()
        if (AppPreferences.isDiagnosticMode()) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.layoutGeneral, GeneralPreferenceFragment())
                    .commit()
            supportFragmentManager.beginTransaction()
                    .add(R.id.layoutDiagnostics, DiagnosticPreferenceFragment())
                    .commit()
            supportFragmentManager.beginTransaction()
                    .add(R.id.layoutDiagnosticsOptions, DiagnosticOptionsPreferenceFragment())
                    .commit()
            supportFragmentManager.beginTransaction()
                    .add(R.id.layoutDebugging, DebuggingPreferenceFragment())
                    .commit()
            supportFragmentManager.beginTransaction()
                    .add(R.id.layoutTesting, TestingPreferenceFragment())
                    .commit()
            findViewById<View>(R.id.layoutDiagnosticsOptions).visibility = View.VISIBLE
            findViewById<View>(R.id.layoutDiagnostics).visibility = View.VISIBLE
            findViewById<View>(R.id.layoutDebugging).visibility = View.VISIBLE
            findViewById<View>(R.id.layoutTesting).visibility = View.VISIBLE
        }
        mScrollView = findViewById(R.id.scrollViewSettings)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        try {
            setSupportActionBar(toolbar)
        } catch (ignored: Exception) { //Ignore crash in Samsung
        }
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setTitle(R.string.settings)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (AppPreferences.isDiagnosticMode()) {
            menuInflater.inflate(R.menu.menu_settings, menu)
        }
        return true
    }

    fun onDisableDiagnostics(@Suppress("UNUSED_PARAMETER") item: MenuItem?) {
        Toast.makeText(this, getString(R.string.diagnosticModeDisabled),
                Toast.LENGTH_SHORT).show()
        AppPreferences.disableDiagnosticMode()
        changeActionBarStyleBasedOnCurrentMode()
        invalidateOptionsMenu()
        clearTests()
        removeAllFragments()
    }

    private fun clearTests() {
        val viewModel = ViewModelProvider(this).get(TestListViewModel::class.java)
        viewModel.clearTests()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        if (applicationContext.getString(R.string.languageKey) == s) {
            app!!.setAppLanguage(null, false, null)
            val resultIntent = Intent(intent)
            resultIntent.getBooleanExtra("refresh", true)
            setResult(Activity.RESULT_OK, resultIntent)
            clearTests()
            PreferencesUtil.setBoolean(this, R.string.refreshKey, true)
            recreate()
        }
    }

    public override fun onPause() {
        val scrollbarPosition = mScrollView!!.scrollY
        PreferencesUtil.setInt(this, "settingsScrollPosition", scrollbarPosition)
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(this.applicationContext)
                .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onPostResume() {
        super.onPostResume()
        mScrollPosition = PreferencesUtil.getInt(this, "settingsScrollPosition", 0)
        mScrollView!!.post { mScrollView!!.scrollTo(0, mScrollPosition) }
    }
}