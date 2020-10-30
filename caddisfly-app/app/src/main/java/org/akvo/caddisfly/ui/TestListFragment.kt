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

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_list.*
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.model.TestSampleType
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.viewmodel.TestListViewModel

class TestListFragment : Fragment() {
    private var mListener: OnListFragmentInteractionListener? = null
    private val mTestInfoClickCallback: TestInfoClickCallback = object : TestInfoClickCallback {
        override fun onClick(testInfo: TestInfo?) {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                val runnable = Runnable { mListener!!.onListFragmentInteraction(testInfo) }
                Handler().postDelayed(runnable, 100)
            }
        }
    }
    private var mTestType: TestType? = null
    private var mTestInfoAdapter: TestInfoAdapter? = null
    private var mSampleType: TestSampleType? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)
        if (arguments != null) {
            mTestType = requireArguments()[ConstantKey.TYPE] as TestType?
            mSampleType = requireArguments()[ConstantKey.SAMPLE_TYPE_KEY] as TestSampleType?
        }
        mTestInfoAdapter = TestInfoAdapter(mTestInfoClickCallback)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (context != null) {
            list_types.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
        list_types.setHasFixedSize(true)
        list_types.adapter = mTestInfoAdapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadTests()
        list_types.adapter = mTestInfoAdapter
    }

    private fun loadTests() {
        val viewModel = ViewModelProvider(this).get(TestListViewModel::class.java)
        val tests = viewModel.getTests(mSampleType, mTestType)
        if (tests.size == 1) {
            mListener!!.onListFragmentInteraction(tests[0])
        } else {
            mTestInfoAdapter!!.setTestList(tests)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnListFragmentInteractionListener) {
            context
        } else {
            throw IllegalArgumentException(context.toString()
                    + " must implement OnCalibrationSelectedListener")
        }
    }

    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(testInfo: TestInfo?)
    }

    companion object {
        const val TAG = "TestListViewModel"

        @JvmStatic
        fun newInstance(testType: TestType?, sampleType: TestSampleType?): TestListFragment {
            val fragment = TestListFragment()
            val args = Bundle()
            args.putSerializable(ConstantKey.TYPE, testType)
            args.putSerializable(ConstantKey.SAMPLE_TYPE_KEY, sampleType)
            fragment.arguments = args
            return fragment
        }
    }
}