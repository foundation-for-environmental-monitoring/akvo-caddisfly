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

package org.akvo.caddisfly.navigation

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.AmbiguousViewMatcherException
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import junit.framework.TestCase.fail
import org.akvo.caddisfly.R.id
import org.akvo.caddisfly.R.string
import org.akvo.caddisfly.common.TestConstants
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.enterDiagnosticMode
import org.akvo.caddisfly.util.TestHelper.getString
import org.akvo.caddisfly.util.TestHelper.goToMainScreen
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.saveCalibration
import org.akvo.caddisfly.util.TestUtil.childAtPosition
import org.akvo.caddisfly.util.TestUtil.isEmulator
import org.akvo.caddisfly.util.TestUtil.sleep
import org.akvo.caddisfly.util.mDevice
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.`object`.HasToString.hasToString
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.DecimalFormatSymbols

@RunWith(AndroidJUnit4::class)
@LargeTest
class SurveyTest {

    @get:Rule
    val mActivityRule = activityScenarioRule<MainActivity>()

    @Before
    fun setUp() {
        loadData(ApplicationProvider.getApplicationContext())
        TestHelper.clearPreferences()
    }

    @Test
    fun testChangeTestType() {
        goToMainScreen()
        onView(withText(string.settings)).perform(click())
        onView(withId(id.scrollViewSettings)).perform(ViewActions.swipeUp())
        onView(withText(string.calibrate)).perform(click())
        onView(
            allOf(
                withId(id.list_types),
                childAtPosition(
                    withClassName(`is`("android.widget.LinearLayout")),
                    0
                )
            )
        ).perform(
            actionOnItemAtPosition<ViewHolder?>(
                TestConstants.TEST_INDEX, click()
            )
        )
        if (isEmulator) {
//            onView(withText(string.errorCameraFlashRequired))
//                    .inRoot(withDecorView(not(`is`(mActivityRule.activity.window
//                            .decorView)))).check(matches(isDisplayed()))
            return
        }
//        val dfs =

        DecimalFormatSymbols()
        onView(
            allOf(
                withId(id.calibrationList),
                childAtPosition(
                    withClassName(`is`("android.widget.RelativeLayout")),
                    0
                )
            )
        ).perform(actionOnItemAtPosition<ViewHolder?>(4, click()))

//        onView(withText("0" + dfs.getDecimalSeparator() + "0 mg/l")).check(matches(isDisplayed()));

        Espresso.pressBack()
        Espresso.pressBack()
        Espresso.pressBack()
        onView(withText(string.settings)).perform(click())
        onView(withId(id.scrollViewSettings)).perform(ViewActions.swipeUp())
        onView(withText(string.calibrate)).perform(click())

        val text =
            "0 - 125 mg/kg (" + String.format(getString(string.up_to_with_dilution), "625+") + ")"

        onView(withText(text)).perform(click())
        onView(withText("15")).check(matches(isDisplayed()))
        try {
            onView(withText("mg/kg")).check(matches(isDisplayed()))
            fail("Multiple matches not found")
        } catch (e: AmbiguousViewMatcherException) {
            // multiple matches found
        }

        Espresso.pressBack()
        onView(withId(id.list_types)).perform(ViewActions.swipeUp())

        onView(withText("4 - 10 ")).perform(click())
        onView(withText("7")).check(matches(isDisplayed()))
    }

    @Test
    fun testStartASurvey() {
//        val screenshotName = TestConstants.CUVETTE_TEST_ID_1
//            .substring(TestConstants.CUVETTE_TEST_ID_1.lastIndexOf("-") + 1)

        saveCalibration("TestValid", TestConstants.CUVETTE_TEST_ID_1)
        onView(withText(string.settings)).perform(click())
        onView(withText(string.about)).check(matches(isDisplayed())).perform(click())
        enterDiagnosticMode()
        goToMainScreen()
        onView(withText(string.settings)).perform(click())
        onView(withId(id.scrollViewSettings)).perform(ViewActions.swipeUp())
        onView(withText(string.calibrate)).perform(click())
        onView(
            allOf(
                withId(id.list_types),
                childAtPosition(
                    withClassName(`is`("android.widget.LinearLayout")),
                    0
                )
            )
        ).perform(
            actionOnItemAtPosition<ViewHolder?>(
                TestConstants.TEST_INDEX, click()
            )
        )
        if (isEmulator) {
//            onView(withText(string.errorCameraFlashRequired))
//                    .inRoot(withDecorView(not(`is`(mActivityRule.activity.window
//                            .decorView)))).check(matches(isDisplayed()))
            return
        }
        onView(withId(id.menuLoad)).perform(click())
        sleep(1000)
        onData(hasToString(startsWith("TestValid"))).perform(click())
        goToMainScreen()
        gotoSurveyForm()
//        clickExternalSourceButton(TestConstants.CUVETTE_TEST_ID_1, screenshotName)
        onView(withId(id.button_prepare)).check(matches(isDisplayed()))
        onView(withId(id.button_prepare)).perform(click())
        onView(withId(id.buttonNoDilution)).check(matches(isDisplayed()))
        onView(withId(id.buttonDilution1)).check(matches(isDisplayed()))
        onView(withId(id.buttonDilution2)).check(matches(isDisplayed()))
        onView(withId(id.buttonNoDilution)).perform(click())

        //onView(withId(R.id.buttonStart)).perform(click());


        mDevice.waitForWindowUpdate("", 1000)
    }

    companion object {
        @JvmStatic
        @BeforeClass
        fun initialize() {
            if (!TestHelper.isDeviceInitialized()) {
                mDevice = UiDevice.getInstance(getInstrumentation())
            }
        }
    }
}