package org.akvo.caddisfly.repository

import android.content.Context
import android.graphics.Color
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.akvo.caddisfly.R
import org.akvo.caddisfly.app.CaddisflyApp.Companion.app
import org.akvo.caddisfly.app.CaddisflyApp.Companion.db
import org.akvo.caddisfly.entity.Calibration
import org.akvo.caddisfly.entity.CalibrationDetail
import org.akvo.caddisfly.helper.SwatchHelper.loadCalibrationFromFile
import org.akvo.caddisfly.model.*
import org.akvo.caddisfly.preference.AppPreferences.isDiagnosticMode
import org.akvo.caddisfly.util.AssetsManager
import org.akvo.caddisfly.util.PreferencesUtil.getInt
import org.akvo.caddisfly.util.PreferencesUtil.getLong
import timber.log.Timber
import java.io.IOException
import java.util.*

class TestConfigRepository {
    private val assetsManager: AssetsManager = AssetsManager()

    @SuppressWarnings("unused")
    fun getTests(testType: TestType): List<TestInfo> {
        return getTests(testType, TestSampleType.ALL)
    }

    /**
     * Get list of tests by type of test.
     *
     * @param testType the test type
     * @return the list of tests
     */
    fun getTests(testType: TestType, testSampleType: TestSampleType): List<TestInfo> {
        var testInfoList: MutableList<TestInfo> = ArrayList()
        if (testSampleType === TestSampleType.ALL) {
            if (testMap.containsKey(testType.toString())) {
                return testMap[testType.toString()]!!
            }
        } else {
            if (testMap.containsKey(testType.toString() + testSampleType.toString())) {
                return testMap[testType.toString() + testSampleType.toString()]!!
            }
        }
        try {
            testInfoList = Gson().fromJson(assetsManager.json, TestConfig::class.java).tests
            for (i in testInfoList.indices.reversed()) {
                if (testInfoList[i].subtype !== testType) {
                    testInfoList.removeAt(i)
                } else if (testSampleType !== TestSampleType.ALL
                        && testInfoList[i].sampleType !== testSampleType) {
                    testInfoList.removeAt(i)
                }
            }
            testInfoList.sortWith(Comparator { object1: TestInfo, object2: TestInfo ->
                object1.name!!.compareTo(object2.name!!, ignoreCase = true)
            })
            if (isDiagnosticMode()) {
                addExperimentalTests(testType, testSampleType, testInfoList)
            }
            val testConfig = Gson().fromJson(assetsManager.customJson, TestConfig::class.java)
            if (testConfig != null) {
                val customList = testConfig.tests
                for (i in customList.indices.reversed()) {
                    if (customList[i].subtype !== testType) {
                        customList.removeAt(i)
                    } else if (testSampleType !== TestSampleType.ALL
                            && customList[i].sampleType !== testSampleType) {
                        customList.removeAt(i)
                    }
                }
                if (customList.size > 0) {
                    customList.sortWith(Comparator { object1: TestInfo, object2: TestInfo ->
                        object1.name!!.compareTo(object2.name!!, ignoreCase = true)
                    })
                    testInfoList.add(TestInfo("Custom"))
                    testInfoList.addAll(customList)
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        if (testSampleType === TestSampleType.ALL) {
            testMap[testType.toString()] = testInfoList
        } else {
            testMap[testType.toString() + testSampleType.toString()] = testInfoList
        }
        for (i in testInfoList.indices.reversed()) {
            val testInfo = testInfoList[i]
            localizeTestName(testInfo)
        }
        return testInfoList
    }

    private fun addExperimentalTests(testType: TestType, testSampleType: TestSampleType, testInfoList: MutableList<TestInfo>) {
        val testConfig = Gson().fromJson(assetsManager.experimentalJson, TestConfig::class.java)
        if (testConfig != null) {
            val experimentalList = testConfig.tests
            for (i in experimentalList.indices.reversed()) {
                if (experimentalList[i].subtype !== testType) {
                    experimentalList.removeAt(i)
                } else if (testSampleType !== TestSampleType.ALL
                        && experimentalList[i].sampleType !== testSampleType) {
                    experimentalList.removeAt(i)
                }
            }
            if (experimentalList.size > 0) {
                experimentalList.sortWith(Comparator { object1: TestInfo, object2: TestInfo ->
                    object1.name!!.compareTo(object2.name!!, ignoreCase = true)
                })
                testInfoList.add(TestInfo("Experimental"))
                testInfoList.addAll(experimentalList)
            }
        }
    }

    /**
     * Get the test details from json config.
     *
     * @param id the test id
     * @return the test object
     */
    fun getTestInfo(id: String): TestInfo? {
        var testInfo: TestInfo?
        testInfo = getTestInfoItem(assetsManager.json, id)
        if (testInfo == null) {
            if (isDiagnosticMode()) {
                testInfo = getTestInfoItem(assetsManager.experimentalJson, id)
            }
            if (testInfo == null) {
                testInfo = getTestInfoItem(assetsManager.customJson, id)
            }
        }
        testInfo?.let { localizeTestName(it) }
        return testInfo
    }

    private fun localizeTestName(testInfo: TestInfo) {
        if (testInfo.name != null) {
            var name = testInfo.name!!.toLowerCase(Locale.ROOT)
                    .replace(")", "")
                    .replace("(", "")
                    .replace("- ", "")
                    .replace(" ", "_")
            val nameId = app!!.resources
                    .getIdentifier(name, "string",
                            app!!.packageName)
            if (nameId > 0) {
                name = app!!.getString(nameId)
                if (name != ".") {
                    testInfo.name = name
                }
            }
        }
    }

    private fun getTestInfoItem(json: String, id: String): TestInfo? {
        val testInfoList: List<TestInfo>
        try {
            val testConfig = Gson().fromJson(json, TestConfig::class.java)
            if (testConfig != null) {
                testInfoList = testConfig.tests
                for (testInfo in testInfoList) {
                    if (testInfo.uuid.equals(id, ignoreCase = true)) {
                        if (testInfo.subtype === TestType.CHAMBER_TEST) {
                            val dao = db!!.calibrationDao()

                            // if range values are defined as comma delimited text then convert to array
                            convertRangePropertyToArray(testInfo)
                            var calibrations = dao!!.getAll(testInfo.uuid)
                            if (calibrations!!.isEmpty()) {
                                calibrations = getPlaceHolderCalibrations(testInfo)

                                // get any calibrations saved by previous version of the app
                                val calibrationsOld = getBackedUpCalibrations(testInfo)
                                for (calibration in calibrations) {
                                    for (calibrationOld in calibrationsOld) {
                                        if (calibration.value == calibrationOld.value) {
                                            calibration.color = calibrationOld.color
                                        }
                                    }
                                }
                                if (calibrations.isNotEmpty()) {
                                    dao.insertAll(calibrations)
                                }
                            }
                            testInfo.calibrations = calibrations
                        }
                        return testInfo
                    }
                }
            }
        } catch (e: JsonSyntaxException) {
            // do nothing
        }
        return null
    }

    private fun getPlaceHolderCalibrations(testInfo: TestInfo): List<Calibration> {
        val calibrations: MutableList<Calibration> = ArrayList()
        for (colorItem in testInfo.results!![0].colors) {
            val calibration = Calibration()
            calibration.uid = testInfo.uuid!!
            calibration.color = Color.TRANSPARENT
            calibration.value = colorItem.value!!
            calibrations.add(calibration)
        }
        return calibrations
    }

    private fun getBackedUpCalibrations(testInfo: TestInfo): List<Calibration> {
        var calibrations: ArrayList<Calibration> = ArrayList()
        val context: Context? = app
        val colors = testInfo.results!![0].colors
        for (color in colors) {
            val key = String.format(Locale.US, "%s-%.2f",
                    testInfo.uuid, color.value)
            val calibration = Calibration()
            calibration.uid = testInfo.uuid!!
            calibration.color = getInt(context, key, 0)
            calibration.value = color.value!!
            calibrations.add(calibration)
        }
        val calibrationDetail = CalibrationDetail()
        calibrationDetail.uid = testInfo.uuid!!
        val date = getLong(context!!,
                testInfo.uuid, R.string.calibrationDateKey)
        if (date > 0) {
            calibrationDetail.date = date
        }
        val expiry = getLong(context,
                testInfo.uuid, R.string.calibrationExpiryDateKey)
        if (expiry > 0) {
            calibrationDetail.expiry = expiry
        }
        val dao = db!!.calibrationDao()
        dao!!.insert(calibrationDetail)
        if (calibrations.size < 1) {
            try {
                calibrations = loadCalibrationFromFile(testInfo, "_AutoBackup")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            var colorFound = false
            for (calibration in calibrations) {
                if (calibration.color != 0) {
                    colorFound = true
                    break
                }
            }
            if (!colorFound) {
                try {
                    calibrations = loadCalibrationFromFile(testInfo, "_AutoBackup")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return calibrations
    }

    private fun convertRangePropertyToArray(testInfo: TestInfo) {
        // If colors are defined as comma delimited range values then create array
        try {
            if (testInfo.results!![0].colors.isEmpty()
                    && testInfo.ranges!!.isNotEmpty()) {
                val values = testInfo.ranges!!.split(",").toTypedArray()
                for (value in values) {
                    testInfo.results!![0].colors.add(ColorItem(value.toDouble()))
                }
            }
        } catch (ignored: NumberFormatException) {
            // do nothing
        }
    }

    fun clear() {
        testMap.clear()
    }

    companion object {
        private val testMap = HashMap<String, List<TestInfo>>()
    }

}