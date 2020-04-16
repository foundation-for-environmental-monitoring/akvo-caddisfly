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
package org.akvo.caddisfly.helper

import android.content.Context
import android.graphics.Color
import android.os.Build
import org.akvo.caddisfly.app.CaddisflyApp.Companion.db
import org.akvo.caddisfly.app.CaddisflyApp.Companion.getAppVersion
import org.akvo.caddisfly.common.ChamberTestConfig
import org.akvo.caddisfly.entity.Calibration
import org.akvo.caddisfly.model.*
import org.akvo.caddisfly.preference.AppPreferences
import org.akvo.caddisfly.util.ApiUtil
import org.akvo.caddisfly.util.ColorUtil
import org.akvo.caddisfly.util.DateUtil
import org.akvo.caddisfly.util.FileUtil
import org.apache.commons.math3.util.Precision
import timber.log.Timber
import java.io.IOException
import java.text.*
import java.util.*
import kotlin.math.max
import kotlin.math.min

object SwatchHelper {
    private const val MAX_DISTANCE = 999

    @Transient
    private val symbols = DecimalFormatSymbols(Locale.US)

    @Transient
    private val decimalFormat = DecimalFormat("#.###", symbols)

    /**
     * Analyzes the color and returns a result info.
     *
     * @param photoColor The color to compare
     * @param swatches   The range of colors to compare against
     */
    @JvmStatic
    fun analyzeColor(steps: Int, photoColor: ColorInfo, swatches: List<Swatch>): ResultDetail {
        //Find the color within the generated gradient that matches the photoColor
        val colorCompareInfo: ColorCompareInfo = getNearestColorFromSwatches(photoColor.color, swatches)
        //set the result
        val resultDetail = ResultDetail((-1).toDouble(), photoColor.color, photoColor.quality)
        if (colorCompareInfo.result > -1) {
            resultDetail.result = colorCompareInfo.result
        }
        resultDetail.calibrationSteps = steps
        resultDetail.matchedColor = colorCompareInfo.matchedColor
        resultDetail.distance = colorCompareInfo.distance
        return resultDetail
    }

    /**
     * Compares the colorToFind to all colors in the color range and finds the nearest matching color.
     *
     * @param colorToFind The colorToFind to compare
     * @param swatches    The range of colors from which to return the nearest colorToFind
     * @return details of the matching color with its corresponding value
     */
    private fun getNearestColorFromSwatches(
            colorToFind: Int, swatches: List<Swatch>): ColorCompareInfo {
        var distance: Double
        distance = ColorUtil.getMaxDistance(AppPreferences.colorDistanceTolerance.toDouble())
        var resultValue = -1.0
        var matchedColor = -1
        var tempDistance: Double
        var nearestDistance = MAX_DISTANCE.toDouble()
        var nearestMatchedColor = -1
        for (i in swatches.indices) {
            val tempColor = swatches[i].color
            tempDistance = ColorUtil.getColorDistance(tempColor, colorToFind)
            if (nearestDistance > tempDistance) {
                nearestDistance = tempDistance
                nearestMatchedColor = tempColor
            }
            if (tempDistance == 0.0) {
                resultValue = swatches[i].value
                matchedColor = swatches[i].color
                break
            } else if (tempDistance < distance) {
                distance = tempDistance
                resultValue = swatches[i].value
                matchedColor = swatches[i].color
            }
        }
        //if no result was found add some diagnostic info
        if (resultValue == -1.0) {
            distance = nearestDistance
            matchedColor = nearestMatchedColor
        }
        return ColorCompareInfo(resultValue, matchedColor, distance)
    }

