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

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.TestConstants;
import org.akvo.caddisfly.model.TestSampleType;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.ui.MainActivity;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.akvo.caddisfly.common.ChamberTestConfig.DELAY_BETWEEN_SAMPLING;
import static org.akvo.caddisfly.common.ChamberTestConfig.DELAY_INITIAL;
import static org.akvo.caddisfly.common.ChamberTestConfig.SKIP_SAMPLING_COUNT;
import static org.akvo.caddisfly.common.TestConstants.CUVETTE_TEST_TIME_DELAY;
import static org.akvo.caddisfly.common.TestConstants.IS_EXPECTED_RESULT;
import static org.akvo.caddisfly.common.TestConstants.IS_EXTRA_DELAY;
import static org.akvo.caddisfly.common.TestConstants.IS_HAS_DILUTION;
import static org.akvo.caddisfly.common.TestConstants.IS_TEST_GROUP;
import static org.akvo.caddisfly.common.TestConstants.IS_TEST_ID;
import static org.akvo.caddisfly.common.TestConstants.IS_TEST_NAME;
import static org.akvo.caddisfly.common.TestConstants.IS_TEST_TYPE;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.enterDiagnosticMode;
import static org.akvo.caddisfly.util.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.leaveDiagnosticMode;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.saveCalibration;
import static org.akvo.caddisfly.util.TestHelper.takeScreenshot;
import static org.akvo.caddisfly.util.TestUtil.childAtPosition;
import static org.akvo.caddisfly.util.TestUtil.clickListViewItem;
import static org.akvo.caddisfly.util.TestUtil.doesNotExistOrGone;
import static org.akvo.caddisfly.util.TestUtil.getText;
import static org.akvo.caddisfly.util.TestUtil.nextPage;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

@SuppressWarnings("ConstantConditions")
@RunWith(AndroidJUnit4.class)
@LargeTest
@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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

    public static void runTest(String testId, boolean useDiagnosticMode,
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

        goToMainScreen();

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
                TestConstants.IS_TEST_INDEX, click()));

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(1000);

        onData(hasToString(startsWith(IS_TEST_NAME + "_NoMatch"))).perform(click());

        sleep(1000);

        if (!useDiagnosticMode && !BuildConfig.FLAVOR.equals("experiment")) {
            leaveDiagnosticMode();
        }

        goToMainScreen();

        if (isExternal) {

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
                    TestConstants.IS_TEST_INDEX, click()));

            onView(withId(R.id.buttonRunTest)).perform(click());
        }

        if (hasDilution) {
            onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

            onView(withId(R.id.buttonNoDilution)).perform(click());
        }

        for (int i = 0; i < 7; i++) {
            nextPage();
        }

//        onView(withId(R.id.layoutWait)).check(matches(isDisplayed()));

        sleep((DELAY_INITIAL + CUVETTE_TEST_TIME_DELAY + (DELAY_BETWEEN_SAMPLING *
                (AppPreferences.getSamplingTimes() + SKIP_SAMPLING_COUNT + IS_EXTRA_DELAY))) * 1000);

        onView(withText(R.string.cancel)).perform(click());

        if (isExternal) {

            clickExternalSourceButton(testId);

            sleep(1000);

            onView(withId(R.id.button_prepare)).check(matches(isDisplayed()));

            onView(withId(R.id.button_prepare)).perform(click());

        } else {

            onView(allOf(withContentDescription("Navigate up"),
                    withParent(withId(R.id.toolbar)),
                    isDisplayed())).perform(click());

            onView(allOf(withId(R.id.list_types),
                    childAtPosition(
                            withClassName(is("android.widget.LinearLayout")),
                            0))).perform(actionOnItemAtPosition(
                    TestConstants.IS_TEST_INDEX, click()));

            onView(withId(R.id.buttonRunTest)).perform(click());
        }

        if (hasDilution) {
            onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

            onView(withId(R.id.buttonNoDilution)).perform(click());

        }

