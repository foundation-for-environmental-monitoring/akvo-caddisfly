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

package org.akvo.caddisfly.instruction

import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.LinearLayout
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R.id
import org.akvo.caddisfly.R.string
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.goToMainScreen
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
import org.akvo.caddisfly.util.TestUtil.nextPage
import org.akvo.caddisfly.util.TestUtil.nextSurveyPage
import org.akvo.caddisfly.util.TestUtil.sleep
import org.akvo.caddisfly.util.mDevice
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class StriptestInstructions {
    @Rule
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        loadData(mActivityTestRule.activity, mCurrentLanguage)

//        SharedPreferences prefs =
//                PreferenceManager.getDefaultSharedPreferences(mActivityTestRule.getActivity());
//        prefs.edit().clear().apply();

//        resetLanguage();

    }

    @Test
    fun arsenicStripTestInstructions() {
        goToMainScreen()
        gotoSurveyForm()
        nextSurveyPage(3, "Arsenic")
        clickExternalSourceButton(0)
        sleep(1000)
        mDevice.waitForIdle()
        sleep(1000)
        onView(withText("Arsenic (0 - 500)"))
                .check(matches(isDisplayed()))
        val appCompatButton2: ViewInteraction = onView(allOf(withId(id.button_instructions), withText(string.instructions), childAtPosition(childAtPosition(withClassName(`is`("android.widget.LinearLayout")),
                1),
                1), isDisplayed()))
        appCompatButton2.perform(click())
        onView(withText(string.as_safety))
                .check(matches(isDisplayed()))
        onView(withText("Arsenic (0 - 500)"))
                .check(matches(isDisplayed()))
        nextPage()
        onView(withText(string.as_ins_1))
                .check(matches(isDisplayed()))
        onView(withId(id.pager_indicator)).check(matches(isDisplayed()))
        onView(withId(id.viewPager))
                .perform(swipeLeft())
        val appCompatImageButton: ViewInteraction = onView(allOf(withContentDescription(string.navigate_up), withParent(withId(id.toolbar)), isDisplayed()))
        appCompatImageButton.perform(click())

//        ViewInteraction imageView2 = onView(
//                allOf(withId(R.id.imageBrand),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.coordinatorLayout),
//                                        0),
//                                1),
//                        isDisplayed()));
//        imageView2.check(matches(isDisplayed()));


        val button1: ViewInteraction = onView(allOf(withId(id.button_prepare), childAtPosition(childAtPosition(
                IsInstanceOf.instanceOf(LinearLayout::class.java),
                1),
                0), isDisplayed()))
        button1.check(matches(isDisplayed()))
        val appCompatButton3: ViewInteraction = onView(allOf(withId(id.button_instructions), withText(string.instructions), childAtPosition(childAtPosition(withClassName(`is`("android.widget.LinearLayout")),
                1),
                1), isDisplayed()))
        appCompatButton3.perform(click())
        pressBack()

//        ViewInteraction imageView3 = onView(
//                allOf(withId(R.id.imageBrand),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.coordinatorLayout),
//                                        0),
//                                1),
//                        isDisplayed()));
//        imageView3.check(matches(isDisplayed()));


        val button2: ViewInteraction = onView(allOf(withId(id.button_prepare), childAtPosition(childAtPosition(
                IsInstanceOf.instanceOf(LinearLayout::class.java),
                1),
                0), isDisplayed()))
        button2.check(matches(isDisplayed()))
        onView(allOf(withContentDescription(string.navigate_up), withParent(withId(id.toolbar)), isDisplayed())).perform(click())
    }

    companion object {
        @JvmStatic
        @BeforeClass
        fun initialize() {
            if (!TestHelper.isDeviceInitialized()) {
                mDevice = UiDevice.getInstance(getInstrumentation())
            }
        }

        private fun childAtPosition(
                parentMatcher: Matcher<View>, position: Int): Matcher<View> {
            return object : TypeSafeMatcher<View>() {
                override fun describeTo(description: Description) {
                    description.appendText("Child at position $position in parent ")
                    parentMatcher.describeTo(description)
                }

                override fun matchesSafely(view: View): Boolean {
                    val parent: ViewParent? = view.parent
                    return (parent is ViewGroup && parentMatcher.matches(parent)
                            && view == parent.getChildAt(position))
                }
            }
        }
    }
}