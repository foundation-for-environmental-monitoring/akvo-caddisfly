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
package org.akvo.caddisfly.widget

import android.content.Context
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import org.akvo.caddisfly.R

/**
 * A single numbered row for a list.
 */
class RowView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null) : TableRow(context, attrs) {
    private val textNumber: TextView
    private val textPara: TextView
    fun setNumber(s: String?) {
        textNumber.text = s
    }

    fun append(s: Spanned?) {
        textPara.append(s)
    }

    val string: String
        get() = textPara.text.toString()

    fun enableLinks() {
        textPara.movementMethod = LinkMovementMethod.getInstance()
    }

    init {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.row_view, this, true)
        val tableRow = getChildAt(0) as TableRow
        textNumber = tableRow.getChildAt(0) as TextView
        textPara = tableRow.getChildAt(1) as TextView
    }
}