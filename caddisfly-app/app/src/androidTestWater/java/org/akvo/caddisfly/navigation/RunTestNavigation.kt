package org.akvo.caddisfly.navigation


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import org.akvo.caddisfly.R
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestUtil.childAtPosition
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class RunTestNavigation {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule<MainActivity>(MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            "android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE")

    @Before
    fun setUp() {
        TestHelper.loadData(mActivityTestRule.activity, TestHelper.mCurrentLanguage)
        TestHelper.clearPreferences(mActivityTestRule)
    }

    @Test
    fun runTestNavigation() {
        onView(allOf(withId(R.id.buttonRunTest), withText("Run Test"), isDisplayed()))
                .perform(click())

        val relativeLayout = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.list_types),
                                childAtPosition(
                                        withClassName(`is`("android.widget.LinearLayout")),
                                        0)),
                        6),
                        isDisplayed()))
        relativeLayout.perform(click())

        onView(allOf(withContentDescription("Navigate up"), isDisplayed())).perform(click())

        onView(allOf(withContentDescription("Navigate up"), isDisplayed())).perform(click())

        onView(allOf(withId(R.id.buttonCalibrate), withText("Calibrate"), isDisplayed()))
                .perform(click())

        val relativeLayout3 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.list_types),
                                childAtPosition(
                                        withClassName(`is`("android.widget.LinearLayout")),
                                        0)),
                        6),
                        isDisplayed()))
        relativeLayout3.perform(click())

        val floatingActionButton = onView(
                allOf(withId(R.id.fabEditCalibration),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_container),
                                        0),
                                1),
                        isDisplayed()))
        floatingActionButton.perform(click())

        val appCompatEditText = onView(
                allOf(withId(R.id.editExpiryDate),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.custom),
                                        0),
                                2),
                        isDisplayed()))
        appCompatEditText.perform(click())

        val appCompatButton5 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                allOf(withClassName(`is`("com.android.internal.widget.ButtonBarLayout")),
                                        childAtPosition(
                                                withClassName(`is`("android.widget.LinearLayout")),
                                                3)),
                                3),
                        isDisplayed()))
        appCompatButton5.perform(click())

        val appCompatButton6 = onView(
                allOf(withId(android.R.id.button1), withText("Save"),
                        childAtPosition(
                                allOf(withClassName(`is`("com.android.internal.widget.ButtonBarLayout")),
                                        childAtPosition(
                                                withClassName(`is`("android.widget.LinearLayout")),
                                                3)),
                                3),
                        isDisplayed()))
        appCompatButton6.perform(click())

        val appCompatImageButton2 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(`is`("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageButton2.perform(click())

        val appCompatImageButton3 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withId(R.id.mainLayout),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageButton3.perform(click())

        onView(allOf(withId(R.id.buttonRunTest), withText("Run Test"), isDisplayed()))
                .perform(click())

        val relativeLayout4 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.list_types),
                                childAtPosition(
                                        withClassName(`is`("android.widget.LinearLayout")),
                                        0)),
                        6),
                        isDisplayed()))
        relativeLayout4.perform(click())

        val appCompatButton8 = onView(
                allOf(withId(R.id.buttonNoDilution), withText("No Dilution"),
                        childAtPosition(
                                allOf(withId(R.id.layoutDilutions),
                                        childAtPosition(
                                                withId(R.id.fragment_container),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatButton8.perform(click())

        val appCompatImageButton4 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(`is`("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageButton4.perform(click())

        val appCompatImageButton5 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(`is`("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageButton5.perform(click())

        val appCompatImageButton6 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withId(R.id.mainLayout),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageButton6.perform(click())
    }
}
