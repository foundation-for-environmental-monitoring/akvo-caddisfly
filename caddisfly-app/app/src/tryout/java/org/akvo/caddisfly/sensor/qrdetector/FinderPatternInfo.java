/*
 * Copyright 2007 ZXing authors
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

package org.akvo.caddisfly.sensor.qrdetector;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * <p>Encapsulates information about finder patterns in an image, including the location of
 * the three finder patterns, and their estimated module size.</p>
 *
 * @author Sean Owen
 */
public final class FinderPatternInfo {

    private FinderPattern bottomLeft;
    private FinderPattern topLeft;
    private FinderPattern topRight;
    private FinderPattern bottomRight;

    public FinderPatternInfo(FinderPattern[] patternInfo) {

        ArrayList<FinderPattern> patternArrayList = new ArrayList<>(Arrays.asList(patternInfo));

        float currentValue = Float.MAX_VALUE;
        for (FinderPattern patternCenter : patternArrayList) {
            float temp = patternCenter.getX() + patternCenter.getY();
            if (temp < currentValue) {
                currentValue = temp;
                topLeft = patternCenter;
            }
        }

        patternArrayList.remove(topLeft);

        currentValue = 0;
        for (FinderPattern patternCenter : patternArrayList) {
            float temp = patternCenter.getX() + patternCenter.getY();
            if (temp > currentValue) {
                currentValue = temp;
                bottomRight = patternCenter;
            }
        }

        patternArrayList.remove(bottomRight);

        currentValue = 0;
        for (FinderPattern patternCenter : patternArrayList) {
            if (patternCenter.getX() > currentValue) {
                currentValue = patternCenter.getX();
                topRight = patternCenter;
            }
        }

        patternArrayList.remove(topRight);

        bottomLeft = patternArrayList.get(0);

        if (Math.abs(topLeft.getX() - bottomLeft.getX()) > 20 ||
                Math.abs(topLeft.getY() - topRight.getY()) > 20) {
            topLeft = new FinderPattern(0, 0, 0);
            bottomRight = new FinderPattern(0, 0, 0);
            topRight = new FinderPattern(0, 0, 0);
            bottomLeft = new FinderPattern(0, 0, 0);
        }
    }

    public FinderPattern getBottomLeft() {
        return bottomLeft;
    }

    public FinderPattern getTopLeft() {
        return topLeft;
    }

    public FinderPattern getTopRight() {
        return topRight;
    }

    public FinderPattern getBottomRight() {
        return bottomRight;
    }
}
