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
import android.widget.ArrayAdapter
import android.widget.ListView
import org.akvo.caddisfly.R
import org.akvo.caddisfly.util.PreferencesUtil
import java.util.*

class DiagnosticInfoActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnostic_info)
        title = "Diagnostic Information"
        val mListView = findViewById<ListView>(R.id.listInformation)
        val infoList = ArrayList<String>()
        infoList.add(String.format(Locale.getDefault(), "Number of calibrations%ns:%d  e:%d",
                PreferencesUtil.getInt(this, R.string.totalSuccessfulCalibrationsKey, 0),
                PreferencesUtil.getInt(this, R.string.totalFailedCalibrationsKey, 0)))
        infoList.add(String.format(Locale.getDefault(), "Number of tests%ns:%d  e:%d",
                PreferencesUtil.getInt(this, R.string.totalSuccessfulTestsKey, 0),
                PreferencesUtil.getInt(this, R.string.totalFailedTestsKey, 0)))
        val infoAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, infoList)
        if (mListView != null) {
            mListView.adapter = infoAdapter
        }
    }
}