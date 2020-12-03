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
package org.akvo.caddisfly.sensor.chamber

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.akvo.caddisfly.R
import org.akvo.caddisfly.databinding.FragmentSelectDilutionBinding
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.sensor.chamber.EditCustomDilution.Companion.newInstance
import org.akvo.caddisfly.util.toLocalString
import java.util.*

class SelectDilutionFragment : Fragment() {
    private var testInfo: TestInfo? = null
    private var mListener: OnDilutionSelectedListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            testInfo = requireArguments().getParcelable(ARG_PARAM_TEST_INFO)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentSelectDilutionBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_select_dilution,
            container, false
        )
        val dilutions = testInfo!!.dilutions
        binding.buttonNoDilution.setOnClickListener { mListener!!.onDilutionSelected(1) }
        if (dilutions.size > 1) {
            val dilution = dilutions[1]
            binding.buttonDilution1.text = String.format(
                Locale.getDefault(),
                getString(R.string.times_dilution), dilution
            )
            binding.buttonDilution1.setOnClickListener { mListener!!.onDilutionSelected(dilution) }
        }
        if (dilutions.size > 2) {
            val dilution = dilutions[2]
            binding.buttonDilution2.text = String.format(Locale.getDefault(),
                getString(R.string.times_dilution), dilution
            )
            binding.buttonDilution2.setOnClickListener { mListener!!.onDilutionSelected(dilution) }
        } else {
            binding.buttonDilution2.visibility = View.GONE
        }
        if (dilutions.size > 3) {
            binding.buttonCustomDilution.setOnClickListener { showCustomDilutionDialog() }
        } else {
            binding.buttonCustomDilution.visibility = View.GONE
        }
        (binding.root.findViewById<View>(R.id.textTitle) as TextView).text =
            testInfo!!.name!!.toLocalString()
        return binding.root
    }

    private fun showCustomDilutionDialog() {
        if (activity != null) {
            val ft = requireActivity().supportFragmentManager.beginTransaction()
            val editCustomDilution = newInstance()
            editCustomDilution.show(ft, "editCustomDilution")
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnDilutionSelectedListener) {
            context
        } else {
            throw IllegalArgumentException(context.toString()
                    + " must implement OnDilutionSelectedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnDilutionSelectedListener {
        fun onDilutionSelected(dilution: Int)
    }

    companion object {
        private const val ARG_PARAM_TEST_INFO = "testInfo"

        /**
         * Create new instance of fragment.
         *
         * @param testInfo the test
         * @return the fragment
         */
        fun newInstance(testInfo: TestInfo?): SelectDilutionFragment {
            val fragment = SelectDilutionFragment()
            val args = Bundle()
            args.putParcelable(ARG_PARAM_TEST_INFO, testInfo)
            fragment.arguments = args
            return fragment
        }
    }
}