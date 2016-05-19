package com.example.xyzreader.ui;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    float offset ;
    Typeface typefaceMedium;
    Typeface typefaceBold;
    Typeface typefaceRegular;
    Interpolator interpolator;
    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        offset = getResources().getDimensionPixelSize(R.dimen.offset_y);
        interpolator =
                AnimationUtils.loadInterpolator(this, android.R.interpolator.linear_out_slow_in);


        final View toolbarContainerView = findViewById(R.id.toolbar_container);
          mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            refresh();
        }
    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        typefaceRegular=  Typeface.createFromAsset(getResources().getAssets(), "Roboto-Regular.ttf");
        typefaceMedium=  Typeface.createFromAsset(getResources().getAssets(), "Roboto-Medium.ttf");
        typefaceBold=  Typeface.createFromAsset(getResources().getAssets(), "Roboto-Bold.ttf");
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    private boolean mIsRefreshing = false;

    public static final float LARGE_SCALE = 1.5f;
    private boolean symmetric = false;
    private boolean small = false;

    @Override
    protected void onSaveInstanceState(Bundle outState) {

//        outState.putInt("Pager_Current",);
//        outState.putInt("Selected_match",selected_match_id);
//        getSupportFragmentManager().putFragment(outState,"my_main",my_main);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        Log.v(save_tag,"will retrive");
//        Log.v(save_tag,"fragment: "+String.valueOf(savedInstanceState.getInt("Pager_Current")));
//        Log.v(save_tag,"selected id: "+savedInstanceState.getInt("Selected_match"));
//        current_fragment = savedInstanceState.getInt("Pager_Current");
//        selected_match_id = savedInstanceState.getInt("Selected_match");
        //my_main = (PagerFragment) getSupportFragmentManager().getFragment(savedInstanceState,"my_main");
        super.onRestoreInstanceState(savedInstanceState);
    }

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
       mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }
    public void changeSize(View card) {
        Interpolator interpolator = AnimationUtils.loadInterpolator(this, android.R
                .interpolator.fast_out_slow_in);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(card, View.SCALE_X, (small ? LARGE_SCALE : 1f));
        scaleX.setInterpolator(interpolator);
        scaleX.setDuration(symmetric ? 600L : 200L);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(card, View.SCALE_Y, (small ? LARGE_SCALE : 1f));
        scaleY.setInterpolator(interpolator);
        scaleY.setDuration(600L);
        scaleX.start();
        scaleY.start();

        // toggle the state so that we switch between large/small and symmetric/asymmetric
//        small = !small;
//        if (small) {
//            symmetric = !symmetric;
//        }
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Adapter adapter = new Adapter(cursor,this);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private Cursor mCursor;
        private final int[] COLORS = new int[] { 0xff956689, 0xff80678A, 0xff6A6788,
                0xff546683, 0xff3F657B, 0xff3F657B };
        public Adapter(Cursor cursor,Activity activity) {
            mCursor = cursor;
            host=activity;
            inflater = LayoutInflater.from(host);
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(ArticleLoader.Query._ID);
        }
        private Activity host;
        private  LayoutInflater inflater;
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ViewHolder vh = new ViewHolder(view);



            view.setVisibility(View.VISIBLE);
            view.setTranslationY(offset);
            view.setAlpha(0.85f);
            // then animate back to natural position
            view.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setInterpolator(interpolator)
                    .setDuration(1000L)
                    .start();
            // increase the offset distance for the next view
            offset *= 1.5f;
            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder,final int position) {
            final int color = COLORS[position % COLORS.length];
            mCursor.moveToPosition(position);
            holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            holder.titleView.setContentDescription(mCursor.getString(ArticleLoader.Query.TITLE));
            Typeface typefaceRegular=  Typeface.createFromAsset(getResources().getAssets(), "Roboto-Regular.ttf");
            Typeface typefaceMedium=  Typeface.createFromAsset(getResources().getAssets(), "Roboto-Medium.ttf");
         //   Typeface typefaceBold=  Typeface.createFromAsset(getResources().getAssets(), "Roboto-Bold.ttf");
            holder.titleView.setTypeface(typefaceMedium);
            holder.subtitleView.setTypeface(typefaceRegular);

            holder.subtitleView.setText(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR));
            holder.subtitleView.setContentDescription(holder.subtitleView.getText());
         //   ViewHolder viewHolder = (ViewHolder) view.getTag();

            //String imgUrl = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));

            Uri uri = Uri.parse( mCursor.getString(ArticleLoader.Query.THUMB_URL));
            Glide.with(ArticleListActivity.this).load(mCursor.getString(ArticleLoader.Query.THUMB_URL)).asBitmap()
                    .listener(new RequestListener<String, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            if (resource != null) {
                                Palette.generateAsync(resource,
                                        new Palette.PaletteAsyncListener() {
                                            @Override
                                            public void onGenerated(Palette palette) {
                                                Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();
                                                Palette.Swatch darkMutedSwatch = palette.getDarkMutedSwatch();
                                                Palette.Swatch lightVibrantSwatch = palette.getLightVibrantSwatch();
                                                Palette.Swatch lightMutedSwatch = palette.getLightMutedSwatch();

                                                Palette.Swatch backgroundAndContentColors = darkVibrantSwatch;
                                                if (backgroundAndContentColors == null) {
                                                    backgroundAndContentColors = darkMutedSwatch;
                                                }
                                                Palette.Swatch titleAndFabColors = lightVibrantSwatch;

                                                if (titleAndFabColors == null) {
                                                    titleAndFabColors = lightMutedSwatch;

                                                }

                                                if (backgroundAndContentColors != null) {
                                                    holder.bottombar.setBackgroundColor(backgroundAndContentColors
                                                            .getRgb());

                                                } else {
                                                    holder.bottombar.setBackgroundColor(palette.getDarkMutedColor
                                                            (0xFF333333));
                                                }


                                            }
                                        });
                            }
                            return false;
                        }
                    })
                    .into(holder.thumbnailView);

//            holder.thumbnailView.setImageUrl(
//                    mCursor.getString(ArticleLoader.Query.THUMB_URL),
//                    ImageLoaderHelper.getInstance(ArticleListActivity.this).getImageLoader());
            holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                     changeSize( view);

                    Intent intent =  new Intent(Intent.ACTION_VIEW,
                            ItemsContract.Items.buildItemUri(getItemId(holder.getAdapterPosition())));
                    boolean curve = (position % 2 == 0);
                    intent.putExtra(ArticleDetailActivity.EXTRA_COLOR, color);
                 intent.putExtra(ArticleDetailActivity.EXTRA_CURVE, curve);
                    Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(host).toBundle();

//                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(
//                            host, holder.thumbnailView, holder.thumbnailView.getTransitionName()).toBundle());
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            ItemsContract.Items.buildItemUri(getItemId(holder.getAdapterPosition()))),bundle);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public DynamicHeightNetworkImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;
public ViewGroup bottombar;


        public ViewHolder(View view) {
            super(view);
            thumbnailView = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
bottombar = (ViewGroup)view.findViewById(R.id.bottomBar);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
        }
    }
}
