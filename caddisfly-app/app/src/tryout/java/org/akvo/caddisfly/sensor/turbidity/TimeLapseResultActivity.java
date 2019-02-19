package org.akvo.caddisfly.sensor.turbidity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.AssetsManager;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.GMailSender;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import timber.log.Timber;

import static org.akvo.caddisfly.util.ApiUtil.isAppInLockTaskMode;

/**
 * Activity that displays the results.
 */
public class TimeLapseResultActivity extends BaseActivity {
    static FilenameFilter imageFilter = (dir, name) -> {
        String lowercaseName = name.toLowerCase();
        return lowercaseName.endsWith(".jpg");
    };
    File folder;
    boolean isTurbid;
    String durationString;
    String startDate;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.US);
    private Button buttonSave;
    private LinearLayout rootLayout;
    private TestInfo testInfo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strip_result);
        setTitle(R.string.result);

        folder = FileHelper.getFilesDir(FileHelper.FileType.TEMP_IMAGE,
                getIntent().getStringExtra(ConstantKey.SAVE_FOLDER));

        testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);

        buttonSave = findViewById(R.id.button_save);
        buttonSave.setOnClickListener(v -> {

            Intent resultIntent = new Intent();

            for (int i = 0; i < testInfo.getResults().size(); i++) {
                Result result = testInfo.getResults().get(i);
                resultIntent.putExtra(result.getName().replace(" ", "_")
                        + testInfo.getResultSuffix(), result.getResult());

                if (i == 0) {
                    resultIntent.putExtra(SensorConstants.VALUE, result.getResult());
                }
            }

            setResult(Activity.RESULT_OK, resultIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    && AppConfig.USE_SCREEN_PINNING) {
                stopLockTask();
            }

            finish();
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        rootLayout = findViewById(R.id.layout_results);
        rootLayout.removeAllViews();

        startAnalysis();

        buttonSave.setVisibility(View.VISIBLE);
    }

    @SuppressLint("InflateParams")
    private void inflateView(String valueString, Bitmap resultImage, int layout) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout itemResult;
        if (inflater != null) {

            if (layout == 0) {
                layout = R.layout.item_result;
            }

            itemResult = (LinearLayout) inflater.inflate(layout, null, false);

            TextView textDetails = itemResult.findViewById(R.id.testDetails);
            if (textDetails != null) {
                String testId = PreferencesUtil.getString(this, R.string.colif_testIdKey, "");
                String description = PreferencesUtil.getString(this, R.string.colif_descriptionKey, "");
                String broth = PreferencesUtil.getString(this, R.string.colif_brothMediaKey, "");

                if (!description.isEmpty()) {
                    testId += ", " + description + ", " + broth;
                }

                textDetails.setText(testId);
            }

            TextView textStartTime = itemResult.findViewById(R.id.startTime);
            if (textStartTime != null) {
                textStartTime.setText(startDate);
            }

            TextView textDetectTime = itemResult.findViewById(R.id.detectTime);
            if (textDetectTime != null) {
                textDetectTime.setText(String.format("Completed at: %s",
                        simpleDateFormat.format(Calendar.getInstance().getTime())));
            }

            if (resultImage != null) {
//                resultImage = ImageUtil.rotateImage(resultImage, 90);
                ImageView imageResult = itemResult.findViewById(R.id.image_result);
                imageResult.setImageBitmap(resultImage);
            }

            TextView textResult = itemResult.findViewById(R.id.text_result);
            if (valueString.isEmpty()) {
                textResult.setVisibility(View.GONE);
            } else {
                textResult.setText(valueString);
            }

            rootLayout.addView(itemResult);
        }
    }

    private void sendEmail(String testId, String body, File firstImage, File turbidImage,
                           File lastImage, String from, String to, String password) {
        new Thread(() -> {
            try {
                GMailSender sender = new GMailSender(from, password);
                sender.sendMail("Coliform test: " + testId,
                        body, firstImage, turbidImage, lastImage, from, to);
            } catch (Exception e) {
                Timber.e(e);
            }
        }).start();
    }

    private void startAnalysis() {

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

                List<ImageInfo> imageInfos = new ArrayList<>();
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();

                for (File file : files) {

                    String details[] = file.getName().split("_");

                    if (details.length < 3) {
                        return;
                    }

//                    String reportDate = details[1];

                    int blurCount = Integer.parseInt(details[3]);

                    ImageInfo imageInfo = new ImageInfo();
                    imageInfo.setCount(blurCount);
                    imageInfo.setImageFile(file);
                    imageInfos.add(imageInfo);
                }

                int firstImageValue = 0;

//                int totalTime = 0;
                try {
                    ImageInfo imageInfo = imageInfos.get(0);
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

                    ImageInfo imageInfo = imageInfos.get(i);

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

            boolean resultValue = isTurbid;

            String emailTemplate;
            if (resultValue) {
                emailTemplate = AssetsManager.getInstance().loadJsonFromAsset("templates/email_template_unsafe.html");
            } else {
                emailTemplate = AssetsManager.getInstance().loadJsonFromAsset("templates/email_template_safe.html");
            }

            String testId = PreferencesUtil.getString(this, R.string.colif_testIdKey, "");
            String description = PreferencesUtil.getString(this, R.string.colif_descriptionKey, "");

            if (!description.isEmpty()) {
                testId += ", " + description;
            }

            long startTime = PreferencesUtil.getLong(this, ConstantKey.TEST_START_TIME);
            startDate = simpleDateFormat.format(new Date(startTime));

            String testDuration;
            long duration = Calendar.getInstance().getTimeInMillis() - startTime;
            if (TimeUnit.MILLISECONDS.toHours(duration) > 0) {
                testDuration = String.format(Locale.US, "Duration: %s hours & %s minutes",
                        TimeUnit.MILLISECONDS.toHours(duration),
                        TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1) + 1);
            } else {
                testDuration = String.format(Locale.US, "Duration: %s minutes",
                        TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1) + 1);
            }

            durationString = PreferencesUtil.getString(this, "turbidDuration", "");

            if (emailTemplate != null) {
                emailTemplate = emailTemplate.replace("{startTime}", startDate);

                String broth = PreferencesUtil.getString(this, R.string.colif_brothMediaKey, "");

                emailTemplate = emailTemplate.replace("{testDetails}", "test Id: " + testId + ", " + "Broth: " + broth);
                emailTemplate = emailTemplate.replace("{detectionDuration}", durationString);
                emailTemplate = emailTemplate.replace("{testDuration}", testDuration);
                emailTemplate = emailTemplate.replace("{detectionTime}", "Completed at: " +
                        simpleDateFormat.format(Calendar.getInstance().getTime()));
            }

            for (Result result : testInfo.getResults()) {
                switch (result.getName()) {
                    case "Broth":
                        result.setResult(PreferencesUtil.getString(this,
                                getString(R.string.colif_brothMediaKey), ""));
                        break;
                    case "Sample volume":
                        result.setResult(PreferencesUtil.getString(this,
                                getString(R.string.colif_volumeKey), ""));
                        break;
                    case "Time to detect":
                        result.setResult(durationString);
                        break;
                    case "First Image":
                        if (firstImage != null) {
                            inflateView("",
                                    BitmapFactory.decodeFile(firstImage.getAbsolutePath()), 0);
                        }
                        break;
                    case "Turbid Image":
                        if (resultValue) {
                            if (turbidImage != null) {
                                inflateView("",
                                        BitmapFactory.decodeFile(turbidImage.getAbsolutePath()), 0);
                            }
                        }
                        break;
                    case "Last Image":
                        if (lastImage != null) {
                            inflateView("",
                                    BitmapFactory.decodeFile(lastImage.getAbsolutePath()), 0);
                        }
                        break;
                    default:
                        if (resultValue) {
                            result.setResult(getString(R.string.high_risk_unsafe));
                            inflateView(result.getResult(), null,
                                    R.layout.item_warning_result);
                        } else {
                            result.setResult("Low Risk - Possibly Safe");
                            inflateView(result.getResult(), null,
                                    R.layout.item_safe_result);
                        }
                        break;
                }
            }

            String notificationEmails = AppPreferences.getNotificationEmails();

            String email = PreferencesUtil.getString(this, "username", "");
            String password = PreferencesUtil.getString(this, "password", "");
            if (!email.isEmpty() && !password.isEmpty() && !notificationEmails.isEmpty()) {
                sendEmail(testId, emailTemplate, firstImage, turbidImage, lastImage, email, notificationEmails, password);
            }

            if (emailTemplate != null && lastImage != null && firstImage != null) {
                emailTemplate = emailTemplate.replaceAll("cid:firstImage", firstImage.getName());
                emailTemplate = emailTemplate.replaceAll("cid:lastImage", lastImage.getName());
                if (turbidImage != null) {
                    emailTemplate = emailTemplate.replaceAll("cid:turbidImage", turbidImage.getName());
                }

                if (isTurbid) {
                    FileUtil.saveToFile(folder, "positive", description);
                } else {
                    FileUtil.saveToFile(folder, "negative", description);
                }

                emailTemplate = emailTemplate.replace("margin:20px auto;", "");

                FileUtil.saveToFile(folder, "result.html", emailTemplate);

            }
        }
    }

    public static class ImageInfo {

        File imageFile;
        private int count;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        File getImageFile() {
            return imageFile;
        }

        void setImageFile(File imageFile) {
            this.imageFile = imageFile;
        }

    }
}