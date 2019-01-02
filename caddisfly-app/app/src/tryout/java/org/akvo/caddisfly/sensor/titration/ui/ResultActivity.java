package org.akvo.caddisfly.sensor.titration.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.titration.models.DecodeData;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.ColorUtil;
import org.akvo.caddisfly.util.ImageUtil;
import org.json.JSONObject;

import static org.akvo.caddisfly.sensor.titration.decode.DecodeProcessor.applyGrayScale;
import static org.akvo.caddisfly.util.ImageUtil.convertYUV420_NV21toRGB8888;

/**
 * Activity that displays the results.
 */
public class ResultActivity extends BaseActivity {
    private static DecodeData mDecodeData;
    int measureStart = 0;
    int measureEnd = 0;
    float liquidLevel = -1;
    private Button buttonSave;
    private Bitmap finalImage;
    private LinearLayout layout;

    private TestInfo testInfo;

    public static void setDecodeData(DecodeData decodeData) {
        mDecodeData = decodeData;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strip_result);
        setTitle(R.string.result);

        testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);

        buttonSave = findViewById(R.id.button_save);
        buttonSave.setOnClickListener(v -> {

            Intent resultIntent = new Intent();

            resultIntent.putExtra(testInfo.getResults().get(0).getCode(),
                    testInfo.getResults().get(0).getResult());
            resultIntent.putExtra(testInfo.getResults().get(1).getCode(),
                    testInfo.getResults().get(1).getResult());
//            resultIntent.putExtra(testInfo.getResults().get(2).getCode(),
//                    ImageUtil.encodeImage(finalImage));

            final SparseArray<String> results = new SparseArray<>();

            for (int i = 0; i < testInfo.getResults().size(); i++) {
                Result result = testInfo.getResults().get(i);
                results.put(i + 1, result.getResult());
            }

            JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo,
                    results, null, -1, null);

            resultIntent.putExtra(SensorConstants.VALUE, resultJson.toString());

            setResult(Activity.RESULT_OK, resultIntent);

            finish();

//            String path;

//            if (totalImage != null) {
//
//                // store image on sd card
//                path = FileUtil.writeBitmapToExternalStorage(totalImage,
//                        FileHelper.FileType.RESULT_IMAGE, totalImageUrl);
//
//                intent.putExtra(ConstantKey.IMAGE, path);
//
//                if (path.length() == 0) {
//                    totalImageUrl = "";
//                }
//            } else {
//                totalImageUrl = "";
//            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finalImage.recycle();
    }

    @Override
    public void onResume() {
        super.onResume();
        layout = findViewById(R.id.layout_results);
        layout.removeAllViews();

        TestInfo testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);

        computeResults(testInfo);
        showResults(testInfo);
    }

    private void computeResults(TestInfo testInfo) {

        byte[] iDataArray = mDecodeData.getDecodeImageByteArray();

        int width = mDecodeData.getDecodeWidth();
        int height = mDecodeData.getDecodeHeight();

        int[] pixels = applyGrayScale(iDataArray, width, height);

        Bitmap tempImage = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);

        int[] finalPixels = convertYUV420_NV21toRGB8888(iDataArray, width, height);

        Bitmap tempFinalImage = Bitmap.createBitmap(finalPixels, width, height, Bitmap.Config.ARGB_8888);

        int top = (int) mDecodeData.getPatternInfo().getTopLeft().getY();
        int bottom = (int) mDecodeData.getPatternInfo().getBottomLeft().getY();
        int left = (int) mDecodeData.getPatternInfo().getBottomLeft().getX();
        int right = (int) mDecodeData.getPatternInfo().getTopRight().getX();

        Bitmap totalImage = Bitmap.createBitmap(tempImage, left, top, right - left,
                bottom - top, null, false);

        finalImage = Bitmap.createBitmap(tempFinalImage, left, top, right - left,
                bottom - top, null, false);

        int center = totalImage.getHeight() / 2;
        int measureLine = (int) (totalImage.getHeight() * 0.7);

        int measureThreshold = 255;
        int measureCount = 0;

        int col = 30;
        while (col < totalImage.getWidth() - 30) {
            int measurePixel = totalImage.getPixel(col, measureLine);
            int measurePixelCompare = totalImage.getPixel(col - 15, measureLine);
            if (Math.abs(Color.red(measurePixel) - Color.red(measurePixelCompare)) > 50) {
                if (Color.red(measurePixel) < Color.red(measurePixelCompare)) {
                    if (Color.red(measurePixel) < measureThreshold) {
                        measureThreshold = Color.red(measurePixel);
                    }

                    measureCount++;

                    if (measureCount == 1) {
                        measureStart = col;
                    } else if (measureCount == 16) {
                        measureEnd = col;
                    }

                    col += 15;
                }
            }
            col++;
        }

        Bitmap canvasBitmap = Bitmap.createBitmap(finalImage.getWidth(), finalImage.getHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(canvasBitmap);
        canvas.drawBitmap(finalImage, 0, 0, null);

        Paint blackPaint = new Paint();
        blackPaint.setAntiAlias(true);
        blackPaint.setColor(Color.BLACK);

        int liquidLevelStart = 0;

        if (measureCount == 16) {

            int count = 0;
            int foundPixel = 0;
            for (int x = measureEnd - 30; x >= measureStart; x--) {
                int pixel = totalImage.getPixel(x, center);

                double distance;
                if (count == 0) {
                    int nextPixel = totalImage.getPixel(x - 3, center);
                    distance = ColorUtil.getColorDistance(pixel, nextPixel);
                } else {
                    distance = ColorUtil.getColorDistance(pixel, foundPixel);
                }

                if (distance > 20) {
                    if (count == 0) {
                        foundPixel = pixel;
                        liquidLevelStart = x;
                    }
                    count++;
                } else {
                    count = 0;
                }

                if (count > 30) {
                    break;
                }
            }

            try {
                if (measureStart > 0) {
                    int totalMeasure = measureEnd - measureStart;

                    liquidLevel = measureEnd - liquidLevelStart;

                    liquidLevel = (150f * liquidLevel) / totalMeasure;
                }
            } catch (Exception e) {
                e.printStackTrace();
                liquidLevel = -1;
            }
        }

        testInfo.getResults().get(0).setResult(liquidLevel, 0, 0);
        testInfo.getResults().get(1).setResult(liquidLevel, 0, 0);

        mDecodeData.addStripImage(pixels, 0);

        if (measureStart > 0) {
            drawTriangle(canvas, blackPaint, measureStart, center - 90, 30);
        }
        if (measureEnd > 0) {
            drawTriangle(canvas, blackPaint, measureEnd, center - 90, 30);
        }
        if (liquidLevel > 0) {
            Paint greenPaint = new Paint();
            greenPaint.setAntiAlias(true);
            greenPaint.setColor(Color.rgb(0, 153, 0));

            drawTriangle(canvas, greenPaint, liquidLevelStart, center - 110, 60);
        }

        finalImage = Bitmap.createScaledBitmap(canvasBitmap, (int) (finalImage.getWidth() * 0.8),
                (int) (finalImage.getHeight() * 0.8), true);
        tempFinalImage.recycle();
        totalImage.recycle();
        tempImage.recycle();
    }

    // displays results and creates image to send to database
    // if patchResultList is null, it means that the strip was not found.
    // In that case, we show a default error image.
    private void showResults(TestInfo testInfo) {

        // create and display view with results
        // here, the total result image is also created
        createView(testInfo);

        // show buttons
        buttonSave.setVisibility(View.VISIBLE);
    }

    private void createView(TestInfo testInfo) {

        for (Result result : testInfo.getResults()) {
            if (result.getName().equals("Photo")) {
                inflateView(result.getName(), "", finalImage);
            } else {
                if (liquidLevel > 0) {
                    // create image to display on screen
                    inflateView(result.getName(), result.getResult() + " " +
                            result.getUnit(), null);
                } else {
                    inflateView(result.getName(), "No Result", null);
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private void inflateView(String title, String valueString, Bitmap resultImage) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout itemResult;
        if (inflater != null) {
            itemResult = (LinearLayout) inflater.inflate(R.layout.item_result, null, false);
            TextView textTitle = itemResult.findViewById(R.id.text_title);
            textTitle.setText(title);

            if (resultImage != null) {
                resultImage = ImageUtil.rotateImage(resultImage, 90);
                ImageView imageResult = itemResult.findViewById(R.id.image_result);
                imageResult.setImageBitmap(resultImage);
            }

            TextView textResult = itemResult.findViewById(R.id.text_result);
            if (valueString.isEmpty()) {
                textResult.setVisibility(View.GONE);
            } else {
                textResult.setText(valueString);
            }

            layout.addView(itemResult);
        }
    }

    public void drawTriangle(Canvas canvas, Paint paint, int x, int y, int width) {
        int halfWidth = width / 2;

        Path path = new Path();
        path.moveTo(x, y + halfWidth); // Top
        path.lineTo(x - halfWidth, y - halfWidth); // Bottom left
        path.lineTo(x + halfWidth, y - halfWidth); // Bottom right
        path.lineTo(x, y + halfWidth); // Back to Top
        path.close();

        canvas.drawPath(path, paint);
    }
}