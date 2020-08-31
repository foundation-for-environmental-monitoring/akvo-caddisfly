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
import androidx.test.rule.GrantPermissionRule
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.TestConstants.TEST_INDEX
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestUtil
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
                    ), TEST_INDEX
                ),
                isDisplayed()
            )
        )
        relativeLayout3.perform(click())

        if (TestUtil.isEmulator) {
//            onView(withText(R.string.error_camera_flash_required))
//                .inRoot(
//                    RootMatchers.withDecorView(
//                        Matchers.not(
//                            `is`(
//                                mActivityTestRule.activity.window
//                                    .decorView
//                            )
//                        )
//                    )
//                ).check(matches(isDisplayed()))
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
                childAtPosition(
                    allOf(
                        withClassName(`is`("com.android.internal.widget.ButtonBarLayout")),
                        childAtPosition(
                            withClassName(`is`("android.widget.LinearLayout")),
                            3
                        )
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        appCompatButton5.perform(click())

        val appCompatButton6 = onView(
            allOf(
                withId(android.R.id.button1), withText(R.string.save),
                childAtPosition(
                    allOf(
                        withClassName(`is`("com.android.internal.widget.ButtonBarLayout")),
                        childAtPosition(
                            withClassName(`is`("android.widget.LinearLayout")),
                            3
                        )
                    ),
                    3
                ),
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

        val relativeLayout4 = onView(
            allOf(
                childAtPosition(
                    allOf(
                        withId(R.id.list_types),
                        childAtPosition(
                            withClassName(`is`("android.widget.LinearLayout")),
                            0
                        )
                    ), TEST_INDEX
                ),
                isDisplayed()
            )
        )
        relativeLayout4.perform(click())

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
    }
}
