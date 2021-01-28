package org.akvo.caddisfly.test

import android.content.SharedPreferences
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import junit.framework.TestCase.assertNotNull
import org.akvo.caddisfly.R.id
import org.akvo.caddisfly.R.string
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.repository.TestConfigRepository
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.getString
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.startSurveyApp
import org.akvo.caddisfly.util.TestHelper.startSurveyForm
import org.akvo.caddisfly.util.TestHelper.takeScreenshot
import org.akvo.caddisfly.util.TestUtil.childAtPosition
import org.akvo.caddisfly.util.TestUtil.nextSurveyPage
import org.akvo.caddisfly.util.TestUtil.sleep
import org.akvo.caddisfly.util.mDevice
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TitrationTest {

    @get:Rule
    val mActivityRule = activityScenarioRule<MainActivity>()

    @Before
    fun setUp() {
        loadData(ApplicationProvider.getApplicationContext())
        val prefs: SharedPreferences =
            getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
        prefs.edit().clear().apply()
    }

    @Test
    fun runCarbonateTitrationTest() {
        val testConfigRepository = TestConfigRepository()
        val testList = testConfigRepository.getTests(TestType.TITRATION)
        val uuid = testList[2].uuid
        val screenshotName = uuid.substring(uuid.lastIndexOf("-") + 1)

        startSurveyApp()

        takeScreenshot(screenshotName)

        startSurveyForm()

        nextSurveyPage(5, getString(string.titration))

        takeScreenshot(screenshotName)

        clickExternalSourceButton(0)

        takeScreenshot(screenshotName)

        onView(withText(string.next)).perform(click())

        takeScreenshot(screenshotName)

        onView(withId(id.editTitration1)).perform(pressImeActionButton())

        sleep(1000)

        onView(withId(id.editTitration1)).check(matches(isDisplayed()))
            .perform(replaceText("12"), closeSoftKeyboard())

        takeScreenshot(screenshotName)

        onView(allOf(withId(id.editTitration1), withText("12"), isDisplayed()))
            .perform(pressImeActionButton())

        sleep(1000)

        onView(withId(id.buttonAccept)).perform(click())

        assertNotNull(mDevice.findObject(By.text(getString(string.carbonate) + ": ")))
        assertNotNull(mDevice.findObject(By.text("300.00")))

        takeScreenshot(screenshotName)

        mDevice.waitForIdle()

        mDevice.pressBack()

        mDevice.pressBack()
    }

    @Test
    fun runCalciumTitrationTest() {
        val testConfigRepository = TestConfigRepository()
        val testList = testConfigRepository.getTests(TestType.TITRATION)
        val uuid = testList[1].uuid
        val screenshotName = uuid.substring(uuid.lastIndexOf("-") + 1)
        mDevice.waitForIdle()

        sleep(2000)
        startSurveyApp()
        sleep(2000)
        takeScreenshot(screenshotName)
        startSurveyForm()
        sleep(2000)
        nextSurveyPage(5, getString(string.titration))

        takeScreenshot(screenshotName)
        clickExternalSourceButton(1)
        sleep(2000)
        takeScreenshot(screenshotName)
        onView(withText(string.next)).perform(click())
        takeScreenshot(screenshotName)

        onView(withId(id.editTitration1)).check(matches(isDisplayed()))
            .perform(replaceText("123"), closeSoftKeyboard())
        onView(withId(id.editTitration2)).check(matches(isDisplayed()))
            .perform(replaceText("12"), closeSoftKeyboard())
        onView(
            allOf(
                withId(id.editTitration2), withText("12"),
                childAtPosition(
                    childAtPosition(withId(id.fragment_container), 0),
                    4
                ), isDisplayed()
            )
        ).perform(pressImeActionButton())

        sleep(1000)

        onView(withId(id.editTitration1)).check(matches(isDisplayed()))
            .perform(replaceText("12"), closeSoftKeyboard())
        onView(withId(id.editTitration2)).check(matches(isDisplayed()))
            .perform(replaceText("20"), closeSoftKeyboard())

        sleep(1000)
        takeScreenshot(screenshotName)
        onView(
            allOf(
                withId(id.editTitration2), withText("20"),
                childAtPosition(
                    childAtPosition(withId(id.fragment_container), 0),
                    4
                ), isDisplayed()
            )
        ).perform(pressImeActionButton())

        onView(withId(id.buttonAccept)).perform(click())

        sleep(1000)

        assertNotNull(mDevice.findObject(By.text(getString(string.calcium) + ": ")))
        assertNotNull(mDevice.findObject(By.text("100.00")))
        assertNotNull(mDevice.findObject(By.text(getString(string.magnesium) + ": ")))
        assertNotNull(mDevice.findObject(By.text("40.00")))

        takeScreenshot(screenshotName)

        mDevice.waitForIdle()

        mDevice.pressBack()

        mDevice.pressBack()
    }

    @Test
    fun runTotalHardness() {
        val testConfigRepository = TestConfigRepository()
        val testList = testConfigRepository.getTests(TestType.TITRATION)
        val uuid = testList[1].uuid
        val screenshotName = uuid.substring(uuid.lastIndexOf("-") + 1)
        mDevice.waitForIdle()

        sleep(2000)
        startSurveyApp()
        sleep(2000)
        takeScreenshot(screenshotName)
        startSurveyForm()
        sleep(2000)
        nextSurveyPage(5, getString(string.titration) + " 2")

        takeScreenshot(screenshotName)
        clickExternalSourceButton(0)
        sleep(2000)
        takeScreenshot(screenshotName)
        onView(withText(string.next)).perform(click())
        takeScreenshot(screenshotName)

        onView(withId(id.editTitration1)).check(matches(isDisplayed()))
            .perform(replaceText("123"), closeSoftKeyboard())
        onView(withId(id.editTitration1)).perform(pressImeActionButton())

        sleep(1000)

        takeScreenshot(screenshotName)

        onView(withId(id.buttonAccept)).perform(click())

        sleep(1000)

        assertNotNull(mDevice.findObject(By.text(getString(string.total_hardness) + ": ")))
        assertNotNull(mDevice.findObject(By.text("3075.00")))

        takeScreenshot(screenshotName)

        mDevice.waitForIdle()

        mDevice.pressBack()

        mDevice.pressBack()
    }

    @Test
    fun runTotalAlkalinity() {
        val testConfigRepository = TestConfigRepository()
        val testList = testConfigRepository.getTests(TestType.TITRATION)
        val uuid = testList[1].uuid
        val screenshotName = uuid.substring(uuid.lastIndexOf("-") + 1)
        mDevice.waitForIdle()

        sleep(2000)
        startSurveyApp()
        sleep(2000)
        takeScreenshot(screenshotName)
        startSurveyForm()
        sleep(2000)
        nextSurveyPage(
            5, getString(
                string.water_tests
            )
        )

        takeScreenshot(screenshotName)
        clickExternalSourceButton(1)
        sleep(2000)
        takeScreenshot(screenshotName)
        onView(withText(string.next)).perform(click())
        takeScreenshot(screenshotName)

        onView(withId(id.editTitration1)).check(matches(isDisplayed()))
            .perform(replaceText("123"), closeSoftKeyboard())
        onView(withId(id.editTitration2)).check(matches(isDisplayed()))
            .perform(replaceText("12"), closeSoftKeyboard())
        onView(
            allOf(
                withId(id.editTitration2), withText("12"),
                childAtPosition(
                    childAtPosition(withId(id.fragment_container), 0),
                    4
                ), isDisplayed()
            )
        ).perform(pressImeActionButton())

        val error = String.format(
            getString(string.titration_entry_error),
            "Titration 1 drops",
            "Titration 2 drops",
        )
        onView(withId(id.editTitration1)).check(matches(hasErrorText(error)))

        onView(withId(id.editTitration1)).check(matches(isDisplayed()))
            .perform(replaceText("12"), closeSoftKeyboard())
        onView(withId(id.editTitration2)).check(matches(isDisplayed()))
            .perform(replaceText("20"), closeSoftKeyboard())

        sleep(1000)
        takeScreenshot(screenshotName)
        onView(
            allOf(
                withId(id.editTitration2), withText("20"),
                childAtPosition(
                    childAtPosition(withId(id.fragment_container), 0),
                    4
                ), isDisplayed()
            )
        ).perform(pressImeActionButton())

        onView(withId(id.buttonAccept)).perform(click())

        sleep(1000)

        assertNotNull(
            mDevice.findObject(
                By.text(
                    getString(
                        string.total_alkalinity
                    )
                )
            )
        )
        assertNotNull(mDevice.findObject(By.text("3200.00")))

        takeScreenshot(screenshotName)

        mDevice.waitForIdle()

        mDevice.pressBack()

        mDevice.pressBack()
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