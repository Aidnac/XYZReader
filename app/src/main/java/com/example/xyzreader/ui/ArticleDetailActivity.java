package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, ImageLoaderHelper.ImageLoaderCacheWaiter {

    private Cursor mCursor;
    private long mStartId;
    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;

    private ImageLoaderHelper imageLoaderHelper;
    private String mSelectedItemImageUrl;

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView photoView;
    private View scrim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        imageLoaderHelper = ImageLoaderHelper.getInstance(this);

        getLoaderManager().initLoader(0, null, this);

        collapsingToolbarLayout = findViewById(R.id.collapse_toolbar);
        photoView = findViewById(R.id.photo);
        scrim = findViewById(R.id.scrim);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager =  findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                }
                mSelectedItemImageUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
                if (imageLoaderHelper.getImageLoader().isCached(mSelectedItemImageUrl,0,0)){
                    Bitmap bitmap = imageLoaderHelper.getBitmap(getCacheKey(mSelectedItemImageUrl));
                    setBackdropImage(bitmap);
                }else setBackdropDefaults();
            }
        });



        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    mSelectedItemImageUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }
    public void setBackdropImage(Bitmap bitmap){
        if(bitmap != null) {
            Palette palette = Palette.from(bitmap).generate();
            photoView.setImageBitmap(bitmap);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getWindow().setStatusBarColor(palette.getDarkVibrantColor(ContextCompat.getColor(this, android.R.color.transparent)));
            collapsingToolbarLayout.setContentScrimColor(palette.getVibrantColor(ContextCompat.getColor(this, android.R.color.transparent)));
            scrim.setVisibility(View.GONE);
        }else setBackdropDefaults();
    }

    public void setBackdropDefaults(){
        photoView.setImageBitmap(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, android.R.color.transparent));
        scrim.setVisibility(View.VISIBLE);
    }

    public String getCacheKey(String url) {
        return (new StringBuilder(url.length() + 12)).append("#W").append(0).append("#H").append(0).append(url).toString();
    }

    @Override
    public void onAddedToCache(String key, Bitmap bitmap) {
        if(getCacheKey(mSelectedItemImageUrl).equals(key)){
            setBackdropImage(bitmap);
        }
    }


    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }


}
