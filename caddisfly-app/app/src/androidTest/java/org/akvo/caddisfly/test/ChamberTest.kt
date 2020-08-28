package org.akvo.caddisfly.test

import android.widget.DatePicker
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R.id
import org.akvo.caddisfly.R.string
import org.akvo.caddisfly.app.CaddisflyApp
import org.akvo.caddisfly.common.ChamberTestConfig
import org.akvo.caddisfly.common.ChamberTestConfig.DELAY_BETWEEN_SAMPLING
import org.akvo.caddisfly.common.ChamberTestConfig.SKIP_SAMPLING_COUNT
import org.akvo.caddisfly.common.Constants
import org.akvo.caddisfly.common.TestConstants
import org.akvo.caddisfly.common.TestConstants.CUVETTE_TEST_TIME_DELAY
import org.akvo.caddisfly.common.TestConstants.DELAY_EXTRA
import org.akvo.caddisfly.common.TestConstants.TEST_INDEX
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.enterDiagnosticMode
import org.akvo.caddisfly.util.TestHelper.goToMainScreen
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.leaveDiagnosticMode
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.saveCalibration
import org.akvo.caddisfly.util.TestHelper.startSurveyApp
import org.akvo.caddisfly.util.TestHelper.startSurveyForm
import org.akvo.caddisfly.util.TestHelper.takeScreenshot
import org.akvo.caddisfly.util.TestUtil.childAtPosition
import org.akvo.caddisfly.util.TestUtil.clickListViewItem
import org.akvo.caddisfly.util.TestUtil.getText
import org.akvo.caddisfly.util.TestUtil.isEmulator
import org.akvo.caddisfly.util.TestUtil.sleep
import org.akvo.caddisfly.util.mDevice
import org.hamcrest.Matchers.*
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class ChamberTest {

    @get:Rule
    val mActivityRule = activityScenarioRule<MainActivity>()

    @Before
    fun setUp() {
        loadData(ApplicationProvider.getApplicationContext())
        TestHelper.clearPreferences()
    }

    @Test
    fun testFreeChlorine() {
        saveCalibration("TestValidChlorine", Constants.FREE_CHLORINE_ID_2)
        onView(withText(string.settings)).perform(click())
        onView(withText(string.about)).check(matches(isDisplayed()))
    }

    @Test
    fun testStartHighLevelTest() {

        val screenshotName = TestConstants.CUVETTE_TEST_ID_1
            .substring(TestConstants.CUVETTE_TEST_ID_1.lastIndexOf("-") + 1)

        saveCalibration("HighLevelTest", TestConstants.CUVETTE_TEST_ID_1)

        onView(withText(string.settings)).perform(click())
        onView(withText(string.about)).check(matches(isDisplayed())).perform(click())
        val version: String? = CaddisflyApp.getAppVersion(false)
        onView(withText(version)).check(matches(isDisplayed()))
        enterDiagnosticMode()
        goToMainScreen()
        try {
            onView(withText(string.settings)).perform(click())
            onView(withId(id.scrollViewSettings)).perform(ViewActions.swipeUp())
            onView(withText(string.calibrate)).perform(click())
        } catch (e: Exception) {
            onView(withText(string.waterCalibrate)).perform(click())
        }

        onView(
            allOf(
                withId(id.list_types),
                childAtPosition(
                    withClassName(`is`("android.widget.LinearLayout")),
                    0
                )
            )
        ).perform(
            actionOnItemAtPosition<ViewHolder?>(
                TEST_INDEX, click()
            )
        )
        if (isEmulator) {
//            onView(withText(string.error_camera_flash_required))
//                    .inRoot(withDecorView(not(`is`(mActivityRule.activity.window
//                            .decorView)))).check(matches(isDisplayed()))
            return
        }
        onView(withId(id.menuLoad)).perform(click())
        sleep(1000)
        clickListViewItem("HighLevelTest")
        sleep(1000)
        goToMainScreen()
        onView(withText(string.settings)).perform(click())
        leaveDiagnosticMode()
        goToMainScreen()
        try {
            sleep(2000)
            takeScreenshot(screenshotName)
            onView(withText(string.settings)).perform(click())
            onView(withId(id.scrollViewSettings)).perform(ViewActions.swipeUp())
            takeScreenshot(screenshotName)
            onView(withText(string.calibrate)).perform(click())
            takeScreenshot(screenshotName)
        } catch (e: Exception) {
            onView(withText(string.waterCalibrate)).perform(click())
        }
        onView(
            allOf(
                withId(id.list_types),
                childAtPosition(
                    withClassName(`is`("android.widget.LinearLayout")),
                    0
                )
            )
        ).perform(
            actionOnItemAtPosition<ViewHolder?>(
                TEST_INDEX, click()
            )
        )

        takeScreenshot(screenshotName)

        onView(withId(id.fabEditCalibration)).perform(click())
        takeScreenshot(screenshotName)
        onView(withId(id.editExpiryDate)).perform(click())
        val date: Calendar = Calendar.getInstance()
        date.add(Calendar.MONTH, 2)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                    date.get(Calendar.DATE)
                )
            )
        onView(withId(android.R.id.button1)).perform(click())
        onView(withText(string.save)).perform(click())
        val recyclerView2: ViewInteraction = onView(
            allOf(
                withId(id.calibrationList),
                childAtPosition(
                    withClassName(`is`("android.widget.RelativeLayout")),
                    0
                )
            )
        )
        recyclerView2.perform(actionOnItemAtPosition<ViewHolder?>(4, click()))
        onView(withId(id.layoutWait)).check(matches(isDisplayed()))
        sleep(3000)

        takeScreenshot(screenshotName)
        sleep(
            (TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY + DELAY_EXTRA
                    + DELAY_BETWEEN_SAMPLING * (ChamberTestConfig.SAMPLING_COUNT_DEFAULT + SKIP_SAMPLING_COUNT))
                    * 1000
        )

        takeScreenshot(screenshotName)
        onView(withId(id.buttonOk)).perform(click())
        goToMainScreen()

        startSurveyApp()
        sleep(2000)
        takeScreenshot(screenshotName)

        startSurveyForm()
        sleep(2000)
        clickExternalSourceButton(TestConstants.CUVETTE_TEST_ID_1, screenshotName)
        sleep(1000)

        takeScreenshot(screenshotName)

        onView(withId(id.button_prepare)).check(matches(isDisplayed()))
        onView(withId(id.button_prepare)).perform(click())
        onView(withId(id.buttonNoDilution)).check(matches(isDisplayed()))
        onView(withId(id.buttonNoDilution)).perform(click())

        onView(allOf(withId(id.textDilution), withText(string.no_dilution)))
            .check(matches(isCompletelyDisplayed()))
        onView(allOf(withId(id.textDilution), withText(string.no_dilution)))
            .check(matches(isCompletelyDisplayed()))
        onView(withId(id.layoutWait)).check(matches(isDisplayed()))

        sleep(
            (TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY + DELAY_EXTRA
                    + DELAY_BETWEEN_SAMPLING * (ChamberTestConfig.SAMPLING_COUNT_DEFAULT + SKIP_SAMPLING_COUNT))
                    * 1000
        )
        onView(withId(id.buttonAccept)).perform(click())

        clickExternalSourceButton(TestConstants.CUVETTE_TEST_ID_1, "")
        onView(withId(id.button_prepare)).check(matches(isDisplayed()))
        onView(withId(id.button_prepare)).perform(click())
        onView(withId(id.buttonDilution1)).check(matches(isDisplayed()))
        takeScreenshot(screenshotName)

        onView(withId(id.buttonDilution1)).perform(click())
        onView(
            allOf(
                withId(id.textDilution), withText(
                    String.format(
                        getInstrumentation().targetContext
                            .getString(string.times_dilution), 2
                    )
                )
            )
        )
            .check(matches(isCompletelyDisplayed()))

        //Test Start Screen
        onView(withId(id.layoutWait)).check(matches(isDisplayed()))
        sleep(3000)
        takeScreenshot(screenshotName)

        sleep(
            (TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY + DELAY_EXTRA
                    + DELAY_BETWEEN_SAMPLING * (ChamberTestConfig.SAMPLING_COUNT_DEFAULT + SKIP_SAMPLING_COUNT))
                    * 1000
        )
        onView(withText(getInstrumentation().targetContext.getString(string.test_with_dilution)))
            .check(matches(isDisplayed()))

        //High levels found dialog
        takeScreenshot(screenshotName)
        onView(withId(id.buttonAccept)).perform(click())
        clickExternalSourceButton(TestConstants.CUVETTE_TEST_ID_1, "")
        onView(withId(id.button_prepare)).check(matches(isDisplayed()))
        onView(withId(id.button_prepare)).perform(click())
        onView(withId(id.buttonDilution1)).check(matches(isDisplayed()))

        onView(withId(id.buttonDilution1)).perform(click())
        onView(
            allOf(
                withId(id.textDilution), withText(
                    String.format(
                        getInstrumentation().targetContext
                            .getString(string.times_dilution), 5
                    )
                )
            )
        ).check(matches(isCompletelyDisplayed()))

        //Test Progress Screen
        onView(withId(id.layoutWait)).check(matches(isDisplayed()))
        sleep(
            (TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY + DELAY_EXTRA
                    + DELAY_BETWEEN_SAMPLING * (ChamberTestConfig.SAMPLING_COUNT_DEFAULT + SKIP_SAMPLING_COUNT))
                    * 1000
        )
        val resultString = getText(withId(id.textResult))
        assertTrue(resultString!!.contains(">"))
        @Suppress("ConstantConditionIf")
        if (CUVETTE_TEST_TIME_DELAY > 0) {
            val result: Double = (resultString.replace(">", "").trim { it <= ' ' }).toDouble()
            assertTrue("Result is wrong", result > 49)
            onView(withText(getInstrumentation().targetContext.getString(string.test_with_dilution)))
                .check(matches(isDisplayed()))
        } else {
            val result: Double = (resultString.replace(">", "").trim { it <= ' ' }).toDouble()
            assertTrue("Result is wrong", result > 9)
            onView(withText(getInstrumentation().targetContext.getString(string.test_with_dilution)))
                .check(matches(not(isDisplayed())))
        }

        takeScreenshot(screenshotName)
        onView(withId(id.buttonAccept)).perform(click())
        mDevice.waitForIdle()
        sleep(2000)
        assertNotNull(mDevice.findObject(By.text(resultString)))
        takeScreenshot(screenshotName)

        mDevice.pressBack()
        mDevice.pressBack()
    }

    @Test
    fun testStartNoDilutionTest() {
        saveCalibration("TestValid", TestConstants.CUVETTE_TEST_ID_1)
        onView(withText(string.settings)).perform(click())
        onView(withText(string.about)).check(matches(isDisplayed())).perform(click())
        val version: String? = CaddisflyApp.getAppVersion(false)
        onView(withText(version)).check(matches(isDisplayed()))
        enterDiagnosticMode()
        goToMainScreen()
        try {
            onView(withText(string.settings)).perform(click())
            onView(withId(id.scrollViewSettings)).perform(ViewActions.swipeUp())
            onView(withText(string.calibrate)).perform(click())
        } catch (e: Exception) {
            onView(withText(string.waterCalibrate)).perform(click())
        }
        onView(
            allOf(
                withId(id.list_types),
                childAtPosition(
                    withClassName(`is`("android.widget.LinearLayout")),
                    0
                )
            )
        ).perform(
            actionOnItemAtPosition<ViewHolder?>(
                TEST_INDEX, click()
            )
        )
        if (isEmulator) {
//            onView(withText(string.error_camera_flash_required))
//                .inRoot(
//                    withDecorView(
//                        not(
//                            `is`(
//                                mActivityRule.activity.window
//                                    .decorView
//                            )
//                        )
//                    )
//                ).check(matches(isDisplayed()))
            return
        }
        onView(withId(id.menuLoad)).perform(click())
        sleep(1000)
        onData(hasToString(startsWith("TestValid"))).perform(click())
        goToMainScreen()
        onView(withText(string.settings)).perform(click())
        leaveDiagnosticMode()
        goToMainScreen()
        try {
            onView(withText(string.settings)).perform(click())
            onView(withId(id.scrollViewSettings)).perform(ViewActions.swipeUp())
            onView(withText(string.calibrate)).perform(click())
        } catch (e: Exception) {
            onView(withText(string.waterCalibrate)).perform(click())
        }
        onView(
            allOf(
                withId(id.list_types),
                childAtPosition(
                    withClassName(`is`("android.widget.LinearLayout")),
                    0
                )
            )
        ).perform(
            actionOnItemAtPosition<ViewHolder?>(
                TEST_INDEX, click()
            )
        )
        onView(withId(id.fabEditCalibration)).perform(click())

//        onView(withText(R.string.save)).perform(click());


        onView(withId(id.editExpiryDate)).perform(click())
        val date: Calendar = Calendar.getInstance()
        date.add(Calendar.MONTH, 2)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                    date.get(Calendar.DATE)
                )
            )
        onView(withId(android.R.id.button1)).perform(click())
        onView(withText(string.save)).perform(click())
        val recyclerView2: ViewInteraction = onView(
            allOf(
                withId(id.calibrationList),
                childAtPosition(
                    withClassName(`is`("android.widget.RelativeLayout")),
                    0
                )
            )
        )
        recyclerView2.perform(actionOnItemAtPosition<ViewHolder?>(2, click()))
        sleep(
            (TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY + DELAY_EXTRA
                    + DELAY_BETWEEN_SAMPLING * (ChamberTestConfig.SAMPLING_COUNT_DEFAULT + SKIP_SAMPLING_COUNT))
                    * 1000
        )
        onView(withId(id.buttonOk)).perform(click())
        goToMainScreen()
        gotoSurveyForm()
        sleep(1000)
        clickExternalSourceButton(TestConstants.CUVETTE_TEST_ID_1, "")
        sleep(1000)
        onView(withId(id.button_prepare)).check(matches(isDisplayed()))
        onView(withId(id.button_prepare)).perform(click())
        onView(withId(id.buttonNoDilution)).check(matches(isDisplayed()))
        onView(withId(id.buttonNoDilution)).perform(click())
        sleep(
            (TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY + DELAY_EXTRA
                    + DELAY_BETWEEN_SAMPLING * (ChamberTestConfig.SAMPLING_COUNT_DEFAULT + SKIP_SAMPLING_COUNT))
                    * 1000
        )

        //Result dialog


        takeScreenshot("screenshotName")
        val resultString = getText(withId(id.textResult))
        onView(withId(id.buttonAccept)).perform(click())
        mDevice.waitForIdle()
        assertNotNull(mDevice.findObject(By.text(resultString)))

//        onView(withId(android.R.id.list)).check(matches(withChildCount(is(greaterThan(0)))));
//        onView(withText(R.string.startTestConfirm)).check(matches(isDisplayed()));


    }

    companion object {
        private const val TEST_START_DELAY = 24

        @JvmStatic
        @BeforeClass
        fun initialize() {
            if (!TestHelper.isDeviceInitialized()) {
                mDevice = UiDevice.getInstance(getInstrumentation())
            }
        }
    }
}