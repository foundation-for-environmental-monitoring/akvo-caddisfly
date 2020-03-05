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
package org.akvo.caddisfly.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CircleView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null,
                                           defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private val circlePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(canvas: Canvas) {
        val w = width
        val h = height
        canvas.drawCircle(w / 2f, h / 2f, RADIUS.toFloat(), circlePaint)
        super.onDraw(canvas)
    }

    companion object {
        private const val RADIUS = 40
    }

    init {
        circlePaint.color = Color.YELLOW
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeWidth = 5f
    }
}