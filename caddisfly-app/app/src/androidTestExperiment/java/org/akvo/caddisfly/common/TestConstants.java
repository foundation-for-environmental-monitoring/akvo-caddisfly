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

package org.akvo.caddisfly.common;

import org.akvo.caddisfly.model.TestSampleType;

public final class TestConstants {

    public static final int SENSOR_TESTS_COUNT = 3;
    public static final int STRIP_TESTS_COUNT = 21;
    public static final int MD610_TESTS_COUNT = 53;
    public static final int MANUAL_TESTS_COUNT = 3;

    // Soil - Available Iron
//    public static final String CUVETTE_TEST_ID_1 = "3353f5cf-1cd2-4bf5-b47f-15d3db917add";
//    public static final int TEST_INDEX = 1;

    // Soil - pH
//    public static final String TEST_GROUP = "Soil Tests 1";
//    public static final String TEST_NAME = "pH";
//    public static final String CUVETTE_TEST_ID_1 = "485a0c0a-0af5-40bc-baee-f5f0b03ff739";
//    public static final int TEST_START_DELAY = 0;
//    public static final int TEST_INDEX = 0;
//    public static final int CUVETTE_TEST_TIME_DELAY = 30;
//    public static final double EXPECTED_RESULT = 3.9;
//    public static final boolean IS_HAS_DILUTION = false;

    // Water - Fluoride
    public static final TestSampleType IS_TEST_TYPE = TestSampleType.WATER;
    public static final String TEST_GROUP = "Water Tests 1";
    public static final String TEST_NAME = "Fluoride";
    public static final String CUVETTE_TEST_ID_1 = Constants.FLUORIDE_ID;
    public static final int TEST_START_DELAY = 34;
    public static final int TEST_INDEX = 6;
    public static final int CUVETTE_TEST_TIME_DELAY = 0;
    public static final double EXPECTED_RESULT = 1.4;
    public static final boolean IS_HAS_DILUTION = true;

    public static final int CUVETTE_TEST_TIME_DELAY = 180;

}
