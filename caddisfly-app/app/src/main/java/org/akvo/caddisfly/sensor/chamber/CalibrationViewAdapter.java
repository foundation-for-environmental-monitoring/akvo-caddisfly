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

package org.akvo.caddisfly.sensor.chamber;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.model.ColorItem;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.ColorUtil;

import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CalibrationViewAdapter extends RecyclerView.Adapter<CalibrationViewAdapter.ViewHolder> {

    private final TestInfo testInfo;
    private final CalibrationItemFragment.OnCalibrationSelectedListener mListener;

    CalibrationViewAdapter(TestInfo items, CalibrationItemFragment.OnCalibrationSelectedListener listener) {
        testInfo = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        int layout;
        if (AppPreferences.getShowDebugInfo()) {
            layout = R.layout.item_debug_calibration;
        } else {
            layout = R.layout.item_calibration;
        }

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mItem = testInfo.getCalibrations().get(position);

        String format = "%." + testInfo.getDecimalPlaces() + "f";
        holder.textValue.setText(String.valueOf(String.format(Locale.getDefault(), format,
                holder.mItem.value)));

        Result result = testInfo.getResults().get(0);
        List<ColorItem> presetColors = result.getReferenceColors();
        List<ColorItem> colors = result.getColors();
        List<Calibration> oneStepColors = testInfo.getOneStepCalibrations();
        if (position < colors.size()) {
            int presetColor = 0;
            int oneStepColor = 0;
            if (presetColors.size() > position) {
                presetColor = presetColors.get(position).getRgbInt();
            }
            if (oneStepColors.size() > position) {
                oneStepColor = oneStepColors.get(position).color;
            }

            int color = colors.get(position).getRgbInt();

            if (testInfo.getPivotCalibration() == holder.mItem.value
                    && presetColor != Color.TRANSPARENT
                    && color != Color.TRANSPARENT) {
                holder.imagePivotArrow.setVisibility(View.VISIBLE);
            } else {
                holder.imagePivotArrow.setVisibility(View.INVISIBLE);
            }

            holder.buttonPreset.setBackground(new ColorDrawable(presetColor));
            holder.buttonColor.setBackground(new ColorDrawable(color));
            holder.buttonOneStep.setBackground(new ColorDrawable(oneStepColor));

            if (null != holder.textUnit) {
                holder.textUnit.setText(String.valueOf(result.getUnit()));
            }

            //display additional information if we are in diagnostic mode
            if (AppPreferences.getShowDebugInfo()) {

                if (null == holder.textReference) {
                    holder.textRgb.setText(String.format("r: %s", ColorUtil.getColorRgbString(color)));
                    holder.textRgb.setVisibility(View.VISIBLE);

                    float[] colorHsv = new float[3];
                    Color.colorToHSV(color, colorHsv);
                    holder.textHsv.setText(String.format(Locale.getDefault(),
                            "h: %.0f  %.2f  %.2f", colorHsv[0], colorHsv[1], colorHsv[2]));
                    holder.textHsv.setVisibility(View.VISIBLE);

                } else {
                    holder.textReference.setText(ColorUtil.getColorRgbString(presetColor));
                    holder.textOneStep.setText(ColorUtil.getColorRgbString(oneStepColor));
                    holder.textCalibration.setText(ColorUtil.getColorRgbString(color));
                }

                double distance = 0;
                if (position > 0) {
                    int previousColor = colors.get(position - 1).getRgbInt();
                    distance = ColorUtil.getColorDistance(previousColor, color);
                }

                holder.textBrightness.setText(String.format(Locale.getDefault(), "d %d", (int) distance));
                holder.textBrightness.setVisibility(View.VISIBLE);
            }
        }

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onCalibrationSelected(holder.mItem);
            }
        });

        holder.mView.setOnLongClickListener(v -> {
            if (null != mListener) {
                mListener.onCalibrationLongClick(holder.mItem, position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return testInfo.getCalibrations().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final Button buttonColor;
        final TextView textValue;
        final TextView textUnit;
        private final ImageView imagePivotArrow;
        private final Button buttonPreset;
        private final TextView textRgb;
        private final TextView textHsv;
        private final TextView textBrightness;
        private final Button buttonOneStep;

        private final TextView textReference;
        private final TextView textOneStep;
        private final TextView textCalibration;

        Calibration mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            imagePivotArrow = view.findViewById(R.id.imagePivotArrow);
            buttonPreset = view.findViewById(R.id.buttonPresetColor);
            buttonColor = view.findViewById(R.id.buttonColor);
            buttonOneStep = view.findViewById(R.id.buttonOneStepColor);
            textValue = view.findViewById(R.id.textValue);
            textUnit = view.findViewById(R.id.textUnit);
            textRgb = view.findViewById(R.id.textRgb);
            textHsv = view.findViewById(R.id.textHsv);
            textBrightness = view.findViewById(R.id.textBrightness);

            textReference = view.findViewById(R.id.textReference);
            textOneStep = view.findViewById(R.id.textOneStep);
            textCalibration = view.findViewById(R.id.textCalibration);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + textValue.getText() + "'";
        }
    }
}
