package org.akvo.caddisfly.navigation

import android.widget.DatePicker
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
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
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R.id
import org.akvo.caddisfly.R.string
import org.akvo.caddisfly.common.TestConstants
import org.akvo.caddisfly.common.TestConstants.CUVETTE_TEST_NAME_1
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.enterDiagnosticMode
import org.akvo.caddisfly.util.TestHelper.goToMainScreen
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.leaveDiagnosticMode
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.saveCalibration
import org.akvo.caddisfly.util.TestHelper.startSurveyApp
import org.akvo.caddisfly.util.TestHelper.takeScreenshot
import org.akvo.caddisfly.util.TestUtil
import org.akvo.caddisfly.util.TestUtil.childAtPosition
import org.akvo.caddisfly.util.TestUtil.sleep
import org.akvo.caddisfly.util.TestUtil.swipeUp
import org.akvo.caddisfly.util.mDevice
import org.hamcrest.Matchers.*
import org.hamcrest.`object`.HasToString.hasToString
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class NavigationTest {

    @get:Rule
    val mActivityRule = activityScenarioRule<MainActivity>()

    @Rule
    @JvmField
    var mGrantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"
        )

    @Before
    fun setUp() {
        loadData(ApplicationProvider.getApplicationContext())
        clearPreferences()
    }

    @Test
    fun testNavigateAll() {

        val screenshotName = TestConstants.CUVETTE_TEST_ID_1
            .substring(TestConstants.CUVETTE_TEST_ID_1.lastIndexOf("-") + 1)

        saveCalibration("TestInvalid", TestConstants.CUVETTE_TEST_ID_1)

        mDevice.waitForWindowUpdate("", 2000)
        goToMainScreen()

        //Main Screen
        takeScreenshot()

        onView(withText(string.settings)).perform(click())

        //Settings Screen
        takeScreenshot()
        onView(withText(string.about)).check(matches(isDisplayed())).perform(click())
        mDevice.waitForWindowUpdate("", 1000)

        //About Screen
        takeScreenshot()
        Espresso.pressBack()

        onView(withText(string.about)).check(matches(isDisplayed())).perform(click())
        enterDiagnosticMode()
        goToMainScreen()
        onView(withText(string.settings)).perform(click())
        onView(withId(id.scrollViewSettings)).perform(ViewActions.swipeUp())
        onView(withText(string.calibrate)).perform(click())
        sleep(2000)

        try {
            onView(allOf(withText(CUVETTE_TEST_NAME_1), isDisplayed())).perform(click())
        } catch (e: Exception) {
            swipeUp()
            sleep(1000)
            onView(allOf(withText(CUVETTE_TEST_NAME_1), isDisplayed())).perform(click())
        }

        if (TestUtil.isEmulator) {
//            onView(withText(string.error_camera_flash_required))
//                    .inRoot(withDecorView(not(`is`(mActivityRule.activity.window
//                            .decorView)))).check(matches(isDisplayed()))
            return
        }

        sleep(1000)
        onView(withId(id.menuLoad)).perform(click())
        sleep(1000)
        onData(hasToString(startsWith("TestInvalid"))).perform(click())
        sleep(1000)

        onView(
            withText(
                String.format(
                    "%s. %s",
                    getInstrumentation().targetContext.getString(string.calibration_is_invalid),
                    getInstrumentation().targetContext.getString(string.try_recalibrating)
                )
            )
        ).check(matches(isDisplayed()))
        leaveDiagnosticMode()
        sleep(4000)

        goToMainScreen()
        onView(withText(string.settings)).perform(click())
        onView(withId(id.scrollViewSettings)).perform(ViewActions.swipeUp())
        onView(withText(string.calibrate)).perform(click())

        //Test Types Screen
        takeScreenshot()
        onView(
            allOf(
                withId(id.list_types), childAtPosition(
                    withClassName(`is`("android.widget.LinearLayout")),
                    0
                )
            )
        ).perform(
            actionOnItemAtPosition<ViewHolder?>(
                TestConstants.TEST_INDEX, click()
            )
        )

        //Calibrate Swatches Screen

        takeScreenshot()

//        DecimalFormatSymbols dfs = new DecimalFormatSymbols();

        onView(withId(id.fabEditCalibration)).perform(click())

//        onView(withId(R.id.editBatchCode))
//                .perform(typeText("TEST 123#*@!"), closeSoftKeyboard());
//

        onView(withId(id.editExpiryDate)).perform(click())
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(2025, 8, 25))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withText(string.save)).perform(click())
        val recyclerView3: ViewInteraction = onView(
            allOf(
                withId(id.calibrationList), childAtPosition(
                    withClassName(`is`("android.widget.RelativeLayout")),
                    0
                )
            )
        )
        recyclerView3.perform(actionOnItemAtPosition<ViewHolder?>(4, click()))

        // onView(withText("2" + dfs.getDecimalSeparator() + "0 mg/l")).perform(click());

        //onView(withId(R.id.buttonStart)).perform(click());

        saveCalibration("TestValid", TestConstants.CUVETTE_TEST_ID_1)
        goToMainScreen()
        onView(withText(string.settings)).perform(click())
        onView(withText(string.about)).check(matches(isDisplayed())).perform(click())
        enterDiagnosticMode()
        Espresso.pressBack()
        Espresso.pressBack()
        onView(withText(string.settings)).perform(click())
        onView(withId(id.scrollViewSettings)).perform(ViewActions.swipeUp())
        onView(withText(string.calibrate)).perform(click())

        sleep(1000)
        onView(allOf(withText(CUVETTE_TEST_NAME_1), isDisplayed())).perform(click())
        onView(withId(id.menuLoad)).perform(click())
        sleep(2000)
        onData(hasToString(startsWith("TestValid"))).perform(click())
        sleep(2000)
        leaveDiagnosticMode()

        goToMainScreen()
        onView(withText(string.settings)).perform(click())
        onView(withId(id.scrollViewSettings)).perform(ViewActions.swipeUp())
        onView(withText(string.calibrate)).perform(click())

        goToMainScreen()
        startSurveyApp()
        takeScreenshot()
        gotoSurveyForm()
        clickExternalSourceButton(TestConstants.CUVETTE_TEST_ID_1, screenshotName)
        onView(withId(id.button_prepare)).check(matches(isDisplayed()))
        onView(withId(id.button_prepare)).perform(click())
        onView(withId(id.buttonNoDilution)).check(matches(isDisplayed()))

        //Dilution dialog
        takeScreenshot()
        TestUtil.goBack(5)

        startSurveyApp()
        takeScreenshot()
        gotoSurveyForm()
        clickExternalSourceButton(TestConstants.CUVETTE_TEST_ID_1, screenshotName)
        onView(withText(CUVETTE_TEST_NAME_1)).check(matches(isDisplayed()))

