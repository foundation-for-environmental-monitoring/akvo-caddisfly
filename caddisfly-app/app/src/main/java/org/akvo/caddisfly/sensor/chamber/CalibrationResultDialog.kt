package org.akvo.caddisfly.sensor.chamber

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import org.akvo.caddisfly.R
import org.akvo.caddisfly.entity.Calibration
import java.util.*

class CalibrationResultDialog : DialogFragment() {
    private var calibration: Calibration? = null
    private var decimalPlaces = 0
    private var unit: String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_calibration_result, container, false)
        dialog!!.setTitle(R.string.calibrated)
        val buttonColorExtract = view.findViewById<Button>(R.id.buttonColorExtract)
        val textValue = view.findViewById<TextView>(R.id.textValue)
        val textUnit = view.findViewById<TextView>(R.id.textUnit)
        buttonColorExtract.setBackgroundColor(calibration!!.color)
        val format = "%." + decimalPlaces + "f"
        textValue.text = String.format(Locale.getDefault(), format, calibration!!.value, "")
        textUnit.text = unit
        val buttonOk = view.findViewById<Button>(R.id.buttonOk)
        buttonOk.visibility = View.VISIBLE
        buttonOk.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    activity!!.stopLockTask()
                } catch (ignored: Exception) {
                }
            }
            dismiss()
        }
        return view
    }

    companion object {
        /**
         * Instance of dialog.
         *
         * @param calibration   the result
         * @param decimalPlaces decimal places
         * @return the dialog
         */
        @JvmStatic
        fun newInstance(calibration: Calibration?, decimalPlaces: Int, unit: String?): DialogFragment {
            val fragment = CalibrationResultDialog()
            val args = Bundle()
            fragment.decimalPlaces = decimalPlaces
            fragment.calibration = calibration
            fragment.unit = unit
            fragment.arguments = args
            return fragment
        }
    }
}