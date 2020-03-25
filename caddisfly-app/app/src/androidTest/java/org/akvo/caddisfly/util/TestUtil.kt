@file:Suppress("DEPRECATION")

package org.akvo.caddisfly.util

import android.os.Build
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.test.InstrumentationRegistry.getContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.uiautomator.*
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.TestConstantKeys
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.AllOf.allOf

/**
 * Utility functions for automated testing
 */
object TestUtil {

    val isEmulator: Boolean
        get() = (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)


    fun getText(matcher: Matcher<View?>?): String? {
        val stringHolder = arrayOf<String?>(null)
        onView(matcher).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View?>? {
                return isAssignableFrom(TextView::class.java)
            }

            override fun getDescription(): String {
                return "getting text from a TextView"
            }

            override fun perform(uiController: UiController?, view: View) {
                val tv = view as TextView //Save, because of check in getConstraints()

                stringHolder[0] = tv.text.toString()
            }
        })
        return stringHolder[0]
    }

    fun sleep(time: Int) {
        SystemClock.sleep(time.toLong())
    }

//    val activityInstance: Activity
//        get() {
//            val activity = arrayOfNulls<Activity?>(1)
//            getInstrumentation().runOnMainSync {
//                val resumedActivities: Collection<*> = ActivityLifecycleMonitorRegistry.getInstance()
//                        .getActivitiesInStage(Stage.RESUMED)
//                if (resumedActivities.iterator().hasNext()) {
//                    activity[0] = resumedActivities.iterator().next() as Activity?
//                }
//            }
//            return activity[0]!!
//        }

    internal fun findButtonInScrollable(name: String?) {
        val listView = UiScrollable(UiSelector().className(ScrollView::class.java.name))
        listView.maxSearchSwipes = 10
        listView.waitForExists(5000)
        try {
            listView.scrollTextIntoView(name)
        } catch (ignored: Exception) {
        }
    }

    fun clickListViewItem(name: String): Boolean {
        val listView = UiScrollable(UiSelector())
        listView.maxSearchSwipes = 4
        listView.waitForExists(3000)
        val listViewItem: UiObject
        try {
            if (listView.scrollTextIntoView(name)) {
                listViewItem = listView.getChildByText(UiSelector()
                        .className(TextView::class.java.name), "" + name + "")
                listViewItem.click()
            } else {
                return false
            }
        } catch (e: UiObjectNotFoundException) {
            return false
        }
        return true
    }

    private fun swipeLeft() {
        if (isEmulator) {
            clickExternalSourceButton(getContext(), TestConstantKeys.NEXT)
        } else {
            mDevice.waitForIdle()
            mDevice.swipe(500, 400, 50, 400, 4)
            mDevice.waitForIdle()
        }
    }

    private fun swipeDown() {
        for (i in 0..2) {
            mDevice.waitForIdle()
            mDevice.swipe(300, 400, 300, 750, 4)
        }
    }

    fun goBack(times: Int) {
        for (i in 0 until times) {
            mDevice.pressBack()
        }
    }

    fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return (parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position))
            }
        }
    }

    @Suppress("unused")
    fun nextPage() {
        onView(allOf(withId(R.id.image_pageRight), isDisplayed())).perform(click())
        mDevice.waitForIdle()
    }

    fun nextSurveyPage(times: Int, tabName: String) {
        var tab: UiObject2? = mDevice.findObject(By.text(tabName))
        if (tab == null) {
            for (i in 0..11) {
                swipeLeft()
                mDevice.waitForIdle()
                tab = mDevice.findObject(By.text(tabName))
                if (tab != null) {
                    break
                }
                tab = mDevice.findObject(By.text("Soil Tests 1"))
                if (tab != null) {
                    for (ii in 0 until times) {
                        mDevice.waitForIdle()
                        swipeLeft()
                        sleep(300)
                        tab = mDevice.findObject(By.text(tabName))
                        if (tab != null) {
                            break
                        }
                    }
                    break
                }
            }
        }
        swipeDown()
        mDevice.waitForIdle()
    }
}