//        //Calibration incomplete
        takeScreenshot()

        // Chlorine not calibrated
        //onView(withText(R.string.cannotStartTest)).check(matches(isDisplayed()));

        //onView(withId(android.R.id.button2)).perform(click());

        mDevice.pressBack()
        mDevice.waitForWindowUpdate("", 2000)

//        clickExternalSourceButton(TestConstants.CUVETTE_TEST_ID_2);

//        onView(withText(R.string.chromium)).check(matches(isDisplayed()));

//        onView(withText(R.string.cannotStartTest)).check(matches(isDisplayed()));

//        takeScreenshot();

//        mDevice.pressBack();

//        TestUtil.nextSurveyPage(3);
//
//        //Unknown test
//        clickExternalSourceButton(0, TestConstant.USE_EXTERNAL_SOURCE);
//
//        onView(withText(R.string.cannotStartTest)).check(matches(isDisplayed()));
//
//        mDevice.pressBack();

//        TestUtil.swipeRight(7);
//
//        clickExternalSourceButton(0); //Iron
//
        //onView(withText(R.string.cannotStartTest)).check(matches(isDisplayed()));

        //onView(withText(R.string.ok)).perform(click());

        mDevice.pressBack()

        //mDevice.pressBack();
        //onView(withId(android.R.id.button1)).perform(click());
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