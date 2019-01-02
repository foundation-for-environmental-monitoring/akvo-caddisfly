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

package org.akvo.caddisfly.helper;

import android.util.SparseArray;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.common.TestConstants;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.repository.TestConfigRepository;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.Assert.assertEquals;

@SuppressWarnings("unused")
@RunWith(RobolectricTestRunner.class)
public class ResultsTest {

    @Test
    public void testColorimetryResult() {

        TestConfigRepository testConfigRepository = new TestConfigRepository();
        TestInfo testInfo = testConfigRepository.getTestInfo(TestConstants.CUVETTE_TEST_ID_1);

        assert testInfo != null;

        SparseArray<String> results = new SparseArray<>();
        results.put(1, "> 2.0");

        JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo, results, null, -1, "");

        // Replace items that cannot be tested (e.g. currentTime)
        String json = resultJson.toString().replaceAll("(\"testDate\":\").*?\"", "$1today\"");
        json = json.replaceAll("(\"appVersion\":\").*?\"", "$1version\"");
        json = json.replaceAll("(\"country\":\").*?\"", "$1\"");

        String expectedJson = "{\"type\":\"" + BuildConfig.APPLICATION_ID + "\",\"name\":\"" + TestConstants.CUVETTE_TEST_NAME_1 + "\",\"uuid\":\"" + TestConstants.CUVETTE_TEST_ID_1 + "\",\"result\":[{\"dilution\":0,\"name\":\"" + TestConstants.CUVETTE_TEST_NAME_1 + "\",\"unit\":\"" + TestConstants.CUVETTE_TEST_UNIT + "\",\"id\":1,\"value\":\"> 2.0\"}],\"testDate\":\"today\"}";

        assertEquals(expectedJson, json);
    }
}
