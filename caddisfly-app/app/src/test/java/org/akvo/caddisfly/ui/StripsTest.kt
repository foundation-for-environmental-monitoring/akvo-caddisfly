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

package org.akvo.caddisfly.ui

import android.R
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.akvo.caddisfly.R.id
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.common.UnitTestConstants.SAMPLE_TYPE
import org.akvo.caddisfly.common.UnitTestConstants.STRIP_TESTS_COUNT
import org.akvo.caddisfly.common.UnitTestConstants.STRIP_TEST_NAME
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.model.TestSampleType
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.util.toLocalString
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowLooper

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class StripsTest {
    @Test
    fun titleIsCorrect() {
        val intent = Intent()
        intent.putExtra(ConstantKey.TYPE, TestType.STRIP_TEST)
        intent.putExtra(ConstantKey.SAMPLE_TYPE, TestSampleType.WATER)
        val controller: ActivityController<*> = Robolectric.buildActivity(TestListActivity::class.java, intent).create()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        controller.start().visible()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        val activity = controller.get() as Activity
        val textView: TextView = activity.findViewById(id.textToolbarTitle)
        assertEquals(textView.text, "Select Test")
    }

    @Test
    fun testCount() {
        val intent = Intent()
        intent.putExtra(ConstantKey.TYPE, TestType.STRIP_TEST)
        intent.putExtra(ConstantKey.SAMPLE_TYPE, SAMPLE_TYPE)
        val controller: ActivityController<*> = Robolectric.buildActivity(TestListActivity::class.java, intent).create()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        controller.start().visible()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        val activity = controller.get() as Activity
        val recyclerView: RecyclerView = activity.findViewById(id.list_types)
        assertSame(STRIP_TESTS_COUNT, recyclerView.childCount)
        @Suppress("ConstantConditionIf")
        if (STRIP_TESTS_COUNT > 0) {
            val adapter = recyclerView.adapter as TestInfoAdapter?
            recyclerView.adapter
            assertEquals(STRIP_TEST_NAME,
                    adapter!!.getItemAt(1).name)
            assertEquals(STRIP_TEST_NAME,
                    (recyclerView.getChildAt(1).findViewById<View>(id.text_title) as TextView).text)
        }
    }

    @Test
    fun testTitles() {
        val intent = Intent()
        intent.putExtra(ConstantKey.TYPE, TestType.STRIP_TEST)
        intent.putExtra(ConstantKey.SAMPLE_TYPE, SAMPLE_TYPE)
        val controller: ActivityController<*> = Robolectric.buildActivity(TestListActivity::class.java, intent).create()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        controller.start().visible()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        val activity = controller.get() as Activity
        val recyclerView: RecyclerView = activity.findViewById(id.list_types)
        for (i in 0 until recyclerView.childCount) {
            val testInfo: TestInfo = (recyclerView.adapter as TestInfoAdapter?)!!.getItemAt(0)
            val title: String? = testInfo.name!!.toLocalString()
            assertEquals(title,
                    (recyclerView.getChildAt(0).findViewById<View>(id.text_title) as TextView).text)
        }
    }

//    @Test
//    fun clickTest() {
//        val intent = Intent()
//        intent.putExtra(ConstantKey.TYPE, TestType.STRIP_TEST)
//        intent.putExtra(ConstantKey.SAMPLE_TYPE, TestConstants.SAMPLE_TYPE)
//        val controller: ActivityController<*> = Robolectric.buildActivity(TestListActivity::class.java, intent).create()
//
//        Robolectric.flushForegroundThreadScheduler()
//        ShadowLooper.pauseMainLooper()
//
//        controller.start().visible()
//
//        Robolectric.flushForegroundThreadScheduler()
//        ShadowLooper.pauseMainLooper()
//
//        val activity = controller.get() as Activity
//        val recyclerView: RecyclerView = activity.findViewById(id.list_types)
//        assertSame(TestConstants.STRIP_TESTS_COUNT, recyclerView.childCount)
//        if (TestConstants.STRIP_TESTS_COUNT > 0) {
//            recyclerView.getChildAt(0).performClick()
//            ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
//            val nextIntent: Intent = shadowOf(activity).nextStartedActivity
//            if (nextIntent.component != null) {
//                assertEquals(TestActivity::class.java.canonicalName,
//                        nextIntent.component!!.className)
//            }
//        }
//    }

    @Test
    fun clickHome() {
        val intent = Intent()
        intent.putExtra(ConstantKey.TYPE, TestType.STRIP_TEST)
        intent.putExtra(ConstantKey.SAMPLE_TYPE, SAMPLE_TYPE)
        val controller: ActivityController<*> = Robolectric.buildActivity(TestListActivity::class.java, intent).create()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        controller.start().visible()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        val activity = controller.get() as Activity
        val shadowActivity: ShadowActivity = shadowOf(activity)
        shadowActivity.clickMenuItem(R.id.home)
        val nextIntent: Intent? = shadowOf(activity).nextStartedActivity
        assertNull(nextIntent)
    }
}