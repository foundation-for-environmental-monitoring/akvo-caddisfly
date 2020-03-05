package org.akvo.caddisfly.sensor.titration

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_titration_input.*
import org.akvo.caddisfly.R
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.ui.BaseFragment
import org.akvo.caddisfly.util.MathUtil
import timber.log.Timber
import java.util.*

class TitrationInputFragment : BaseFragment() {
    private var mListener: OnSubmitResultListener? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_titration_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Handler().postDelayed({ showSoftKeyboard(editTitration1) }, 200)

        editTitration1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                editTitration1.error = null
                editTitration2.error = null
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        editTitration2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                editTitration1?.error = null
                editTitration2.error = null
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        if (arguments != null) {
            val testInfo: TestInfo? = arguments!!.getParcelable(ARG_PARAM1)
            if (testInfo != null) {
                if (testInfo.results.size > 1) {
                    textInput1.text = String.format("%s (N1)", testInfo.results[0].name)
                    textInput2.text = String.format("%s (N2)", testInfo.results[1].name)
                    editTitration2.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            if (mListener != null) {
                                val n1String = editTitration1.text.toString()
                                val n2String = editTitration2.text.toString()
                                if (n1String.isEmpty()) {
                                    editTitration1.error = "Value is required"
                                    editTitration1.requestFocus()
                                } else {
                                    val results = FloatArray(testInfo.results.size)
                                    val n1 = n1String.toFloat()
                                    if (n2String.isEmpty()) {
                                        editTitration2.error = "Value is required"
                                        editTitration2.requestFocus()
                                    } else {
                                        val n2 = n2String.toFloat()
                                        if (n1 > n2) {
                                            editTitration1.error = getString(R.string.titration_entry_error)
                                            editTitration1.requestFocus()
                                        } else {
                                            closeKeyboard(activity, editTitration2)
                                            closeKeyboard(context, editTitration1)
                                            for (i in testInfo.results.indices) {
                                                val formula = testInfo.results[i].formula
                                                if (formula.isNotEmpty()) {
                                                    results[i] = MathUtil.eval(String.format(Locale.US, formula, n1, n2)).toFloat()
                                                }
                                            }
                                            mListener!!.onSubmitResult(results)
                                        }
                                    }
                                }
                            }
                            return@setOnEditorActionListener true
                        }
                        false
                    }
                } else {
                    textInput1.visibility = View.GONE
                    textInput2.visibility = View.GONE
                    editTitration2.visibility = View.GONE
                    editTitration1.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            if (mListener != null) {
                                val n1String = editTitration1.text.toString()
                                if (n1String.isEmpty()) {
                                    editTitration1.error = getString(R.string.value_is_required)
                                    editTitration1.requestFocus()
                                } else {
                                    closeKeyboard(activity, editTitration2)
                                    closeKeyboard(context, editTitration1)
                                    val results = FloatArray(testInfo.results.size)
                                    val n1 = n1String.toFloat()
                                    val formula = testInfo.results[0].formula
                                    results[0] = MathUtil.eval(String.format(Locale.US, formula, n1)).toFloat()
                                    mListener!!.onSubmitResult(results)
                                }
                            }
                            return@setOnEditorActionListener true
                        }
                        false
                    }
                }
            }
        }
    }

    private fun closeKeyboard(context: Context?, input: EditText?) {
        try {
            val imm = context!!.getSystemService(
                    Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(input!!.windowToken, 0)
            if (activity != null) {
                val view = activity!!.currentFocus
                if (view != null) {
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun showSoftKeyboard(view: View?) {
        if (activity != null && view!!.requestFocus()) {
            val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    //    private void hideSoftKeyboard(View view) {
    //        if (getActivity() != null) {
    //            InputMethodManager imm = (InputMethodManager)
    //                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    //            if (imm != null) {
    //                imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
    //            }
    //        }
    //    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnSubmitResultListener) {
            context
        } else {
            throw IllegalArgumentException(context.toString()
                    + " must implement OnSubmitResultListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnSubmitResultListener {
        fun onSubmitResult(results: FloatArray)
    }

    companion object {
        private const val ARG_PARAM1 = "param1"

        /**
         * Get the instance.
         */
        fun newInstance(testInfo: TestInfo?): TitrationInputFragment {
            val fragment = TitrationInputFragment()
            val args = Bundle()
            args.putParcelable(ARG_PARAM1, testInfo)
            fragment.arguments = args
            return fragment
        }
    }
}