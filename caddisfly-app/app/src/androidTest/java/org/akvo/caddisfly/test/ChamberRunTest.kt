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

package org.akvo.caddisfly.test

import android.widget.DatePicker
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.RequiresDevice
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R.id
import org.akvo.caddisfly.R.string
import org.akvo.caddisfly.app.CaddisflyApp
import org.akvo.caddisfly.common.ChamberTestConfig
import org.akvo.caddisfly.common.ChamberTestConfig.DELAY_BETWEEN_SAMPLING
import org.akvo.caddisfly.common.ChamberTestConfig.SKIP_SAMPLING_COUNT
import org.akvo.caddisfly.common.Constants
import org.akvo.caddisfly.common.TestConstants
import org.akvo.caddisfly.common.TestConstants.CUVETTE_TEST_TIME_DELAY
import org.akvo.caddisfly.common.TestConstants.DELAY_EXTRA
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.enterDiagnosticMode
import org.akvo.caddisfly.util.TestHelper.goToMainScreen
import org.akvo.caddisfly.util.TestHelper.leaveDiagnosticMode
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
import org.akvo.caddisfly.util.TestHelper.saveCalibration
import org.akvo.caddisfly.util.TestHelper.takeScreenshot
import org.akvo.caddisfly.util.TestUtil.childAtPosition
import org.akvo.caddisfly.util.TestUtil.clickListViewItem
import org.akvo.caddisfly.util.TestUtil.getText
import org.akvo.caddisfly.util.TestUtil.isEmulator
import org.akvo.caddisfly.util.TestUtil.sleep
import org.akvo.caddisfly.util.mDevice
import org.hamcrest.Matchers.*
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@LargeTest
@RunWith(AndroidJUnit4::class)
class ChamberRunTest {
    @JvmField
    @Rule
    var mActivityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        loadData(mActivityRule.activity, mCurrentLanguage)
        TestHelper.clearPreferences(mActivityRule)
//        resetLanguage();
    }

    @Test
    @RequiresDevice
    fun testFreeChlorine() {
        saveCalibration("TestValidChlorine", Constants.FREE_CHLORINE_ID_2)
        onView(withText(string.settings)).perform(click())
        onView(withText(string.about)).check(matches(isDisplayed()))
    }

    @Test
    @RequiresDevice
    fun testStartHighLevelTest() {
        saveCalibration("HighLevelTest", TestConstants.CUVETTE_TEST_ID_1)
        onView(withText(string.settings)).perform(click())
        onView(withText(string.about)).check(matches(isDisplayed())).perform(click())
        val version: String? = CaddisflyApp.getAppVersion(false)
        onView(withText(version)).check(matches(isDisplayed()))
        enterDiagnosticMode()
        goToMainScreen()
        try {
            onView(withText(string.calibrate)).perform(click())
        } catch (e: Exception) {
            onView(withText(string.waterCalibrate)).perform(click())
        }
        onView(allOf(withId(id.list_types),
                childAtPosition(withClassName(`is`("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition<ViewHolder?>(
                TestConstants.TEST_INDEX, click()))
        if (isEmulator) {
            onView(withText(string.errorCameraFlashRequired))
                    .inRoot(withDecorView(not(`is`(mActivityRule.activity.window
                            .decorView)))).check(matches(isDisplayed()))
            return
        }
        onView(withId(id.menuLoad)).perform(click())
        sleep(1000)
        clickListViewItem("HighLevelTest")
        sleep(1000)
        goToMainScreen()
        onView(withText(string.settings)).perform(click())
        leaveDiagnosticMode()
        goToMainScreen()
        try {
            onView(withText(string.calibrate)).perform(click())
        } catch (e: Exception) {
            onView(withText(string.waterCalibrate)).perform(click())
        }
        onView(allOf(withId(id.list_types),
                childAtPosition(withClassName(`is`("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition<ViewHolder?>(
                TestConstants.TEST_INDEX, click()))
        onView(withId(id.fabEditCalibration)).perform(click())

        onView(withId(id.editExpiryDate)).perform(click())
        val date: Calendar = Calendar.getInstance()
        date.add(Calendar.MONTH, 2)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
                .perform(PickerActions.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                        date.get(Calendar.DATE)))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withText(string.save)).perform(click())
        val recyclerView2: ViewInteraction = onView(
                allOf(withId(id.calibrationList),
                        childAtPosition(withClassName(`is`("android.widget.RelativeLayout")),
                                0)))
        recyclerView2.perform(actionOnItemAtPosition<ViewHolder?>(4, click()))
        onView(withId(id.layoutWait)).check(matches(isDisplayed()))
        sleep((TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY + DELAY_EXTRA
                + DELAY_BETWEEN_SAMPLING * (ChamberTestConfig.SAMPLING_COUNT_DEFAULT + SKIP_SAMPLING_COUNT))
                * 1000)
        onView(withId(id.buttonOk)).perform(click())
        goToMainScreen()

        onView(withText(string.runTest)).perform(click())
        onView(allOf(withId(id.list_types),
                childAtPosition(withClassName(`is`("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition<ViewHolder?>(
                TestConstants.TEST_INDEX, click()))

        sleep(1000)
        onView(withId(id.buttonNoDilution)).check(matches(isDisplayed()))
        onView(withId(id.buttonNoDilution)).perform(click())
        onView(allOf(withId(id.textDilution), withText(string.noDilution)))
                .check(matches(isCompletelyDisplayed()))
        onView(allOf(withId(id.textDilution), withText(string.noDilution)))
                .check(matches(isCompletelyDisplayed()))
        onView(withId(id.layoutWait)).check(matches(isDisplayed()))
        sleep((TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY + DELAY_EXTRA
                + DELAY_BETWEEN_SAMPLING * (ChamberTestConfig.SAMPLING_COUNT_DEFAULT + SKIP_SAMPLING_COUNT))
                * 1000)
        onView(withId(id.buttonAccept)).perform(click())

        goToMainScreen()
        onView(withText(string.runTest)).perform(click())
        onView(allOf(withId(id.list_types),
                childAtPosition(withClassName(`is`("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition<ViewHolder?>(
                TestConstants.TEST_INDEX, click()))

        onView(withId(id.buttonDilution1)).check(matches(isDisplayed()))
        onView(withId(id.buttonDilution1)).perform(click())
        onView(allOf(withId(id.textDilution), withText(String.format(mActivityRule.activity
                .getString(string.timesDilution), 2))))
                .check(matches(isCompletelyDisplayed()))

        takeScreenshot()
        onView(withId(id.layoutWait)).check(matches(isDisplayed()))
        sleep((TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY + DELAY_EXTRA
                + DELAY_BETWEEN_SAMPLING * (ChamberTestConfig.SAMPLING_COUNT_DEFAULT + SKIP_SAMPLING_COUNT))
                * 1000)
        onView(withText(mActivityRule.activity.getString(string.testWithDilution)))
                .check(matches(isDisplayed()))

        //High levels found dialog


        takeScreenshot()
        onView(withId(id.buttonAccept)).perform(click())

        goToMainScreen()
        onView(withText(string.runTest)).perform(click())
        onView(allOf(withId(id.list_types),
                childAtPosition(withClassName(`is`("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition<ViewHolder?>(
                TestConstants.TEST_INDEX, click()))

        onView(withId(id.buttonDilution2)).check(matches(isDisplayed()))
        onView(withId(id.buttonDilution2)).perform(click())
        onView(allOf(withId(id.textDilution), withText(String.format(mActivityRule.activity
                .getString(string.timesDilution), 5)))).check(matches(isCompletelyDisplayed()))

        takeScreenshot()
        onView(withId(id.layoutWait)).check(matches(isDisplayed()))
        sleep((TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY + DELAY_EXTRA
                + DELAY_BETWEEN_SAMPLING * (ChamberTestConfig.SAMPLING_COUNT_DEFAULT + SKIP_SAMPLING_COUNT))
                * 1000)
        val resultString = getText(withId(id.textResult))
        assertTrue(resultString!!.contains(">"))
        @Suppress("ConstantConditionIf")
        if (CUVETTE_TEST_TIME_DELAY > 0) {
            val result: Double = (resultString.replace(">", "").trim { it <= ' ' }).toDouble()
            assertTrue("Result is wrong", result > 49)
            onView(withText(mActivityRule.activity.getString(string.testWithDilution)))
                    .check(matches(isDisplayed()))
        } else {
            val result: Double = (resultString.replace(">", "").trim { it <= ' ' }).toDouble()
            assertTrue("Result is wrong", result > 9)
            onView(withText(mActivityRule.activity.getString(string.testWithDilution)))
                    .check(matches(not(isDisplayed())))
        }
        onView(withId(id.buttonAccept)).perform(click())
        mDevice.waitForIdle()
    }

    @Test
    @RequiresDevice
    fun testStartNoDilutionTest() {
        saveCalibration("TestValid", TestConstants.CUVETTE_TEST_ID_1)
        onView(withText(string.settings)).perform(click())
        onView(withText(string.about)).check(matches(isDisplayed())).perform(click())
        val version: String? = CaddisflyApp.getAppVersion(false)
        onView(withText(version)).check(matches(isDisplayed()))
        enterDiagnosticMode()
        goToMainScreen()
        try {
            onView(withText(string.calibrate)).perform(click())
        } catch (e: Exception) {
            onView(withText(string.waterCalibrate)).perform(click())
        }
        onView(allOf(withId(id.list_types),
                childAtPosition(withClassName(`is`("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition<ViewHolder?>(
                TestConstants.TEST_INDEX, click()))
        if (isEmulator) {
            onView(withText(string.errorCameraFlashRequired))
                    .inRoot(withDecorView(not(`is`(mActivityRule.activity.window
                            .decorView)))).check(matches(isDisplayed()))
            return
        }
        onView(withId(id.menuLoad)).perform(click())
        sleep(1000)
        onData(hasToString(startsWith("TestValid"))).perform(click())
        goToMainScreen()
        onView(withText(string.settings)).perform(click())
        leaveDiagnosticMode()
        goToMainScreen()
        try {
            onView(withText(string.calibrate)).perform(click())
        } catch (e: Exception) {
            onView(withText(string.waterCalibrate)).perform(click())
        }
        onView(allOf(withId(id.list_types),
                childAtPosition(withClassName(`is`("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition<ViewHolder?>(
                TestConstants.TEST_INDEX, click()))
        onView(withId(id.fabEditCalibration)).perform(click())

        onView(withId(id.editExpiryDate)).perform(click())
        val date: Calendar = Calendar.getInstance()
        date.add(Calendar.MONTH, 2)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
                .perform(PickerActions.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                        date.get(Calendar.DATE)))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withText(string.save)).perform(click())
        val recyclerView2: ViewInteraction = onView(
                allOf(withId(id.calibrationList),
                        childAtPosition(withClassName(`is`("android.widget.RelativeLayout")),
                                0)))
        recyclerView2.perform(actionOnItemAtPosition<ViewHolder?>(2, click()))
        sleep((TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY + DELAY_EXTRA
                + DELAY_BETWEEN_SAMPLING * (ChamberTestConfig.SAMPLING_COUNT_DEFAULT + SKIP_SAMPLING_COUNT))
                * 1000)
        onView(withId(id.buttonOk)).perform(click())
        goToMainScreen()

        sleep(1000)

        goToMainScreen()
        onView(withText(string.runTest)).perform(click())
        onView(allOf(withId(id.list_types),
                childAtPosition(withClassName(`is`("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition<ViewHolder?>(
                TestConstants.TEST_INDEX, click()))

        sleep(1000)
        onView(withId(id.buttonNoDilution)).check(matches(isDisplayed()))
        onView(withId(id.buttonNoDilution)).perform(click())
        sleep((TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY + DELAY_EXTRA
                + DELAY_BETWEEN_SAMPLING * (ChamberTestConfig.SAMPLING_COUNT_DEFAULT + SKIP_SAMPLING_COUNT))
                * 1000)

        takeScreenshot()
//        val resultString = getText(withId(id.textResult))
        onView(withId(id.buttonAccept)).perform(click())
        mDevice.waitForIdle()
//        onView(withId(android.R.id.list)).check(matches(withChildCount(is(greaterThan(0)))));
//        onView(withText(R.string.startTestConfirm)).check(matches(isDisplayed()));
    }

    companion object {
        private const val TEST_START_DELAY = 24
        @JvmStatic
        @BeforeClass
        fun initialize() {
            if (!TestHelper.isDeviceInitialized()) {
                mDevice = UiDevice.getInstance(getInstrumentation())
            }
        }
    }
}