package org.akvo.caddisfly.test;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.RequiresDevice;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.ChamberTestConfig;
import org.akvo.caddisfly.common.TestConstants;
import org.akvo.caddisfly.model.TestSampleType;
import org.akvo.caddisfly.ui.MainActivity;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.akvo.caddisfly.common.ChamberTestConfig.DELAY_BETWEEN_SAMPLING;
import static org.akvo.caddisfly.common.TestConstants.CUVETTE_TEST_ID_1;
import static org.akvo.caddisfly.common.TestConstants.CUVETTE_TEST_TIME_DELAY;
import static org.akvo.caddisfly.common.TestConstants.EXPECTED_RESULT;
import static org.akvo.caddisfly.common.TestConstants.IS_HAS_DILUTION;
import static org.akvo.caddisfly.common.TestConstants.IS_TEST_TYPE;
import static org.akvo.caddisfly.common.TestConstants.TEST_GROUP;
import static org.akvo.caddisfly.common.TestConstants.TEST_NAME;
import static org.akvo.caddisfly.common.TestConstants.TEST_START_DELAY;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.enterDiagnosticMode;
import static org.akvo.caddisfly.util.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.leaveDiagnosticMode;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.takeScreenshot;
import static org.akvo.caddisfly.util.TestUtil.childAtPosition;
import static org.akvo.caddisfly.util.TestUtil.clickListViewItem;
import static org.akvo.caddisfly.util.TestUtil.doesNotExistOrGone;
import static org.akvo.caddisfly.util.TestUtil.getText;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RetryTest {

    private static final String TAG = "Instrumented Test";

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @BeforeClass
    public static void initialize() {
        if (mDevice == null) {
            mDevice = UiDevice.getInstance(getInstrumentation());

            for (int i = 0; i < 5; i++) {
                mDevice.pressBack();
            }
        }
    }

    @Before
    public void setUp() {

        loadData(mActivityRule.getActivity(), mCurrentLanguage);

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(mActivityRule.getActivity());
        prefs.edit().clear().apply();
    }

    @Test
    @RequiresDevice
    public void testRetry() {
        runTest(CUVETTE_TEST_ID_1, EXPECTED_RESULT, false,
                false, IS_HAS_DILUTION, true);
    }

    @Test
    @RequiresDevice
    public void testDilutionRetry() {
        runTest(CUVETTE_TEST_ID_1, EXPECTED_RESULT, false,
                false, IS_HAS_DILUTION, true);
    }

    @Test
    @RequiresDevice
    public void testDiagnosticRetry() {
        runTest(CUVETTE_TEST_ID_1, EXPECTED_RESULT, true,
                false, IS_HAS_DILUTION, true);
    }

    @Test
    @RequiresDevice
    public void testDiagnosticDebugRetry() {
        runTest(CUVETTE_TEST_ID_1, EXPECTED_RESULT, true,
                true, IS_HAS_DILUTION, true);
    }

    @Test
    @RequiresDevice
    public void testInternalRetry() {
        runTest(CUVETTE_TEST_ID_1, EXPECTED_RESULT, true,
                true, IS_HAS_DILUTION, false);
    }

    public void runTest(String testId, double expectedResult, boolean useDiagnosticMode,
                        boolean showDebugInfo, boolean hasDilution, boolean isExternal) {

        Log.i(TAG, "Test 1");

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        String version = CaddisflyApp.getAppVersion(false);

        onView(withText(version)).check(matches(isDisplayed()));

        enterDiagnosticMode();

        goToMainScreen();

        onView(withId(R.id.actionSettings)).perform(click());

        if (showDebugInfo) {
            clickListViewItem("Show debug info");
        }

        if (!useDiagnosticMode) {
            leaveDiagnosticMode();
        }

        goToMainScreen();

        if (!isExternal) {

            gotoSurveyForm();

            clickExternalSourceButton(testId);

            sleep(1000);

            onView(withId(R.id.button_prepare)).check(matches(isDisplayed()));

            onView(withId(R.id.button_prepare)).perform(click());

        } else {

            try {
                onView(withText(R.string.calibrate)).perform(click());
            } catch (Exception e) {
                if (IS_TEST_TYPE == TestSampleType.SOIL) {
                    onView(withText(R.string.soilCalibrate)).perform(click());
                } else {
                    onView(withText(R.string.waterCalibrate)).perform(click());
                }
            }

            onView(allOf(withId(R.id.list_types),
                    childAtPosition(
                            withClassName(is("android.widget.LinearLayout")),
                            0))).perform(actionOnItemAtPosition(
                    TestConstants.TEST_INDEX, click()));

            onView(withId(R.id.buttonRunTest)).perform(click());
        }

        if (hasDilution) {
            onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

            onView(withId(R.id.buttonNoDilution)).perform(click());

            onView(allOf(withId(R.id.textDilution), withText(R.string.noDilution)))
                    .check(matches(isCompletelyDisplayed()));

            onView(allOf(withId(R.id.textDilution), withText(R.string.noDilution)))
                    .check(matches(isCompletelyDisplayed()));
        }

        onView(withId(R.id.layoutWait)).check(matches(isDisplayed()));

        sleep((TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY
                + (DELAY_BETWEEN_SAMPLING * ChamberTestConfig.SAMPLING_COUNT_DEFAULT))
                * 1000);

        onView(withText(R.string.cancel)).perform(click());

        if (isExternal) {

            clickExternalSourceButton(testId);

            sleep(1000);

            onView(withId(R.id.button_prepare)).check(matches(isDisplayed()));

            onView(withId(R.id.button_prepare)).perform(click());

        } else {

            onView(allOf(withId(R.id.list_types),
                    childAtPosition(
                            withClassName(is("android.widget.LinearLayout")),
                            0))).perform(actionOnItemAtPosition(
                    TestConstants.TEST_INDEX, click()));

            onView(withId(R.id.buttonRunTest)).perform(click());
        }

        if (hasDilution) {
            onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

            onView(withId(R.id.buttonNoDilution)).perform(click());

            onView(allOf(withId(R.id.textDilution), withText(R.string.noDilution)))
                    .check(matches(isCompletelyDisplayed()));

            onView(allOf(withId(R.id.textDilution), withText(R.string.noDilution)))
                    .check(matches(isCompletelyDisplayed()));
        }

        onView(withId(R.id.layoutWait)).check(matches(isDisplayed()));

        Log.i(TAG, "Test 2");

        sleep((TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY
                + (DELAY_BETWEEN_SAMPLING * ChamberTestConfig.SAMPLING_COUNT_DEFAULT))
                * 1000);

        onView(withText(R.string.retry)).perform(click());

        Log.i(TAG, "Test 3");

        sleep((TEST_START_DELAY +
                (DELAY_BETWEEN_SAMPLING * ChamberTestConfig.SAMPLING_COUNT_DEFAULT))
                * 1000);

        onView(withText(R.string.retry)).check(doesNotExistOrGone());

        onView(withText(R.string.ok)).perform(click());

        Log.i(TAG, "Test 4");

        if (isExternal) {

            clickExternalSourceButton(testId);

            sleep(1000);

            onView(withId(R.id.button_prepare)).check(matches(isDisplayed()));

            onView(withId(R.id.button_prepare)).perform(click());

        } else {

            onView(allOf(withId(R.id.list_types),
                    childAtPosition(
                            withClassName(is("android.widget.LinearLayout")),
                            0))).perform(actionOnItemAtPosition(
                    TestConstants.TEST_INDEX, click()));

            onView(withId(R.id.buttonRunTest)).perform(click());
        }

        //Test Start Screen
        takeScreenshot();

        if (hasDilution) {
            onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

            onView(withId(R.id.buttonNoDilution)).perform(click());

            onView(allOf(withId(R.id.textDilution), withText(R.string.noDilution)))
                    .check(matches(isCompletelyDisplayed()));

            onView(allOf(withId(R.id.textDilution), withText(R.string.noDilution)))
                    .check(matches(isCompletelyDisplayed()));
        }

        onView(withId(R.id.layoutWait)).check(matches(isDisplayed()));

        //Test Progress Screen
        takeScreenshot();

        onView(withId(R.id.layoutWait)).check(matches(isDisplayed()));

        sleep((TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY
                + (DELAY_BETWEEN_SAMPLING * ChamberTestConfig.SAMPLING_COUNT_DEFAULT))
                * 1000);

        String resultString = getText(withId(R.id.textResult));

        double result = Double.valueOf(resultString.replace(">", "").trim());
        assertTrue("Result is wrong", result > expectedResult);

        if (showDebugInfo) {
            onView(withText(R.string.ok)).perform(click());
        } else {
            onView(withId(R.id.buttonAccept)).perform(click());
        }

        if (isExternal) {

            mDevice.waitForIdle();

            assertNotNull(mDevice.findObject(By.text(TEST_GROUP)));

            assertNotNull(mDevice.findObject(By.text(TEST_NAME)));

            assertNotNull(mDevice.findObject(By.text(resultString)));
        }
    }
}
