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

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.TestConstants;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.repository.TestConfigRepository;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.filters.RequiresDevice;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.UiDevice;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.akvo.caddisfly.util.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.resetLanguage;
import static org.akvo.caddisfly.util.TestHelper.takeScreenshot;
import static org.akvo.caddisfly.util.TestUtil.childAtPosition;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("PMD.NcssMethodCount")
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BluetoothInstructions {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    private StringBuilder jsArrayString = new StringBuilder();

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

        resetLanguage();
    }

    @Test
    @RequiresDevice
    @Ignore
    public void testInstructionsAll() {

        String path = Environment.getExternalStorageDirectory().getPath() + "/"
                + BuildConfig.APPLICATION_ID + "/screenshots";

        File folder = new File(path);
        if (!folder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            folder.mkdirs();
        }

        mDevice.waitForWindowUpdate("", 2000);

        goToMainScreen();

        onView(withText("MD 610 Photometer")).perform(click());

        TestConfigRepository testConfigRepository = new TestConfigRepository();
        List<TestInfo> testList = testConfigRepository.getTests(TestType.BLUETOOTH);
        for (int i = 0; i < TestConstants.MD610_TESTS_COUNT; i++) {

            assertEquals(testList.get(i).getSubtype(), TestType.BLUETOOTH);

            String id = testList.get(i).getUuid();

            id = id.substring(id.lastIndexOf("-") + 1, id.length());

            int pages = navigateToTest(i, id);

            jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],");

        }

//        Log.d("Caddisfly", jsArrayString.toString());

    }

    private int navigateToTest(int index, String id) {

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.list_types),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                0)));
        recyclerView.perform(actionOnItemAtPosition(index, click()));

//        if (TestUtil.isEmulator()) {
//            onView(withText("Bluetooth not supported."))
//                    .inRoot(withDecorView(not(is(mActivityTestRule.getActivity().getWindow()
//                            .getDecorView())))).check(matches(isDisplayed()));
//            return;
//        }

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

        TestUtil.sleep(5000);

        onView(allOf(withId(R.id.button_connect), withText("Connect"))).perform(click());

        TestUtil.sleep(2000);

        onView(withText(R.string.test_selected)).perform(click());

        onView(withText("Instructions")).perform(click());

        int pages = 0;
        for (int i = 0; i < 17; i++) {
            pages++;

            try {

                takeScreenshot(id, i);

                onView(withId(R.id.image_pageRight)).perform(click());

            } catch (Exception e) {
                TestUtil.sleep(300);
                Espresso.pressBack();
                TestUtil.sleep(300);
                Espresso.pressBack();
                TestUtil.sleep(300);
                Espresso.pressBack();
                TestUtil.sleep(300);
                Espresso.pressBack();
                TestUtil.sleep(600);
                Espresso.pressBack();
                TestUtil.sleep(600);
                break;
            }
        }
        return pages;
    }
}
