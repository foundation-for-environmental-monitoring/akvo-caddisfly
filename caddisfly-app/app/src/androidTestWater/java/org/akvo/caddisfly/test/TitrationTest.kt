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
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
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
        gotoSurveyForm()
        nextSurveyPage(5, "Water Tests")
        clickExternalSourceButton(0)
        onView(withText(string.next)).perform(click())

        onView(withId(id.editTitration1)).perform(pressImeActionButton())

        sleep(1000)

        onView(withId(id.editTitration1)).check(matches(isDisplayed()))
            .perform(replaceText("12"), closeSoftKeyboard())

        onView(allOf(withId(id.editTitration1), withText("12"), isDisplayed()))
            .perform(pressImeActionButton())

        sleep(1000)

        assertNotNull(mDevice.findObject(By.text("Carbonate: ")))
        assertNotNull(mDevice.findObject(By.text("300.00")))

        mDevice.waitForIdle()

        mDevice.pressBack()

        mDevice.pressBack()
    }

    @Test
    fun runCalciumTitrationTest() {

        mDevice.waitForIdle()

        mDevice.pressBack()

        mDevice.pressBack()

        sleep(2000)
        gotoSurveyForm()
        sleep(2000)
        nextSurveyPage(5, "Water Tests")
        clickExternalSourceButton(1)
        onView(withText(string.next)).perform(click())

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
        onView(
            allOf(
                withId(id.editTitration2), withText("20"),
                childAtPosition(
                    childAtPosition(withId(id.fragment_container), 0),
                    4
                ), isDisplayed()
            )
        ).perform(pressImeActionButton())

        sleep(1000)

        assertNotNull(mDevice.findObject(By.text("Calcium: ")))
        assertNotNull(mDevice.findObject(By.text("100.00")))
        assertNotNull(mDevice.findObject(By.text("Magnesium: ")))
        assertNotNull(mDevice.findObject(By.text("40.00")))

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