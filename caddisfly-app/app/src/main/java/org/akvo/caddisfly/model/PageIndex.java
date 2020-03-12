package org.akvo.caddisfly.model;

import android.util.SparseArray;

public class PageIndex {
    private final SparseArray<PageType> pages = new SparseArray<>();
    private int skipToIndex = -1;
    private int skipToIndex2 = -1;

    public void setPhotoIndex(int index) {
        pages.put(index, PageType.PHOTO);
    }

    public void setInputIndex(int index) {
        pages.put(index, PageType.INPUT);
    }

    public void setResultIndex(int index) {
        pages.put(index, PageType.RESULT);
    }

    public int getSkipToIndex() {
        return skipToIndex;
    }

    public void setSkipToIndex(int value) {
        skipToIndex = value;
    }

    public int getSkipToIndex2() {
        return skipToIndex2;
    }

    public void setSkipToIndex2(int value) {
        skipToIndex2 = value;
    }

    public void clear() {
        pages.clear();
    }
}
