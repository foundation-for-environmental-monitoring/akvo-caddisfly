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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.android.synthetic.main.fragment_calibration_list.*
import org.akvo.caddisfly.R
import org.akvo.caddisfly.app.CaddisflyApp.Companion.db
import org.akvo.caddisfly.common.ConstantKey.IS_INTERNAL
import org.akvo.caddisfly.databinding.FragmentCalibrationListBinding
import org.akvo.caddisfly.entity.Calibration
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.preference.AppPreferences.isDiagnosticMode
import org.akvo.caddisfly.sensor.chamber.CalibrationItemFragment.OnCalibrationSelectedListener
import org.akvo.caddisfly.viewmodel.TestInfoViewModel
import java.text.DateFormat
import java.util.*

/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the [OnCalibrationSelectedListener]
 * interface.
 */

class CalibrationItemFragment : Fragment() {
    private var binding: FragmentCalibrationListBinding? = null
    private lateinit var testInfo: TestInfo
    private var mListener: OnCalibrationSelectedListener? = null
    private var isInternal = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            testInfo = arguments!!.getParcelable(ARG_TEST_INFO)!!
            isInternal = arguments!!.getBoolean(IS_INTERNAL, true)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_calibration_list, container, false)
        val model = ViewModelProvider(this).get(TestInfoViewModel::class.java)
        model.setTest(testInfo)
        binding!!.testInfoViewModel = model
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (context != null) {
            calibrationList.addItemDecoration(DividerItemDecoration(context, 1))
        }
        loadDetails()
        if (!isInternal || !isDiagnosticMode()) {
            layoutButtons.visibility = View.GONE
        }
    }

    /**
     * Display the calibration details such as date, expiry, batch number etc...
     */
    fun loadDetails() {
        setAdapter(testInfo)
        val calibrationDetail = db?.calibrationDao()!!.getCalibrationDetails(testInfo.uuid)
        if (calibrationDetail != null) {
            binding!!.textSubtitle.text = calibrationDetail.cuvetteType
            if (calibrationDetail.date > 0) {
                binding!!.textSubtitle1.text = DateFormat
                        .getDateInstance(DateFormat.MEDIUM).format(Date(calibrationDetail.date))
            } else {
                binding!!.textSubtitle1.text = ""
            }
            if (calibrationDetail.expiry > 0) {
                binding!!.textSubtitle2.text = String.format("%s: %s", getString(R.string.expires),
                        DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(calibrationDetail.expiry)))
            } else {
                binding!!.textSubtitle2.text = ""
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnCalibrationSelectedListener) {
            context
        } else {
            throw IllegalArgumentException(context.toString()
                    + " must implement OnCalibrationSelectedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    fun setAdapter(testInfo: TestInfo) {
        binding!!.calibrationList.adapter = CalibrationViewAdapter(testInfo, mListener)
        binding!!.invalidateAll()
    }

    interface OnCalibrationSelectedListener {
        fun onCalibrationSelected(item: Calibration?)
    }

    companion object {
        private const val ARG_TEST_INFO = "testInfo"

        /**
         * Get instance of CalibrationItemFragment.
         *
         * @param testInfo   the test info
         * @param isInternal is it an internal call or called by an external app
         * @return the fragment
         */
        fun newInstance(testInfo: TestInfo?, isInternal: Boolean): CalibrationItemFragment {
            val fragment = CalibrationItemFragment()
            val args = Bundle()
            args.putParcelable(ARG_TEST_INFO, testInfo)
            args.putBoolean(IS_INTERNAL, isInternal)
            fragment.arguments = args
            return fragment
        }
    }
}