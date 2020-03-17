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

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.akvo.caddisfly.R
import org.akvo.caddisfly.entity.Calibration
import org.akvo.caddisfly.model.ColorItem
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.preference.AppPreferences.showDebugInfo
import org.akvo.caddisfly.sensor.chamber.CalibrationItemFragment.OnCalibrationSelectedListener
import org.akvo.caddisfly.util.ColorUtil
import java.util.*

class CalibrationViewAdapter internal constructor(
        private val testInfo: TestInfo,
        private val mListener: OnCalibrationSelectedListener?) :
        RecyclerView.Adapter<CalibrationViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_calibration, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = testInfo.calibrations[position]
        val format = "%." + testInfo.decimalPlaces + "f"
        holder.textValue.text = String.format(Locale.getDefault(), format,
                holder.mItem!!.value)
        val result = testInfo.results!![0]
        val colors: List<ColorItem> = result.colors
        if (position < colors.size) {
            val color = colors[position].rgbInt
            holder.mIdView.background = ColorDrawable(color)
            holder.textUnit.text = result.unit.toString()

            //display additional information if we are in diagnostic mode
            if (showDebugInfo) {
                holder.textUnit.visibility = View.GONE
                holder.textRgb.text = String.format("r: %s", ColorUtil.getColorRgbString(color))
                holder.textRgb.visibility = View.VISIBLE
                val colorHsv = FloatArray(3)
                Color.colorToHSV(color, colorHsv)
                holder.textHsv.text = String.format(Locale.getDefault(),
                        "h: %.0f  %.2f  %.2f", colorHsv[0], colorHsv[1], colorHsv[2])
                holder.textHsv.visibility = View.VISIBLE
                var distance = 0.0
                if (position > 0) {
                    val previousColor = colors[position - 1].rgbInt
                    distance = ColorUtil.getColorDistance(previousColor, color)
                }
                holder.textBrightness.text = String.format(Locale.getDefault(),
                        "d:%.2f  b: %d", distance, ColorUtil.getBrightness(color))
                holder.textBrightness.visibility = View.VISIBLE
            }
        }
        holder.mView.setOnClickListener {
            mListener?.onCalibrationSelected(holder.mItem)
        }
    }

    override fun getItemCount(): Int {
        return testInfo.calibrations.size
    }

    class ViewHolder internal constructor(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: Button = mView.findViewById(R.id.buttonColor)
        val textValue: TextView = mView.findViewById(R.id.textValue)
        val textUnit: TextView = mView.findViewById(R.id.textUnit)
        val textRgb: TextView = mView.findViewById(R.id.textRgb)
        val textHsv: TextView = mView.findViewById(R.id.textHsv)
        val textBrightness: TextView = mView.findViewById(R.id.textBrightness)
        var mItem: Calibration? = null
        override fun toString(): String {
            return super.toString() + " '" + textValue.text + "'"
        }
    }
}