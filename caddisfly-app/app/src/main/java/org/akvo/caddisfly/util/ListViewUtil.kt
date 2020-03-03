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
package org.akvo.caddisfly.util

import android.widget.ListView

/**
 * Utility functions for ListView manipulation.
 */
object ListViewUtil {
    /**
     * Set the ListView height based on the number of rows and their height
     *
     *
     * Height for ListView needs to be set if used as a child of another ListView.
     * The child ListView will not display any scrollbars so the height needs to be
     * set so that all the rows are visible
     *
     * @param listView    the list view
     * @param extraHeight extra bottom padding
     */
    @JvmStatic
    fun setListViewHeightBasedOnChildren(listView: ListView?, extraHeight: Int) {
        if (listView == null) {
            return
        }
        val listAdapter = listView.adapter ?: return
        var totalHeight = 0
        for (i in 0 until listAdapter.count) {
            val listItem = listAdapter.getView(i, null, listView)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
        }
        val params = listView.layoutParams
        params.height = extraHeight + totalHeight + listView.dividerHeight * (listAdapter.count - 1)
        listView.layoutParams = params
    }
}