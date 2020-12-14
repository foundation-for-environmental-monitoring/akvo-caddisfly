package org.akvo.caddisfly.sensor.turbidity;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.FileUtil;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class AnalyseActivity extends BaseActivity {

//    private static final String TAG = "AnalyseActivity";

    private static final int PERMISSION_ALL = 1;

    static StringBuilder html2 = new StringBuilder();
    static String style = "<style>body{font-family:sans-serif}h1{font-size:18px;margin-bottom:5px}.nopadding{padding:0} @media print {body {-webkit-print-color-adjust: exact;}}td{font-size: 14px;text-align:center;padding:0 10px;}tfoot td{background-color:rgb(75,75,75);color:white;}tfoot td{padding:10 10px;}th{font-size: 14px;background-color:rgb(75,75,75);color:white;padding:10 10px;margin-bottom: 10px;}</style></head><body>";
    static FilenameFilter imageFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            String lowercaseName = name.toLowerCase();
            return lowercaseName.endsWith(".jpg");
        }
    };
    static Point centerPoint;
    private final List<Fragment> fragments = new ArrayList<>();
    String path;
    AnalyseActivity.PagerAdapter pagerAdapter;
    Activity mActivity;
//    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
//        @Override
//        public void onManagerConnected(int status) {
//            switch (status) {
//                case LoaderCallbackInterface.SUCCESS:
//
//                    String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
//
//                    if (!ApiUtil.hasPermissions(getBaseContext(), permissions)) {
//                        ActivityCompat.requestPermissions(mActivity, permissions, PERMISSION_ALL);
//                    } else {
//                        startAnalysis();
//                    }
//
//                    break;
//                default:
//                    super.onManagerConnected(status);
//                    break;
//            }
//        }
//    };
    boolean isTurbid;

    private static Integer getColor(String filename, Point centerPoint) throws IOException {
//        File file = new File(filename);
//        ImageInputStream is = ImageIO.createImageInputStream(file);
//        Iterator iterator = ImageIO.getImageReaders(is);
//
//        if (!iterator.hasNext()) {
//            System.out.println("Cannot load the specified file " + file);
//            System.exit(1);
//        }
//        ImageReader imageReader = (ImageReader) iterator.next();
//        imageReader.setInput(is);
//
//        BufferedImage image = imageReader.read(0);
//        int x = (int) centerPoint.x;
//        int y = (int) centerPoint.y;
//
//        BufferedImage croppedImage = image.getSubimage(x - 25, y - 25, 50, 50);
//
//        int height = croppedImage.getHeight();
//        int width = croppedImage.getWidth();
//
//        Map<Integer, Integer> m = new HashMap<>();
//        for (int i = 0; i < width; i++) {
//            for (int j = 0; j < height; j++) {
//                int rgb = croppedImage.getRGB(i, j);
//                int[] rgbArr = getRGBArr(rgb);
//                // Filter out grays....
//                if (!isGray(rgbArr)) {
//                    Integer counter = m.get(rgb);
//                    if (counter == null)
//                        counter = 0;
//                    counter++;
//                    m.put(rgb, counter);
//                }
//            }
//        }
//        //String colourHex =
//        return getMostCommonColour(m);
        return 0;
    }

    /**
     * Computes the Euclidean distance between the two colors
     *
     * @param color1 the first color
     * @param color2 the color to compare with
     * @return the distance between the two colors
     */
    private static double getColorDistanceRgb(int color1, int color2) {
        double r, g, b;

        r = Math.pow(Color.red(color2) - Color.red(color1), 2.0);
        g = Math.pow(Color.green(color2) - Color.green(color1), 2.0);
        b = Math.pow(Color.blue(color2) - Color.blue(color1), 2.0);

        return Math.sqrt(b + g + r);
    }

    private static String getColorHexString(int color) {
        return ("00" + Integer.toHexString(Color.red(color))).substring(Integer.toHexString(Color.red(color)).length()) +
                ("00" + Integer.toHexString(Color.green(color))).substring(Integer.toHexString(Color.green(color)).length()) +
                ("00" + Integer.toHexString(Color.blue(color))).substring(Integer.toHexString(Color.blue(color)).length());

    }

    private static boolean isGreen(int color, int compareColor, String media) {
        float[] hsb = new float[3];
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsb);

        //Color.RGBtoHSB(97, 58, 1, hsb);

        if (hsb[1] < 0.1 && hsb[2] > 0.9) return false;
        else if (hsb[2] < 0.1) return false;
        else {
            float deg = hsb[0] * 360;
            //if (deg >=  25 && deg < 70) return true;
            if (media.equalsIgnoreCase("himedia")) {
                return (deg >= 25 && deg < 70) && getColorDistanceRgb(color, compareColor) > 70;
            } else {
                return (deg >= 10 && deg < 40) && getColorDistanceRgb(color, compareColor) > 50;
            }
        }
    }

