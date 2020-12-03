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
package org.akvo.caddisfly.util

import android.graphics.Bitmap
import android.graphics.Color
import android.util.SparseIntArray
import org.akvo.caddisfly.common.ChamberTestConfig
import org.akvo.caddisfly.model.ColorInfo
import timber.log.Timber
import java.util.*
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * The minimum color distance at which the colors are considered equivalent
 */
const val MIN_DISTANCE = 6.0

/**
 * Set of utility functions for color calculations and analysis
 */
object ColorUtil {

    @JvmStatic
    fun getMaxDistance(defaultValue: Double): Double {
        return if (defaultValue > 0) {
            defaultValue
        } else {
            ChamberTestConfig.MAX_COLOR_DISTANCE_RGB.toDouble()
        }
    }

    /**
     * Get the most common color from the bitmap
     *
     * @param bitmap       The bitmap from which to extract the color
     * @param sampleLength The max length of the image to traverse
     * @return The extracted color information
     */
    @JvmStatic
    fun getColorFromBitmap(bitmap: Bitmap,
                           sampleLength: Int): ColorInfo {
        var highestCount = 0
        var commonColor = -1
        var counter: Int
        var goodPixelCount = 0
        var totalPixels = 0
        var quality = 0.0
        val colorsFound: Int
        try {
            val m = SparseIntArray()
            for (i in 0 until min(bitmap.width, sampleLength)) {
                for (j in 0 until min(bitmap.height, sampleLength)) {
                    val color = bitmap.getPixel(i, j)
                    if (color != Color.TRANSPARENT) {
                        totalPixels++
                        counter = m[color]
                        counter++
                        m.put(color, counter)
                        if (counter > highestCount) {
                            commonColor = color
                            highestCount = counter
                        }
                    }
                }
            }

            // check the quality of the photo
            colorsFound = m.size()
            var goodColors = 0
            for (i in 0 until colorsFound) {
                if (areColorsSimilar(commonColor, m.keyAt(i))) {
                    goodColors++
                    goodPixelCount += m.valueAt(i)
                }
            }
            val quality1 = goodPixelCount.toDouble() / totalPixels * 100.0
            val quality2 = (colorsFound - goodColors).toDouble() / colorsFound * 100.0
            quality = min(quality1, 100 - quality2)
            m.clear()
        } catch (e: Exception) {
            Timber.e(e)
        }
        return ColorInfo(commonColor, quality.toInt())
    }

    /**
     * Get the brightness of a given color
     *
     * @param color The color
     * @return The brightness value
     */
    @JvmStatic
    fun getBrightness(color: Int): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return sqrt(r * r * .241 + g * g * .691 + b * b * .068
        ).toInt()
    }

    /**
     * Computes the Euclidean distance between the two colors
     *
     * @param color1 the first color
     * @param color2 the color to compare with
     * @return the distance between the two colors
     */
    @JvmStatic
    fun getColorDistance(color1: Int, color2: Int): Double {
        val r: Double = (Color.red(color2) - Color.red(color1).toDouble()).pow(2.0)
        val g: Double = (Color.green(color2) - Color.green(color1).toDouble()).pow(2.0)
        val b: Double = (Color.blue(color2) - Color.blue(color1).toDouble()).pow(2.0)
        return sqrt(b + g + r)
    }

    //    public static boolean areColorsTooDissimilar(int color1, int color2) {
    //        return getColorDistanceRgb(color1, color2) > MAX_SAMPLING_COLOR_DISTANCE_RGB;
    //    }
    @JvmStatic
    fun areColorsSimilar(color1: Int, color2: Int): Boolean {
        return getColorDistance(color1, color2) < MIN_DISTANCE
    }

    /**
     * Get the color that lies in between two colors
     *
     * @param startColor The first color
     * @param endColor   The last color
     * @param n          Number of steps between the two colors
     * @param i          The index at which the color is to be calculated
     * @return The newly generated color
     */
    @JvmStatic
    fun getGradientColor(startColor: Int, endColor: Int, n: Int, i: Int): Int {
        return Color.rgb(interpolate(Color.red(startColor), Color.red(endColor), n, i),
                interpolate(Color.green(startColor), Color.green(endColor), n, i),
                interpolate(Color.blue(startColor), Color.blue(endColor), n, i))
    }

    /**
     * Get the color component that lies between the two color component points
     *
     * @param start The first color component value
     * @param end   The last color component value
     * @param n     Number of steps between the two colors
     * @param i     The index at which the color is to be calculated
     * @return The calculated color component
     */
    @JvmStatic
    private fun interpolate(start: Int, end: Int, n: Int, i: Int): Int {
        return (start.toFloat() + (end.toFloat() - start.toFloat()) / n * i).toInt()
    }

    /**
     * Convert color value to RGB string
     *
     * @param color The color to convert
     * @return The rgb value as string
     */
    @JvmStatic
    fun getColorRgbString(color: Int): String {
        return if (color == Color.TRANSPARENT) {
            ""
        } else String.format(Locale.getDefault(), "%d  %d  %d", Color.red(color), Color.green(color), Color.blue(color))
    }

    /**
     * Convert rgb string color to color
     *
     * @param rgbValue The rgb string representation of the color
     * @return An Integer color value
     */
    @JvmStatic
    fun getColorFromRgb(rgbValue: String): Int {
        var rgb = rgbValue.trim()
        rgb = rgb.replace(",", " ").trim()
        if (rgb.isEmpty()) {
            return 0
        }
        val rgbArray = rgb.split("\\s+".toPattern()).toTypedArray()
        return Color.rgb(rgbArray[0].toInt(), rgbArray[1].toInt(), rgbArray[2].toInt())
    }
}
