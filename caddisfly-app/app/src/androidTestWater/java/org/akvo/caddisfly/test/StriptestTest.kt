package org.akvo.caddisfly.test

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R.id
import org.akvo.caddisfly.R.string
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.activateTestMode
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
import org.akvo.caddisfly.util.TestUtil
import org.akvo.caddisfly.util.TestUtil.nextSurveyPage
import org.akvo.caddisfly.util.TestUtil.sleep
import org.akvo.caddisfly.util.clickStartButton
import org.akvo.caddisfly.util.mDevice
import org.hamcrest.Matchers
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StriptestTest {

    @Rule
    @JvmField
    var mActivityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        loadData(mActivityRule.activity, mCurrentLanguage)
        clearPreferences(mActivityRule)
//        resetLanguage();
    }

    @Test
    fun startStriptest() {
        activateTestMode(mActivityRule.activity)
        testArsenic()
        testArsenic2()
    }

    @After
    fun tearDown() {
        clearPreferences(mActivityRule)
    }

    private fun testArsenic() {
        gotoSurveyForm()
        nextSurveyPage(3, "Arsenic")
        clickExternalSourceButton(0)
        mDevice.waitForIdle()
        sleep(1000)
        onView(withText("Prepare for test")).perform(click())
        sleep(9000)

        for (i in 0..16) {
            try {
                TestUtil.nextPage()
            } catch (e: Exception) {
                sleep(200)
                break
            }
        }

        if (TestUtil.isEmulator) {
            onView(withText(string.camera_not_good))
                    .inRoot(RootMatchers.withDecorView(Matchers.not(Matchers.`is`(mActivityRule.activity.window
                            .decorView)))).check(matches(isDisplayed()))
            return
        }

        clickStartButton()
        sleep(5000)
        onView(withText(string.result)).check(matches(isDisplayed()))
        onView(withText("Arsenic")).check(matches(isDisplayed()))
        onView(withText(string.no_result)).check(matches(isDisplayed()))
        onView(withId(id.image_result)).check(matches(isDisplayed()))

        onView(
            Matchers.allOf(
                withId(id.buttonSubmitResult), withText(string.submit_result),
                isDisplayed())).perform(click())
    }

    private fun testArsenic2() {
        gotoSurveyForm()
        nextSurveyPage(3, "Arsenic")
        clickExternalSourceButton(2)
        mDevice.waitForIdle()
        sleep(1000)
        onView(withText("Prepare for test")).perform(click())
        sleep(9000)

        for (i in 0..16) {
            try {
                TestUtil.nextPage()
            } catch (e: Exception) {
                sleep(200)
                break
            }
        }

        if (TestUtil.isEmulator) {
            onView(withText(string.camera_not_good))
                    .inRoot(RootMatchers.withDecorView(Matchers.not(Matchers.`is`(mActivityRule.activity.window
                            .decorView)))).check(matches(isDisplayed()))
            return
        }

        clickStartButton()
        sleep(5000)
        onView(withText(string.result)).check(matches(isDisplayed()))
        onView(withText("Arsenic")).check(matches(isDisplayed()))
        onView(withText("No Result")).check(matches(isDisplayed()))
        onView(withId(id.image_result)).check(matches(isDisplayed()))

        onView(
            Matchers.allOf(
                withId(id.buttonSubmitResult), withText(string.submit_result),
                isDisplayed())).perform(click())

//        assertNotNull(mDevice.findObject(By.text("Unit: ")))
//        assertNotNull(mDevice.findObject(By.text("ug/l")))
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