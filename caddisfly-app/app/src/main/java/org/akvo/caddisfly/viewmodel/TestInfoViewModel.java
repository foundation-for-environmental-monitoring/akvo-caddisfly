/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.akvo.caddisfly.viewmodel;

import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.model.Instruction;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.StringUtil;
import org.akvo.caddisfly.widget.RowView;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestInfoViewModel extends AndroidViewModel {

    private static TestInfo testInfo;
    public final ObservableField<TestInfo> test = new ObservableField<>();

    public TestInfoViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Sets the content of the view with formatted string.
     *
     * @param linearLayout the layout
     * @param instruction  the instruction key
     */
    @BindingAdapter("content")
    public static void setContent(LinearLayout linearLayout, Instruction instruction) {

        if (instruction == null || instruction.section == null) {
            return;
        }

        Context context = linearLayout.getContext();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        Point size = new Point();
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getRealSize(size);
        }

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        for (int i = 0; i < instruction.section.size(); i++) {
            String text = instruction.section.get(i);
            if (text.contains("image:")) {
                insertImage(linearLayout, context, size, displayMetrics, i, text);
            } else {

                RowView rowView = new RowView(context);

                Matcher m1 = Pattern.compile("^(\\d+?\\.\\s*)(.*)").matcher(text);
                if (m1.find()) {
                    rowView.setNumber(m1.group(1).trim());
                    text = m1.group(2).trim();
                }

                String[] sentences = (text + ". ").split("\\.\\s+");

                for (int j = 0; j < sentences.length; j++) {
                    if (j > 0) {
                        rowView.append(new SpannableString(" "));
                    }
                    rowView.append(StringUtil.toInstruction((AppCompatActivity) context,
                            testInfo, sentences[j].trim()));

                    if (StringUtil.getStringResourceByName(context, sentences[j]).toString().contains("[/a]")) {
                        rowView.enableLinks();
                    }
                }

                // set an id for the view to be able to find it for unit testing
                rowView.setId(i);

                linearLayout.addView(rowView);

                SpannableStringBuilder builder = new SpannableStringBuilder();
                Spanned spanned2 = StringUtil.getStringResourceByName(context, text);
                builder.append(spanned2);
            }
        }
    }

    @BindingAdapter("testSubtitle")
    public static void setSubtitle(TextView view, TestInfo testInfo) {
        String subTitle = testInfo.getMinMaxRange();
//        if (testInfo.getBrand() != null) {
//            subTitle = testInfo.getBrand() + ", ";
//        }
        if (!testInfo.getMinMaxRange().isEmpty()) {
            Matcher matcher = Pattern.compile("<dilutionRange>(.*?)</dilutionRange>").matcher(subTitle);
            if (matcher.find()) {
                subTitle = matcher.replaceAll(String.format(view.getResources()
                        .getString(R.string.up_to_with_dilution), matcher.group(1)));
            }
        }
        view.setText(subTitle);
    }

    private static void insertImage(LinearLayout linearLayout, Context context, Point size,
                                    DisplayMetrics displayMetrics, int i, String text) {

        String imageName = text.substring(text.indexOf(":") + 1);

        int resourceId = context.getResources().getIdentifier("drawable/in_" + imageName,
                "id", BuildConfig.APPLICATION_ID);

        if (resourceId > 0) {

            double divisor = 3;
            if (displayMetrics.densityDpi > 250) {
                divisor = 2.4;
            }

            if (size.y > displayMetrics.heightPixels) {
                divisor += 0.3;
            }

            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (displayMetrics.heightPixels / divisor));

            llp.setMargins(0, 0, 0, 20);

            final AppCompatImageView imageView = new AppCompatImageView(context);
            imageView.setImageResource(resourceId);
            imageView.setLayoutParams(llp);
            imageView.setContentDescription(imageName);

            // set an id for the view to be able to find it for unit testing
            imageView.setId(i);

            linearLayout.addView(imageView);

        } else {

            String image = Constants.ILLUSTRATION_PATH + imageName + ".webp";

            InputStream ims = null;
            try {
                ims = context.getAssets().open(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (ims != null) {

                ImageView imageView = new ImageView(linearLayout.getContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                imageView.setImageDrawable(Drawable.createFromStream(ims, null));

                double divisor = 3.1;
                if (displayMetrics.densityDpi > 250) {
                    divisor = 2.7;
                }

                if (size.y > displayMetrics.heightPixels) {
                    divisor += 0.3;
                }

                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        (int) (displayMetrics.heightPixels / divisor));

                llp.setMargins(0, 0, 0, 20);
                imageView.setLayoutParams(llp);

                imageView.setContentDescription(imageName);

                // set an id for the view to be able to find it for unit testing
                imageView.setId(i);

                linearLayout.addView(imageView);
            }
        }
    }

    /**
     * Sets the image scale.
     *
     * @param imageView the image view
     * @param scaleType the scale type
     */
    @BindingAdapter("imageScale")
    public static void setImageScale(ImageView imageView, String scaleType) {
        if (scaleType != null) {
            imageView.setScaleType("fitCenter".equals(scaleType)
                    ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
    }

    @BindingAdapter("imageUrl")
    public static void setImageUrl(ImageView imageView, String name) {
        setImage(imageView, Constants.BRAND_IMAGE_PATH + name + ".webp");
    }

    private static void setImage(ImageView imageView, String theName) {
        if (theName != null) {
            Context context = imageView.getContext();
            try {
                String name = theName.replace(" ", "-");
                InputStream ims = context.getAssets().open(name);
                imageView.setImageDrawable(Drawable.createFromStream(ims, null));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setTest(TestInfo testInfo) {
        this.test.set(testInfo);
        TestInfoViewModel.testInfo = testInfo;
    }
}
