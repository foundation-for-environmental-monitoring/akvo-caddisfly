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

package org.akvo.caddisfly.navigation;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.test.espresso.AmbiguousViewMatcherException;
import androidx.test.espresso.Espresso;
import androidx.test.filters.LargeTest;
import androidx.test.filters.RequiresDevice;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.UiDevice;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.TestConstants;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DecimalFormatSymbols;

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
import static junit.framework.Assert.fail;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.currentHashMap;
import static org.akvo.caddisfly.util.TestHelper.enterDiagnosticMode;
import static org.akvo.caddisfly.util.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.saveCalibration;
import static org.akvo.caddisfly.util.TestUtil.childAtPosition;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SurveyTest {

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
    public void testChangeTestType() {

        goToMainScreen();

        onView(withText(R.string.calibrate)).perform(click());

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

        DecimalFormatSymbols dfs = new DecimalFormatSymbols();

        onView(allOf(withId(R.id.calibrationList),
                childAtPosition(
                        withClassName(is("android.widget.RelativeLayout")),
                        0))).perform(actionOnItemAtPosition(4, click()));

//        onView(withText("0" + dfs.getDecimalSeparator() + "0 mg/l")).check(matches(isDisplayed()));

        Espresso.pressBack();

        Espresso.pressBack();

        Espresso.pressBack();

        onView(withText(R.string.calibrate)).perform(click());

//        onView(withText(currentHashMap.get("chlorine"))).perform(click());

        onView(withText(currentHashMap.get("soilRange"))).perform(click());

        onView(withText("15")).check(matches(isDisplayed()));

        try {
            onView(withText("mg/kg")).check(matches(isDisplayed()));
            fail("Multiple matches not found");
        } catch (AmbiguousViewMatcherException e) {
            // multiple matches found
        }

        Espresso.pressBack();

        onView(withText("4 - 10 ")).perform(click());

        onView(withText("7")).check(matches(isDisplayed()));

        //        onView(withText("0" + dfs.getDecimalSeparator() + "5 mg/l")).check(matches(isDisplayed()));

    }

    @Test
    @RequiresDevice
    public void testStartASurvey() {

        saveCalibration("TestValid", TestConstants.CUVETTE_TEST_ID_1);

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        enterDiagnosticMode();

        goToMainScreen();

        onView(withText(R.string.calibrate)).perform(click());

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

        gotoSurveyForm();

        clickExternalSourceButton(TestConstants.CUVETTE_TEST_ID_1);

        onView(withId(R.id.button_prepare)).check(matches(isDisplayed()));

        onView(withId(R.id.button_prepare)).perform(click());

        onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonDilution1)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonDilution2)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonNoDilution)).perform(click());

        //onView(withId(R.id.buttonStart)).perform(click());

        mDevice.waitForWindowUpdate("", 1000);

    }
}
