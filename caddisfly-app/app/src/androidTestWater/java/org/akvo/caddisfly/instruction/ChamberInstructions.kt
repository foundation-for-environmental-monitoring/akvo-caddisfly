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
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.RequiresDevice
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import junit.framework.TestCase.assertEquals
import org.akvo.caddisfly.R
import org.akvo.caddisfly.R.string
import org.akvo.caddisfly.common.Constants
import org.akvo.caddisfly.common.TestConstants
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.repository.TestConfigRepository
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.getString
import org.akvo.caddisfly.util.TestHelper.goToMainScreen
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
import org.akvo.caddisfly.util.TestHelper.takeScreenshot
import org.akvo.caddisfly.util.TestUtil.nextSurveyPage
import org.akvo.caddisfly.util.TestUtil.sleep
import org.akvo.caddisfly.util.mDevice
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.*
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ChamberInstructions {
    private val jsArrayString = StringBuilder()

    @Rule
    @JvmField
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
    @RequiresDevice
    fun testInstructionsBackcase() {
        goToMainScreen()
        gotoSurveyForm()
        clickExternalSourceButton(TestConstants.CUVETTE_TEST_ID_1)
        sleep(1000)
        mDevice.waitForIdle()
        sleep(1000)
        val id = TestConstants.CUVETTE_TEST_ID_1.substring(
                TestConstants.CUVETTE_TEST_ID_1.lastIndexOf("-") + 1
        )
        takeScreenshot(id, -1)
        mDevice.waitForIdle()
        onView(withText(getString(mActivityTestRule.activity, string.instructions))).perform(click())
        for (i in 0..16) {
            try {
                takeScreenshot(id, i)
                onView(withId(R.id.image_pageRight)).perform(click())
            } catch (e: Exception) {
                sleep(600)
                Espresso.pressBack()
                break
            }
        }
    }

    @Test
    @RequiresDevice
    fun testInstructionsBackcase2() {
        goToMainScreen()
        gotoSurveyForm()
        nextSurveyPage(4, "Water Tests 2")
        clickExternalSourceButton(0)
        sleep(1000)
        mDevice.waitForIdle()
        sleep(1000)
        val id = Constants.FREE_CHLORINE_ID.substring(
                Constants.FREE_CHLORINE_ID.lastIndexOf("-") + 1)
        takeScreenshot(id, -1)
        mDevice.waitForIdle()

//        onView(withText(R.string.cannotStartTest)).check(matches(isDisplayed()));


        onView(withText(getString(mActivityTestRule.activity, string.instructions))).perform(click())
        for (i in 0..16) {
            try {
                takeScreenshot(id, i)
                onView(withId(R.id.image_pageRight)).perform(click())
            } catch (e: Exception) {
                sleep(600)
                Espresso.pressBack()
                break
            }
        }
    }

    @Test
    @Ignore
    @RequiresDevice
    fun testInstructionsAll() {
        goToMainScreen()
        onView(withText(getString(mActivityTestRule.activity, string.stripTest))).perform(click())
        val testConfigRepository = TestConfigRepository()
        val testList: List<TestInfo> = testConfigRepository.getTests(TestType.STRIP_TEST)
        @Suppress("ConstantConditionIf")
        if (TestConstants.STRIP_TESTS_COUNT == 1) {
            checkInstructions(testList[0].uuid)
        }

//        Log.e("Caddisfly", jsArrayString.toString());
        else {
            for (i in 0 until TestConstants.STRIP_TESTS_COUNT) {
                assertEquals(testList[i].subtype, TestType.STRIP_TEST)
                var id: String = testList[i].uuid
                id = id.substring(id.lastIndexOf("-") + 1)
                val pages = navigateToTest(i, id)
                jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],")
            }
        }

    }

    private fun navigateToTest(index: Int, id: String): Int {
        val recyclerView: ViewInteraction = onView(allOf(withId(R.id.list_types), childAtPosition(withClassName(`is`("android.widget.LinearLayout")),
                0)))
        recyclerView.perform(actionOnItemAtPosition<ViewHolder?>(index, click()))
        mDevice.waitForIdle()
        return checkInstructions(id)
    }

    private fun checkInstructions(id: String): Int {
        sleep(1000)
        takeScreenshot(id, -1)
        mDevice.waitForIdle()
        onView(withText(getString(mActivityTestRule.activity, string.instructions))).perform(click())
        var pages = 0
        for (i in 0..16) {
            pages++
            try {
                takeScreenshot(id, i)
                onView(withId(R.id.image_pageRight)).perform(click())
            } catch (e: Exception) {
                sleep(600)
                Espresso.pressBack()
                Espresso.pressBack()
                sleep(600)
                break
            }
        }
        return pages
    }

    companion object {
        @JvmStatic
        @BeforeClass
        fun initialize() {
            if (!TestHelper.isDeviceInitialized()) {
                mDevice = UiDevice.getInstance(getInstrumentation())
            }
        }

        @Suppress("SameParameterValue")
        private fun childAtPosition(
                parentMatcher: Matcher<View>,
                position: Int): Matcher<View> {
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