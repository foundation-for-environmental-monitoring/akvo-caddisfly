package org.akvo.caddisfly.diagnostic

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.akvo.caddisfly.R

class StateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val value: TextView = itemView.findViewById(R.id.textValue)
    val rgb: TextView = itemView.findViewById(R.id.textRgb)
    val hsv: TextView = itemView.findViewById(R.id.textHsv)
    val swatch: TextView = itemView.findViewById(R.id.textSwatch)
}