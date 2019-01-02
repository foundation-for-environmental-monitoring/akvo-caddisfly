package org.akvo.caddisfly.util;

import android.graphics.Color;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.preference.AppPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.InfoViewHolder> {

    @Nullable
    private List<ResultDetail> mResults = new ArrayList<>();

    public ResultAdapter() {
    }

    @NonNull
    @Override
    public InfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.message, parent, false);
        return new InfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InfoViewHolder holder, int position) {
        ResultDetail resultDetail = mResults.get(position);
        holder.bind(resultDetail);

        if (position == 0) {
//            holder.itemView.setBackgroundColor();
            holder.value.setTypeface(null, Typeface.BOLD);
            holder.rgb.setTypeface(null, Typeface.BOLD);
        } else {
            holder.value.setTypeface(null, Typeface.NORMAL);
            holder.rgb.setTypeface(null, Typeface.NORMAL);
        }
    }

    @Override
    public long getItemId(int position) {
        Object listItem = mResults.get(position);
        return listItem.hashCode();
    }

    @Override
    public int getItemCount() {
        return mResults == null ? 0 : mResults.size();
    }

    public void add(String s, boolean ignoreNoResult) {
        if (mResults != null) {
            if (s.isEmpty()) {
                mResults.add(0, new ResultDetail(-2, -2, 0));
            } else {
                String[] values = s.split(",");

                int color = parseInt(values[1]);
                int quality = 0;
                if (values.length > 2) {
                    quality = parseInt(values[2]);
                }
                ResultDetail resultDetail = new ResultDetail(parseDouble(values[0]), color, quality);

                if (ignoreNoResult && resultDetail.getResult() < 0) {
                    return;
                }

                if (mResults.size() > 0) {
                    int previousColor = mResults.get(0).getColor();
                    if (previousColor == -2) {
                        previousColor = mResults.get(1).getColor();
                    }
                    resultDetail.setDistance(ColorUtil.getColorDistance(previousColor, color));
                }
                mResults.add(0, resultDetail);
            }
        }
    }

    public List<ResultDetail> getList() {
        return mResults;
    }

    public void clear() {
        if (mResults != null) {
            mResults.clear();
        }
    }

    static class InfoViewHolder extends RecyclerView.ViewHolder {

        LinearLayout layout;
        TextView value;
        TextView rgb;
        TextView hsv;
        Button swatch;

        public InfoViewHolder(View itemView) {
            super(itemView);

            layout = itemView.findViewById(R.id.layoutRow);
            swatch = itemView.findViewById(R.id.buttonColor);
            value = itemView.findViewById(R.id.textValue);
            rgb = itemView.findViewById(R.id.textRgb);
            hsv = itemView.findViewById(R.id.textHsv);
        }

        public void bind(ResultDetail model) {
            int color = model.getColor();

            swatch.setBackgroundColor(model.getColor());
            if (model.getResult() > -1) {
                value.setText(String.format(Locale.getDefault(), "%.2f",
                        model.getResult()));
            } else if (model.getResult() == -2) {
                value.setText("Paused");
                layout.setBackgroundColor(Color.TRANSPARENT);
                rgb.setText("");
                return;
            } else {
                value.setText("");
            }

            if (model.getResult() < 0 ||
                    model.getDistance() > AppPreferences.getColorDistanceTolerance()) {
                layout.setBackgroundColor(Color.rgb(241, 241, 241));
                value.setTextColor(Color.GRAY);
                rgb.setTextColor(Color.GRAY);
            } else {
                layout.setBackgroundColor(Color.TRANSPARENT);
                value.setTextColor(Color.BLACK);
                rgb.setTextColor(Color.BLACK);
            }

            rgb.setText(
                    String.format(Locale.getDefault(), "d:%.0f   q:%s   c: %s",
                            model.getDistance(), model.getQuality(), ColorUtil.getColorRgbString(color)));
        }
    }
}
