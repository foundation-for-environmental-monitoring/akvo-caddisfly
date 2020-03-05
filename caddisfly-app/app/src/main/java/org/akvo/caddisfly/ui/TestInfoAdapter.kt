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
package org.akvo.caddisfly.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.akvo.caddisfly.R
import org.akvo.caddisfly.databinding.TestItemBinding
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.ui.TestInfoAdapter.TestInfoViewHolder

class TestInfoAdapter internal constructor(private val mTestInfoClickCallback: TestInfoClickCallback?) : RecyclerView.Adapter<TestInfoViewHolder>() {
    private var mTestList: MutableList<out TestInfo>? = null
    fun setTestList(testList: MutableList<out TestInfo>) {
        if (mTestList != null) {
            mTestList!!.clear()
        }
        mTestList = testList
        notifyItemRangeInserted(0, testList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestInfoViewHolder {
        val binding: TestItemBinding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.context), R.layout.test_item,
                        parent, false)
        binding.callback = mTestInfoClickCallback
        return TestInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TestInfoViewHolder, position: Int) {
        holder.binding.testInfo = mTestList!![position]
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return if (mTestList == null) 0 else mTestList!!.size
    }

    fun getItemAt(i: Int): TestInfo {
        return mTestList!![i]
    }

    class TestInfoViewHolder(val binding: TestItemBinding) : RecyclerView.ViewHolder(binding.root)

}