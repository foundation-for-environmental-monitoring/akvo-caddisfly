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

package org.akvo.caddisfly.sensor.chamber;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.databinding.FragmentResultBinding;
import org.akvo.caddisfly.model.QualityGuide;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.Standard;
import org.akvo.caddisfly.model.TestInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import static org.akvo.caddisfly.common.ConstantKey.IS_INTERNAL;
import static org.akvo.caddisfly.common.ConstantKey.TEST_INFO;

public class ResultFragment extends Fragment {

    /**
     * Get the instance.
     */
    public static ResultFragment newInstance(TestInfo testInfo, boolean isInternal) {
        ResultFragment fragment = new ResultFragment();
        Bundle args = new Bundle();
        args.putParcelable(TEST_INFO, testInfo);
        args.putBoolean(IS_INTERNAL, isInternal);
        fragment.setArguments(args);
        return fragment;
    }

    static public String readStringFromResource(Context ctx, int resourceID) {
        StringBuilder contents = new StringBuilder();
        String sep = System.getProperty("line.separator");

        try {
            InputStream is = ctx.getResources().openRawResource(resourceID);

            try (BufferedReader input = new BufferedReader(new InputStreamReader(is), 1024 * 8)) {
                String line;
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(sep);
                }
            }
        } catch (IOException ex) {
            return null;
        }

        return contents.toString();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentResultBinding b = DataBindingUtil.inflate(inflater,
                R.layout.fragment_result, container, false);
        View view = b.getRoot();

        if (!BuildConfig.showExperimentalTests) {
            b.buttonSendToServer.setVisibility(View.GONE);
        }

        if (getActivity() != null) {
            getActivity().setTitle(R.string.result);
        }

        if (getArguments() != null) {
            TestInfo testInfo = getArguments().getParcelable(TEST_INFO);
            if (testInfo != null) {
                Result result = testInfo.getResults().get(0);

                String json = readStringFromResource(Objects.requireNonNull(getActivity()),
                        R.raw.quality_guide_ind);

                b.safeInfoLayout.setVisibility(View.VISIBLE);

                List<Standard> standards = new Gson().fromJson(json, QualityGuide.class).getStandards();
                for (Standard standard : standards) {
                    if (standard.getUuid() != null && standard.getUuid().equalsIgnoreCase(testInfo.getUuid())) {
                        if (standard.getMax() != null && result.getResultValue() > standard.getMax()) {
                            b.unsafeInfoLayout.setVisibility(View.VISIBLE);
                            b.safeInfoLayout.setVisibility(View.GONE);
                        }

                        if (standard.getMin() != null && standard.getMin() < result.getResultValue()) {
                            b.unsafeInfoLayout.setVisibility(View.VISIBLE);
                            b.safeInfoLayout.setVisibility(View.GONE);
                        }
                        break;
                    }
                }

                b.textResult.setText(result.getResult());
                b.textTitle.setText(testInfo.getName());
                b.textDilution.setText(getResources().getQuantityString(R.plurals.dilutions,
                        testInfo.getDilution(), testInfo.getDilution()));
                b.textUnit.setText(result.getUnit());

                if (testInfo.getDilution() == testInfo.getMaxDilution()) {
                    b.dilutionLayout.setVisibility(View.GONE);
                } else if (result.highLevelsFound()) {
                    b.dilutionLayout.setVisibility(View.VISIBLE);
                } else {
                    b.dilutionLayout.setVisibility(View.GONE);
                }
            }
        }

        return view;
    }
}

