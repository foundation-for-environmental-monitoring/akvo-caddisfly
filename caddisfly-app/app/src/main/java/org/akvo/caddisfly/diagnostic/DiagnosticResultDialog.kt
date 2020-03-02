package org.akvo.caddisfly.diagnostic

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import org.akvo.caddisfly.R
import org.akvo.caddisfly.model.ResultDetail
import org.akvo.caddisfly.sensor.chamber.BaseRunTest.OnResultListener
import org.akvo.caddisfly.util.ColorUtil
import java.util.*

class DiagnosticResultDialog : DialogFragment() {
    private var resultDetails: ArrayList<ResultDetail>? = null
    private var result: ResultDetail? = null
    private var mListener: OnDismissed? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_diagnostic_result, container, false)
        val listResults = view.findViewById<ListView>(R.id.listResults)
        listResults.adapter = ResultListAdapter()
        val testFailed = arguments?.getBoolean("testFailed")!!
        val isCalibration = arguments?.getBoolean("isCalibration")!!
        val buttonColorExtract = view.findViewById<Button>(R.id.buttonColorExtract)
        val buttonSwatchColor = view.findViewById<Button>(R.id.buttonSwatchColor)
        val textExtractedRgb = view.findViewById<TextView>(R.id.textExtractedRgb)
        val textSwatchRgb = view.findViewById<TextView>(R.id.textSwatchRgb)
        //        TextView textDimension = view.findViewById(R.id.textDimension);
        val textDistance = view.findViewById<TextView>(R.id.textDistance)
        val textQuality = view.findViewById<TextView>(R.id.textQuality)
        val buttonCancel = view.findViewById<Button>(R.id.buttonCancel)
        val buttonRetry = view.findViewById<Button>(R.id.buttonRetry)
        buttonColorExtract.setBackgroundColor(result!!.color)
        buttonSwatchColor.setBackgroundColor(result!!.matchedColor)
        textExtractedRgb.text = String.format("%s", ColorUtil.getColorRgbString(result!!.color))
        textSwatchRgb.text = String.format("%s", ColorUtil.getColorRgbString(result!!.matchedColor))
        textDistance.text = String.format(Locale.getDefault(), "D: %.2f", result!!.distance)
        textQuality.text = String.format(Locale.getDefault(), "Q: %d%%", result!!.quality)
        if (testFailed) {
            dialog?.setTitle(R.string.no_result)
        } else {
            if (isCalibration) {
                val tableDetails = view.findViewById<TableLayout>(R.id.tableDetails)
                tableDetails.visibility = View.GONE
                if (result!!.color == Color.TRANSPARENT) {
                    dialog?.setTitle(R.string.error)
                } else {
                    dialog?.setTitle(String.format("%s: %s", getString(R.string.result),
                            ColorUtil.getColorRgbString(result!!.color)))
                }
            } else {
                dialog?.setTitle(String.format(Locale.getDefault(),
                        "%.2f %s", result!!.result, ""))
            }
        }
        buttonCancel.visibility = View.GONE
        buttonRetry.visibility = View.GONE
        val buttonOk = view.findViewById<Button>(R.id.buttonOk)
        buttonOk.visibility = View.VISIBLE
        buttonOk.setOnClickListener {
            if (mListener != null) {
                mListener!!.onDismissed()
            }
            dismiss()
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnResultListener) {
            mListener = context as OnDismissed
        }
    }

    interface OnDismissed {
        fun onDismissed()
    }

    private inner class ResultListAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return resultDetails!!.size
        }

        override fun getItem(position: Int): Any? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, view: View, parent: ViewGroup): View? {
            val inflater = activity?.layoutInflater
            @SuppressLint("ViewHolder") val rowView = inflater?.inflate(R.layout.row_info, parent, false)
            if (rowView != null) {
                val imageView = rowView.findViewById<ImageView>(R.id.imageView)
                val textRgb = rowView.findViewById<TextView>(R.id.textRgb)
                val textSwatch = rowView.findViewById<TextView>(R.id.textSwatch)
                val result = resultDetails!![position]
                imageView.setImageBitmap(result.croppedBitmap)
                val color = result.color
                textSwatch.setBackgroundColor(color)
                //display rgb value
                val r = Color.red(color)
                val g = Color.green(color)
                val b = Color.blue(color)
                textRgb.text = String.format(Locale.getDefault(), "%d  %d  %d", r, g, b)
            }
            return rowView
        }
    }

    companion object {
        /**
         * Instance of dialog.
         *
         * @param testFailed    did test fail
         * @param resultDetail  the result
         * @param resultDetails the result details
         * @param isCalibration is this in calibration mode
         * @return the dialog
         */
        @JvmStatic
        fun newInstance(testFailed: Boolean, resultDetail: ResultDetail?,
                        resultDetails: ArrayList<ResultDetail>?,
                        isCalibration: Boolean): DialogFragment {
            val fragment = DiagnosticResultDialog()
            val args = Bundle()
            fragment.result = resultDetail
            fragment.resultDetails = resultDetails
            args.putBoolean("testFailed", testFailed)
            args.putBoolean("isCalibration", isCalibration)
            fragment.arguments = args
            return fragment
        }
    }
}