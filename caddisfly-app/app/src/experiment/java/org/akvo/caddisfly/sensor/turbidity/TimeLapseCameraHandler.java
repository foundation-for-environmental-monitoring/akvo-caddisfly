package org.akvo.caddisfly.sensor.turbidity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.PowerManager;
import androidx.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.ImageUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.akvo.caddisfly.util.ImageUtil.createGreyMatrix;
import static org.akvo.caddisfly.util.ImageUtil.createThresholdMatrix;

@SuppressWarnings("deprecation")
class TimeLapseCameraHandler implements Camera.PictureCallback {

    private static final int MIN_PICTURE_WIDTH = 640;
    private static final int MIN_PICTURE_HEIGHT = 480;
    private static final int MIN_SUPPORTED_WIDTH = 400;
    private static final int METERING_AREA_SIZE = 100;
    private static final int PREVIEW_START_WAIT_MILLIS = 3000;
    private static final int WAKE_LOCK_RELEASE_DELAY = 10000;
    private static final int PICTURE_TAKEN_DELAY = 2000;
    private static final int EXPOSURE_COMPENSATION = -2;
    private final PowerManager.WakeLock wakeLock;
    @NonNull
    private final Context mContext;
    private Camera mCamera;
    private String mSavePath;

    TimeLapseCameraHandler(@NonNull Context context) {
        mContext = context;

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        wakeLock = Objects.requireNonNull(pm).newWakeLock(PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "WakeLock");
        wakeLock.acquire(15000);
    }

    @NonNull
    @SuppressWarnings("UnusedReturnValue")
    Boolean takePicture(String savePath) {

        mSavePath = savePath;
        try {

            if (mCamera == null) {
                mCamera = Camera.open();
            }
            mCamera.enableShutterSound(false);

            try {
                mCamera.setPreviewTexture(new SurfaceTexture(10));
            } catch (Exception ex) {
                return false;
            }

            setCamera(mCamera);
            mCamera.startPreview();
            try {
                Thread.sleep(PREVIEW_START_WAIT_MILLIS);
            } catch (Exception ignored) {
            }

            mCamera.takePicture(null, null, this);

            (new Handler()).postDelayed(() -> {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }, WAKE_LOCK_RELEASE_DELAY);

        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    @Override
    public void onPictureTaken(@NonNull byte[] data, Camera camera) {

        mCamera.release();
        mCamera = null;
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = mContext.registerReceiver(null, intentFilter);
        int batteryPercent = -1;

        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryPercent = (int) ((level / (float) scale) * 100);
        }

        if (AppPreferences.isTestMode()) {
            int numberOfSamples = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                    "colif_NumberOfSamples", "1"));

            int imageCount = PreferencesUtil.getInt(mContext, "imageCount", 0);
            String demoFileName = "start.jpg";
            if (!PreferencesUtil.getBoolean(mContext, R.string.coliformResultSafeKey, false)) {
                if (imageCount <= 1) {
                    demoFileName = "start.jpg";
                } else if (imageCount >= numberOfSamples) {
                    demoFileName = "end.jpg";
                } else if (imageCount >= numberOfSamples / 2) {
                    demoFileName = "turbid.jpg";
                }
            }

            String image = Constants.TEST_IMAGE_PATH + demoFileName;
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeStream(mContext.getAssets().open(image));
                data = ImageUtil.bitmapToBytes(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String date = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(new Date());
        String fileName = date + "_" + batteryPercent + "_" + getThresholdNonZeroCount(data, 150) + "_";

        saveImage(data, fileName);

        try {
            Thread.sleep(PICTURE_TAKEN_DELAY);
        } catch (Exception ignored) {
        }
    }

    private int getThresholdNonZeroCount(byte[] bytes, int threshold) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Paint bitmapPaint = new Paint();

        Bitmap pic = ImageUtil.getBitmap(bytes);
        Bitmap result = Bitmap.createBitmap(pic.getWidth(), pic.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        // gray scale
        bitmapPaint.setColorFilter(new ColorMatrixColorFilter(createGreyMatrix()));
        canvas.drawBitmap(pic, 0, 0, bitmapPaint);

        // black and white
        bitmapPaint.setColorFilter(new ColorMatrixColorFilter(createThresholdMatrix(threshold)));
        canvas.drawBitmap(result, 0, 0, bitmapPaint);

        int nonZero = 0;
        for (int x = 0; x < result.getWidth(); x++) {
            for (int y = 0; y < result.getHeight(); y++) {
                int pixel = result.getPixel(x, y);
                if (pixel == -1) {
                    nonZero++;
                }
            }
        }
        return nonZero;
    }

    private void setCamera(Camera camera) {
        mCamera = camera;
//        List<String> mSupportedFlashModes = mCamera.getParameters().getSupportedFlashModes();
        Camera.Parameters parameters = mCamera.getParameters();

        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        List<String> supportedWhiteBalance = mCamera.getParameters().getSupportedWhiteBalance();
        if (supportedWhiteBalance != null && supportedWhiteBalance.contains(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT)) {
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
        }

        List<String> supportedSceneModes = mCamera.getParameters().getSupportedSceneModes();
        if (supportedSceneModes != null && supportedSceneModes.contains(Camera.Parameters.SCENE_MODE_AUTO)) {
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        }

        List<String> supportedColorEffects = mCamera.getParameters().getSupportedColorEffects();
        if (supportedColorEffects != null && supportedColorEffects.contains(Camera.Parameters.EFFECT_NONE)) {
            parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
        }

        List<Integer> supportedPictureFormats = mCamera.getParameters().getSupportedPictureFormats();
        if (supportedPictureFormats != null && supportedPictureFormats.contains(ImageFormat.JPEG)) {
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setJpegQuality(100);
        }

        List<String> focusModes = parameters.getSupportedFocusModes();

        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        } else {
            // Attempt to set focus to infinity if supported
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
            }
        }

        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<>();
            Rect areaRect1 = new Rect(-METERING_AREA_SIZE, -METERING_AREA_SIZE,
                    METERING_AREA_SIZE, METERING_AREA_SIZE);
            meteringAreas.add(new Camera.Area(areaRect1, 1000));
            parameters.setMeteringAreas(meteringAreas);
        }

        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

        parameters.setExposureCompensation(EXPOSURE_COMPENSATION);

        parameters.setZoom(0);

        parameters.setPictureSize(MIN_PICTURE_WIDTH, MIN_PICTURE_HEIGHT);

        try {
            mCamera.setParameters(parameters);
        } catch (Exception ex) {
            List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();

            parameters.setPictureSize(
                    supportedPictureSizes.get(supportedPictureSizes.size() - 1).width,
                    supportedPictureSizes.get(supportedPictureSizes.size() - 1).height);

            for (Camera.Size size : supportedPictureSizes) {
                if (size.width > MIN_SUPPORTED_WIDTH && size.width < 1000) {
                    parameters.setPictureSize(size.width, size.height);
                    break;
                }
            }

            mCamera.setParameters(parameters);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void saveImage(@NonNull byte[] data, String fileName) {

        File folder = FileHelper.getFilesDir(FileHelper.FileType.TEMP_IMAGE, mSavePath);

        File photo = new File(folder, fileName + ".jpg");

        try {
            FileOutputStream fos = new FileOutputStream(photo.getPath());

            fos.write(data);
            fos.close();
        } catch (Exception ignored) {

        }

        Intent intent = new Intent("custom-event-name");
        intent.putExtra(ConstantKey.SAVE_FOLDER, mSavePath);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
