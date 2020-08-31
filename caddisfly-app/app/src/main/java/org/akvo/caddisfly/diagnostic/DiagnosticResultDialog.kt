package org.akvo.caddisfly.diagnostic

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import org.akvo.caddisfly.R
import org.akvo.caddisfly.model.ResultDetail
import org.akvo.caddisfly.sensor.chamber.BaseRunTest.OnResultListener
import org.akvo.caddisfly.util.ColorUtil
import java.util.*

class DiagnosticResultDialog : DialogFragment() {
    private var resultDetails: ArrayList<ResultDetail>? = null
    private var result: ResultDetail? = null
    private var mListener: OnDismissed? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_diagnostic_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listResults = view.findViewById<RecyclerView>(R.id.listResults)
        listResults.adapter = ResultListAdapter(resultDetails!!)
        val testFailed = arguments?.getBoolean("testFailed")!!
        val isCalibration = arguments?.getBoolean("isCalibration")!!
        val buttonColorExtract = view.findViewById<Button>(R.id.buttonColorExtract)
        val buttonSwatchColor = view.findViewById<Button>(R.id.buttonSwatchColor)
        val textExtractedRgb = view.findViewById<TextView>(R.id.textExtractedRgb)
        val textSwatchRgb = view.findViewById<TextView>(R.id.textSwatchRgb)
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
                    dialog?.setTitle(
                        String.format(
                            "%s: %s", getString(R.string.result),
                            ColorUtil.getColorRgbString(result!!.color)
                        )
                    )
                }
            } else {
                dialog?.setTitle(
                    String.format(
                        Locale.getDefault(),
                        "%.2f %s", result!!.result, ""
                    )
                )
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

    class ResultListAdapter(
        private val values: List<ResultDetail>
    ) : RecyclerView.Adapter<ResultListAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_info, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val result = values[position]
            holder.imageView.setImageBitmap(result.croppedBitmap)
            val color = result.color
            holder.textSwatch.setBackgroundColor(color)

            //display rgb value
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)
            holder.textRgb.text = String.format(Locale.getDefault(), "%d  %d  %d", r, g, b)
        }

        override fun getItemCount(): Int = values.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.imageView)
            val textRgb: TextView = view.findViewById(R.id.textRgb)
            val textSwatch: TextView = view.findViewById(R.id.textSwatch)
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
        fun newInstance(
            testFailed: Boolean, resultDetail: ResultDetail?,
            resultDetails: ArrayList<ResultDetail>?,
            isCalibration: Boolean
        ): DialogFragment {
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