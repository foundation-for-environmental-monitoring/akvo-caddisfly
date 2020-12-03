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

package org.akvo.caddisfly.misc

import android.os.RemoteException
import android.widget.DatePicker
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R.id
import org.akvo.caddisfly.R.string
import org.akvo.caddisfly.common.TestConstants
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.enterDiagnosticMode
import org.akvo.caddisfly.util.TestHelper.goToMainScreen
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.takeScreenshot
import org.akvo.caddisfly.util.TestUtil.childAtPosition
import org.akvo.caddisfly.util.TestUtil.clickListViewItem
import org.akvo.caddisfly.util.TestUtil.isEmulator
import org.akvo.caddisfly.util.TestUtil.sleep
import org.akvo.caddisfly.util.mDevice
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
@LargeTest
class MiscTest {

    @get:Rule
    val mActivityRule = activityScenarioRule<MainActivity>()

    @Before
    fun setUp() {
        loadData(ApplicationProvider.getApplicationContext())
        TestHelper.clearPreferences()
    }

    @Test
    fun testSoftwareNotices() {
        onView(withText(string.settings)).perform(click())
        onView(withText(string.about)).check(matches(isDisplayed())).perform(click())
        onView(withText(string.legal_information)).check(matches(isDisplayed())).perform(click())
        Espresso.pressBack()
        Espresso.pressBack()
    }

    @Test
    fun testSwatches() {
        onView(withText(string.settings)).perform(click())
        onView(withText(string.about)).check(matches(isDisplayed())).perform(click())
        enterDiagnosticMode()
        goToMainScreen()
        onView(withText(string.settings)).perform(click())
        onView(withId(id.scrollViewSettings)).perform(ViewActions.swipeUp())
        onView(withText(string.calibrate)).perform(click())
        val recyclerView: ViewInteraction = onView(
            allOf(
                withId(id.list_types),
                childAtPosition(
                    withClassName(`is`("android.widget.LinearLayout")),
                    0
                )
            )
        )
        recyclerView.perform(actionOnItemAtPosition<ViewHolder?>(3, click()))

//        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        if (isEmulator) {
//            onView(withText(string.error_camera_flash_required))
//                .inRoot(
//                    withDecorView(
//                        not(
//                            `is`(
//                                mActivityRule.activity.window
//                                    .decorView
//                            )
//                        )
//                    )
//                ).check(matches(isDisplayed()))
            return
        }
        onView(withId(id.actionSwatches)).perform(click())
        Espresso.pressBack()
        onView(withId(id.actionSwatches)).check(matches(isDisplayed()))
        Espresso.pressBack()
    }

    @Test
    fun testRestartAppDuringAnalysis() {
        onView(withText(string.settings)).perform(click())
        onView(withId(id.scrollViewSettings)).perform(ViewActions.swipeUp())
        onView(withText(string.calibrate)).perform(click())
        onView(withId(id.list_types)).perform(
            actionOnItemAtPosition<ViewHolder?>(
                TestConstants.TEST_INDEX, click()
            )
        )
        if (isEmulator) {
//            onView(withText(string.error_camera_flash_required))
//                .inRoot(
//                    withDecorView(
//                        not(
//                            `is`(
//                                mActivityRule.activity.window
//                                    .decorView
//                            )
//                        )
//                    )
//                ).check(matches(isDisplayed()))
            return
        }

//        DecimalFormatSymbols dfs = new DecimalFormatSymbols();

        onView(withId(id.fabEditCalibration)).perform(click())

//        onView(withId(R.id.editBatchCode))
//                .perform(typeText("TEST 123#*@!"), closeSoftKeyboard());

        onView(withId(id.editExpiryDate)).perform(click())
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(2025, 8, 25))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withText(string.save)).perform(click())
        onView(
            allOf(
                withId(id.calibrationList), childAtPosition(
                    withClassName(`is`("android.widget.RelativeLayout")),
                    0
                )
            )
        ).perform(actionOnItemAtPosition<ViewHolder?>(4, click()))

//        onView(withText("2" + dfs.getDecimalSeparator() + "0 mg/l")).perform(click());

        //onView(withId(R.id.buttonStart)).perform(click());

        mDevice.pressHome()
        try {
            mDevice.pressRecentApps()
        } catch (e: RemoteException) {
            Timber.e(e)
        }
        sleep(2000)
        mDevice.click(mDevice.displayWidth / 2, mDevice.displayHeight / 2 + 300)
        mDevice.click(mDevice.displayWidth / 2, mDevice.displayHeight / 2 + 300)
        mDevice.click(mDevice.displayWidth / 2, mDevice.displayHeight / 2 + 300)
        mDevice.waitForWindowUpdate("", 1000)

        //clickListViewItem("Automated Tests");

        clickListViewItem("test caddisfly")
    }

    @Test
    fun testNoFlash() {
        goToMainScreen()

        //Main Screen
        takeScreenshot()
        onView(withText(string.settings)).perform(click())

        //Settings Screen
        takeScreenshot()
        onView(withText(string.about)).check(matches(isDisplayed())).perform(click())
        mDevice.waitForWindowUpdate("", 1000)

        //About Screen
        takeScreenshot()
        Espresso.pressBack()

        onView(withText(string.about)).check(matches(isDisplayed())).perform(click())
        enterDiagnosticMode()
        goToMainScreen()
        onView(withText(string.settings)).perform(click())

        onView(withId(id.scrollViewSettings)).perform(ViewActions.swipeUp())

        onView(withText(string.calibrate)).check(matches(isDisplayed())).perform(click())

        sleep(4000)
        onView(
            allOf(
                withId(id.list_types), childAtPosition(
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
            onView(withText(string.error_camera_flash_required)).perform(click())
        }
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