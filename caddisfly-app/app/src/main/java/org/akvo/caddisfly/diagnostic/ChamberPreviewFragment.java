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

package org.akvo.caddisfly.diagnostic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.chamber.BaseRunTest;
import org.akvo.caddisfly.sensor.chamber.RunTest;

public class ChamberPreviewFragment extends BaseRunTest implements RunTest {

    /**
     * Instance of fragment.
     *
     * @param testInfo the test info
     * @return the fragment
     */
    public static ChamberPreviewFragment newInstance(TestInfo testInfo) {
        ChamberPreviewFragment fragment = new ChamberPreviewFragment();
        Bundle args = new Bundle();
        args.putParcelable(ConstantKey.TEST_INFO, testInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initializeTest() {
        super.initializeTest();
        binding.imageIllustration.setVisibility(View.GONE);
        binding.circleView.setVisibility(View.GONE);
        binding.textInfo.setVisibility(View.GONE);
        binding.layoutFooter.setVisibility(View.GONE);

        if (!cameraStarted) {
            setupCamera();
            turnFlashOn();
            cameraStarted = true;
            binding.startCaptureButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding.startCaptureButton.setOnClickListener(view -> {
//            stopPreview();
//            turnFlashOff();
            binding.startCaptureButton.setVisibility(View.GONE);
//            pictureCount = AppPreferences.getSamplingTimes() - 1;
            startRepeatingTask();
        });
        return binding.getRoot();
    }
}
