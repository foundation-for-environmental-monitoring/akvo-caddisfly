package org.akvo.caddisfly.navigation


import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.TestConstants.CALIBRATION_TEST_INDEX
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestUtil
import org.akvo.caddisfly.util.TestUtil.childAtPosition
import org.akvo.caddisfly.util.TestUtil.sleep
import org.akvo.caddisfly.util.TestUtil.swipeUp
import org.akvo.caddisfly.util.mDevice
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class RunTestNavigation {

    @Rule
    @JvmField
    var mGrantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        "android.permission.CAMERA",
        "android.permission.WRITE_EXTERNAL_STORAGE"
    )

    @get:Rule
    val mActivityRule = activityScenarioRule<MainActivity>()

    @Before
    fun setUp() {
        TestHelper.loadData(ApplicationProvider.getApplicationContext())
        TestHelper.clearPreferences()
    }

    @Test
    fun runTestNavigation() {
//        onView(allOf(withId(R.id.buttonRunTest), withText("Run Test"), isDisplayed()))
//                .perform(click())
//
//        val relativeLayout = onView(
//                allOf(childAtPosition(
//                        allOf(withId(R.id.list_types),
//                                childAtPosition(
//                                        withClassName(`is`("android.widget.LinearLayout")),
//                                        0)), TEST_INDEX),
//                        isDisplayed()))
//        relativeLayout.perform(click())
//
//        onView(allOf(withContentDescription("Navigate up"), isDisplayed())).perform(click())
//
//        onView(allOf(withContentDescription("Navigate up"), isDisplayed())).perform(click())

        onView(withText(R.string.settings)).perform(click())
        onView(withId(R.id.scrollViewSettings)).perform(ViewActions.swipeUp())
        onView(withText(R.string.calibrate)).check(matches(isDisplayed())).perform(click())

        val relativeLayout3 = onView(
            allOf(
                childAtPosition(
                    allOf(
                        withId(R.id.list_types),
                        childAtPosition(
                            withClassName(`is`("android.widget.LinearLayout")),
                            0
                        )
                    ), CALIBRATION_TEST_INDEX
                ),
                isDisplayed()
            )
        )
        relativeLayout3.perform(click())

        if (TestUtil.isEmulator) {
            return
        }

        val floatingActionButton = onView(
            allOf(
                withId(R.id.fabEditCalibration),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.fragment_container),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        floatingActionButton.perform(click())

        val appCompatEditText = onView(
            allOf(
                withId(R.id.editExpiryDate),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.custom),
                        0
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        appCompatEditText.perform(click())

        val appCompatButton5 = onView(
            allOf(
                withId(android.R.id.button1), withText(R.string.ok),
                isDisplayed()
            )
        )
        appCompatButton5.perform(click())

        val appCompatButton6 = onView(
            allOf(
                withId(android.R.id.button1), withText(R.string.save),
                isDisplayed()
            )
        )
        appCompatButton6.perform(click())

        val appCompatImageButton2 = onView(
            allOf(
                withContentDescription(R.string.navigate_up),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(`is`("android.widget.LinearLayout")),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatImageButton2.perform(click())

        onView(
            allOf(
                withContentDescription(R.string.navigate_up),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withId(R.id.mainLayout),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        ).perform(click())

        onView(allOf(withContentDescription(R.string.navigate_up), isDisplayed())).perform(click())

        onView(allOf(withId(R.id.buttonRunTest), withText(R.string.run_test), isDisplayed()))
            .perform(click())

        swipeUp()

        sleep(2000)

        onView(allOf(withText(R.string.fluoride), isDisplayed())).perform(click())

        val appCompatButton8 = onView(
            allOf(
                withId(R.id.buttonNoDilution), withText(R.string.no_dilution),
                childAtPosition(
                    allOf(
                        withId(R.id.layoutDilutions),
                        childAtPosition(
                            withId(R.id.fragment_container),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatButton8.perform(click())

        val appCompatImageButton4 = onView(
            allOf(
                withContentDescription(R.string.navigate_up),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(`is`("android.widget.LinearLayout")),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatImageButton4.perform(click())

        val appCompatImageButton5 = onView(
            allOf(
                withContentDescription(R.string.navigate_up),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(`is`("android.widget.LinearLayout")),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatImageButton5.perform(click())

        val appCompatImageButton6 = onView(
            allOf(
                withContentDescription(R.string.navigate_up),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withId(R.id.mainLayout),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatImageButton6.perform(click())
    }

    companion object {
        @JvmStatic
        @BeforeClass
        fun initialize() {
            if (!TestHelper.isDeviceInitialized()) {
                mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            }
        }
    }
}
