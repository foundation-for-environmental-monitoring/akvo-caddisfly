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
import static org.akvo.caddisfly.common.TestConstants.CUVETTE_TEST_TIME_DELAY;
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
import static org.akvo.caddisfly.util.TestUtil.clickListViewItem;
import static org.akvo.caddisfly.util.TestUtil.doesNotExistOrGone;
import static org.akvo.caddisfly.util.TestUtil.getText;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RetryTest {

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
        testRetryTest(TestConstants.CUVETTE_TEST_ID_1, 3.9, false,
                false, false);
    }

    @Test
    @RequiresDevice
    public void testDilutionRetry() {
        testRetryTest(TestConstants.CUVETTE_TEST_ID_1, 1.4, false,
                false, true);
    }

    @Test
    @RequiresDevice
    public void testDiagnosticRetry() {
        testRetryTest(TestConstants.CUVETTE_TEST_ID_1, 3.9, true,
                false, false);
    }

    @Test
    @RequiresDevice
    public void testDiagnosticDebugRetry() {
        testRetryTest(TestConstants.CUVETTE_TEST_ID_1, 3.9, true,
                true, false);
    }

    public void testRetryTest(String testId, double expectedResult, boolean useDiagnosticMode,
                              boolean showDebugInfo, boolean hasDilution) {

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

        gotoSurveyForm();

        clickExternalSourceButton(testId);

        sleep(1000);

        onView(withId(R.id.button_prepare)).check(matches(isDisplayed()));

        onView(withId(R.id.button_prepare)).perform(click());

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

        onView(withText(R.string.retry)).perform(click());

        sleep((TEST_START_DELAY +
                (DELAY_BETWEEN_SAMPLING * ChamberTestConfig.SAMPLING_COUNT_DEFAULT))
                * 1000);

        onView(withText(R.string.retry)).check(doesNotExistOrGone());

        onView(withText(R.string.ok)).perform(click());

        clickExternalSourceButton(TestConstants.CUVETTE_TEST_ID_1);

        onView(withId(R.id.button_prepare)).check(matches(isDisplayed()));

        onView(withId(R.id.button_prepare)).perform(click());

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

        mDevice.waitForIdle();

        assertNotNull(mDevice.findObject(By.text("Soil Tests 1")));

        assertNotNull(mDevice.findObject(By.text("pH")));

        assertNotNull(mDevice.findObject(By.text(resultString)));

    }
}
