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


import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestConstant;
import org.akvo.caddisfly.util.TestUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.resetLanguage;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class ManualTests {

    @Rule
    public IntentsTestRule<MainActivity> mIntentsRule = new IntentsTestRule<>(MainActivity.class);

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

        loadData(mIntentsRule.getActivity(), mCurrentLanguage);

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(mIntentsRule.getActivity());
        prefs.edit().clear().apply();

        resetLanguage();
    }

    @Before
    public void stubCameraIntent() {
        Instrumentation.ActivityResult result = createImageCaptureStub();

        // Stub the Intent.
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result);
    }

    private Instrumentation.ActivityResult createImageCaptureStub() {
        // Put the drawable in a bundle.
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", BitmapFactory.decodeResource(
                mIntentsRule.getActivity().getResources(), R.drawable.place_device));

        // Create the Intent that will include the bundle.
        Intent resultData = new Intent();
        resultData.putExtras(bundle);

        // Create the ActivityResult with the Intent.
        return new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
    }

    @Test
    @Ignore
    public void runManualTurbidityTest() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage(6);

        clickExternalSourceButton(0, TestConstant.GO_TO_TEST);

        SystemClock.sleep(3000);

        onView(withText("Instructions")).check(matches(isDisplayed())).perform(click());

        onView(withText("Connect the turbidity tubes and fill the tube with the water sample until you cannot see the black circle at the yellow bottom anymore."))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText("Empty the tube with small intervals until you can see the black circle at the yellow bottom again and read out the turbidity on the side of the tube."))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText("Fill in the obtained turbidity value in the app."))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText("Empty the tube completely, clean if necessary, disconnect tubes and store them."))
                .check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        onView(withText("Instructions")).check(matches(isDisplayed())).perform(click());

        onView(withText("Connect the turbidity tubes and fill the tube with the water sample until you cannot see the black circle at the yellow bottom anymore."))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText("Empty the tube with small intervals until you can see the black circle at the yellow bottom again and read out the turbidity on the side of the tube."))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText("Fill in the obtained turbidity value in the app."))
                .check(matches(isDisplayed()));

        pressBack();

        onView(withText("Next")).check(matches(isDisplayed())).perform(click());

        TestUtil.sleep(200);

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("12345"), closeSoftKeyboard());

        onView(withText("Submit Result")).perform(click());

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("1234"), closeSoftKeyboard());

        onView(withText("Submit Result")).perform(click());

        assertNotNull(mDevice.findObject(By.text("Turbidity: 1234.0 NTU")));

    }

    @Test
    @Ignore
    public void runManualPhTest() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage(6);

        clickExternalSourceButton(1, TestConstant.GO_TO_TEST);

        SystemClock.sleep(3000);

        onView(withText("Instructions")).check(matches(isDisplayed())).perform(click());

        onView(withText(R.string.sd_on))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_50_dip_sample))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_fill_photo_fill_in))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_turn_off))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_rinse))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_maintenance))
                .check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        onView(withText("Instructions")).check(matches(isDisplayed())).perform(click());

        onView(withText(R.string.sd_on))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_50_dip_sample))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        pressBack();

        onView(withText("Next")).check(matches(isDisplayed())).perform(click());

        TestUtil.sleep(200);

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("12345"), closeSoftKeyboard());

        onView(withText("Submit Result")).perform(click());

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("1234"), closeSoftKeyboard());

        onView(withText("Submit Result")).perform(click());

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("12"), closeSoftKeyboard());

        onView(withText("Submit Result")).perform(click());

        assertNotNull(mDevice.findObject(By.text("pH: 12.0 ")));

    }

    @Test
    @Ignore
    public void runManualEcTest() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage(6);

        clickExternalSourceButton(2, TestConstant.GO_TO_TEST);

        SystemClock.sleep(3000);

        onView(withText("Instructions")).check(matches(isDisplayed())).perform(click());

        onView(withText(R.string.sd_on))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_70_dip_sample))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_fill_photo_fill_in))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_turn_off))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_70_rinse))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_70_maintenance))
                .check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        onView(withText("Instructions")).check(matches(isDisplayed())).perform(click());

        onView(withText(R.string.sd_on))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_70_dip_sample))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        pressBack();

        onView(withText("Next")).check(matches(isDisplayed())).perform(click());

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("12345"), closeSoftKeyboard());

        onView(withText("Submit Result")).perform(click());

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("1234"), closeSoftKeyboard());

        onView(withText("Submit Result")).perform(click());

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("21"), closeSoftKeyboard());

        onView(withText("Submit Result")).perform(click());

        onView(withText("ms/cm")).perform(click());

        onView(withText("Submit Result")).perform(click());

        onView(withId(R.id.editResult)).perform(replaceText("20"));

        onView(withText("Submit Result")).perform(click());

        assertNotNull(mDevice.findObject(By.text("Electrical Conductivity: 20000.0 μS/cm")));

        clickExternalSourceButton(2, TestConstant.GO_TO_TEST);

        SystemClock.sleep(3000);

        onView(withText("Next")).perform(click());

        onView(withId(R.id.editResult)).perform(replaceText("200"));

        onView(withText("Submit Result")).perform(click());

        onView(withText("μS/cm")).perform(click());

        onView(withId(R.id.editResult)).perform(replaceText("20001"));

        onView(withText("Submit Result")).perform(click());

        onView(withId(R.id.editResult)).perform(replaceText("20000"));

        onView(withText("ms/cm")).perform(click());

        onView(withText("Submit Result")).perform(click());

        onView(withText("μS/cm")).perform(click());

        onView(withText("Submit Result")).perform(click());

        assertNotNull(mDevice.findObject(By.text("Electrical Conductivity: 20000.0 μS/cm")));

    }
}