    /**
     * Generate the calibration details file.
     *
     * @param context  the context
     * @param testInfo the test
     * @return the calibration file content
     */
    @JvmStatic
    fun generateCalibrationFile(context: Context?, testInfo: TestInfo, internal: Boolean): String {
        val calibrationDetails = StringBuilder()
        var calibrationDate: Long = 0
        for (calibration in testInfo.calibrations) {
            if (calibrationDate < calibration.date) {
                calibrationDate = calibration.date
            }
            //            calibrationDetails.append(String.format(Locale.US, "%.2f", calibration.value))
//                    .append("=")
//                    .append(ColorUtil.getColorRgbString(calibration.color));
            calibrationDetails.append(decimalFormat.format(calibration.value))
                    .append("=")
                    .append(ColorUtil.getColorRgbString(calibration.color))
            calibrationDetails.append('\n')
        }
        val calibrationDetail = db?.calibrationDao()!!.getCalibrationDetails(testInfo.uuid)
        calibrationDetails.append("Name: ")
        calibrationDetails.append(testInfo.name)
        if (calibrationDetail?.cuvetteType != null) {
            calibrationDetails.append("\n")
            calibrationDetails.append("Cuvette: ")
            calibrationDetails.append(calibrationDetail.cuvetteType)
        }
        calibrationDetails.append("\n")
        calibrationDetails.append("UUID: ")
        calibrationDetails.append(testInfo.uuid)
        if (internal) {
            calibrationDetails.append("\n")
            calibrationDetails.append("Date: ")
            calibrationDetails.append(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(System.currentTimeMillis()))
        }
        if (calibrationDate > 0) {
            calibrationDetails.append("\n")
            calibrationDetails.append("Calibrated: ")
            calibrationDetails.append(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(calibrationDate))
        }
        if (calibrationDetail!!.expiry > 0) {
            calibrationDetails.append("\n")
            calibrationDetails.append("ReagentExpiry: ")
            calibrationDetails.append(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calibrationDetail.expiry))
        }
        calibrationDetails.append("\n")
        calibrationDetails.append("Version: ")
        calibrationDetails.append(getAppVersion(true))
        calibrationDetails.append("\n")
        calibrationDetails.append("Model: ")
        calibrationDetails.append(Build.MODEL).append(" (")
                .append(Build.PRODUCT).append(")")
        calibrationDetails.append("\n")
        calibrationDetails.append("OS: ")
        calibrationDetails.append(Build.VERSION.RELEASE).append(" (")
                .append(Build.VERSION.SDK_INT).append(")")
        if (internal) {
            calibrationDetails.append("\n")
            calibrationDetails.append("DeviceId: ")
            calibrationDetails.append(ApiUtil.getInstallationId(context!!))
        }
        return calibrationDetails.toString()
    }

    /**
     * Load the calibration details from file.
     *
     * @param testInfo the test
     * @param fileName the file name
     * @return the list of calibrations loaded
     * @throws IOException IO exception
     */
    @JvmStatic
    @Throws(IOException::class)
    fun loadCalibrationFromFile(testInfo: TestInfo, fileName: String): ArrayList<Calibration> {
        val calibrations: ArrayList<Calibration> = ArrayList()
        val path = FileHelper.getFilesDir(FileType.CALIBRATION, testInfo.uuid)
        val dao = db!!.calibrationDao()
        val calibrationDetails = FileUtil.loadFromFile(path, fileName)
        if (calibrationDetails != null) {
            val calibrationDetail = dao!!.getCalibrationDetails(testInfo.uuid)
            calibrationDetail!!.uid = testInfo.uuid
            for (i in calibrationDetails.indices.reversed()) {
                val line = calibrationDetails[i]
                if (!line.contains("=")) {
                    if (line.contains("Calibrated:")) {
                        val calendar = Calendar.getInstance()
                        val date = DateUtil.convertStringToDate(line.substring(line.indexOf(':') + 1),
                                "yyyy-MM-dd HH:mm")
                        if (date != null) {
                            calendar.time = date
                            calibrationDetail.date = calendar.timeInMillis
                        }
                    }
                    if (line.contains("ReagentExpiry:")) {
                        val calendar = Calendar.getInstance()
                        val date = DateUtil.convertStringToDate(line.substring(line.indexOf(':') + 1),
                                "yyyy-MM-dd")
                        if (date != null) {
                            calendar.time = date
                            calibrationDetail.expiry = calendar.timeInMillis
                        }
                    }
                    if (line.contains("Cuvette:")) {
                        calibrationDetail.cuvetteType = line.substring(line.indexOf(':') + 1).trim { it <= ' ' }
                    }
                    calibrationDetails.removeAt(i)
                }
            }
            dao.insert(calibrationDetail)
            for (rgb in calibrationDetails) {
                val values = rgb.split("=").toTypedArray()
                val calibration = Calibration()
                calibration.uid = testInfo.uuid
                calibration.date = Date().time
                calibration.value = stringToDouble(values[0])
                if (values.size > 1) {
                    calibration.color = ColorUtil.getColorFromRgb(values[1])
                }
                calibrations.add(calibration)
            }
            if (calibrations.size > 0) {
                dao.insertAll(calibrations)
            } else {
                throw IOException()
            }
        }
        return calibrations
    }

    /**
     * Auto generate the color swatches for the given test type.
     *
     * @param swatches The test object
     * @return The list of generated color swatches
     */
    @JvmStatic
    fun generateGradient(swatches: MutableList<Swatch>): MutableList<Swatch> {
        val list: MutableList<Swatch> = ArrayList()
        if (swatches.size < 2) {
            return list
        }
        // Predict 2 more points in the calibration list to account for high levels of contamination
        val swatch1 = swatches[swatches.size - 2]
        val swatch2 = swatches[swatches.size - 1]
        swatches.add(predictNextColor(swatch1, swatch2))
        swatches.add(predictNextColor(swatch2, swatches[swatches.size - 1]))
        for (i in 0 until swatches.size - 1) {
            val startColor = swatches[i].color
            val endColor = swatches[i + 1].color
            val startValue = swatches[i].value
            val endValue = swatches[i + 1].value
            val increment = (endValue - startValue) / ChamberTestConfig.INTERPOLATION_COUNT
            val steps = ((endValue - startValue) / increment).toInt()
            for (j in 0 until steps) {
                val color = ColorUtil.getGradientColor(startColor, endColor, steps, j)
                list.add(Swatch(startValue + j * increment, color, Color.TRANSPARENT))
            }
        }
        list.add(Swatch(swatches[swatches.size - 1].value,
                swatches[swatches.size - 1].color, Color.TRANSPARENT))
        return list
    }

    private fun predictNextColor(swatch1: Swatch, swatch2: Swatch): Swatch {
        val valueDiff = swatch2.value - swatch1.value
        val color1 = swatch1.color
        val color2 = swatch2.color
        val r = getNextLinePoint(Color.red(color1), Color.red(color2))
        val g = getNextLinePoint(Color.green(color1), Color.green(color2))
        val b = getNextLinePoint(Color.blue(color1), Color.blue(color2))
        return Swatch(swatch2.value + valueDiff, Color.rgb(r, g, b), Color.TRANSPARENT)
    }

    private fun getNextLinePoint(y: Int, y2: Int): Int {
        val diff = y2 - y
        return min(255, max(0, y2 + diff))
    }

    /**
     * Validate the color by looking for missing color, duplicate colors, color out of sequence etc...
     *
     * @param testInfo the test Information
     * @return True if valid otherwise false
     */
    @JvmStatic
    fun isSwatchListValid(testInfo: TestInfo?): Boolean {
        if (testInfo == null) {
            return false
        }
        var result = true
        val calibrations = testInfo.calibrations
        if (calibrations.isEmpty()) {
            return false
        }
        for (swatch1 in calibrations) {
            if (swatch1.color == Color.TRANSPARENT || swatch1.color == Color.BLACK) { //Calibration is incomplete
                result = false
                break
            }
            for (swatch2 in calibrations) {
                if (swatch1 != swatch2 && ColorUtil.areColorsSimilar(swatch1.color, swatch2.color)) { //Duplicate color
                    result = false
                    break
                }
            }
        }
        return result
    }

    /**
     * Validate the color by looking for missing color, duplicate colors, color out of sequence etc...
     *
     * @param swatches the range of colors
     * @return True if calibration is complete
     */
    @JvmStatic
    fun isCalibrationComplete(swatches: List<Swatch>): Boolean {
        for (swatch in swatches) {
            if (swatch.color == 0 || swatch.color == Color.BLACK) { //Calibration is incomplete
                return false
            }
        }
        return true
    }

    /**
     * Convert a string number into a double value.
     *
     * @param text the text to be converted to number
     * @return the double value
     */
    private fun stringToDouble(text: String): Double {
        val tempText = text.replace(",".toRegex(), ".")
        val nf = NumberFormat.getInstance(Locale.US)
        return try {
            nf.parse(tempText)!!.toDouble()
        } catch (e: ParseException) {
            Timber.e(e)
            0.0
        }
    }

    /**
     * Get the average value from list of results.
     *
     * @param resultDetails the result info
     * @return the average value
     */
    @JvmStatic
    fun getAverageResult(resultDetails: ArrayList<ResultDetail>): Double {
        var result = 0.0
        for (i in resultDetails.indices) {
            val color1 = resultDetails[i].color
            for (j in resultDetails.indices) {
                val color2 = resultDetails[j].color
                if (ColorUtil.getColorDistance(color1, color2) > AppPreferences.averagingColorDistanceTolerance) {
                    return (-1).toDouble()
                }
            }
        }
        for (i in resultDetails.indices) {
            val value = resultDetails[i].result
            result += if (value > -1) {
                value
            } else {
                return (-1).toDouble()
            }
        }
        result = try {
            Precision.round(result / resultDetails.size, 2)
        } catch (ex: Exception) {
            -1.0
        }
        return result
    }

    /**
     * Returns an average color from a list of results.
     * If any color does not closely match the rest of the colors then it returns -1
     *
     * @param resultDetails the list of results
     * @return the average color
     */
    @JvmStatic
    fun getAverageColor(resultDetails: ArrayList<ResultDetail>): Int {
        var red = 0
        var green = 0
        var blue = 0
        for (i in resultDetails.indices) {
            val color1 = resultDetails[i].color
            //if invalid color return 0
            if (color1 == Color.TRANSPARENT) {
                return color1
            }
            //check all the colors are mostly similar otherwise return -1
            for (j in resultDetails.indices) {
                val color2 = resultDetails[j].color
                if (ColorUtil.getColorDistance(color1, color2) > AppPreferences.averagingColorDistanceTolerance) {
                    return Color.TRANSPARENT
                }
            }
            red += Color.red(color1)
            green += Color.green(color1)
            blue += Color.blue(color1)
        }
        //return an average color
        val resultCount = resultDetails.size
        return Color.rgb(red / resultCount, green / resultCount, blue / resultCount)
    }
}