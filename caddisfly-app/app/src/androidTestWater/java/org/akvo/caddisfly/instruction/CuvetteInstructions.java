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

package org.akvo.caddisfly.instruction;


import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.RequiresDevice;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.TestConstants;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.akvo.caddisfly.common.ChamberTestConfig.DELAY_BETWEEN_SAMPLING;
import static org.akvo.caddisfly.common.ChamberTestConfig.DELAY_INITIAL;
import static org.akvo.caddisfly.common.ChamberTestConfig.SKIP_SAMPLING_COUNT;
import static org.akvo.caddisfly.common.TestConstants.CUVETTE_TEST_TIME_DELAY;
import static org.akvo.caddisfly.common.TestConstants.IS_EXTRA_DELAY;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.getString;
import static org.akvo.caddisfly.util.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.leaveDiagnosticMode;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.takeScreenshot;
import static org.akvo.caddisfly.util.TestUtil.sleep;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CuvetteInstructions {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

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

        loadData(mActivityTestRule.getActivity(), mCurrentLanguage);

//        SharedPreferences prefs =
//                PreferenceManager.getDefaultSharedPreferences(mActivityTestRule.getActivity());
//        prefs.edit().clear().apply();

//        resetLanguage();
    }


    @Test
    @RequiresDevice
    public void testInstructionsNoDilution() {

        int screenShotIndex = -1;

        leaveDiagnosticMode();

//        setJsonVersion(2);

        goToMainScreen();

        gotoSurveyForm();

        clickExternalSourceButton(TestConstants.IS_TEST_ID);

        sleep(1000);

        mDevice.waitForIdle();

        TestUtil.sleep(1000);

        String id = TestConstants.IS_TEST_ID.substring(
                TestConstants.IS_TEST_ID.lastIndexOf("-") + 1
        );

        takeScreenshot(id, screenShotIndex);
        screenShotIndex++;

        mDevice.waitForIdle();

        onView(withText(getString(mActivityTestRule.getActivity(), R.string.next))).perform(click());

        takeScreenshot(id, screenShotIndex);
        screenShotIndex++;

        onView(withText(
                mActivityTestRule.getActivity().getString(R.string.noDilution)))
                .perform(click());

        for (int i = 0; i < 20; i++) {

            try {
                takeScreenshot(id, screenShotIndex);
                screenShotIndex++;
                onView(withId(R.id.image_pageRight)).perform(click());
                mDevice.waitForIdle();
            } catch (Exception e) {
                TestUtil.sleep(600);
                break;
            }
        }

        sleep((DELAY_INITIAL + CUVETTE_TEST_TIME_DELAY + (DELAY_BETWEEN_SAMPLING *
                (AppPreferences.getSamplingTimes() + SKIP_SAMPLING_COUNT + IS_EXTRA_DELAY))) * 1000);

        takeScreenshot(id, screenShotIndex);
        screenShotIndex++;

        onView(withText("Result")).check(matches(isDisplayed()));
//        onView(withText("0.49")).check(matches(isDisplayed()));

//        List<UiObject2> button1s = mDevice.findObjects(By.text(
//                getString(mActivityTestRule.getActivity(), R.string.next)));
//        if (button1s.size() > 0) {
//            button1s.get(0).click();
//        }

        sleep(1000);

        onView(withId(R.id.image_pageRight)).perform(click());

        takeScreenshot(id, screenShotIndex);

        onView(withText("Finish")).check(matches(isDisplayed()));

        List<UiObject2> buttonAccept = mDevice.findObjects(By.text(
                getString(mActivityTestRule.getActivity(), R.string.acceptResult)));
        if (buttonAccept.size() > 0) {
            buttonAccept.get(0).click();
        }

    }

    @Test
    @RequiresDevice
    public void testInstructionsCuvette() {

        int screenShotIndex = -1;

        leaveDiagnosticMode();

//        setJsonVersion(2);

        goToMainScreen();

        gotoSurveyForm();

        clickExternalSourceButton(TestConstants.IS_TEST_ID);

        sleep(1000);

        mDevice.waitForIdle();

        TestUtil.sleep(1000);

        String id = TestConstants.IS_TEST_ID.substring(
                TestConstants.IS_TEST_ID.lastIndexOf("-") + 1
        );

        takeScreenshot(id, screenShotIndex);
        screenShotIndex++;

        mDevice.waitForIdle();

        onView(withText(getString(mActivityTestRule.getActivity(), R.string.next))).perform(click());

        takeScreenshot(id, screenShotIndex);
        screenShotIndex++;

        onView(withText(
                String.format(mActivityTestRule.getActivity().getString(R.string.timesDilution), 2)))
                .perform(click());

        for (int i = 0; i < 20; i++) {

            try {
                takeScreenshot(id, screenShotIndex);
                screenShotIndex++;
                onView(withId(R.id.image_pageRight)).perform(click());

                TestUtil.sleep(500);

            } catch (Exception e) {
                TestUtil.sleep(600);
                break;
            }
        }

        sleep((DELAY_INITIAL + CUVETTE_TEST_TIME_DELAY + (DELAY_BETWEEN_SAMPLING *
                (AppPreferences.getSamplingTimes() + SKIP_SAMPLING_COUNT + IS_EXTRA_DELAY))) * 1000);

        takeScreenshot(id, screenShotIndex);
        screenShotIndex++;

        onView(withText("Result")).check(matches(isDisplayed()));
//        onView(withText("0.49")).check(matches(isDisplayed()));

//        List<UiObject2> button1s = mDevice.findObjects(By.text(
//                getString(mActivityTestRule.getActivity(), R.string.next)));
//        if (button1s.size() > 0) {
//            button1s.get(0).click();
//        }

        sleep(1000);

        onView(withId(R.id.image_pageRight)).perform(click());

        takeScreenshot(id, screenShotIndex);

        onView(withText("Finish")).check(matches(isDisplayed()));

        List<UiObject2> buttonAccept = mDevice.findObjects(By.text(
                getString(mActivityTestRule.getActivity(), R.string.acceptResult)));
        if (buttonAccept.size() > 0) {
            buttonAccept.get(0).click();
        }
    }
}
