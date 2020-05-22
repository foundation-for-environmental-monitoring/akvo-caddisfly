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

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.widget.Button
import android.widget.TextView
import org.akvo.caddisfly.R
import org.akvo.caddisfly.preference.SettingsActivity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowLooper


@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class MainTest {
    @Test
    fun titleIsCorrect() {
        val controller =
                Robolectric.buildActivity(MainActivity::class.java).create()

        controller.start()
        val activity: Activity = controller.get()
        assertEquals(activity.title, activity.getString(R.string.app_name))
        val textView: TextView = activity.findViewById(R.id.textToolbarTitle)
        assertEquals(textView.text, activity.getString(R.string.app_name))
    }

//    @Test
//    public void onCreateShouldInflateTheMenu() {
//        Activity activity = Robolectric.setupActivity(MainActivity.class);
//
//        Toolbar toolbar = activity.findViewById(R.id.toolbar);
//        activity.onCreateOptionsMenu(toolbar.getMenu());
//
//        ShadowActivity shadowActivity = shadowOf(activity);
//
//        assertTrue(shadowActivity.getOptionsMenu().hasVisibleItems());
//        assertTrue(shadowActivity.getOptionsMenu().findItem(R.id.actionSettings).isVisible());
//    }


    @Test
    fun onClickSettings() {
        val controller =
                Robolectric.buildActivity(MainActivity::class.java).create()
        controller.start()
        val activity: Activity = controller.get()
        val button: Button = activity.findViewById(R.id.buttonSettings)
        button.performClick()
        val intent: Intent = shadowOf(activity).nextStartedActivity
        if (intent.component != null) {
            assertEquals(SettingsActivity::class.java.canonicalName,
                    intent.component!!.className)
        }
    }

    @Test
    fun clickingCalibrate() {
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val controller: ActivityController<*> = Robolectric.buildActivity(MainActivity::class.java).create().start()
        val activity = controller.get() as Activity
        val button: Button = activity.findViewById(R.id.buttonSettings)
        button.performClick()
        val nextIntent: Intent? = shadowOf(activity).nextStartedActivity
        assertEquals(SettingsActivity::class.java.canonicalName,
                nextIntent!!.component!!.className)
        val application: ShadowApplication = shadowOf(activity.application)
        application.grantPermissions(*permissions)
        controller.resume()
        button.performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        button.performClick()
        val intent: Intent = shadowOf(activity).nextStartedActivity
        if (intent.component != null) {
            assertEquals(SettingsActivity::class.java.canonicalName,
                    intent.component!!.className)
        }
    }
}