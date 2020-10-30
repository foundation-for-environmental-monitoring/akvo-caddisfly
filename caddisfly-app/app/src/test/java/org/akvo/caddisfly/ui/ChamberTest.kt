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

import android.Manifest.permission
import android.R
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import junit.framework.TestCase.*
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.R.id
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.common.ConstantKey.SAMPLE_TYPE_KEY
import org.akvo.caddisfly.common.SensorConstants
import org.akvo.caddisfly.common.UnitTestConstants.CUVETTE_TESTS_COUNT
import org.akvo.caddisfly.common.UnitTestConstants.CUVETTE_TEST_ID_1
import org.akvo.caddisfly.common.UnitTestConstants.CUVETTE_TEST_NAME_1
import org.akvo.caddisfly.common.UnitTestConstants.CUVETTE_TEST_NAME_2
import org.akvo.caddisfly.common.UnitTestConstants.CUVETTE_TEST_NAME_3
import org.akvo.caddisfly.common.UnitTestConstants.CUVETTE_TEST_NAME_4
import org.akvo.caddisfly.common.UnitTestConstants.SAMPLE_TYPE
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.model.TestSampleType
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.sensor.chamber.ChamberTestActivity
import org.akvo.caddisfly.util.toLocalString
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.*

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class ChamberTest {
    @Test
    fun titleIsCorrect() {
        val intent = Intent()
        intent.putExtra(ConstantKey.TYPE, TestType.CHAMBER_TEST)
        intent.putExtra(SAMPLE_TYPE_KEY, TestSampleType.WATER)
        val controller: ActivityController<*> =
            Robolectric.buildActivity(TestListActivity::class.java, intent).create()

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
        intent.putExtra(ConstantKey.TYPE, TestType.CHAMBER_TEST)
        intent.putExtra(SAMPLE_TYPE_KEY, SAMPLE_TYPE)
        val controller: ActivityController<*> =
            Robolectric.buildActivity(TestListActivity::class.java, intent).create()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        controller.start().visible()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        val activity = controller.get() as Activity
        val recyclerView: RecyclerView = activity.findViewById(id.list_types)
        assertSame(CUVETTE_TESTS_COUNT, recyclerView.adapter?.itemCount)
        assertTestTitle(recyclerView, 0, CUVETTE_TEST_NAME_1)
        assertTestTitle(recyclerView, 1, CUVETTE_TEST_NAME_2)
        assertTestTitle(recyclerView, 2, CUVETTE_TEST_NAME_3)
        assertTestTitle(recyclerView, 3, CUVETTE_TEST_NAME_4)
    }

    private fun assertTestTitle(recyclerView: RecyclerView, index: Int, title: String) {
        assertEquals(
            title,
            (recyclerView.adapter as TestInfoAdapter?)!!.getItemAt(index).name
        )
        assertEquals(
            title,
            (recyclerView.getChildAt(index).findViewById<View>(id.text_title) as TextView).text
        )
    }

    @Test
    fun testTitles() {
        val intent = Intent()
        intent.putExtra(ConstantKey.TYPE, TestType.CHAMBER_TEST)
        intent.putExtra(SAMPLE_TYPE_KEY, TestSampleType.WATER)
        val controller: ActivityController<*> =
            Robolectric.buildActivity(TestListActivity::class.java, intent).create()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        controller.start().visible()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        val activity = controller.get() as Activity
        val recyclerView: RecyclerView = activity.findViewById(id.list_types)
        for (i in 0 until recyclerView.childCount) {
            val testInfo: TestInfo = (recyclerView.adapter as TestInfoAdapter?)!!.getItemAt(i)
            assertTestTitle(recyclerView, i, testInfo.name!!.toLocalString())
        }
    }

    @Test
    fun clickTest() {
        val permissions = arrayOf(permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE)
        val intent = Intent()
        intent.putExtra(ConstantKey.TYPE, TestType.CHAMBER_TEST)
        intent.putExtra(SAMPLE_TYPE_KEY, SAMPLE_TYPE)
        val controller: ActivityController<*> =
            Robolectric.buildActivity(TestListActivity::class.java, intent).create()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        controller.start().visible()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        val activity = controller.get() as Activity
        val recyclerView: RecyclerView = activity.findViewById(id.list_types)
        recyclerView.getChildAt(1).performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        val nextIntent: Intent? = shadowOf(activity).nextStartedActivity
        assertEquals(nextIntent!!.action, "android.content.pm.action.REQUEST_PERMISSIONS")
//        assertNull(nextIntent)
        val application: ShadowApplication = shadowOf(activity.application)
        application.grantPermissions(*permissions)
        controller.resume()
        val pm: ShadowPackageManager = shadowOf(activity.application.packageManager)
        pm.setSystemFeature(PackageManager.FEATURE_CAMERA, true)
        pm.setSystemFeature(PackageManager.FEATURE_CAMERA_FLASH, true)
        recyclerView.getChildAt(1).performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        val nextIntent2: Intent = shadowOf(activity).nextStartedActivity
        if (nextIntent2.component != null) {
            assertEquals(
                ChamberTestActivity::class.java.canonicalName,
                nextIntent2.component!!.className
            )
        }
    }

    @Test
    fun clickHome() {
        val intent = Intent()
        intent.putExtra(ConstantKey.TYPE, TestType.CHAMBER_TEST)
        intent.putExtra(SAMPLE_TYPE_KEY, TestSampleType.WATER)
        val controller: ActivityController<*> =
            Robolectric.buildActivity(TestListActivity::class.java, intent).create()

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

    @Test
    fun testExternalWithoutPermission() {
        val intent = Intent(BuildConfig.APPLICATION_ID)
        val data = Bundle()
        data.putString(SensorConstants.TEST_ID, CUVETTE_TEST_ID_1)
        data.putString(CADDISFLY_QUESTION_ID, "123")
        data.putString(CADDISFLY_QUESTION_TITLE, "Fluoride")
        data.putString(CADDISFLY_LANGUAGE, "en")
        intent.putExtras(data)
        intent.type = "text/plain"
        val controller: ActivityController<*> =
            Robolectric.buildActivity(TestActivity::class.java, intent).create()
        val activity = controller.get() as Activity
        controller.start()
        val button: Button = activity.findViewById(id.button_prepare)
        button.performClick()
        val alert: AlertDialog? = ShadowAlertDialog.getLatestAlertDialog()
        assertNull(alert)
    }

    @Test
    fun testExternalWithPermission() {
        val permissions = arrayOf(permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE)
        val intent = Intent(BuildConfig.APPLICATION_ID)
        val data = Bundle()
        data.putString(SensorConstants.TEST_ID, CUVETTE_TEST_ID_1)
        data.putString(CADDISFLY_QUESTION_ID, "123")
        data.putString(CADDISFLY_QUESTION_TITLE, "Fluoride")
        data.putString(CADDISFLY_LANGUAGE, "en")
        intent.putExtras(data)
        intent.type = "text/plain"
        val controller: ActivityController<*> =
            Robolectric.buildActivity(TestActivity::class.java, intent).create()
        val activity = controller.get() as Activity
        val application: ShadowApplication = shadowOf(activity.application)
        application.grantPermissions(*permissions)
        val pm: ShadowPackageManager = shadowOf(activity.application.packageManager)
        pm.setSystemFeature(PackageManager.FEATURE_CAMERA, true)
        pm.setSystemFeature(PackageManager.FEATURE_CAMERA_FLASH, true)
        controller.start()
        val button: Button = activity.findViewById(id.button_prepare)
        button.performClick()
        val alert: AlertDialog? = ShadowAlertDialog.getLatestAlertDialog()
        val sAlert: ShadowAlertDialog = shadowOf(alert)
        assertEquals(
            "Calibration for " + CUVETTE_TEST_NAME_1 + " is incomplete\r\n" +
                    "\r\nDo you want to calibrate now?", sAlert.message
        )
    }

//
//    @Test
//    public void testFromExternalActivity() {
//        assertTrue(testExternalActivity(false));
//        assertTrue(testExternalActivity(true));
//    }
//
//    private boolean testExternalActivity(boolean setCalibration) {
//
//        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
//
//        Intent intent = new Intent(AppConfig.EXTERNAL_APP_ACTION);
//
//        Bundle data = new Bundle();
//        data.putString(CADDISFLY_RESOURCE_ID, SensorConstants.FLUORIDE_ID);
//        data.putString(CADDISFLY_QUESTION_ID, "123");
//        data.putString(CADDISFLY_QUESTION_TITLE, "Fluoride");
//        data.putString(CADDISFLY_LANGUAGE, "en");
//
//        intent.putExtras(data);
//        intent.setType("text/plain");
//
//        ActivityController controller = Robolectric.buildActivity(TestActivity.class).withIntent(intent).create();
//        Activity activity = (Activity) controller.get();
//
//        ShadowApplication application = shadowOf(activity.getApplication());
//        application.grantPermissions(permissions);
//
//        ShadowPackageManager pm = shadowOf(RuntimeEnvironment.application.getPackageManager());
//        pm.setSystemFeature(PackageManager.FEATURE_CAMERA, true);
//        pm.setSystemFeature(PackageManager.FEATURE_CAMERA_FLASH, true);
//
//        controller.start();
//
//        if (setCalibration) {
//            controller.stop();
//
//            saveCalibration(" _AutoBackup");
//
//            controller.resume();
//
//            Intent nextIntent = shadowOf(activity).getNextStartedActivity();
//            if (nextIntent.getComponent() != null) {
//                assertEquals(ColorimetryTestActivity.class.getCanonicalName(),
//                        nextIntent.getComponent().getClassName());
//            }
//        } else {
//
//            AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
//            ShadowAlertDialog sAlert = shadowOf(alert);
//
//            assertEquals(sAlert.getTitle().toString(), activity.getString(R.string.cannotStartTest));
//
//            assertTrue(sAlert.getMessage().toString().contains(activity.getString(R.string.doYouWantToCalibrate)));
//        }
//
//        return true;
//    }
//
//    @Test
//    public void testExternalTestStart() {
//
//        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
//        CaddisflyApp.getApp().loadTestConfigurationByUuid(SensorConstants.FLUORIDE_ID);
//
//        Intent intent = new Intent();
//        intent.putExtra("testInfo", new TestInfo());
//        ActivityController controller = Robolectric.buildActivity(ColorimetryTestActivity.class).withIntent(intent).create();
//        Activity activity = (Activity) controller.get();
//
//        ShadowApplication application = shadowOf(activity.getApplication());
//        application.grantPermissions(permissions);
//
//        ShadowPackageManager pm = shadowOf(RuntimeEnvironment.application.getPackageManager());
//        pm.setSystemFeature(PackageManager.FEATURE_CAMERA, true);
//        pm.setSystemFeature(PackageManager.FEATURE_CAMERA_FLASH, true);
//
//        controller.resume();
//
//        Button button = activity.findViewById(R.id.button_prepare);
//        button.performClick();
//
//        Intent nextIntent = shadowOf(activity).getNextStartedActivity();
//        if (nextIntent.getComponent() != null) {
//            assertEquals(SelectDilutionActivity.class.getCanonicalName(),
//                    nextIntent.getComponent().getClassName());
//        }
//    }

    companion object {
        private const val CADDISFLY_QUESTION_ID = "questionId"
        private const val CADDISFLY_QUESTION_TITLE = "questionTitle"
        private const val CADDISFLY_LANGUAGE = "language"
//        fun saveCalibration(name: String) {
//            val file = ("0.0=255  38  186\n"
//                    + "0.5=255  51  129\n"
//                    + "1.0=255  59  89\n"
//                    + "1.5=255  62  55\n"
//                    + "2.0=255  81  34\n")
//            val path: File? = FileHelper.getFilesDir(FileType.CALIBRATION, Constants.FLUORIDE_ID)
//            FileUtil.saveToFile(path, name, file)
//        }
    }
}