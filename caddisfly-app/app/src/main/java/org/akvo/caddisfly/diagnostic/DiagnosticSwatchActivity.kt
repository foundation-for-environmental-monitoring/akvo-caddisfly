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

import android.os.Bundle
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.ui.BaseActivity

class DiagnosticSwatchActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swatch)
        val testInfo: TestInfo? = intent.getParcelableExtra(ConstantKey.TEST_INFO)
        if (savedInstanceState == null) {
            val fragment: DiagnosticSwatchFragment = DiagnosticSwatchFragment.newInstance(testInfo)
            supportFragmentManager.beginTransaction()
                    .add(R.id.layoutContainer, fragment)
                    .commit()
        }
    }
}