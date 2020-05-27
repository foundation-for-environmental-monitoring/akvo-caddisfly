package org.akvo.caddisfly.diagnostic

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R.id
import org.akvo.caddisfly.R.string
import org.akvo.caddisfly.common.TestConstants
import org.akvo.caddisfly.common.TestConstants.CUVETTE_TEST_INDEX
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.RecyclerViewMatcher
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.getString
import org.akvo.caddisfly.util.TestHelper.goToMainScreen
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestUtil
import org.akvo.caddisfly.util.mDevice
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiagnosticTest {

    @get:Rule
    val mActivityRule = activityScenarioRule<MainActivity>()

    @Before
    fun setUp() {
        loadData(ApplicationProvider.getApplicationContext())
        TestHelper.clearPreferences()
    }

    @Test
    fun testDiagnosticMode() {
        goToMainScreen()
        onView(withText(string.settings)).perform(click())
        onView(withText(string.about)).check(matches(isDisplayed())).perform(click())
        for (i in 0..9) {
            onView(withId(id.textVersion)).perform(click())
        }
        goToMainScreen()
        onView(withText(string.settings)).perform(click())
        onView(withId(id.scrollViewSettings)).perform(ViewActions.swipeUp())
        onView(withText(string.calibrate)).perform(click())

        val testName = getString(
            ApplicationProvider.getApplicationContext(),
            TestConstants.CUVETTE_TEST_NAME_1
        )

        onView(
            RecyclerViewMatcher(id.list_types)
                .atPositionOnView(CUVETTE_TEST_INDEX, id.text_title)
        ).check(matches(withText(testName)))
            .perform(click())

        mDevice.waitForIdle()

        if (TestUtil.isEmulator) {
//            onView(withText(string.error_camera_flash_required))
//                    .inRoot(withDecorView(not(`is`(mActivityRule.activity.window
//                            .decorView)))).check(matches(isDisplayed()))
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