//    private static int[] getRGBArr(int pixel) {
//        int alpha = (pixel >> 24) & 0xff;
//        int red = (pixel >> 16) & 0xff;
//        int green = (pixel >> 8) & 0xff;
//        int blue = (pixel) & 0xff;
//        return new int[]{red, green, blue};
//
//    }
//
//    private static boolean isGray(int[] rgbArr) {
//        int rgDiff = rgbArr[0] - rgbArr[1];
//        int rbDiff = rgbArr[0] - rgbArr[2];
//        // Filter out black, white and grays...... (tolerance within 10 pixels)
//        int tolerance = 3;
//        if (rgDiff > tolerance || rgDiff < -tolerance)
//            if (rbDiff > tolerance || rbDiff < -tolerance) {
//                return false;
//            }
//        return true;
//    }

    public static void saveToFile(File folder, String name, String data) {
        if (!folder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            folder.mkdirs();
        }

        File file = new File(folder, name);

        try {
            if (!file.exists()) {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileWriter filewriter = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(filewriter);

            out.write(data);

            out.close();
            filewriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyse);

        mActivity = this;

        setTitle("Coliform Result");

//        final PageIndicatorView footerView = (PageIndicatorView) findViewById(R.id.pager_indicator);

        pagerAdapter = new AnalyseActivity.PagerAdapter(getSupportFragmentManager());

//        Button buttonAnalyzeImages = (Button) findViewById(R.id.buttonAnalyzeImages);

//        buttonAnalyzeImages.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_ALL) {
            // If request is cancelled, the result arrays are empty.
            boolean granted = false;
            for (int grantResult : grantResults) {
                if (grantResult != PERMISSION_GRANTED) {
                    granted = false;
                    break;
                } else {
                    granted = true;
                }
            }
            if (granted) {
                startAnalysis();
            } else {
                Toast.makeText(this, getString(R.string.storagePermission), Toast.LENGTH_LONG).show();
                ApiUtil.startInstalledAppDetailsActivity(this);
                finish();
            }
        }
    }

    private void moveColiformsFolder() {
        final File sourcePath = new File(FileUtil.getFilesStorageDir(CaddisflyApp.getApp(), false)
                + File.separator + "Akvo Caddisfly" + File.separator + "image" + File.separator + "Coliforms");
        final File destinationPath = new File(FileUtil.getFilesStorageDir(CaddisflyApp.getApp(), false)
                + FileHelper.ROOT_DIRECTORY + File.separator + "image" + File.separator + "Coliforms");

        if (sourcePath.exists() && sourcePath.isDirectory()) {
            File[] sourceFiles = sourcePath.listFiles();
            if (sourceFiles != null) {
                for (File file : sourceFiles) {
                    File destinationFile = new File(destinationPath + File.separator + file.getName());
                    //noinspection ResultOfMethodCallIgnored
                    file.renameTo(destinationFile);
                }

                sourceFiles = sourcePath.listFiles();
                if (sourceFiles != null && sourceFiles.length == 0) {
                    //noinspection ResultOfMethodCallIgnored
                    sourcePath.delete();
                }
            }
        }
    }


    private void startAnalysis() {

        moveColiformsFolder();


        File folder = FileHelper.getFilesDir(FileHelper.FileType.IMAGE, "Coliforms");
        if (folder.isDirectory()) {

            File[] folders = folder.listFiles();
            if (folders != null && folders.length > 0) {
                for (File imageFolder : folders) {
                    path = imageFolder.getPath();
                    ResultInfo resultInfo = readResult(new File(imageFolder, "result"));

                    if (resultInfo == null) {
                        new AnalyzeTask(imageFolder).execute();

                        //resultInfo = analyzeFiles();
                    } else {

                        if (resultInfo.version < ApiUtil.getAppVersionCode(this)) {
                            new AnalyzeTask(imageFolder).execute();
                        } else {
                            fragments.add(AnalyseDetailFragment.newInstance(resultInfo));
                        }
                    }

                }
            } else {
                Toast.makeText(this, "No files to analyze", Toast.LENGTH_LONG).show();
                finish();
            }
        }

        ViewPager mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private ResultInfo analyzeFiles(File folder) {

        ResultInfo resultInfo = new ResultInfo();

        String firstImageName = "";
        int firstImageValue = 0;
        String incubationImageName = "";
        boolean colorChangeFound = false;
        int fileCount = 0;
        //int incubationTime = 0;
        //int confirmed = 0;

        centerPoint = null;

        int timeAtBlurriness;
        //double maxDifference = 0;

        try {
            if (folder.exists()) {
                String[] details = folder.getName().split("_");

                if (details.length < 7) {
                    return null;
                }

                int testNumber = Integer.valueOf(details[0]);
                String reportDate = details[1];
                //String reportTime = details[2];
                String phone = details[3];
                String chamber = details[4];
                String media = details[5];
                String volume = details[6];

                String type = "";
                if (details.length > 7) {
                    type = details[7].replace("*", "&#176;");
                }

                String beforeQty = "-";
                if (details.length > 8) {
                    beforeQty = details[8];
                }

                String mainTitle = media + " - " + beforeQty;

                Calendar reportCalendar = Calendar.getInstance();
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd", Locale.US);

                try {
                    reportCalendar.setTime(sdf1.parse(reportDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);

                String title = dateFormat.format(reportCalendar.getTime()) + " - " + phone + " - " + chamber;

                List<ImageInfo> imageInfos = new ArrayList<>();

                //int previousResultValue = 0;
                //Mat destInitialMat = null;


                File mainFolder = new File(folder.getAbsolutePath() + File.separator + "input");
                if (!mainFolder.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    mainFolder.mkdirs();
                }


                mainFolder = new File(folder.getAbsolutePath() + File.separator + "output");
                if (!mainFolder.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    mainFolder.mkdirs();
                }


                File[] files = folder.listFiles(imageFilter);
                if (files != null) {
                    fileCount = files.length;
                    for (File file : files) {


                        //getDominantColor(file);

                        Mat initialMat = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

                        if (centerPoint == null) {
                            //centerPoint = getCenter(initialMat);
                            centerPoint = new Point(initialMat.width() / 2, initialMat.height() / 2);
                            //Mat initialMat = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//                    int x = (int)centerPoint.x;
//                    int y = (int)centerPoint.y;
//                    Rect roi = new Rect(x - 130, y - 130, 260, 260);
//                    initialMat = new Mat(initialMat, roi);
//                    destInitialMat= initialMat;
//                    Imgproc.threshold(initialMat, destInitialMat,127,255,Imgproc.THRESH_TOZERO);
                        }

                        int x = (int) centerPoint.x;
                        int y = (int) centerPoint.y;

                        Mat colorMat = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_COLOR);

                        Rect roi = new Rect(x - 150, y - 150, 300, 300);
                        Mat matImageGrey = new Mat(initialMat, roi);
                        Mat matImageColor = new Mat(colorMat, roi);

                        ImageInfo imageInfo = new ImageInfo();

                        try {

                            //Mat source = Imgcodecs.imread("digital_image_processing.jpg",  Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
                            //Mat destination = new Mat(source.rows(),source.cols(),source.type());

                            Imgproc.threshold(matImageGrey, matImageGrey, 90, 255, Imgproc.THRESH_TOZERO);
                            int count = Core.countNonZero(matImageGrey);
                            imageInfo.setCount(count);
                            imageInfo.setImageName(file.getName());

                            imageInfo.setMat(matImageGrey);

                            Imgcodecs.imwrite(folder.getAbsolutePath() + File.separator + "output" + File.separator + file.getName(), matImageGrey);
                            Imgcodecs.imwrite(folder.getAbsolutePath() + File.separator + "input" + File.separator + file.getName(), matImageColor);
                        } catch (Exception e) {
                            System.out.println("error: " + e.getMessage());
                        }

                        imageInfos.add(imageInfo);
                    }
                }

                if (imageInfos.size() < 1) {
                    return null;
                }

                String scriptStart = "<html><head><script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script><script type=\"text/javascript\">google.charts.load('current', {'packages':['line']});google.charts.setOnLoadCallback(drawChart);function drawChart() {var data = new google.visualization.DataTable();data.addColumn('number', 'Minutes');data.addColumn('number', 'Black');data.addColumn('number', 'White');data.addColumn('number', 'Difference');data.addRows([";
                String scriptEnd = "]);var options = {chart: {title: '" + title + "',subtitle: 'Brightness level of two points on black and white sides of the backdrop',},vAxis: {title: 'Brightness'},width: 700,height: 500,legend: { position: 'bottom', alignment: 'start' },colors:['black','orange','green']};var chart = new google.charts.Line(document.getElementById('linechart_material'));chart.draw(data, google.charts.Line.convertOptions(options));}</script>";
                String reportTitle = "<h1>Coliform Test - " + mainTitle + "</h1>";
                //"<div id=\"linechart_material\" style=\"width: 900px; height: 500px;margin-bottom:20px\"></div>";
                StringBuilder html = new StringBuilder();
                StringBuilder data = new StringBuilder();

                html.append("<table><thead><tr><th>Time</th><th>Minutes</th><th>Color</th><th>Original</th><th>Image</th><th>Threshold</th><th>Brightness</th><th>Difference</th></thead></tr>");

                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.US);

                int totalTime = 0;
                try {
                    ImageInfo imageInfo = imageInfos.get(0);
                    String date = imageInfo.getImageName().substring(0, 13);
                    start.setTime(sdf.parse(date));
                    imageInfo = imageInfos.get(imageInfos.size() - 1);
                    date = imageInfo.getImageName().substring(0, 13);
                    end.setTime(sdf.parse(date));

                    totalTime = (int) ((end.getTimeInMillis() - start.getTimeInMillis()) / 1000 / 60);

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Calendar calendar = Calendar.getInstance();

                long previousDate = 0;
                int totalDiff = 0;
                boolean found = false;
                String incubationHours = "";
                Integer startColor = 0;
                Integer endColor = 0;


                for (int i = 0; i < imageInfos.size(); i++) {


                    ImageInfo imageInfo = imageInfos.get(i);

                    String time = imageInfo.getImageName().substring(0, 13);

                    if (i < 2) {
                        firstImageName = imageInfo.getImageName();
                    }

                    if (i == 2) {
                        firstImageName = imageInfo.getImageName();
                        firstImageValue = imageInfo.getCount();
                    } else if (i < 15 && firstImageValue < imageInfo.getCount()) {
                        firstImageValue = imageInfo.getCount();
                    }

                    try {
                        calendar.setTime(sdf.parse(time));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    long diff = 0;
                    if (previousDate != 0) {
                        diff = (calendar.getTimeInMillis() - previousDate) / 1000 / 60;
                    }

                    previousDate = calendar.getTimeInMillis();

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);

                    totalDiff += diff;


                    if (!isTurbid) {
                        isTurbid = i > 15 && (firstImageValue < 50000 && (imageInfo.getCount() - firstImageValue) > 7700)
                                || (firstImageValue > 5000 && imageInfo.getCount() < 2000)
                                || (firstImageValue > 50000 && (imageInfo.getCount() - firstImageValue) > 6700)
                                || (firstImageValue > 10000 && firstImageValue < 50000 && imageInfo.getCount() < 4000)
                                || (firstImageValue < 5000 && firstImageValue > 3000 && imageInfo.getCount() < 3000);
                    }


                    if (!found && (isTurbid || i == imageInfos.size() - 1)) {

                        //if (confirmed > 2 || i == results.size() - 1) {

                        incubationImageName = imageInfo.getImageName();
                        //incubationTime = totalDiff;
                        timeAtBlurriness = totalDiff;
                        found = true;


                        try {
                            startColor = getColor(path + folder.getName() + File.separator + imageInfo.getImageName(), centerPoint);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        int hours = timeAtBlurriness / 60;
                        int minutes = timeAtBlurriness % 60;
                        incubationHours = String.format(Locale.US, "%d:%02d", hours, minutes);
                    }

//                    double distance = 0;
//                    try {
//                        endColor = getColor(path + folder.getName() + File.separator + imageInfo.getImageName(), centerPoint);
//                        int compareColor = Color.rgb(97, 58, 1);
//                        // distance = getColorDistanceRgb(startColor, endColor);
//                        distance = getColorDistanceRgb(compareColor, endColor);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    if (!colorChangeFound && ((found && isGreen(endColor, startColor, media) && isTurbid) || i == imageInfos.size() - 1)) {

                        colorChangeFound = true;

                        int hours = totalDiff / 60;
                        int minutes = totalDiff % 60;
                        String colorChangeTime = String.format(Locale.US, "%d:%02d", hours, minutes);

                        html2.append("<tr>");
                        html2.append("<td>").append(testNumber).append("</td>");
                        html2.append("<td>").append(dateFormat.format(reportCalendar.getTime())).append("</td>");
                        html2.append("<td>").append(phone).append("</td>");
                        html2.append("<td>").append(chamber).append("</td>");
                        html2.append("<td>").append(media).append("</td>");
                        html2.append("<td>").append(type).append("</td>");
                        html2.append("<td>").append(volume).append("</td>");
                        html2.append("<td>").append(beforeQty).append("</td>");
                        html2.append("<td class=\"nopadding\"><img style=\"width:100px;\" src=\"")
                                .append(folder.getName())
                                .append(File.separator).append(firstImageName).append("\"></td>");
                        if (isTurbid) {
                            html2.append("<td class=\"nopadding\"><img style=\"width:100px;\" src=\"")
                                    .append(folder.getName())
                                    .append(File.separator).append(incubationImageName).append("\"></td>");
                        } else {
                            html2.append("<td class=\"nopadding\" style=\"width:100px;\"></td>");
                        }

                        html2.append("<td class=\"nopadding\"><img style=\"width:100px;\" src=\"")
                                .append(folder.getName())
                                .append(File.separator).append(imageInfo.getImageName()).append("\"></td>");

                        if (i == imageInfos.size() - 1) {
                            if (isTurbid) {
                                html2.append("<td class=\"nopadding\"><div style=\"border-radius:50px;width:50px;height:50px;margin:auto;background:#" + getColorHexString(startColor) + "\">").append("").append("</div></td>");
                            } else {
                                html2.append("<td></td>");
                            }
                            //html2.append("<td class=\"nopadding\"><div style=\"border-radius:50px;width:50px;height:50px;margin:auto;background:#" + getColorHexString(endColor) + "\">").append("").append("</div></td>");
                            html2.append("<td></td>");
                            if (isTurbid) {
                                html2.append("<td>").append(incubationHours).append("</td>");
                            } else {
                                html2.append("<td>").append("-").append("</td>");
                            }
                        } else {
                            html2.append("<td class=\"nopadding\"><div style=\"border-radius:50px;width:50px;height:50px;margin:auto;background:#" + getColorHexString(startColor) + "\">").append("").append("</div></td>");
                            if (getColorDistanceRgb(startColor, endColor) > 10) {
                                html2.append("<td class=\"nopadding\"><div style=\"border-radius:50px;width:50px;height:50px;margin:auto;background:#" + getColorHexString(endColor) + "\">").append("").append("</div></td>");
                            } else {
                                html2.append("<td></td>");
                            }
                            html2.append("<td>").append(incubationHours).append("</td>");
                        }

                        html2.append("<td>").append(colorChangeTime).append("</td>");

                        if (isTurbid) {
                            resultInfo.result = "Present";
                            html2.append("<td style=\"color:#FF0000\">").append("Present").append("</td>");
                        } else {
                            if (firstImageValue < 100) {
                                resultInfo.result = "Unknown";
                                html2.append("<td style=\"color:#6d6d6d\">").append("Unknown").append("</td>");
                            } else if (fileCount < 15) {
                                resultInfo.result = "Incomplete";
                                html2.append("<td style=\"color:#555\">").append("Incomplete").append("</td>");
                            } else {
                                resultInfo.result = "Absent";
                                html2.append("<td style=\"color:#006633\">").append("Absent").append("</td>");
                            }
                        }
                        html2.append("</tr>");

                        resultInfo.version = ApiUtil.getAppVersionCode(this);
                        resultInfo.testNumber = testNumber;
                        resultInfo.date = reportDate;
                        resultInfo.phone = phone;
                        resultInfo.chamber = chamber;
                        resultInfo.media = media;
                        resultInfo.testType = type;
                        resultInfo.volume = volume;
                        resultInfo.startImage = new File(folder, firstImageName).getPath();
                        resultInfo.turbidImage = new File(folder, incubationImageName).getPath();
                        resultInfo.endImage = new File(folder, imageInfo.getImageName()).getPath();
                        resultInfo.turbidTime = incubationHours;
                        resultInfo.totalTime = colorChangeTime;

                    }

                    Integer color = 0;
                    try {
                        color = getColor(path + folder.getName() + File.separator + imageInfo.getImageName(), centerPoint);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    int hours = totalDiff / 60;
                    int minutes = totalDiff % 60;
                    String totalDiffHours = String.format(Locale.US, "%d:%02d", hours, minutes);


                    html.append("<tr>");
                    html.append("<td>").append(timeFormat.format(calendar.getTime())).append("</td>");
                    html.append("<td>").append(totalDiffHours).append("</td>");
                    //html.append("<td>").append(diff).append("</td>");
                    html.append("<td><div style=\"border-radius:50px;width:50px;height:50px;margin:auto;background:#" + getColorHexString(color) + "\">").append("").append("</div></td>");
                    html.append("<td><img style=\"width:100px;\" src=\"").append(imageInfo.getImageName()).append("\"></td>");
                    html.append("<td><img style=\"width:74px;\" src=\"input/").append(imageInfo.getImageName()).append("\"></td>");
                    html.append("<td><img style=\"width:74px;\" src=\"output/").append(imageInfo.getImageName()).append("\"></td>");
                    html.append("<td>").append(imageInfo.getCount()).append("</td>");
                    html.append("<td>").append(firstImageValue - imageInfo.getCount()).append("</td>");

                    if (found) {
                        html.append("<td style=\"background-color:#006633\">");
                    } else {
                        html.append("<td>");
                    }
                    html.append("");
                    html.append("</td>");
                    html.append("</tr>");

                    data.append("[").append(totalDiff).append(",").append(0).append(",").append(0).append(",").append(0).append("],");

                }

                int hours = totalTime / 60;
                int minutes = totalTime % 60;

                html.append("<tfoot><tr><td>").append(String.format(Locale.US, "%d:%02d", hours, minutes)).append("</td><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr></tfoot>");
                html.append("</table></body></html>");

                saveToFile(folder, "report.html", scriptStart + data.toString() + scriptEnd + style + reportTitle + html.toString());
            }


            writeResult(resultInfo, new File(folder, "result"));
        } catch (Exception ignored) {
            return null;
        }
        return resultInfo;
    }

    public void writeResult(Serializable serializable, File file) {
        ObjectOutput out;

        try {
            out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(serializable);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ResultInfo readResult(File file) {
        ObjectInputStream input;

        try {
            input = new ObjectInputStream(new FileInputStream(file));
            ResultInfo resultInfo = (ResultInfo) input.readObject();
            input.close();

            return resultInfo;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private static class ImageInfo {

        private String imageName;
        private int count;
        private Mat mat;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        String getImageName() {
            return imageName;
        }

        void setImageName(String imageName) {
            this.imageName = imageName;
        }

        public void setMat(Mat mat) {
            this.mat = mat;
        }

    }

    private class AnalyzeTask extends AsyncTask<String, Void, ResultInfo> {

        private final File folder;

        AnalyzeTask(File folder) {
            this.folder = folder;
        }

        @Override
        protected ResultInfo doInBackground(String... strings) {
            return analyzeFiles(folder);
        }

        protected void onPostExecute(ResultInfo resultInfo) {
            if (resultInfo != null) {
                fragments.add(AnalyseDetailFragment.newInstance(resultInfo));
                pagerAdapter.notifyDataSetChanged();
            }

        }
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

        PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return ((AnalyseDetailFragment) fragments.get(position)).getTitle();
        }
    }

}
