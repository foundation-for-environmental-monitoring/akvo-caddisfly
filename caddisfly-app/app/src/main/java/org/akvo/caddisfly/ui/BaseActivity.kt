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

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import org.akvo.caddisfly.R
import org.akvo.caddisfly.preference.AppPreferences

/**
 * The base activity with common functions.
 */
abstract class BaseActivity : AppCompatActivity() {
    private var mTitle: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateTheme()
        changeActionBarStyleBasedOnCurrentMode()
    }

    private fun updateTheme() {
        setTheme(R.style.AppTheme_Main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)
        val windowBackground = typedValue.data
        window.setBackgroundDrawable(ColorDrawable(windowBackground))
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        if (toolbar != null) {
            try {
                setSupportActionBar(toolbar)
            } catch (ignored: Exception) { // do nothing
            }
        }
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = ""
        }
    }

    override fun onResume() {
        super.onResume()
        changeActionBarStyleBasedOnCurrentMode()
        supportActionBar?.title = ""
        if (mTitle != null) {
            title = mTitle
        }
    }

    override fun setTitle(title: CharSequence) {
        val textTitle = findViewById<TextView>(R.id.textToolbarTitle)
        if (textTitle != null) {
            mTitle = title.toString()
            textTitle.text = title
        }
    }

    override fun setTitle(titleId: Int) {
        val textTitle = findViewById<TextView>(R.id.textToolbarTitle)
        if (textTitle != null && titleId != 0) {
            mTitle = getString(titleId)
            textTitle.setText(titleId)
        }
    }

    /**
     * Changes the action bar style depending on if the app is in user mode or diagnostic mode
     * This serves as a visual indication as to what mode the app is running in.
     */
    protected fun changeActionBarStyleBasedOnCurrentMode() {
        if (AppPreferences.isDiagnosticMode()) {
            if (supportActionBar != null) {
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(
                        ContextCompat.getColor(this, R.color.diagnostic)))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = ContextCompat.getColor(this, R.color.diagnostic_status)
            }
            val layoutTitle = findViewById<LinearLayout>(R.id.layoutTitleBar)
            layoutTitle?.setBackgroundColor(ContextCompat.getColor(this, R.color.diagnostic))
        } else {
            val typedValue = TypedValue()
            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
            var color = typedValue.data
            if (supportActionBar != null) {
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(color))
            }
            val layoutTitle = findViewById<LinearLayout>(R.id.layoutTitleBar)
            layoutTitle?.setBackgroundColor(color)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true)
                color = typedValue.data
                window.statusBarColor = color
            }
        }
    }
}