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
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import org.akvo.caddisfly.model.GroupType
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.sensor.striptest.utils.ColorUtils
import org.akvo.caddisfly.sensor.striptest.utils.ResultUtils

/**
 * Displays the swatches for the calibrated colors of the test.
 */
class SwatchView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var blockWidth = 0f
    private var lineHeight = 0f
    private val paintColor: Paint
    private val lab = FloatArray(3)
    private var lineCount = 0
    private var extraHeight = 0
    private var totalWidth = 0f
    private var testInfo: TestInfo? = null
    private val blackText: Paint = Paint()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (testInfo != null) {
            var index = -1
            for (resultIndex in testInfo!!.results!!.indices) {
                val colors = testInfo!!.results!![resultIndex].colors
                if (colors.isNotEmpty()) {
                    index += 1
                    val colorCount = colors.size
                    for (i in 0 until colorCount) {
                        val colorItem = colors[i]
                        paintColor.color = colorItem.rgbInt
                        canvas.drawRect(MARGIN + i * totalWidth, MARGIN + index * lineHeight,
                                i * totalWidth + blockWidth, index * lineHeight + blockWidth, paintColor)
                        if (testInfo!!.groupingType == GroupType.INDIVIDUAL
                                || index == testInfo!!.results!!.size - 1) {
                            canvas.drawText(ResultUtils.createValueString(colorItem.value!!.toFloat()),
                                    MARGIN + (i * totalWidth + blockWidth / 2),
                                    MARGIN + index * lineHeight + blockWidth + VAL_BAR_HEIGHT, blackText)
                        }
                    }
                }
            }
        }
    }

    /**
     * Set the test for which the swatches should be displayed.
     *
     * @param testInfo the test
     */
    fun setTestInfo(testInfo: TestInfo) {
        this.testInfo = testInfo
        if (testInfo.groupingType == GroupType.GROUP) {
            gutterSize = 1f
            extraHeight = 40
        }
        lineCount = 0
        for (resultIndex in testInfo.results!!.indices) {
            val colors = testInfo.results!![resultIndex].colors
            if (colors.size > 0) {
                lineCount += 1
                val colorCount = colors.size
                val rgbCols = Array(colorCount) { IntArray(3) }

                // get lab colours and turn them to RGB
                for (i in 0 until colorCount) {
                    val patchColorValues = colors[i].lab
                    if (patchColorValues.isNotEmpty()) {
                        lab[0] = patchColorValues[0].toFloat()
                        lab[1] = patchColorValues[1].toFloat()
                        lab[2] = patchColorValues[2].toFloat()
                        rgbCols[i] = ColorUtils.xyzToRgbInt(ColorUtils.Lab2XYZ(lab))
                        val color = Color.rgb(rgbCols[i][0], rgbCols[i][1], rgbCols[i][2])
                        colors[i].rgbInt = color
                    }
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        if (measuredWidth != 0 && measuredHeight != 0) {
            val width = measuredWidth - MARGIN * 2
            if (testInfo != null) {
                for (resultIndex in testInfo!!.results!!.indices) {
                    val colors = testInfo!!.results!![resultIndex].colors
                    if (colors.size > 0) {
                        val colorCount = colors.size.toFloat()
                        if (blockWidth == 0f) {
                            blockWidth = (width - (colorCount - 4) * gutterSize) / colorCount
                            lineHeight = if (testInfo!!.groupingType == GroupType.GROUP) {
                                blockWidth + VAL_BAR_HEIGHT.toFloat() / 3
                            } else {
                                blockWidth + VAL_BAR_HEIGHT + VAL_BAR_HEIGHT
                            }
                        }
                    }
                }
            }
            totalWidth = gutterSize + blockWidth
        }
        super.onMeasure(widthMeasureSpec,
                MeasureSpec.makeMeasureSpec((lineCount * lineHeight + extraHeight).toInt(), MeasureSpec.EXACTLY))
    }

    companion object {
        private const val VAL_BAR_HEIGHT = 15
        private const val TEXT_SIZE = 20
        private const val MARGIN = 10f
        private var gutterSize = 5f
    }

    init {
        blackText.color = Color.BLACK
        blackText.textSize = TEXT_SIZE.toFloat()
        blackText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        blackText.textAlign = Paint.Align.CENTER
        paintColor = Paint()
        paintColor.style = Paint.Style.FILL
    }
}