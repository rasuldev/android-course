package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rasul on 17.06.2016.
 */
public class PhotoGalleryFragment extends Fragment {
    public static final String TAG = "PhotoGallery";
    public static final int PAGE_SIZE = 100;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private PhotoAdapter mPhotoAdapter;
    private FetchItemsTask mFetchItemsTask;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mPhotoAdapter = new PhotoAdapter(new ArrayList<GalleryItem>());
        mFetchItemsTask = new FetchItemsTask(1);
        mFetchItemsTask.execute();

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDowload(PhotoHolder target, Bitmap thumbnail) {
                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                target.bind(drawable);
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG,"Background message loop started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG,"Background message loop started");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.photo_gallery_recycler_view);
        mLayoutManager = new GridLayoutManager(getActivity(), 3);
        mRecyclerView.setAdapter(mPhotoAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    int total = mLayoutManager.getItemCount();
                    int lastVisible = mLayoutManager.findLastVisibleItemPosition();
                    if (lastVisible > total - 2) {
                        // load next page
                        if (mFetchItemsTask.getStatus() == AsyncTask.Status.FINISHED) {
                            Log.i(TAG, "Load new portion");
                            int pageNum = total / PAGE_SIZE + 1;
                            mFetchItemsTask = new FetchItemsTask(pageNum);
                            mFetchItemsTask.execute();
                        }
                    }
                }

            }
        });
        return v;
    }

    private void setupAdapter(List<GalleryItem> galleryItems) {
        if (isAdded()) {
            mPhotoAdapter.addGalleryItems(galleryItems);
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.gallery_item_photo);
        }

        public void bind(Drawable item) {
            mImageView.setImageDrawable(item);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> mGalleryItems;

        public List<GalleryItem> getGalleryItems() {
            return mGalleryItems;
        }

        public void setGalleryItems(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
            notifyDataSetChanged();
        }

        public void addGalleryItems(List<GalleryItem> galleryItems) {
            mGalleryItems.addAll(galleryItems);
            notifyDataSetChanged();
        }

        public PhotoAdapter(List<GalleryItem> galleryItems) {

            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            holder.bind(getResources().getDrawable(R.drawable.bill_up_close));
            mThumbnailDownloader.queueThumbnail(holder, mGalleryItems.get(position).getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {

        final private int mPage;

        public FetchItemsTask(int page) {
            mPage = page;
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
//            try {
//                String result = new FlickrFetchr().getUrlString("https://ya.ru");
//                Log.i(TAG, "Fetched content: " + result);
//            } catch (IOException e) {
//                Log.e(TAG, "Failed to fetch url: ", e);
//            }
            return new FlickrFetchr().fetchItems(mPage);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            setupAdapter(galleryItems);
        }
    }
}
