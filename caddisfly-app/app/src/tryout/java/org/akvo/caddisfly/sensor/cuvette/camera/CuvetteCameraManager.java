package org.akvo.caddisfly.sensor.cuvette.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;

import org.akvo.caddisfly.common.ChamberTestConfig;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.chamber.ChamberCameraPreview;
import org.akvo.caddisfly.util.ColorUtil;
import org.akvo.caddisfly.util.ImageUtil;

import java.io.ByteArrayOutputStream;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class CuvetteCameraManager {
    private final TestInfo testInfo;
    private final Activity context;
    private LocalBroadcastManager localBroadcastManager;
    private Handler mCameraHandler;
    private Camera mCamera;

    private byte[] bytes;

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] imageData, Camera camera) {

            if (bytes != null && bytes.length > 0 && AppPreferences.isTestMode()) {
                // Use dialog_device_list image if we are in dialog_device_list mode

            } else {
                Camera.Parameters parameters = camera.getParameters();
                YuvImage im = new YuvImage(imageData, ImageFormat.NV21, parameters.getPreviewSize().width, parameters.getPreviewSize().height, null);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                im.compressToJpeg(new Rect(0,0,parameters.getPreviewSize().width, parameters.getPreviewSize().height), 100, out);
                byte[] imageBytes = out.toByteArray();
                Bitmap tempFinalImage = BitmapFactory.decodeByteArray(imageBytes, 0, out.size());

                tempFinalImage = ImageUtil.rotateImage(context, tempFinalImage);

                Bitmap croppedBitmap = ImageUtil.getCroppedBitmap(tempFinalImage,
                        ChamberTestConfig.SAMPLE_CROP_LENGTH_DEFAULT);

                ColorInfo photoColor = ColorUtil.getColorFromBitmap(croppedBitmap,
                        ChamberTestConfig.SAMPLE_CROP_LENGTH_DEFAULT);

                ResultDetail resultDetail = SwatchHelper.analyzeColor(testInfo.getSwatches().size(),
                        photoColor, testInfo.getSwatches());

                Intent localIntent = new Intent("CUVETTE_RESULT_ACTION");
                localIntent.putExtra("cuvette_result",
                        String.valueOf(resultDetail.getResult())
                                + "," + resultDetail.getColor()
                                + "," + resultDetail.getQuality());

                localBroadcastManager.sendBroadcast(localIntent);
            }
        }
    };

    public CuvetteCameraManager(Activity context, TestInfo testInfo) {
        this.context = context;
        this.testInfo = testInfo;
        if (AppPreferences.isTestMode()) {
            bytes = ImageUtil.loadImageBytes(testInfo.getName(), FileHelper.FileType.TEST_IMAGE);
        }

        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public ChamberCameraPreview initCamera(Context context) {
        startCameraThread();

        // open the camera and create a preview surface for it
        ChamberCameraPreview cameraPreview = new ChamberCameraPreview(context);
        mCamera = cameraPreview.getCamera();
        cameraPreview.setupCamera(mCamera);

        return cameraPreview;
    }

    public void setDecodeImageCaptureRequest() {
        if (mCameraHandler != null && mCamera != null) {
            try {
                mCameraHandler.post(() -> {
                    if (mCamera != null) {
                        mCamera.setOneShotPreviewCallback(previewCallback);
                    }
                });
            } catch (Exception ignored) {
                // do nothing
            }
        }
    }

    public void stopCamera() {
        mCamera = null;
        mCameraHandler = null;
    }

    /**
     * Starts a background thread and its Handler.
     */
    private void startCameraThread() {
        HandlerThread mCameraThread = new HandlerThread("CameraBackground");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
    }
}
