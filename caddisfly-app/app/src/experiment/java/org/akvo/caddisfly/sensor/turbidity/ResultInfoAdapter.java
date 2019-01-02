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

package org.akvo.caddisfly.sensor.turbidity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.databinding.ResultItemBinding;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class ResultInfoAdapter extends RecyclerView.Adapter<ResultInfoAdapter.TestInfoViewHolder> {

    private final View.OnClickListener mOnClickListener = view -> {
        ResultInfo item = (ResultInfo) view.getTag();
        Context context = view.getContext();
        Intent intent = new Intent(context, ResultInfoDetailActivity.class);
        intent.putExtra(ResultInfoDetailActivity.ARG_ITEM_ID, item.folder);

        context.startActivity(intent);
    };
    @Nullable
    private List<? extends ResultInfo> mTestList;

    ResultInfoAdapter() {
    }

    void setTestList(final List<? extends ResultInfo> testList) {
        if (mTestList != null) {
            mTestList.clear();
        }
        mTestList = testList;
        notifyItemRangeInserted(0, testList.size());
    }

    @NonNull
    @Override
    public TestInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ResultItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.result_item,
                        parent, false);
        return new TestInfoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TestInfoViewHolder holder, int position) {
        if (mTestList != null) {
            holder.binding.setResultInfo(mTestList.get(position));
            holder.itemView.setTag(mTestList.get(position));
            holder.binding.executePendingBindings();
            holder.itemView.setOnClickListener(mOnClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return mTestList == null ? 0 : mTestList.size();
    }

    static class TestInfoViewHolder extends RecyclerView.ViewHolder {

        final ResultItemBinding binding;

        TestInfoViewHolder(ResultItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
