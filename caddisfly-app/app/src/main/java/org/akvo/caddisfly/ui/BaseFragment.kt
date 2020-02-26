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

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import org.akvo.caddisfly.R

open class BaseFragment : Fragment() {
    protected fun setTitle(view: View, title: String?) {
        if (activity != null) {
            val toolbar: Toolbar = view.findViewById(R.id.toolbar)
            try {
                (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
            } catch (ignored: Exception) { // do nothing
            }
            val actionBar = (activity as AppCompatActivity?)!!.supportActionBar
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false)
                actionBar.setDisplayHomeAsUpEnabled(true)
                val textTitle = view.findViewById<TextView>(R.id.textToolbarTitle)
                if (textTitle != null) {
                    textTitle.text = title
                }
            }
        }
    }
}