package com.bignerdranch.android.photogallery;

import android.content.Intent;
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
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rasul on 17.06.2016.
 */
public class PhotoGalleryFragment extends VisibleFragment {
    public static final String TAG = "PhotoGallery";
    public static final int PAGE_SIZE = 102;
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
        setHasOptionsMenu(true);
        setRetainInstance(true);
        mPhotoAdapter = new PhotoAdapter(new ArrayList<GalleryItem>());
        updateItems();

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDowload(PhotoHolder target, Bitmap thumbnail) {
                if (isAdded()) {
                    Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                    target.bind(drawable);
                }
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG,"Background message loop started");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "QueryTextSubmit: " + query);
                QueryPrefs.setStoredQuery(getActivity(), query);
                mPhotoAdapter.setGalleryItems(new ArrayList<GalleryItem>());
                updateItems();
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "QueryTextChange: " + newText);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPrefs.getStoredQuery(getActivity());
                Log.i(TAG, "Search clicked. Stored query: " + query);
                searchView.setQuery(query, false);
            }
        });

        MenuItem togglePolling = menu.findItem(R.id.menu_toggle_polling);
        togglePolling.setTitle(PollService.isAlarmOn(getActivity()) ? R.string.stop_polling : R.string.start_polling);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPrefs.setStoredQuery(getActivity(), null);
                updateItems();
                return true;
            case R.id.menu_toggle_polling:
                boolean shouldStart = !PollService.isAlarmOn(getActivity());
                PollService.setAlarm(getActivity(), shouldStart);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void updateItems() {
        updateItems(1);
    }

    private void updateItems(int page) {
        if (mFetchItemsTask != null) {
            mFetchItemsTask.cancel(false);
        }
        String query = QueryPrefs.getStoredQuery(getActivity());
        mFetchItemsTask = new FetchItemsTask(query, page);
        mFetchItemsTask.execute();
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
                            updateItems(pageNum);
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
            if (isAdded()) {
                mImageView.setImageDrawable(item);
            }
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

        private final String mQuery;
        private final int mPage;

        public FetchItemsTask(String query, int page) {
            mQuery = query;
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
            List<GalleryItem> items = new ArrayList<>();
            if (mQuery == null) {
                items = new FlickrFetchr().fetchRecent(mPage);
            } else {
                items = new FlickrFetchr().search(mQuery, mPage);
            }
            return isCancelled() ? null : items;
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            setupAdapter(galleryItems);
        }
    }
}
