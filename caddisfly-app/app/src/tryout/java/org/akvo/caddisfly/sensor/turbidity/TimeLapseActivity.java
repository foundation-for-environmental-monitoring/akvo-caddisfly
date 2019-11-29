package org.akvo.caddisfly.sensor.turbidity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceFragmentCompat;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.PermissionsDelegate;
import org.akvo.caddisfly.helper.SoundUtil;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.AssetsManager;
import org.akvo.caddisfly.util.GMailSender;
import org.akvo.caddisfly.util.NetUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static org.akvo.caddisfly.sensor.turbidity.TimeLapseResultActivity.imageFilter;
import static org.akvo.caddisfly.util.ApiUtil.isAppInLockTaskMode;

public class TimeLapseActivity extends BaseActivity {

    private static final int INITIAL_DELAY = 25000;

    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    TextView textInterval;
    int interval = 0;
    int numberOfSamples;
    boolean isTurbid;
    String durationString;
    private View layoutWait;
    private LinearLayout layoutDetails;
    private TextView textCountdown;
    private Calendar futureDate;
    private Runnable runnable;
    private Handler handler;
    private TestInfo testInfo;
    private String folderName;
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            SoundUtil.playShortResource(context, R.raw.beep);

            int delayMinute;

            folderName = intent.getStringExtra(ConstantKey.SAVE_FOLDER);
            File folder = FileHelper.getFilesDir(FileHelper.FileType.TEMP_IMAGE, folderName);

