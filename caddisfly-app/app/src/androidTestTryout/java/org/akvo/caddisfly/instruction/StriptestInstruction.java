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


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.RequiresDevice;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.TestConstants;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.repository.TestConfigRepository;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestUtil;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import timber.log.Timber;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;
import static org.akvo.caddisfly.util.TestHelper.getString;
import static org.akvo.caddisfly.util.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.takeScreenshot;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class StriptestInstruction {

    private final StringBuilder jsArrayString = new StringBuilder();

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @BeforeClass
    public static void initialize() {
        if (mDevice == null) {
            mDevice = UiDevice.getInstance(getInstrumentation());
        }
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
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
    public void instructionsTest() {

        goToMainScreen();

        onView(withText("Strip Test")).perform(click());

        onView(allOf(withId(R.id.list_types),
                childAtPosition(
                        withClassName(is("android.widget.LinearLayout")),
                        0))).perform(actionOnItemAtPosition(
                14, click()));

        onView(allOf(withId(R.id.button_instructions), withText(R.string.instructions),
                isDisplayed())).perform(click());

        onView(withText(R.string.collect_5ml_mehlich_sample))
                .check(matches(isDisplayed()));

        onView(withText("Phosphorous"))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.add_5_drops_reagent_1))
                .check(matches(isDisplayed()));

//        onView(withText(R.string.swirl_and_mix))
//                .check(matches(isDisplayed()));

        onView(withId(R.id.pager_indicator)).check(matches(isDisplayed()));

        onView(withId(R.id.viewPager))
                .perform(swipeLeft());

        onView(withText(R.string.put_6_drops_of_reagent_in_another_container))
                .check(matches(isDisplayed()));

        onView(withText(R.string.place_tube_in_provided_rack))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

//        onView(withText(R.string.dip_container_15s))
//                .check(matches(isDisplayed()));

//        onView(withText(R.string.shake_excess_water))
//                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        TestUtil.nextPage();

        onView(withText(R.string.place_strip_clr))
                .check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription(R.string.navigate_up),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction button1 = onView(
                allOf(withId(R.id.button_prepare),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                        1),
                                0),
                        isDisplayed()));
        button1.check(matches(isDisplayed()));

        onView(allOf(withId(R.id.button_instructions), withText(R.string.instructions),
                isDisplayed())).perform(click());

        pressBack();

        ViewInteraction button2 = onView(
                allOf(withId(R.id.button_prepare),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                        1),
                                0),
                        isDisplayed()));
        button2.check(matches(isDisplayed()));

        pressBack();

        pressBack();

        onView(withText("Strip Test")).check(matches(isDisplayed()));

    }

    @Test
    @RequiresDevice
    public void testInstructionsAll() {

        goToMainScreen();

        onView(withText(getString(mActivityTestRule.getActivity(), R.string.stripTest))).perform(click());

        TestConfigRepository testConfigRepository = new TestConfigRepository();
        List<TestInfo> testList = testConfigRepository.getTests(TestType.STRIP_TEST);

        for (int i = 0; i < TestConstants.STRIP_TESTS_COUNT; i++) {

            assertEquals(testList.get(i).getSubtype(), TestType.STRIP_TEST);

            String id = testList.get(i).getUuid();
            id = id.substring(id.lastIndexOf("-") + 1);

//            if (id.equalsIgnoreCase("aa4a4e3100c9")) {
            int pages = navigateToTest(i, id);

            jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],");
//            }
        }

        Timber.e(jsArrayString.toString());
    }

    private int navigateToTest(int index, String id) {

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.list_types),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                0)));
        recyclerView.perform(actionOnItemAtPosition(index, click()));

        mDevice.waitForIdle();

        TestUtil.sleep(1000);

        takeScreenshot(id, -1);

        mDevice.waitForIdle();

        onView(withText(getString(mActivityTestRule.getActivity(), R.string.instructions))).perform(click());

        int pages = 0;
        for (int i = 0; i < 17; i++) {
            pages++;

            try {
                takeScreenshot(id, i);

                onView(withId(R.id.image_pageRight)).perform(click());

            } catch (Exception e) {
                TestUtil.sleep(600);
                Espresso.pressBack();
                Espresso.pressBack();
                TestUtil.sleep(600);
                break;
            }
        }
        return pages;
    }
}
