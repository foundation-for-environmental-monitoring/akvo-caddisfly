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

package org.akvo.caddisfly.diagnostic

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
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
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.RecyclerViewMatcher
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.goToMainScreen
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
import org.akvo.caddisfly.util.TestUtil
import org.akvo.caddisfly.util.mDevice
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class DiagnosticTest {
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
    fun testDiagnosticMode() {
        onView(withId(id.actionSettings)).perform(click())
        onView(withText(string.about)).check(matches(isDisplayed())).perform(click())
        for (i in 0..9) {
            onView(withId(id.textVersion)).perform(click())
        }
        goToMainScreen()
        onView(withId(id.fabDisableDiagnostics)).check(matches(isDisplayed()))
        goToMainScreen()
        onView(withText(string.calibrate)).perform(click())
        onView(RecyclerViewMatcher(id.list_types)
                .atPositionOnView(0, id.text_title))
                .check(matches(withText(TestConstants.CUVETTE_TEST_NAME_1)))
                .perform(click())

//        ViewInteraction recyclerView = onView(
//                allOf(withId(R.id.list_types),
//                        childAtPosition(
//                                withClassName(is("android.widget.LinearLayout")),
//                                0)));
//        recyclerView.perform(actionOnItemAtPosition(3, click()));


        mDevice.waitForIdle()

//        onView(withText(currentHashMap.get("fluoride"))).perform(scrollTo()).perform(click());

//        onView(withId(R.id.list_types)).perform(RecyclerViewActions
//                .actionOnItem(first(hasDescendant(withText(currentHashMap.get("fluoride")))), click()));


        if (TestUtil.isEmulator) {
            onView(withText(string.errorCameraFlashRequired))
                    .inRoot(withDecorView(not(`is`(mActivityRule.activity.window
                            .decorView)))).check(matches(isDisplayed()))
            return
        }
        onView(withId(id.actionSwatches)).perform(click())
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