//        onView(withId(R.id.layoutWait)).check(matches(isDisplayed()));
//        onView(allOf(withId(R.id.textDilution), withText(R.string.noDilution)))
//                .check(matches(isCompletelyDisplayed()));

        for (int i = 0; i < 7; i++) {
            nextPage();
        }

        Log.i(TAG, "Test 2");

        sleep((DELAY_INITIAL + CUVETTE_TEST_TIME_DELAY + (DELAY_BETWEEN_SAMPLING *
                (AppPreferences.getSamplingTimes() + SKIP_SAMPLING_COUNT + IS_EXTRA_DELAY))) * 1000);
        onView(withText(R.string.retry)).perform(click());

        Log.i(TAG, "Test 3");

        sleep((DELAY_INITIAL + CUVETTE_TEST_TIME_DELAY + (DELAY_BETWEEN_SAMPLING *
                (AppPreferences.getSamplingTimes() + SKIP_SAMPLING_COUNT + IS_EXTRA_DELAY))) * 1000);

        onView(withText(R.string.retry)).check(doesNotExistOrGone());

        onView(withText(R.string.ok)).perform(click());
    }

    @Before
    public void setUp() {

        loadData(mActivityRule.getActivity(), mCurrentLanguage);

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(mActivityRule.getActivity());
        prefs.edit().clear().apply();

        saveCalibration(IS_TEST_NAME + "_Valid", IS_TEST_ID);

        saveCalibration(IS_TEST_NAME + "_NoMatch", IS_TEST_ID);
    }

    @Test
    @RequiresDevice
    public void a_normalRetry() {
        if (!BuildConfig.FLAVOR.equals("experiment")) {
            runTest(IS_TEST_ID, false,
                    false, IS_HAS_DILUTION, true);
        }
    }

    @Test
    @RequiresDevice
    public void b_normalSuccess() {
        if (!BuildConfig.FLAVOR.equals("experiment")) {
            runSuccessTest(IS_TEST_ID, IS_EXPECTED_RESULT, false,
                    false, IS_HAS_DILUTION, true);
        }
    }

    @Test
    @RequiresDevice
    public void c_dilutionRetry() {
        if (!BuildConfig.FLAVOR.equals("experiment")) {
            runTest(IS_TEST_ID, false,
                    false, IS_HAS_DILUTION, true);
        }
    }

    @Test
    @RequiresDevice
    public void d_dilutionSuccess() {
        if (!BuildConfig.FLAVOR.equals("experiment")) {
            runSuccessTest(IS_TEST_ID, IS_EXPECTED_RESULT, false,
                    false, IS_HAS_DILUTION, true);
        }
    }

    @Test
    @RequiresDevice
    public void e_diagnosticRetry() {
        if (!BuildConfig.FLAVOR.equals("experiment")) {
            runTest(IS_TEST_ID, true,
                    false, IS_HAS_DILUTION, true);
        }
    }

    @Test
    @RequiresDevice
    public void f_diagnosticSuccess() {
        if (!BuildConfig.FLAVOR.equals("experiment")) {
            runSuccessTest(IS_TEST_ID, IS_EXPECTED_RESULT, true,
                    false, IS_HAS_DILUTION, true);
        }
    }

    @Test
    @RequiresDevice
    public void g_diagnosticDebugRetry() {
        if (!BuildConfig.FLAVOR.equals("experiment")) {
            runTest(IS_TEST_ID, true,
                    true, IS_HAS_DILUTION, true);
        }
    }

    @Test
    @RequiresDevice
    public void i_internalRetry() {
        runTest(IS_TEST_ID, true,
                false, IS_HAS_DILUTION, false);
    }

    @Test
    @RequiresDevice
    public void j_internalSuccess() {
        runSuccessTest(IS_TEST_ID, IS_EXPECTED_RESULT, true,
                false, IS_HAS_DILUTION, false);
    }

    @Test
    @RequiresDevice
    public void k_internalDebugRetry() {
        runTest(IS_TEST_ID, true,
                true, IS_HAS_DILUTION, false);
    }

    @Test
    @RequiresDevice
    public void l_internalDebugSuccess() {
        runSuccessTest(IS_TEST_ID, IS_EXPECTED_RESULT, true,
                true, IS_HAS_DILUTION, false);
    }

    @Test
    @RequiresDevice
    public void h_diagnosticDebugSuccess() {
        if (!BuildConfig.FLAVOR.equals("experiment")) {
            runSuccessTest(IS_TEST_ID, IS_EXPECTED_RESULT, true,
                    true, IS_HAS_DILUTION, true);
        }
    }

    public void runSuccessTest(String testId, double expectedResult, boolean useDiagnosticMode,
                               boolean showDebugInfo, boolean hasDilution, boolean isExternal) {

        Log.i(TAG, "Test 4");

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        enterDiagnosticMode();

        pressBack();

        if (showDebugInfo) {
            clickListViewItem("Show debug info");
        }

        goToMainScreen();

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
                TestConstants.IS_TEST_INDEX, click()));

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(1000);

        onData(hasToString(startsWith(IS_TEST_NAME + "_Valid"))).perform(click());

        if (!useDiagnosticMode && !BuildConfig.FLAVOR.equals("experiment")) {
            leaveDiagnosticMode();
        }

        if (isExternal) {

            gotoSurveyForm();

            clickExternalSourceButton(testId);

            sleep(1000);

            onView(withId(R.id.button_prepare)).check(matches(isDisplayed()));

            onView(withId(R.id.button_prepare)).perform(click());

        } else {

            onView(withId(R.id.buttonRunTest)).perform(click());
        }

        //Test Start Screen
        takeScreenshot();

        if (hasDilution) {
            onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

            onView(withId(R.id.buttonNoDilution)).perform(click());
        }

        for (int i = 0; i < 7; i++) {
            nextPage();
        }

        onView(withId(R.id.layoutWait)).check(matches(isDisplayed()));

        sleep((DELAY_INITIAL + CUVETTE_TEST_TIME_DELAY + (DELAY_BETWEEN_SAMPLING *
                (AppPreferences.getSamplingTimes() + SKIP_SAMPLING_COUNT + IS_EXTRA_DELAY))) * 1000);

        String resultString = getText(withId(R.id.textResult));

        double result = Double.valueOf(resultString.replace(">", "").trim());
        assertTrue("Result is wrong", result > expectedResult);

        if (showDebugInfo) {
            onView(withText(R.string.ok)).perform(click());
        } else {
            nextPage();
            onView(withText(R.string.acceptResult)).perform(click());
        }

        if (isExternal) {

            mDevice.waitForIdle();

            assertNotNull(mDevice.findObject(By.text(IS_TEST_GROUP)));

            assertNotNull(mDevice.findObject(By.text(IS_TEST_NAME)));

            assertNotNull(mDevice.findObject(By.text(resultString)));
        }
    }
}
