/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.sensor.turbidity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ParseException;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.akvo.caddisfly.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AnalyseDetailFragment extends Fragment {

    private ResultInfo resultInfo;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AnalyseDetailFragment() {
    }

    public static AnalyseDetailFragment newInstance(ResultInfo resultInfo) {
        AnalyseDetailFragment fragment = new AnalyseDetailFragment();
        fragment.setResultInfo(resultInfo);
        return fragment;
    }

    public static String toTitleCase(String input) {
        input = input.toLowerCase();
        char c = input.charAt(0);
        String s = "" + c;
        String f = s.toUpperCase();
        return f + input.substring(1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_analyse_detail, container, false);

//        Drawable instructionDrawable = AssetsManager.getImage(getActivity(),
//                getArguments().getString(ARG_ITEM_IMAGE));
//        if (instructionDrawable != null) {
//            ((ImageView) rootView.findViewById(R.id.image_illustration)).
//                    setImageDrawable(instructionDrawable);
//        }

        LinearLayout layoutResult = rootView.findViewById(R.id.layoutResult);
        LinearLayout layoutImages = rootView.findViewById(R.id.layoutImages);

        TextView textResult = rootView.findViewById(R.id.textResult);
        TextView textTime = rootView.findViewById(R.id.textTime);
        TextView textDate = rootView.findViewById(R.id.textDate);
        TextView textTotalTime = rootView.findViewById(R.id.textTotalTime);

        textResult.setText(resultInfo.result);

        if (resultInfo.result.equalsIgnoreCase("Present")) {
            textResult.setTextColor(Color.RED);
        } else if (resultInfo.result.equalsIgnoreCase("Absent")) {
            textResult.setTextColor(Color.argb(255, 0, 100, 0));
        } else if (resultInfo.result.equalsIgnoreCase("Incomplete")) {
            textResult.setTextColor(Color.DKGRAY);
        }

        textTime.setText(resultInfo.turbidTime);
        textTotalTime.setText(String.format("Total Time: %s", resultInfo.totalTime));

        ImageView imageStart = rootView.findViewById(R.id.imageStart);
        ImageView imageTurbid = rootView.findViewById(R.id.imageTurbid);
        ImageView imageEnd = rootView.findViewById(R.id.imageEnd);

        imageStart.setImageBitmap(getImage(resultInfo.startImage));

        imageTurbid.setImageBitmap(getImage(resultInfo.turbidImage));

        imageEnd.setImageBitmap(getImage(resultInfo.endImage));

//        AddImage(layoutImages, resultInfo.startImage);
//
//        AddImage(layoutImages, resultInfo.turbidImage);
//
//        AddImage(layoutImages, resultInfo.endImage);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
        try {
            Date date = format.parse(resultInfo.date);

            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);
                String datetime = dateFormat.format(date);
                textDate.setText(datetime);

            } catch (ParseException e) {
                e.printStackTrace();
            }

        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        AddText(layoutResult, String.format("%s, %s chamber", resultInfo.phone, toTitleCase(resultInfo.chamber)));

        AddText(layoutResult, String.format("%s, %s, %s", resultInfo.volume, toTitleCase(resultInfo.media), resultInfo.testType));

        return rootView;
    }

    private Bitmap getImage(String image) {
        return BitmapFactory.decodeFile(image);
    }

    private void AddImage(LinearLayout linearLayout, String startImage) {

        ImageView imageView = new ImageView(getActivity());

        imageView.setPadding(0, 0, 0,
                (int) getResources().getDimension(R.dimen.activity_vertical_margin));

        Bitmap bitmap = BitmapFactory.decodeFile(startImage);

        imageView.setImageBitmap(bitmap);

        linearLayout.addView(imageView);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int length = (size.x / 3) - 10;
        imageView.getLayoutParams().width = length;
        imageView.getLayoutParams().height = length;
        imageView.setPadding(0, 0, 0, 0);

        linearLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, length));
    }

    private void AddText(LinearLayout linearLayout, String text) {
        TextView textView = new TextView(getActivity());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.mediumTextSize));

        textView.setPadding(0, 0, 0,
                (int) getResources().getDimension(R.dimen.activity_vertical_margin));

        textView.setGravity(Gravity.CENTER);

//        textView.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.0f,
//                getResources().getDisplayMetrics()), 1.0f);

        textView.setTextColor(Color.argb(255, 100, 100, 100));
        textView.append(text);
        linearLayout.addView(textView);
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    public CharSequence getTitle() {
        return String.valueOf(resultInfo.testNumber);
    }
}
