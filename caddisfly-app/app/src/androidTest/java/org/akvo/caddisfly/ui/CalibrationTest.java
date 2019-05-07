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

package org.akvo.caddisfly.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.DatePicker;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.RequiresDevice;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.TestConstants;
import org.akvo.caddisfly.util.TestUtil;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
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
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.currentHashMap;
import static org.akvo.caddisfly.util.TestHelper.enterDiagnosticMode;
import static org.akvo.caddisfly.util.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.leaveDiagnosticMode;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.saveCalibration;
import static org.akvo.caddisfly.util.TestUtil.childAtPosition;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.object.HasToString.hasToString;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CalibrationTest {

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
    @Ignore
    public void testOutOfSequence() {

        saveCalibration("OutOfSequence", TestConstants.CUVETTE_TEST_ID_1);

        goToMainScreen();

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        enterDiagnosticMode();

        goToMainScreen();

        onView(withText(R.string.calibrate)).perform(click());

        sleep(4000);

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

        sleep(2000);

        onData(hasToString(startsWith("OutOfSequence"))).perform(click());

        sleep(2000);

        onView(withText(String.format("%s. %s", mActivityRule.getActivity().getString(R.string.calibrationIsInvalid),
                mActivityRule.getActivity().getString(R.string.tryRecalibrating)))).check(matches(isDisplayed()));

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(2000);

        onData(hasToString(startsWith("TestValid"))).perform(click());

        sleep(2000);

        onView(withText(String.format("%s. %s", mActivityRule.getActivity().getString(R.string.calibrationIsInvalid),
                mActivityRule.getActivity().getString(R.string.tryRecalibrating)))).check(matches(not(isDisplayed())));

        sleep(2000);

        leaveDiagnosticMode();

        onView(withText(R.string.calibrate)).perform(click());

    }

    @Test
    @RequiresDevice
    public void testExpiryDate() {

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        enterDiagnosticMode();

        Espresso.pressBack();

        Espresso.pressBack();

        onView(withText(R.string.calibrate)).perform(click());

        sleep(500);

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

        sleep(500);

        onData(hasToString(startsWith("TestValid"))).perform(click());

        sleep(500);

        leaveDiagnosticMode();

        onView(withText(R.string.calibrate)).perform(click());

        onView(allOf(withId(R.id.list_types),
                childAtPosition(
                        withClassName(is("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition(
                TestConstants.TEST_INDEX, click()));

        onView(withId(R.id.fabEditCalibration)).perform(click());

//        onView(withId(R.id.editBatchCode))
//                .perform(clearText(), closeSoftKeyboard());
//
//        onView(withId(R.id.editBatchCode))
//                .perform(typeText("    "), closeSoftKeyboard());

//        onView(withText(R.string.save)).perform(click());

        onView(withId(R.id.editExpiryDate)).perform(click());

        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, -1);
        onView(withClassName((Matchers.equalTo(DatePicker.class.getName()))))
                .perform(PickerActions.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                        date.get(Calendar.DATE)));

        onView(withId(android.R.id.button1)).perform(click());

        onView(withText(R.string.save)).perform(click());

//        onView(withId(R.id.editBatchCode))
//                .perform(typeText("TEST 123#*@!"), closeSoftKeyboard());
//
//        onView(withText(R.string.save)).perform(click());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            onView(withText((String.format("%s. %s", mActivityRule.getActivity().getString(R.string.expired),
                    mActivityRule.getActivity().getString(R.string.calibrateWithNewReagent)))))
                    .check(matches(isDisplayed()));
        }

        onView(withId(R.id.fabEditCalibration)).perform(click());

        mDevice.pressBack();

        goToMainScreen();

        gotoSurveyForm();

        clickExternalSourceButton(TestConstants.CUVETTE_TEST_ID_1);

        sleep(500);

        onView(withId(R.id.button_prepare)).check(matches(isDisplayed()));

        onView(withId(R.id.button_prepare)).perform(click());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String message = String.format("%s%n%n%s",
                    mActivityRule.getActivity().getString(R.string.errorCalibrationExpired),
                    mActivityRule.getActivity().getString(R.string.orderFreshBatch));
            onView(withText(message)).check(matches(isDisplayed()));
            onView(withText(R.string.ok)).perform(click());
        }

        mActivityRule.launchActivity(new Intent());

        onView(withText(R.string.calibrate)).perform(click());

//        onView(withText(currentHashMap.get(TestConstant.FLUORIDE))).perform(click());

        onView(allOf(withId(R.id.list_types),
                childAtPosition(
                        withClassName(is("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition(
                TestConstants.TEST_INDEX, click()));

        onView(withId(R.id.fabEditCalibration)).perform(click());

//        onView(withId(R.id.editBatchCode))
//                .perform(typeText("NEW BATCH"), closeSoftKeyboard());

        onView(withId(R.id.editExpiryDate)).perform(click());

        date.add(Calendar.DATE, 364);
        onView(withClassName((Matchers.equalTo(DatePicker.class.getName()))))
                .perform(PickerActions.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                        date.get(Calendar.DATE)));

        onView(withId(android.R.id.button1)).perform(click());

        onView(withText(R.string.save)).perform(click());

        onView(withId(R.id.textCalibrationError)).check(matches(not(isDisplayed())));

        goToMainScreen();

        gotoSurveyForm();

        clickExternalSourceButton(TestConstants.CUVETTE_TEST_ID_1);

        sleep(500);

        onView(withId(R.id.button_prepare)).check(matches(isDisplayed()));

        onView(withId(R.id.button_prepare)).perform(click());

        onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

    }

    //@Test
    public void testIncompleteCalibration() {

        gotoSurveyForm();

        clickExternalSourceButton(0);

        mDevice.waitForWindowUpdate("", 2000);

        onView(withText(R.string.cannotStartTest)).check(matches(isDisplayed()));

        String message = mActivityRule.getActivity().getString(R.string.errorCalibrationIncomplete,
                currentHashMap.get("chlorine"));
        message = String.format("%s%n%n%s", message,
                mActivityRule.getActivity().getString(R.string.doYouWantToCalibrate));

        onView(withText(message)).check(matches(isDisplayed()));

        onView(withText(R.string.cancel)).check(matches(isDisplayed()));

        onView(withText(R.string.calibrate)).check(matches(isDisplayed()));

        onView(withId(android.R.id.button2)).perform(click());

    }

}
