package org.akvo.caddisfly.diagnostic;

import android.view.View;
import android.widget.TextView;

import org.akvo.caddisfly.R;

import androidx.recyclerview.widget.RecyclerView;

public class StateViewHolder extends RecyclerView.ViewHolder {

    final TextView value;
    final TextView rgb;
    final TextView hsv;
    final TextView swatch;

    public StateViewHolder(View itemView) {
        super(itemView);

        swatch = itemView.findViewById(R.id.textSwatch);
        value = itemView.findViewById(R.id.textValue);
        rgb = itemView.findViewById(R.id.textRgb);
        hsv = itemView.findViewById(R.id.textHsv);
    }
}