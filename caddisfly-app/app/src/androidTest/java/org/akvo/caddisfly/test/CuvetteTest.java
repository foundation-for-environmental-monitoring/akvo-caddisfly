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

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.RequiresDevice;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.common.TestConstants;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestUtil;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.akvo.caddisfly.common.ChamberTestConfig.DELAY_BETWEEN_SAMPLING;
import static org.akvo.caddisfly.common.ChamberTestConfig.DELAY_INITIAL;
import static org.akvo.caddisfly.common.ChamberTestConfig.SKIP_SAMPLING_COUNT;
import static org.akvo.caddisfly.common.TestConstants.CUVETTE_TEST_TIME_DELAY;
import static org.akvo.caddisfly.common.TestConstants.IS_EXTRA_DELAY;
import static org.akvo.caddisfly.common.TestConstants.IS_TIME_DELAY;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.enterDiagnosticMode;
import static org.akvo.caddisfly.util.TestHelper.goToCalibrate;
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
import static org.akvo.caddisfly.util.TestUtil.nextPage;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CuvetteTest {

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

        saveCalibration(TestConstants.IS_TEST_HIGH_CALIBRATION, TestConstants.IS_TEST_ID);

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        String version = CaddisflyApp.getAppVersion(false);

        onView(withText(version)).check(matches(isDisplayed()));

        enterDiagnosticMode();

        goToMainScreen();

        goToCalibrate();

        onView(allOf(withId(R.id.list_types),
                childAtPosition(
                        withClassName(is("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition(
                TestConstants.IS_TEST_INDEX, click()));

        if (TestUtil.isEmulator()) {

            onView(withText(R.string.errorCameraFlashRequired))
                    .inRoot(withDecorView(not(is(mActivityRule.getActivity().getWindow()
                            .getDecorView())))).check(matches(isDisplayed()));
            return;
        }

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(1000);

        clickListViewItem(TestConstants.IS_TEST_HIGH_CALIBRATION);

        sleep(1000);

        goToMainScreen();

        onView(withId(R.id.actionSettings)).perform(click());

        leaveDiagnosticMode();

        goToCalibrate();

        onView(allOf(withId(R.id.list_types),
                childAtPosition(
                        withClassName(is("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition(
                TestConstants.IS_TEST_INDEX, click()));

        onView(withId(R.id.fabEditCalibration)).perform(click());

        onView(withText(R.string.save)).perform(click());

        onView(withId(R.id.fabEditCalibration)).perform(click());

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
        recyclerView2.perform(actionOnItemAtPosition(TestConstants.IS_TEST_CALIBRATION_INDEX, click()));

        onView(withId(R.id.layoutWait)).check(matches(isDisplayed()));

        sleep((DELAY_INITIAL + CUVETTE_TEST_TIME_DELAY + (DELAY_BETWEEN_SAMPLING *
                (AppPreferences.getSamplingTimes() + SKIP_SAMPLING_COUNT + IS_EXTRA_DELAY))) * 1000);

        onView(withId(R.id.buttonOk)).perform(click());

        goToMainScreen();

        gotoSurveyForm();

        clickExternalSourceButton(TestConstants.IS_TEST_ID);

        sleep(1000);

        onView(withId(R.id.button_prepare)).check(matches(isDisplayed()));

        onView(withId(R.id.button_prepare)).perform(click());

        if (TestConstants.IS_HAS_DILUTION) {

            onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

            onView(withId(R.id.buttonNoDilution)).perform(click());
        }

        for (int i = 0; i < 7; i++) {
            nextPage();
        }

        sleep((DELAY_INITIAL + CUVETTE_TEST_TIME_DELAY + (DELAY_BETWEEN_SAMPLING *
                (AppPreferences.getSamplingTimes() + SKIP_SAMPLING_COUNT + IS_EXTRA_DELAY))) * 1000);

        nextPage();

        onView(withText(R.string.acceptResult)).perform(click());

        clickExternalSourceButton(TestConstants.IS_TEST_ID);

        onView(withId(R.id.button_prepare)).check(matches(isDisplayed()));

        onView(withId(R.id.button_prepare)).perform(click());

        if (TestConstants.IS_HAS_DILUTION) {

            onView(withId(R.id.buttonDilution1)).check(matches(isDisplayed()));

            onView(withId(R.id.buttonDilution1)).perform(click());

        }

        //Test Start Screen
        takeScreenshot();

        for (int i = 0; i < 8; i++) {
            nextPage();
        }

        sleep((DELAY_INITIAL + CUVETTE_TEST_TIME_DELAY + (DELAY_BETWEEN_SAMPLING *
                (AppPreferences.getSamplingTimes() + SKIP_SAMPLING_COUNT + IS_EXTRA_DELAY))) * 1000);

        //High levels found dialog
        takeScreenshot();

//        onView(allOf(withId(R.id.textDilution), withText(String.format(mActivityRule.getActivity()
//                .getString(R.string.timesDilution), 2))))
//                .check(matches(isCompletelyDisplayed()));

        nextPage();

        if (TestConstants.IS_HAS_DILUTION) {
//            onView(withText(mActivityRule.getActivity().getString(R.string.highLevelRecommendation)))
//                    .check(matches(isDisplayed()));
            onView(withText(mActivityRule.getActivity().getString(R.string.retest)))
                    .check(matches(isDisplayed()));
        }

        onView(withText(R.string.acceptResult)).perform(click());

        clickExternalSourceButton(TestConstants.IS_TEST_ID);

        onView(withId(R.id.button_prepare)).check(matches(isDisplayed()));

        onView(withId(R.id.button_prepare)).perform(click());

        if (TestConstants.IS_HAS_DILUTION) {

            onView(withId(R.id.buttonDilution2)).check(matches(isDisplayed()));

            onView(withId(R.id.buttonDilution2)).perform(click());
        }

        //Test Progress Screen
        takeScreenshot();

        for (int i = 0; i < 8; i++) {
            nextPage();
        }

        sleep((DELAY_INITIAL + CUVETTE_TEST_TIME_DELAY + (DELAY_BETWEEN_SAMPLING *
                (AppPreferences.getSamplingTimes() + SKIP_SAMPLING_COUNT + IS_EXTRA_DELAY))) * 1000);

        String resultString = getText(withId(R.id.textResult));
        assertTrue(resultString.contains(">"));

        if (IS_TIME_DELAY > 0) {
            double result = Double.valueOf(resultString.replace(">", "").trim());
            assertTrue("Result is wrong", result > 49);
            onView(withText(mActivityRule.getActivity().getString(R.string.testWithDilution)))
                    .check(matches(isDisplayed()));
        } else {
            double result = Double.valueOf(resultString.replace(">", "").trim());
            assertTrue("Result is wrong", result > 9);
//            onView(withText(mActivityRule.getActivity().getString(R.string.testWithDilution)))
//                    .check(matches(not(isDisplayed())));
        }

        nextPage();

//        onView(allOf(withId(R.id.textDilution), withText(String.format(mActivityRule.getActivity()
//                .getString(R.string.timesDilution), 5)))).check(matches(isCompletelyDisplayed()));

        onView(withText(R.string.acceptResult)).perform(click());

        mDevice.waitForIdle();

        assertNotNull(mDevice.findObject(By.text(resultString)));

    }

    @Test
    @RequiresDevice
    public void testStartNoDilutionTest() {

        saveCalibration(TestConstants.IS_TEST_VALID_CALIBRATION, TestConstants.IS_TEST_ID);

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        String version = CaddisflyApp.getAppVersion(false);

        onView(withText(version)).check(matches(isDisplayed()));

        enterDiagnosticMode();

        goToMainScreen();

        goToCalibrate();

        onView(allOf(withId(R.id.list_types),
                childAtPosition(
                        withClassName(is("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition(
                TestConstants.IS_TEST_INDEX, click()));

        if (TestUtil.isEmulator()) {

            onView(withText(R.string.errorCameraFlashRequired))
                    .inRoot(withDecorView(not(is(mActivityRule.getActivity().getWindow()
                            .getDecorView())))).check(matches(isDisplayed()));
            return;
        }

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(1000);

        onData(hasToString(startsWith(TestConstants.IS_TEST_VALID_CALIBRATION))).perform(click());

        goToMainScreen();

        onView(withId(R.id.actionSettings)).perform(click());

        leaveDiagnosticMode();

        goToCalibrate();

        onView(allOf(withId(R.id.list_types),
                childAtPosition(
                        withClassName(is("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition(
                TestConstants.IS_TEST_INDEX, click()));

        onView(withId(R.id.fabEditCalibration)).perform(click());

        onView(withText(R.string.save)).perform(click());

        onView(withId(R.id.fabEditCalibration)).perform(click());

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
        recyclerView2.perform(actionOnItemAtPosition(TestConstants.IS_TEST_CALIBRATION_INDEX, click()));

        sleep((DELAY_INITIAL + CUVETTE_TEST_TIME_DELAY + (DELAY_BETWEEN_SAMPLING *
                (AppPreferences.getSamplingTimes() + SKIP_SAMPLING_COUNT + IS_EXTRA_DELAY))) * 1000);

        onView(withId(R.id.buttonOk)).perform(click());

        goToMainScreen();

        gotoSurveyForm();

        sleep(1000);

        clickExternalSourceButton(TestConstants.IS_TEST_ID);

        sleep(1000);

        onView(withId(R.id.button_prepare)).check(matches(isDisplayed()));

        onView(withId(R.id.button_prepare)).perform(click());

        if (TestConstants.IS_HAS_DILUTION) {

            onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

            onView(withId(R.id.buttonNoDilution)).perform(click());
        }

        for (int i = 0; i < 7; i++) {
            nextPage();
        }

        sleep((DELAY_INITIAL + CUVETTE_TEST_TIME_DELAY + (DELAY_BETWEEN_SAMPLING *
                (AppPreferences.getSamplingTimes() + SKIP_SAMPLING_COUNT + IS_EXTRA_DELAY))) * 1000);

        //Result dialog
        takeScreenshot();

        String resultString = getText(withId(R.id.textResult));

        nextPage();

        onView(withText(R.string.acceptResult)).perform(click());

        mDevice.waitForIdle();

        assertNotNull(mDevice.findObject(By.text(resultString)));

//        onView(withId(android.R.id.list)).check(matches(withChildCount(is(greaterThan(0)))));
//        onView(withText(R.string.startTestConfirm)).check(matches(isDisplayed()));

    }

}
