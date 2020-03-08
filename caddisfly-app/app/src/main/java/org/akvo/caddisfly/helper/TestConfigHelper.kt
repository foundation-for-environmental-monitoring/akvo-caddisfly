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

import android.util.SparseArray
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.app.CaddisflyApp.Companion.db
import org.akvo.caddisfly.common.ConstantJsonKey
import org.akvo.caddisfly.common.Constants
import org.akvo.caddisfly.model.GroupType
import org.akvo.caddisfly.model.TestInfo
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions to parse a text config json text.
 */
object TestConfigHelper {
    // Files
    private const val BIT_MASK = 0x00FFFFFF

    /**
     * Creates the json result containing the results for test.
     *
     * @param testInfo       information about the test
     * @param results        the results for the test
     * @param color          the color extracted
     * @param resultImageUrl the url of the image
     * @return the result in json format
     */
    @JvmStatic
    fun getJsonResult(testInfo: TestInfo, results: SparseArray<String>,
                      brackets: SparseArray<String?>?, color: Int,
                      resultImageUrl: String?): JSONObject {
        val resultJson = JSONObject()
        try {
            resultJson.put(ConstantJsonKey.TYPE, BuildConfig.APPLICATION_ID)
            resultJson.put(ConstantJsonKey.NAME, testInfo.name)
            resultJson.put(ConstantJsonKey.UUID, testInfo.uuid)
            val resultsJsonArray = JSONArray()
            for (subTest in testInfo.results) {
                val subTestJson = JSONObject()
                subTestJson.put(ConstantJsonKey.DILUTION, subTest.dilution)
                subTestJson.put(ConstantJsonKey.NAME, subTest.name)
                subTestJson.put(ConstantJsonKey.UNIT, subTest.unit)
                subTestJson.put(ConstantJsonKey.ID, subTest.id)
                // If a result exists for the sub test id then add it
                val id = subTest.id
                if (results.size() >= id) {
                    subTestJson.put(ConstantJsonKey.VALUE, results[id])
                    // if there is a bracket result, include it.
                    if (brackets != null && brackets[id] != null) {
                        subTestJson.put(ConstantJsonKey.BRACKET, brackets[id])
                    }
                }
                if (color > -1) {
                    subTestJson.put("resultColor", Integer.toHexString(color and BIT_MASK))
                    val calibrationDetail = db?.calibrationDao()!!.getCalibrationDetails(testInfo.uuid)
                    // Add calibration details to result
                    subTestJson.put("calibratedDate",
                            SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US)
                                    .format(calibrationDetail!!.date))
                    subTestJson.put("reagentExpiry", calibrationDetail.expiry)
                    subTestJson.put("cuvetteType", calibrationDetail.cuvetteType)
                    val calibrationSwatches = JSONArray()
                    for (calibration in testInfo.calibrations) {
                        calibrationSwatches.put(Integer.toHexString(calibration.color and BIT_MASK))
                    }
                    subTestJson.put("calibration", calibrationSwatches)
                }
                resultsJsonArray.put(subTestJson)
                if (testInfo.groupingType == GroupType.GROUP) {
                    break
                }
            }
            resultJson.put(ConstantJsonKey.RESULT, resultsJsonArray)
            if (resultImageUrl != null && resultImageUrl.isNotEmpty()) {
                resultJson.put(ConstantJsonKey.IMAGE, resultImageUrl)
            }
            // Add current date to result
            resultJson.put(ConstantJsonKey.TEST_DATE, SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US)
                    .format(Calendar.getInstance().time))
        } catch (e: JSONException) {
            Timber.e(e)
        }
        return resultJson
    }
}