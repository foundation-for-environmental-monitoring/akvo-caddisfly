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

package org.akvo.caddisfly.ui

import android.R
import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.widget.DatePicker
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
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
import org.akvo.caddisfly.common.TestConstants
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.currentHashMap
import org.akvo.caddisfly.util.TestHelper.enterDiagnosticMode
import org.akvo.caddisfly.util.TestHelper.goToMainScreen
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.leaveDiagnosticMode
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
import org.akvo.caddisfly.util.TestHelper.saveCalibration
import org.akvo.caddisfly.util.TestUtil.childAtPosition
import org.akvo.caddisfly.util.TestUtil.isEmulator
import org.akvo.caddisfly.util.TestUtil.sleep
import org.akvo.caddisfly.util.mDevice
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.Matchers.*
import org.hamcrest.`object`.HasToString.hasToString
import org.junit.*
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
@LargeTest
class CalibrationTest {
    @Rule
    var mActivityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        loadData(mActivityRule.activity, mCurrentLanguage)
        clearPreferences(mActivityRule)
//        resetLanguage();
    }

    @Test
    @RequiresDevice
    @Ignore
    fun testOutOfSequence() {
        saveCalibration("OutOfSequence", TestConstants.CUVETTE_TEST_ID_1)
        goToMainScreen()
        onView(withId(id.actionSettings)).perform(click())
        onView(withText(string.about)).check(matches(isDisplayed())).perform(click())
        enterDiagnosticMode()
        goToMainScreen()
        onView(withText(string.calibrate)).perform(click())
        sleep(4000)
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
        sleep(2000)
        onData(hasToString(startsWith("OutOfSequence"))).perform(click())
        sleep(2000)
        onView(withText(String.format("%s. %s", mActivityRule.activity.getString(string.calibrationIsInvalid),
                mActivityRule.activity.getString(string.tryRecalibrating)))).check(matches(isDisplayed()))
        onView(withId(id.menuLoad)).perform(click())
        sleep(2000)
        onData(hasToString(startsWith("TestValid"))).perform(click())
        sleep(2000)
        onView(withText(String.format("%s. %s", mActivityRule.activity.getString(string.calibrationIsInvalid),
                mActivityRule.activity.getString(string.tryRecalibrating)))).check(matches(not(isDisplayed())))
        sleep(2000)
        leaveDiagnosticMode()
        onView(withText(string.calibrate)).perform(click())
    }

    @Test
    @RequiresDevice
    fun testExpiryDate() {
        onView(withId(id.actionSettings)).perform(click())
        onView(withText(string.about)).check(matches(isDisplayed())).perform(click())
        enterDiagnosticMode()
        Espresso.pressBack()
        Espresso.pressBack()
        onView(withText(string.calibrate)).perform(click())
        sleep(500)
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
        sleep(500)
        onData(hasToString(startsWith("TestValid"))).perform(click())
        sleep(500)
        leaveDiagnosticMode()
        onView(withText(string.calibrate)).perform(click())
        onView(allOf(withId(id.list_types),
                childAtPosition(withClassName(`is`("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition<ViewHolder?>(
                TestConstants.TEST_INDEX, click()))
        onView(withId(id.fabEditCalibration)).perform(click())

//        onView(withId(R.id.editBatchCode))
//                .perform(clearText(), closeSoftKeyboard());
//
//        onView(withId(R.id.editBatchCode))
//                .perform(typeText("    "), closeSoftKeyboard());

//        onView(withText(R.string.save)).perform(click());


        onView(withId(id.editExpiryDate)).perform(click())
        val date: Calendar = Calendar.getInstance()
        date.add(Calendar.DATE, -1)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
                .perform(PickerActions.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                        date.get(Calendar.DATE)))
        onView(withId(R.id.button1)).perform(click())
        onView(withText(string.save)).perform(click())

//        onView(withId(R.id.editBatchCode))
//                .perform(typeText("TEST 123#*@!"), closeSoftKeyboard());
//
//        onView(withText(R.string.save)).perform(click());


        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            onView(withText(String.format("%s. %s", mActivityRule.activity.getString(string.expired),
                    mActivityRule.activity.getString(string.calibrateWithNewReagent))))
                    .check(matches(isDisplayed()))
        }
        onView(withId(id.fabEditCalibration)).perform(click())
        mDevice.pressBack()
        goToMainScreen()
        gotoSurveyForm()
        clickExternalSourceButton(TestConstants.CUVETTE_TEST_ID_1)
        sleep(500)
        onView(withId(id.button_prepare)).check(matches(isDisplayed()))
        onView(withId(id.button_prepare)).perform(click())
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            val message = String.format("%s%n%n%s",
                    mActivityRule.activity.getString(string.errorCalibrationExpired),
                    mActivityRule.activity.getString(string.orderFreshBatch))
            onView(withText(message)).check(matches(isDisplayed()))
            onView(withText(string.ok)).perform(click())
        }
        mActivityRule.launchActivity(Intent())
        onView(withText(string.calibrate)).perform(click())

//        onView(withText(currentHashMap.get(TestConstant.FLUORIDE))).perform(click());


        onView(allOf(withId(id.list_types),
                childAtPosition(withClassName(`is`("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition<ViewHolder?>(
                TestConstants.TEST_INDEX, click()))
        onView(withId(id.fabEditCalibration)).perform(click())

//        onView(withId(R.id.editBatchCode))
//                .perform(typeText("NEW BATCH"), closeSoftKeyboard());


        onView(withId(id.editExpiryDate)).perform(click())
        date.add(Calendar.DATE, 364)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
                .perform(PickerActions.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                        date.get(Calendar.DATE)))
        onView(withId(R.id.button1)).perform(click())
        onView(withText(string.save)).perform(click())
        onView(withId(id.textCalibrationError)).check(matches(not(isDisplayed())))
        goToMainScreen()
        gotoSurveyForm()
        clickExternalSourceButton(TestConstants.CUVETTE_TEST_ID_1)
        sleep(500)
        onView(withId(id.button_prepare)).check(matches(isDisplayed()))
        onView(withId(id.button_prepare)).perform(click())
        onView(withId(id.buttonNoDilution)).check(matches(isDisplayed()))
    }

    //@Test
    fun testIncompleteCalibration() {
        gotoSurveyForm()
        clickExternalSourceButton(0)
        mDevice.waitForWindowUpdate("", 2000)
        onView(withText(string.cannotStartTest)).check(matches(isDisplayed()))
        var message = mActivityRule.activity.getString(string.errorCalibrationIncomplete,
                currentHashMap["chlorine"])
        message = String.format("%s%n%n%s", message,
                mActivityRule.activity.getString(string.doYouWantToCalibrate))
        onView(withText(message)).check(matches(isDisplayed()))
        onView(withText(string.cancel)).check(matches(isDisplayed()))
        onView(withText(string.calibrate)).check(matches(isDisplayed()))
        onView(withId(R.id.button2)).perform(click())
    }

    companion object {
        @BeforeClass
        fun initialize() {
            if (!TestHelper.isDeviceInitialized()) {
                mDevice = UiDevice.getInstance(getInstrumentation())
            }
        }
    }
}