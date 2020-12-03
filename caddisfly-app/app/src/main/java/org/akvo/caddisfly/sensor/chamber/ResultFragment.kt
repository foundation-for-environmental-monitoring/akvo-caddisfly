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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.ConstantKey.IS_INTERNAL
import org.akvo.caddisfly.common.ConstantKey.TEST_INFO
import org.akvo.caddisfly.databinding.FragmentResultBinding
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.util.toLocalString

class ResultFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val b: FragmentResultBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_result, container, false
        )
        val view = b.root
        if (activity != null) {
            requireActivity().setTitle(R.string.result)
        }
        if (arguments != null) {
            val testInfo: TestInfo? = requireArguments().getParcelable(TEST_INFO)
            if (testInfo != null) {
                var result = testInfo.results!![0]
                for (r in testInfo.results!!) {
                    if (r.display == 1) {
                        result = r
                        break
                    } else if (r.display == 2) {
                        val name = "${r.name}:"
                        b.textTitle1.text = name
                        b.textResult1.text = r.result
                        b.textUnit1.text = r.unit
                    } else if (r.display == 3) {
                        val name = "${r.name}:"
                        b.textTitle2.text = name
                        b.textResult2.text = r.result
                        b.textUnit2.text = r.unit
                    }
                }
                b.textResult.text = result.result
                b.textTitle.text = testInfo.name!!.toLocalString()
                b.textDilution.text = resources.getQuantityString(
                    R.plurals.dilutions,
                    testInfo.dilution, testInfo.dilution
                )
                b.textUnit.text = result.unit
                when {
                    testInfo.dilution == testInfo.maxDilution -> {
                        b.dilutionLayout.visibility = View.GONE
                    }
                    result.highLevelsFound() -> {
                        b.dilutionLayout.visibility = View.VISIBLE
                    }
                    else -> {
                        b.dilutionLayout.visibility = View.GONE
                    }
                }
            }
        }
        return view
    }

    companion object {
        /**
         * Get the instance.
         */
        fun newInstance(testInfo: TestInfo?, isInternal: Boolean): ResultFragment {
            val fragment = ResultFragment()
            val args = Bundle()
            args.putParcelable(TEST_INFO, testInfo)
            args.putBoolean(IS_INTERNAL, isInternal)
            fragment.arguments = args
            return fragment
        }
    }
}