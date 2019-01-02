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

package org.akvo.caddisfly.diagnostic;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.util.ColorUtil;

import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * List of swatches including the generated gradient swatches.
 */
class DiagnosticSwatchesAdapter extends RecyclerView.Adapter<StateViewHolder> {

    private final List<Swatch> swatchList;

    DiagnosticSwatchesAdapter(List<Swatch> swatchList) {
        this.swatchList = swatchList;
    }

    @NonNull
    @Override
    public StateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View swatchView = inflater.inflate(R.layout.row_swatch, parent, false);
        return new StateViewHolder(swatchView);
    }

    @Override
    public void onBindViewHolder(@NonNull StateViewHolder holder, int position) {

        int color = swatchList.get(position).getColor();

        holder.swatch.findViewById(R.id.textSwatch).setBackgroundColor(color);

        holder.value.setText(String.format(Locale.getDefault(), "%.3f", swatchList.get(position).getValue()));

        double distanceRgb = 0;
        if (position > 0) {
            int previousColor = swatchList.get(position - 1).getColor();
            distanceRgb = ColorUtil.getColorDistance(previousColor, color);
        }

        float[] colorHsv = new float[3];
        Color.colorToHSV(color, colorHsv);

        holder.rgb.setText(
                String.format(Locale.getDefault(), "d:%.2f  %s: %s",
                        distanceRgb, "rgb", ColorUtil.getColorRgbString(color)));
        holder.hsv.setText(
                String.format(Locale.getDefault(), "d:%.2f  %s: %.0f  %.2f  %.2f",
                        distanceRgb, "hsv", colorHsv[0], colorHsv[1], colorHsv[1]));
    }

    @Override
    public int getItemCount() {
        return swatchList != null ? swatchList.size() : 0;
    }
}
