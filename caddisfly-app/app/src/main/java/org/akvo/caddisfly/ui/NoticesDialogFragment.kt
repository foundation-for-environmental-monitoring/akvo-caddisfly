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
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import org.akvo.caddisfly.R

class NoticesDialogFragment : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_notices_dialog, container, false)
        makeEverythingClickable(view.findViewById(R.id.about_container))
        return view
    }

    private fun makeEverythingClickable(vg: ViewGroup) {
        for (i in 0 until vg.childCount) {
            if (vg.getChildAt(i) is ViewGroup) {
                makeEverythingClickable(vg.getChildAt(i) as ViewGroup)
            } else if (vg.getChildAt(i) is TextView) {
                val tv = vg.getChildAt(i) as TextView
                tv.setLinkTextColor(ContextCompat.getColor(requireContext(), R.color.accent))
                tv.movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }

    override fun onResume() {
        if (dialog!!.window != null) {
            val params = dialog!!.window!!.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.MATCH_PARENT
            dialog!!.window!!.attributes = params
        }
        super.onResume()
    }

    companion object {
        fun newInstance(): NoticesDialogFragment {
            return NoticesDialogFragment()
        }
    }
}