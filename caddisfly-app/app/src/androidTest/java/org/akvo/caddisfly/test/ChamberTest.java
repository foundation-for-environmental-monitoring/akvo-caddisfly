/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.test;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.DatePicker;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.ChamberTestConfig;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.common.TestConstants;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestUtil;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.filters.LargeTest;
import androidx.test.filters.RequiresDevice;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.akvo.caddisfly.common.ChamberTestConfig.DELAY_BETWEEN_SAMPLING;
import static org.akvo.caddisfly.common.ChamberTestConfig.SKIP_SAMPLING_COUNT;
import static org.akvo.caddisfly.common.TestConstants.CUVETTE_TEST_TIME_DELAY;
import static org.akvo.caddisfly.common.TestConstants.DELAY_EXTRA;
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
import static org.akvo.caddisfly.util.TestUtil.getText;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ChamberTest {

    private static final int TEST_START_DELAY = 24;

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

//        resetLanguage();
    }

    @Test
    @RequiresDevice
    public void testFreeChlorine() {
        saveCalibration("TestValidChlorine", Constants.FREE_CHLORINE_ID_2);

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed()));
    }

    @Test
    @RequiresDevice
    public void testStartHighLevelTest() {

        saveCalibration("HighLevelTest", TestConstants.CUVETTE_TEST_ID_1);

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        String version = CaddisflyApp.getAppVersion(false);

        onView(withText(version)).check(matches(isDisplayed()));

        enterDiagnosticMode();

        goToMainScreen();

        try {
            onView(withText(R.string.calibrate)).perform(click());
        } catch (Exception e) {
            onView(withText(R.string.waterCalibrate)).perform(click());
        }

        onView(allOf(withId(R.id.list_types),
                childAtPosition(
                        withClassName(is("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition(
                TestConstants.TEST_INDEX, click()));

        if (TestUtil.isEmulator()) {

            onView(withText(R.string.errorCameraFlashRequired))
                    .inRoot(withDecorView(not(is(mActivityRule.getActivity().getWindow()
                            .getDecorView())))).check(matches(isDisplayed()));
            return;
        }

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(1000);

        clickListViewItem("HighLevelTest");

        sleep(1000);

        goToMainScreen();

        onView(withId(R.id.actionSettings)).perform(click());

        leaveDiagnosticMode();

        try {
            onView(withText(R.string.calibrate)).perform(click());
        } catch (Exception e) {
            onView(withText(R.string.waterCalibrate)).perform(click());
        }

        onView(allOf(withId(R.id.list_types),
                childAtPosition(
                        withClassName(is("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition(
                TestConstants.TEST_INDEX, click()));

        onView(withId(R.id.fabEditCalibration)).perform(click());

//        onView(withText(R.string.save)).perform(click());

        onView(withId(R.id.editExpiryDate)).perform(click());

        Calendar date = Calendar.getInstance();
        date.add(Calendar.MONTH, 2);
        onView(withClassName((Matchers.equalTo(DatePicker.class.getName()))))
                .perform(PickerActions.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                        date.get(Calendar.DATE)));

        onView(withId(android.R.id.button1)).perform(click());

        onView(withText(R.string.save)).perform(click());

        ViewInteraction recyclerView2 = onView(
                allOf(withId(R.id.calibrationList),
                        childAtPosition(
                                withClassName(is("android.widget.RelativeLayout")),
                                0)));
        recyclerView2.perform(actionOnItemAtPosition(4, click()));

        onView(withId(R.id.layoutWait)).check(matches(isDisplayed()));

        sleep((TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY + DELAY_EXTRA
                + (DELAY_BETWEEN_SAMPLING * (ChamberTestConfig.SAMPLING_COUNT_DEFAULT + SKIP_SAMPLING_COUNT)))
                * 1000);

        onView(withId(R.id.buttonOk)).perform(click());

        goToMainScreen();

        gotoSurveyForm();

        clickExternalSourceButton(TestConstants.CUVETTE_TEST_ID_1);

        sleep(1000);

        onView(withId(R.id.button_prepare)).check(matches(isDisplayed()));

        onView(withId(R.id.button_prepare)).perform(click());

        onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonNoDilution)).perform(click());

        onView(allOf(withId(R.id.textDilution), withText(R.string.noDilution)))
                .check(matches(isCompletelyDisplayed()));

        onView(allOf(withId(R.id.textDilution), withText(R.string.noDilution)))
                .check(matches(isCompletelyDisplayed()));

        onView(withId(R.id.layoutWait)).check(matches(isDisplayed()));

        sleep((TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY + DELAY_EXTRA
                + (DELAY_BETWEEN_SAMPLING * (ChamberTestConfig.SAMPLING_COUNT_DEFAULT + SKIP_SAMPLING_COUNT)))
                * 1000);

        onView(withId(R.id.buttonAccept)).perform(click());

        clickExternalSourceButton(TestConstants.CUVETTE_TEST_ID_1);

        onView(withId(R.id.button_prepare)).check(matches(isDisplayed()));

        onView(withId(R.id.button_prepare)).perform(click());

        onView(withId(R.id.buttonDilution1)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonDilution1)).perform(click());

        onView(allOf(withId(R.id.textDilution), withText(String.format(mActivityRule.getActivity()
                .getString(R.string.timesDilution), 2))))
                .check(matches(isCompletelyDisplayed()));

        //Test Start Screen
        takeScreenshot();

        onView(withId(R.id.layoutWait)).check(matches(isDisplayed()));

        sleep((TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY + DELAY_EXTRA
                + (DELAY_BETWEEN_SAMPLING * (ChamberTestConfig.SAMPLING_COUNT_DEFAULT + SKIP_SAMPLING_COUNT)))
                * 1000);

        onView(withText(mActivityRule.getActivity().getString(R.string.testWithDilution)))
                .check(matches(isDisplayed()));

        //High levels found dialog
        takeScreenshot();

        onView(withId(R.id.buttonAccept)).perform(click());

        clickExternalSourceButton(TestConstants.CUVETTE_TEST_ID_1);

        onView(withId(R.id.button_prepare)).check(matches(isDisplayed()));

        onView(withId(R.id.button_prepare)).perform(click());

        onView(withId(R.id.buttonDilution2)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonDilution2)).perform(click());

        onView(allOf(withId(R.id.textDilution), withText(String.format(mActivityRule.getActivity()
                .getString(R.string.timesDilution), 5)))).check(matches(isCompletelyDisplayed()));

        //Test Progress Screen
        takeScreenshot();

        onView(withId(R.id.layoutWait)).check(matches(isDisplayed()));

        sleep((TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY + DELAY_EXTRA
                + (DELAY_BETWEEN_SAMPLING * (ChamberTestConfig.SAMPLING_COUNT_DEFAULT + SKIP_SAMPLING_COUNT)))
                * 1000);

        String resultString = getText(withId(R.id.textResult));
        assertTrue(resultString.contains(">"));

        if (CUVETTE_TEST_TIME_DELAY > 0) {
            double result = Double.valueOf(resultString.replace(">", "").trim());
            assertTrue("Result is wrong", result > 49);
            onView(withText(mActivityRule.getActivity().getString(R.string.testWithDilution)))
                    .check(matches(isDisplayed()));
        } else {
            double result = Double.valueOf(resultString.replace(">", "").trim());
            assertTrue("Result is wrong", result > 9);
            onView(withText(mActivityRule.getActivity().getString(R.string.testWithDilution)))
                    .check(matches(not(isDisplayed())));
        }

        onView(withId(R.id.buttonAccept)).perform(click());

        mDevice.waitForIdle();

        assertNotNull(mDevice.findObject(By.text(resultString)));

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();
    }

    @Test
    @RequiresDevice
    public void testStartNoDilutionTest() {

        saveCalibration("TestValid", TestConstants.CUVETTE_TEST_ID_1);

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        String version = CaddisflyApp.getAppVersion(false);

        onView(withText(version)).check(matches(isDisplayed()));

        enterDiagnosticMode();

        goToMainScreen();

        try {
            onView(withText(R.string.calibrate)).perform(click());
        } catch (Exception e) {
            onView(withText(R.string.waterCalibrate)).perform(click());
        }

        onView(allOf(withId(R.id.list_types),
                childAtPosition(
                        withClassName(is("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition(
                TestConstants.TEST_INDEX, click()));

        if (TestUtil.isEmulator()) {

            onView(withText(R.string.errorCameraFlashRequired))
                    .inRoot(withDecorView(not(is(mActivityRule.getActivity().getWindow()
                            .getDecorView())))).check(matches(isDisplayed()));
            return;
        }

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(1000);

        onData(hasToString(startsWith("TestValid"))).perform(click());

        goToMainScreen();

        onView(withId(R.id.actionSettings)).perform(click());

        leaveDiagnosticMode();

        try {
            onView(withText(R.string.calibrate)).perform(click());
        } catch (Exception e) {
            onView(withText(R.string.waterCalibrate)).perform(click());
        }

        onView(allOf(withId(R.id.list_types),
                childAtPosition(
                        withClassName(is("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition(
                TestConstants.TEST_INDEX, click()));

        onView(withId(R.id.fabEditCalibration)).perform(click());

//        onView(withText(R.string.save)).perform(click());

        onView(withId(R.id.editExpiryDate)).perform(click());

        Calendar date = Calendar.getInstance();
        date.add(Calendar.MONTH, 2);
        onView(withClassName((Matchers.equalTo(DatePicker.class.getName()))))
                .perform(PickerActions.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                        date.get(Calendar.DATE)));

        onView(withId(android.R.id.button1)).perform(click());

        onView(withText(R.string.save)).perform(click());

        ViewInteraction recyclerView2 = onView(
                allOf(withId(R.id.calibrationList),
                        childAtPosition(
                                withClassName(is("android.widget.RelativeLayout")),
                                0)));
        recyclerView2.perform(actionOnItemAtPosition(2, click()));

        sleep((TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY + DELAY_EXTRA
                + (DELAY_BETWEEN_SAMPLING * (ChamberTestConfig.SAMPLING_COUNT_DEFAULT + SKIP_SAMPLING_COUNT)))
                * 1000);

        onView(withId(R.id.buttonOk)).perform(click());

        goToMainScreen();

        gotoSurveyForm();

        sleep(1000);

        clickExternalSourceButton(TestConstants.CUVETTE_TEST_ID_1);

        sleep(1000);

        onView(withId(R.id.button_prepare)).check(matches(isDisplayed()));

        onView(withId(R.id.button_prepare)).perform(click());

        onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonNoDilution)).perform(click());

        sleep((TEST_START_DELAY + CUVETTE_TEST_TIME_DELAY + DELAY_EXTRA
                + (DELAY_BETWEEN_SAMPLING * (ChamberTestConfig.SAMPLING_COUNT_DEFAULT + SKIP_SAMPLING_COUNT)))
                * 1000);

        //Result dialog
        takeScreenshot();

        String resultString = getText(withId(R.id.textResult));

        onView(withId(R.id.buttonAccept)).perform(click());

        mDevice.waitForIdle();

        assertNotNull(mDevice.findObject(By.text(resultString)));

//        onView(withId(android.R.id.list)).check(matches(withChildCount(is(greaterThan(0)))));
//        onView(withText(R.string.startTestConfirm)).check(matches(isDisplayed()));

    }

}
