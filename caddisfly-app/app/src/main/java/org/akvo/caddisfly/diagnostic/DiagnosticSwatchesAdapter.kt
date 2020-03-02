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
package org.akvo.caddisfly.diagnostic

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.akvo.caddisfly.R
import org.akvo.caddisfly.model.Swatch
import org.akvo.caddisfly.util.ColorUtil
import java.util.*

/**
 * List of swatches including the generated gradient swatches.
 */
internal class DiagnosticSwatchesAdapter(private val swatchList: List<Swatch>?) : RecyclerView.Adapter<StateViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StateViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val swatchView = inflater.inflate(R.layout.row_swatch, parent, false)
        return StateViewHolder(swatchView)
    }

    override fun onBindViewHolder(holder: StateViewHolder, position: Int) {
        val color = swatchList!![position].color
        holder.swatch.findViewById<View>(R.id.textSwatch).setBackgroundColor(color)
        holder.value.text = String.format(Locale.getDefault(), "%.3f", swatchList[position].value)
        var distanceRgb = 0.0
        if (position > 0) {
            val previousColor = swatchList[position - 1].color
            distanceRgb = ColorUtil.getColorDistance(previousColor, color)
        }
        val colorHsv = FloatArray(3)
        Color.colorToHSV(color, colorHsv)
        holder.rgb.text = String.format(Locale.getDefault(), "d:%.2f  %s: %s",
                distanceRgb, "rgb", ColorUtil.getColorRgbString(color))
        holder.hsv.text = String.format(Locale.getDefault(), "d:%.2f  %s: %.0f  %.2f  %.2f",
                distanceRgb, "hsv", colorHsv[0], colorHsv[1], colorHsv[1])
    }

    override fun getItemCount(): Int {
        return swatchList?.size ?: 0
    }

}