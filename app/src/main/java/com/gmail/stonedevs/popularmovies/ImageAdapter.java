package com.gmail.stonedevs.popularmovies;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * Custom Adapter class that fills requested view (GridView) with an array of ImageViews
 */
public class ImageAdapter extends BaseAdapter {
    private SparseArray<ImageView> mImageList;

    public ImageAdapter() {
        this.mImageList = new SparseArray<>();
    }

    @Override
    public int getCount() {
        return mImageList.size();
    }

    @Override
    public ImageView getItem(int position) {
        return mImageList.valueAt(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mImageList == null || mImageList.size() == 0 || position > mImageList.size())
            return null;

        return mImageList.valueAt(position);
    }

    public void add(ImageView imageView) {
        mImageList.put(mImageList.size(), imageView);
        notifyDataSetChanged();
    }

    public void clear() {
        mImageList.clear();
    }
}
