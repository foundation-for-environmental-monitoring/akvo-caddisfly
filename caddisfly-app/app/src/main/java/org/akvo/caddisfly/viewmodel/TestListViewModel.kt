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
package org.akvo.caddisfly.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.model.TestSampleType
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.repository.TestConfigRepository

class TestListViewModel(application: Application) : AndroidViewModel(application) {
    private val testConfigRepository: TestConfigRepository = TestConfigRepository()

    fun getTests(testSampleType: TestSampleType?): ArrayList<TestInfo> {
        return testConfigRepository.getTests(testSampleType!!)
    }

    fun getTests(testSampleType: TestSampleType?, testType: TestType?): ArrayList<TestInfo> {
        return testConfigRepository.getTests(testSampleType!!, testType!!)
    }

    fun getTestInfo(uuid: String): TestInfo? {
        return testConfigRepository.getTestInfo(uuid)
    }

    fun clearTests() {
        testConfigRepository.clear()
    }
}