            delayMinute = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                    R.string.colif_intervalMinutesKey, "1"));

            numberOfSamples = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                    R.string.colif_numberOfSamplesKey, "1"));

            File[] files = folder.listFiles();
            if (files != null) {
                if (files.length >= numberOfSamples) {
                    TurbidityConfig.stopRepeatingAlarm(context, Constants.COLIFORM_ID);
                    showResult();
                    finish();
                } else {
                    textInterval.setText(String.format(Locale.getDefault(),
                            "Done: %d of %d", files.length, numberOfSamples));
                    futureDate = Calendar.getInstance();
                    futureDate.add(Calendar.MINUTE, delayMinute);
                    if (files.length > 1) {
                        if (!PreferencesUtil.getBoolean(getBaseContext(),
                                ConstantKey.TURBID_EMAIL_SENT, false)) {
                            analyseLatestFile(folder);
                        }
                    } else {
                        PreferencesUtil.setBoolean(getBaseContext(),
                                ConstantKey.TURBID_EMAIL_SENT, false);
                    }
                }
            }
        }
    };
    private boolean showTimer = true;
    private Menu menu;

    private void analyseLatestFile(File folder) {
        File firstImage = null;
        File turbidImage = null;
        File lastImage = null;

        if (folder.isDirectory()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.US);

            File[] files = folder.listFiles(imageFilter);
            if (files != null) {

                if (files.length < 2) {
                    return;
                }

                List<TimeLapseResultActivity.ImageInfo> imageInfos = new ArrayList<>();
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();

                for (File file : files) {

                    String[] details = file.getName().split("_");

                    if (details.length < 3) {
                        return;
                    }

//                    String reportDate = details[1];

                    int blurCount = Integer.parseInt(details[3]);

                    TimeLapseResultActivity.ImageInfo imageInfo = new TimeLapseResultActivity.ImageInfo();
                    imageInfo.setCount(blurCount);
                    imageInfo.setImageFile(file);
                    imageInfos.add(imageInfo);
                }

                int firstImageValue = 0;

//                int totalTime = 0;
                try {
                    TimeLapseResultActivity.ImageInfo imageInfo = imageInfos.get(1);
                    String date = imageInfo.getImageFile().getName().substring(0, 13);
                    start.setTime(sdf.parse(date));
                    firstImageValue = imageInfo.getCount();

                    imageInfo = imageInfos.get(imageInfos.size() - 1);
                    date = imageInfo.getImageFile().getName().substring(0, 13);
                    end.setTime(sdf.parse(date));

//                    totalTime = (int) ((end.getTimeInMillis() - start.getTimeInMillis()) / 1000 / 60);

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                firstImage = imageInfos.get(1).imageFile;
                lastImage = imageInfos.get(imageInfos.size() - 1).imageFile;

                for (int i = 1; i < imageInfos.size(); i++) {

                    TimeLapseResultActivity.ImageInfo imageInfo = imageInfos.get(i);

                    isTurbid =
                            (firstImageValue < 2000 && Math.abs(imageInfo.getCount() - firstImageValue) > 1000)
                                    || (firstImageValue < 10000 && Math.abs(imageInfo.getCount() - firstImageValue) > 2000)
                                    || (firstImageValue < 20000 && Math.abs(imageInfo.getCount() - firstImageValue) > 3000)
                                    || (firstImageValue < 50000 && Math.abs(imageInfo.getCount() - firstImageValue) > 4000)
                                    || (firstImageValue >= 50000 && Math.abs(imageInfo.getCount() - firstImageValue) > 7000);

                    if (isTurbid) {
                        turbidImage = imageInfo.imageFile;
                        break;
                    }
                }
            }

            if (isTurbid) {
                String emailTemplate;
                emailTemplate = AssetsManager.getInstance().loadJsonFromAsset("templates/email_template_unsafe.html");
                String testId = PreferencesUtil.getString(this, R.string.colif_testIdKey, "");
                String description = PreferencesUtil.getString(this, R.string.colif_descriptionKey, "");

                if (!description.isEmpty()) {
                    testId += ", " + description;
                }

                if (emailTemplate != null) {

                    String broth = PreferencesUtil.getString(this, R.string.colif_brothMediaKey, "");

                    emailTemplate = emailTemplate.replace("{testDetails}",
                            "test: " + testId + ", " + "Broth: " + broth);

                    long startTime = PreferencesUtil.getLong(this, ConstantKey.TEST_START_TIME);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.US);
                    String date = simpleDateFormat.format(new Date(startTime));
                    emailTemplate = emailTemplate.replace("{startTime}", date);

                    long duration = Calendar.getInstance().getTimeInMillis() - startTime;

                    if (TimeUnit.MILLISECONDS.toHours(duration) > 0) {
                        durationString = String.format(Locale.US, "%s hours & %s minutes",
                                TimeUnit.MILLISECONDS.toHours(duration),
                                TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1));
                    } else {
                        durationString = String.format(Locale.US, "%s minutes",
                                TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1));
                    }

                    emailTemplate = emailTemplate.replace("display:block", "display:none");
                    emailTemplate = emailTemplate.replace("{detectionDuration}", durationString);
                    emailTemplate = emailTemplate.replace("{detectionTime}", "at: " +
                            simpleDateFormat.format(Calendar.getInstance().getTime()));

                    PreferencesUtil.setString(this, "turbidDuration", durationString);
                }

                String notificationEmails = AppPreferences.getNotificationEmails();

                String email = PreferencesUtil.getString(this, "username", "");
                String password = PreferencesUtil.getString(this, "password", "");
                if (!email.isEmpty() && !password.isEmpty() && !notificationEmails.isEmpty()) {
                    sendEmail(testId, emailTemplate, firstImage, turbidImage, lastImage, email, notificationEmails, password);
                }
            }
        }
    }

    private void sendEmail(String testId, String body, File firstImage, File turbidImage,
                           File lastImage, String from, String to, String password) {
        new Thread(() -> {
            try {
                GMailSender sender = new GMailSender(from, password);
                sender.sendMail("Coliform test: " + testId,
                        body, firstImage, turbidImage, lastImage, from, to);
                PreferencesUtil.setBoolean(this, ConstantKey.TURBID_EMAIL_SENT, true);
            } catch (Exception e) {
                Timber.e(e);
            }
        }).start();
    }

    private void startCountdownTimer() {
        showTimer = true;
        handler = new Handler();
        handler.removeCallbacks(runnable);
        runnable = new Runnable() {
            @Override
            public void run() {
                if (showTimer) {
                    handler.postDelayed(this, 1000);
                    try {
                        Calendar currentDate = Calendar.getInstance();
                        if (futureDate != null && !currentDate.after(futureDate)) {
                            long diff = futureDate.getTimeInMillis() - currentDate.getTimeInMillis();
                            long days = diff / (24 * 60 * 60 * 1000);
                            diff -= days * (24 * 60 * 60 * 1000);
                            long hours = diff / (60 * 60 * 1000);
                            diff -= hours * (60 * 60 * 1000);
                            long minutes = diff / (60 * 1000);
                            diff -= minutes * (60 * 1000);
                            long seconds = diff / 1000;
                            textCountdown.setText(String.format(Locale.getDefault(),
                                    "%02d:%02d:%02d", hours, minutes, seconds));
                        }
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_lapse);

        layoutWait = findViewById(R.id.layoutWait);
        layoutDetails = findViewById(R.id.layoutDetails);
        textInterval = findViewById(R.id.textInterval);

        PreferenceFragmentCompat fragment;
        Bundle bundle = new Bundle();

        final TextView textTitle = findViewById(R.id.textTitle);

        testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);

        fragment = new TimeLapsePreferenceFragment();

        bundle.putString("uuid", testInfo.getUuid());
        fragment.setArguments(bundle);

        textTitle.setText(testInfo.getName());
        setTitle(testInfo.getName());

        getSupportFragmentManager().beginTransaction()
                .add(R.id.layoutContent4, fragment)
                .commit();

        LinearLayout layoutTitleBar = findViewById(R.id.layoutTitleBar);
        layoutTitleBar.setVisibility(View.GONE);

        layoutWait.setVisibility(View.VISIBLE);
        layoutDetails.setVisibility(View.GONE);

//        if (TurbidityConfig.isAlarmRunning(this, uuid)) {
//            Log.e("TimeLapse", "Already Running Alarm");
//        }

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-event-name"));

        Button buttonStart = findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(v -> {

            numberOfSamples = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                    R.string.colif_numberOfSamplesKey, "1"));

            if (AppPreferences.useExternalCamera()) {
                permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            }

            if (permissionsDelegate.hasPermissions(permissions)) {
                startTest();
            } else {
                permissionsDelegate.requestPermissions(permissions);
            }
        });
        textCountdown = findViewById(R.id.textCountdown);

        if (AppPreferences.isDiagnosticMode()) {
            textCountdown.setBackground(new ColorDrawable(
                    ContextCompat.getColor(this, R.color.diagnostic)));
            textInterval.setBackground(new ColorDrawable(
                    ContextCompat.getColor(this, R.color.diagnostic)));
        }
    }

    private void showAuthDialog() {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        AuthDialogFragment authDialogFragment = AuthDialogFragment.newInstance();
        authDialogFragment.show(ft, "");
    }

    private void startTest() {

        if (!AppPreferences.getNotificationEmails().isEmpty()) {
            if (PreferencesUtil.getString(this, "username", "").isEmpty() ||
                    PreferencesUtil.getString(this, "password", "").isEmpty()) {
                showAuthDialog();
                return;
            }

            if (!NetUtil.isNetworkAvailable(this)) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Data connection required to send notifications. Connect to the internet and try again.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 250);
                toast.show();

                return;
            }
        }

        PreferencesUtil.setLong(this, ConstantKey.TEST_START_TIME, Calendar.getInstance().getTimeInMillis());

        if (AppPreferences.isTestMode()) {
            PreferencesUtil.setInt(this, "imageCount", 0);
        }

        MenuItem item = menu.findItem(R.id.menu_item_auth);
        item.setVisible(false);

        layoutWait.setVisibility(View.GONE);
        layoutDetails.setVisibility(View.VISIBLE);

        Calendar startDate = Calendar.getInstance();

        String details = "";
        if (testInfo.getUuid().equals("colif")) {

            String media = PreferencesUtil.getString(this, getString(R.string.colif_brothMediaKey), "");
            String volume = PreferencesUtil.getString(this, getString(R.string.colif_volumeKey), "");

            details = "_" + Build.MODEL.replace("_", "-") + "-"
                    + media + "_"
                    + volume + "ml_";

            if (media.isEmpty() || volume.isEmpty()) {
                AlertUtil.showAlert(this, R.string.required, "All fields must be filled", R.string.ok,
                        (dialogInterface, i) -> finish(), null, null);
                return;
            }
        }

