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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.akvo.caddisfly.R
import org.akvo.caddisfly.model.TestInfo

class DiagnosticSwatchFragment : Fragment() {
    var testInfo: TestInfo? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            testInfo = requireArguments().getParcelable(ARG_TEST_INFO)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_swatch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity != null) {
            activity!!.setTitle(R.string.swatches)
        }
        val recyclerView: RecyclerView = view.findViewById(R.id.swatchRecyclerView)
        if (testInfo!!.swatches.size > 0) {
            val diagnosticSwatchesAdapter = DiagnosticSwatchesAdapter(testInfo!!.swatches)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = diagnosticSwatchesAdapter
        }
    }

    companion object {
        private const val ARG_TEST_INFO = "testInfo"
        fun newInstance(testInfo: TestInfo?): DiagnosticSwatchFragment {
            val fragment = DiagnosticSwatchFragment()
            val args = Bundle()
            args.putParcelable(ARG_TEST_INFO, testInfo)
            fragment.arguments = args
            return fragment
        }
    }
}