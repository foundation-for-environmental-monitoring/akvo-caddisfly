package org.akvo.caddisfly.model

import android.util.SparseArray

class PageIndex {
    private val pages = SparseArray<PageType>()
    var skipToIndex = -1
    var skipToIndex2 = -1
    fun setPhotoIndex(index: Int) {
        pages.put(index, PageType.PHOTO)
    }

    fun setInputIndex(index: Int) {
        pages.put(index, PageType.INPUT)
    }

    fun setResultIndex(index: Int) {
        pages.put(index, PageType.RESULT)
    }

    fun clear() {
        pages.clear()
    }
}