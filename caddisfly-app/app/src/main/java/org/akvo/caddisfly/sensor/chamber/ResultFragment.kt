package org.akvo.caddisfly.sensor.chamber

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_result.*
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.ConstantKey.IS_INTERNAL
import org.akvo.caddisfly.common.ConstantKey.TEST_INFO
import org.akvo.caddisfly.databinding.FragmentResultBinding
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.util.toLocalString
import java.util.*

class ResultFragment : Fragment() {
    private var mListener: OnDilutionSelectedListener? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val b: FragmentResultBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_result, container, false
        )

        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().setTitle(R.string.result)

        if (arguments != null) {
            val testInfo: TestInfo? = requireArguments().getParcelable(TEST_INFO)
            if (testInfo != null) {
                val result = testInfo.results!![0]
                textResult.text = result.result
                textTitle.text = testInfo.name!!.toLocalString()
                textDilution.text = resources.getQuantityString(
                    R.plurals.dilutions,
                    testInfo.dilution, testInfo.dilution
                )
                textUnit.text = result.unit
                if (testInfo.dilutions.size > 1) {
                    when {
                        testInfo.dilution > 1 -> {
                            buttonDilution1.visibility = View.GONE
                            buttonCustomDilution.visibility = View.GONE
                            high_level_txt.visibility = View.GONE
                        }
                        result.highLevelsFound() -> {
                            buttonDilution1.visibility = View.VISIBLE
                            high_level_txt.visibility = View.VISIBLE
                            val dilution = testInfo.dilutions[1]
                            buttonDilution1.text = String.format(
                                Locale.getDefault(),
                                getString(R.string.times_dilution), dilution
                            )
                            buttonDilution1.setOnClickListener {
                                mListener!!.onDilutionSelected(
                                    dilution
                                )
                            }
                            if (testInfo.dilutions.size > 2) {
                                buttonCustomDilution.visibility = View.VISIBLE
                                buttonCustomDilution.setOnClickListener { showCustomDilutionDialog() }
                            } else {
                                buttonCustomDilution.visibility = View.GONE
                            }
                        }
                        else -> {
                            buttonDilution1.visibility = View.GONE
                            high_level_txt.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun showCustomDilutionDialog() {
        if (activity != null) {
            val ft = requireActivity().supportFragmentManager.beginTransaction()
            val editCustomDilution = EditCustomDilution.newInstance()
            editCustomDilution.show(ft, "editCustomDilution")
        }
    }

    interface OnDilutionSelectedListener {
        fun onDilutionSelected(dilution: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnDilutionSelectedListener) {
            context
        } else {
            throw IllegalArgumentException(
                context.toString()
                        + " must implement OnDilutionSelectedListener"
            )
        }
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