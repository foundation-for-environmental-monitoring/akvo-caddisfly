package org.akvo.caddisfly.sensor.turbidity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.AssetsManager;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * An activity representing a list of ResultInfos. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ResultInfoDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ResultInfoListActivity extends BaseActivity {

    static FilenameFilter imageFilter = (dir, name) -> {
        String lowercaseName = name.toLowerCase();
        return lowercaseName.endsWith(".jpg");
    };
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultinfo_list);

        setTitle("Result history");

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        View recyclerView = findViewById(R.id.resultinfo_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {

        ResultInfoAdapter resultInfoAdapter = new ResultInfoAdapter();

        List<ResultInfo> tests = new ArrayList<>();

        File folder = FileHelper.getFilesDir(FileHelper.FileType.TEMP_IMAGE, "Coliforms");
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    File positiveFile = new File(f.getAbsolutePath() + "/positive");
                    File negativeFile = new File(f.getAbsolutePath() + "/negative");
                    if (!positiveFile.exists() && !negativeFile.exists()) {
                        startAnalysis(f);
                    }
                }
            }

            for (File f : files) {
                String id = "0";
                if (f.isDirectory()) {
                    if (f.getName().startsWith("_")) {
                        String[] details = f.getName().split("_");
                        if (details.length > 3) {
                            id = details[3];
                        }
                        ResultInfo resultInfo = new ResultInfo(id);
                        resultInfo.setDate(details[1] + details[2]);
                        resultInfo.folder = f.getAbsolutePath();
                        File file = new File(resultInfo.folder + "/positive");
                        if (file.exists()) {
                            resultInfo.description = FileUtil.readText(file);
                            resultInfo.result = "Positive";
                            tests.add(resultInfo);
                        } else {
                            file = new File(resultInfo.folder + "/negative");
                            if (file.exists()) {
                                resultInfo.description = FileUtil.readText(file);
                                resultInfo.result = "Negative";
                                tests.add(resultInfo);
                            }
                        }
                    }
                }
            }
        }

        Collections.sort(tests, (object1, object2) ->
                object2.getTestDate().compareTo(object1.getTestDate()));
        resultInfoAdapter.setTestList(tests);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(resultInfoAdapter);
    }

    private void startAnalysis(File folder) {

        File firstImage = null;
        File turbidImage = null;
        File lastImage = null;
        boolean isTurbid = false;

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

                    String details[] = file.getName().split("_");

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
                    TimeLapseResultActivity.ImageInfo imageInfo = imageInfos.get(0);
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

            boolean resultValue = isTurbid;

            String emailTemplate;
            if (resultValue) {
                emailTemplate = AssetsManager.getInstance().loadJsonFromAsset("templates/email_template_unsafe.html");
            } else {
                emailTemplate = AssetsManager.getInstance().loadJsonFromAsset("templates/email_template_safe.html");
            }

            String testId = "0";

            ResultInfo resultInfo = new ResultInfo(testId);
            resultInfo.setDate(folder.getName().split("_")[1] + folder.getName().split("_")[2]);

            long startTime = resultInfo.getTestDate().getTime();
            String startDate = simpleDateFormat.format(new Date(startTime));

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

            String durationString = PreferencesUtil.getString(this, "turbidDuration", "");

            if (emailTemplate != null) {
                emailTemplate = emailTemplate.replace("{startTime}", startDate);

                String broth = PreferencesUtil.getString(this, R.string.colif_brothMediaKey, "");

                emailTemplate = emailTemplate.replace("{testDetails}", "test Id: " + testId + ", " + "Broth: " + broth);
                emailTemplate = emailTemplate.replace("{detectionDuration}", durationString);
                emailTemplate = emailTemplate.replace("{testDuration}", testDuration);
                emailTemplate = emailTemplate.replace("{detectionTime}", "Completed at: " +
                        simpleDateFormat.format(Calendar.getInstance().getTime()));
            }

            if (emailTemplate != null && lastImage != null && firstImage != null) {
                emailTemplate = emailTemplate.replaceAll("cid:firstImage", firstImage.getName());
                emailTemplate = emailTemplate.replaceAll("cid:lastImage", lastImage.getName());
                if (turbidImage != null) {
                    emailTemplate = emailTemplate.replaceAll("cid:turbidImage", turbidImage.getName());
                }

                if (isTurbid) {
                    FileUtil.saveToFile(folder, "positive", "");
                } else {
                    FileUtil.saveToFile(folder, "negative", "");
                }

                emailTemplate = emailTemplate.replace("margin:20px auto;", "");

                FileUtil.saveToFile(folder, "result.html", emailTemplate);

            }
        }
    }
}
