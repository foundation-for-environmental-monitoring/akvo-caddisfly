package org.akvo.caddisfly.test;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import androidx.test.filters.RequiresDevice;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;
import static org.akvo.caddisfly.util.TestHelper.activateTestMode;
import static org.akvo.caddisfly.util.TestHelper.clearPreferences;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestUtil.sleep;

public class StriptestTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @BeforeClass
    public static void initialize() {
        if (mDevice == null) {
            mDevice = UiDevice.getInstance(getInstrumentation());
        }
    }

    @Before
    public void setUp() {

        loadData(mActivityRule.getActivity(), mCurrentLanguage);

        clearPreferences(mActivityRule);

//        resetLanguage();
    }

    @Test
    @RequiresDevice
    public void startStriptest() {

        activateTestMode(mActivityRule.getActivity());

        testArsenic();

        testArsenic2();
    }

    @After
    public void tearDown() {
        clearPreferences(mActivityRule);
    }

    private void testArsenic() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage(3, "Arsenic");

        clickExternalSourceButton(0);

        mDevice.waitForIdle();

        sleep(1000);

        onView(withText("Prepare for test")).perform(click());

        sleep(11000);

        onView(withText("Start")).perform(click());

        sleep(5000);

        onView(withText("Result")).check(matches(isDisplayed()));
        onView(withText("Arsenic")).check(matches(isDisplayed()));
        onView(withText("No Result")).check(matches(isDisplayed()));

        onView(withId(R.id.image_result)).check(matches(isDisplayed()));

        onView(withText("Save")).check(matches(isDisplayed()));

        onView(withText("Save")).perform(click());

//        assertNotNull(mDevice.findObject(By.text("Result: ")));
//        assertNotNull(mDevice.findObject(By.text("20")));
    }

    private void testArsenic2() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage(3, "Arsenic");

        clickExternalSourceButton(2);

        mDevice.waitForIdle();

        sleep(1000);

        onView(withText("Prepare for test")).perform(click());

        sleep(8000);

        onView(withText("Start")).perform(click());

        sleep(5000);

        onView(withText("Result")).check(matches(isDisplayed()));
        onView(withText("Arsenic")).check(matches(isDisplayed()));
        onView(withText("No Result")).check(matches(isDisplayed()));

        onView(withId(R.id.image_result)).check(matches(isDisplayed()));

        onView(withText("Save")).check(matches(isDisplayed()));

        onView(withText("Save")).perform(click());

        assertNotNull(mDevice.findObject(By.text("Unit: ")));
        assertNotNull(mDevice.findObject(By.text("ug/l")));
    }


}