//        File folder = FileHelper.getFilesDir(FileHelper.FileType.TEMP_IMAGE, testInfo.getName());
//        if (folder.exists()) {
//            FileUtil.deleteRecursive(folder);
//        }

        String testId = PreferencesUtil.getString(this, R.string.colif_testIdKey, "");

        PreferencesUtil.setString(this, R.string.turbiditySavePathKey,
                testInfo.getName() + File.separator + "_"
                        + new SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(startDate.getTime())
                        + details + "_" + testId);

        TurbidityConfig.setRepeatingAlarm(this, INITIAL_DELAY, testInfo.getUuid());

        String date = new SimpleDateFormat("dd MMM yyy HH:mm", Locale.US).format(startDate.getTime());
        ((TextView) findViewById(R.id.textSubtitle))
                .setText(String.format(Locale.getDefault(), "%s %s", "Started", date));

        futureDate = Calendar.getInstance();
        futureDate.add(Calendar.MILLISECOND, INITIAL_DELAY);

        interval = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                R.string.colif_intervalMinutesKey, "1"));

        textInterval.setText(String.format(Locale.getDefault(), "Done: %d of %d", 0, numberOfSamples));

        startCountdownTimer();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && AppConfig.USE_SCREEN_PINNING) {
            startLockTask();
        }
    }

    public void showResult() {
        Intent resultIntent = new Intent(getIntent());
        resultIntent.setClass(this, TimeLapseResultActivity.class);
        resultIntent.putExtra(ConstantKey.TEST_INFO, testInfo);
        resultIntent.putExtra(ConstantKey.SAVE_FOLDER, folderName);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(resultIntent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        showTimer = false;
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCountdownTimer();
    }

    @Override
    public void onBackPressed() {

        if (isAppInLockTaskMode(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                showLockTaskEscapeMessage();
            } else {
                Toast.makeText(this, R.string.screen_pinned, Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onBackPressed();

        if (layoutDetails.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, "Test cancelled", Toast.LENGTH_LONG).show();
        }

        showTimer = false;
        handler.removeCallbacks(runnable);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        TurbidityConfig.stopRepeatingAlarm(this, Constants.COLIFORM_ID);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {

        showTimer = false;
        handler.removeCallbacks(runnable);

        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        TurbidityConfig.stopRepeatingAlarm(this, Constants.COLIFORM_ID);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (permissionsDelegate.resultGranted(requestCode, grantResults)) {
            startTest();
        } else {
            String message = getString(R.string.cameraAndStoragePermissions);

            AlertUtil.showSettingsSnackbar(this,
                    getWindow().getDecorView().getRootView(), message);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timelapse, menu);
        this.menu = menu;
        return true;
    }

    public void onUserDetailsClick(MenuItem item) {
        showAuthDialog();
    }

    public void onResultHistoryClick(MenuItem item) {
        startActivity(new Intent(this, ResultInfoListActivity.class));
    }
}
