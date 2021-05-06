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

import android.app.Activity;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.preference.SettingsActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLooper;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class MainTest {

    @Test
    public void titleIsCorrect() {
        Activity activity = Robolectric.setupActivity(MainActivity.class);
        assertEquals(activity.getTitle(), activity.getString(R.string.app_name));

        TextView textView = activity.findViewById(R.id.textToolbarTitle);
        assertEquals(textView.getText(), activity.getString(R.string.app_name));

    }

    @Test
    public void onCreateShouldInflateTheMenu() {
        Activity activity = Robolectric.setupActivity(MainActivity.class);

        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        ShadowActivity shadowActivity = shadowOf(activity);
//        shadowActivity.onCreateOptionsMenu(toolbar.getMenu());
        assertTrue(shadowActivity.getOptionsMenu().hasVisibleItems());
        assertEquals(shadowActivity.getOptionsMenu().findItem(R.id.actionSettings).isVisible(), true);
    }

    @Test
    public void onClickSettings() {
        Activity activity = Robolectric.setupActivity(MainActivity.class);

        ActionMenuItemView button = activity.findViewById(R.id.actionSettings);

        button.performClick();
        Intent intent = shadowOf(activity).getNextStartedActivity();
        if (intent.getComponent() != null) {
            assertEquals(SettingsActivity.class.getCanonicalName(),
                    intent.getComponent().getClassName());
        }
    }

    @Test
    public void stripTest() {
        Activity activity = Robolectric.setupActivity(MainActivity.class);

        Button button = activity.findViewById(R.id.buttonStripTest);

        button.performClick();
        Intent intent = shadowOf(activity).getNextStartedActivity();
        if (intent.getComponent() != null) {
            assertEquals(TestListActivity.class.getCanonicalName(),
                    intent.getComponent().getClassName());
        }
    }

    @Test
    public void clickingCalibrate() {

        ActivityController controller = Robolectric.buildActivity(MainActivity.class).create().start();
        Activity activity = (Activity) controller.get();

        Button button = activity.findViewById(R.id.buttonCalibrateWater);

        button.performClick();

        Intent nextIntent = shadowOf(activity).getNextStartedActivity();

        assertNull(nextIntent);

        ShadowApplication application = shadowOf(activity.getApplication());
        controller.resume();

        button.performClick();

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        button.performClick();
        Intent intent = shadowOf(activity).getNextStartedActivity();
        if (intent.getComponent() != null) {
            assertEquals(TestListActivity.class.getCanonicalName(),
                    intent.getComponent().getClassName());
        }
    }

}
