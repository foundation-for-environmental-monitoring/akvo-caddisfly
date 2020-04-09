package org.akvo.caddisfly.test

import android.content.SharedPreferences
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import junit.framework.TestCase.assertNotNull
import org.akvo.caddisfly.R.id
import org.akvo.caddisfly.R.string
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestConstant
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
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
    @JvmField
    @Rule
    var mActivityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        loadData(mActivityRule.activity, mCurrentLanguage)
        val prefs: SharedPreferences = getDefaultSharedPreferences(mActivityRule.activity)
        prefs.edit().clear().apply()
    }

    @Test
    fun runCarbonateTitrationTest() {
        gotoSurveyForm()
        nextSurveyPage(3, "Water Tests")
        clickExternalSourceButton(0, TestConstant.GO_TO_TEST)
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

        mActivityRule.finishActivity()

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
        nextSurveyPage(3, "Water Tests")
        clickExternalSourceButton(1, TestConstant.GO_TO_TEST)
        onView(withText(string.next)).perform(click())

        onView(withId(id.editTitration1)).check(matches(isDisplayed()))
                .perform(replaceText("123"), closeSoftKeyboard())
        onView(withId(id.editTitration2)).check(matches(isDisplayed()))
                .perform(replaceText("12"), closeSoftKeyboard())
        onView(allOf(withId(id.editTitration2), withText("12"),
                childAtPosition(childAtPosition(withId(id.fragment_container), 0),
                        4), isDisplayed())).perform(pressImeActionButton())

        sleep(1000)

        onView(withId(id.editTitration1)).check(matches(isDisplayed()))
                .perform(replaceText("12"), closeSoftKeyboard())
        onView(withId(id.editTitration2)).check(matches(isDisplayed()))
                .perform(replaceText("20"), closeSoftKeyboard())
        onView(allOf(withId(id.editTitration2), withText("20"),
                childAtPosition(childAtPosition(withId(id.fragment_container), 0),
                        4), isDisplayed())).perform(pressImeActionButton())

        sleep(1000)

        assertNotNull(mDevice.findObject(By.text("Calcium: ")))
        assertNotNull(mDevice.findObject(By.text("100.00")))
        assertNotNull(mDevice.findObject(By.text("Magnesium: ")))
        assertNotNull(mDevice.findObject(By.text("40.00")))

        mActivityRule.finishActivity()

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