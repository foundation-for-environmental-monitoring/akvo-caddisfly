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

import android.content.Context
import android.util.AttributeSet
import androidx.preference.PreferenceCategory
import org.akvo.caddisfly.R

/**
 * A custom category style for the preferences screen.
 */
class PreferenceCategoryCustom : PreferenceCategory {
    @Suppress("unused")
    constructor(context: Context?) : super(context) {
        layoutResource = R.layout.preference_category
    }

    @Suppress("unused")
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        layoutResource = R.layout.preference_category
    }
}