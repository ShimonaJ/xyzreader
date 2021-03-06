package com.example.xyzreader.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.remote.Config;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    private static final float PARALLAX_FACTOR = 1.25f;

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private ObservableScrollView mScrollView;
    private DrawInsetsFrameLayout mDrawInsetsFrameLayout;
    private ColorDrawable mStatusBarColorDrawable;

    private int mTopInset;
    private View mPhotoContainerView;
    private ImageView mPhotoView;
    private int mScrollY;
    private boolean mIsCard = false;
    private int mStatusBarFullOpacityBottom;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
      //m  arguments.putLong(ArticleDetailActivity.EXTRA_COLOR, extra_col);

        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);

        return fragment;
    }
private void anim(){
if(getActivity()!=null) {
    final int startScrollPos = getResources().getDimensionPixelSize(R.dimen.init_scroll_up_distance);
    Animator animatator = ObjectAnimator.ofInt(mScrollView, "scrollY", startScrollPos);
    animatator.setInterpolator(AnimationUtils.loadInterpolator(getActivity(), android.R.interpolator.linear_out_slow_in));
    animatator.setStartDelay(1000);
    animatator.setDuration(700);
    animatator.start();


    animatator = ObjectAnimator.ofInt(mScrollView, "scrollY", 150);
    animatator.setInterpolator(AnimationUtils.loadInterpolator(getActivity(), android.R.interpolator.linear_out_slow_in));
    animatator.setStartDelay(1700);
    animatator.setDuration(500);
    animatator.start();
}

}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
                R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);


      //  getActivity().startPostponedEnterTransition();
        mDrawInsetsFrameLayout = (DrawInsetsFrameLayout)
                mRootView.findViewById(R.id.draw_insets_frame_layout);
        mDrawInsetsFrameLayout.setOnInsetsCallback(new DrawInsetsFrameLayout.OnInsetsCallback() {
            @Override
            public void onInsetsChanged(Rect insets) {
                mTopInset = insets.top;
            }
        });


        mScrollView = (ObservableScrollView) mRootView.findViewById(R.id.scrollview);
        mScrollView.setCallbacks(new ObservableScrollView.Callbacks() {
            @Override
            public void onScrollChanged() {
                mScrollY = mScrollView.getScrollY();
                if(getActivityCast()!=null) {
                    getActivityCast().onUpButtonFloorChanged(mItemId, ArticleDetailFragment.this);
                }
                mPhotoContainerView.setTranslationY((int) (mScrollY - mScrollY / PARALLAX_FACTOR));
                updateStatusBar();
            }
        });

        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);
        mPhotoContainerView = mRootView.findViewById(R.id.photo_container);


        mStatusBarColorDrawable = new ColorDrawable(0);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mScrollView.setScrollY(200);


                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });
        bindViews();
        updateStatusBar();

        return mRootView;
    }

    private void updateStatusBar() {
        int color = 0;
        if (mPhotoView != null && mTopInset != 0 && mScrollY > 0) {
            float f = progress(mScrollY,
                    mStatusBarFullOpacityBottom - mTopInset * 3,
                    mStatusBarFullOpacityBottom - mTopInset);
            color = Color.argb((int) (255 * f),
                    (int) (Color.red(mMutedColor) * 0.9),
                    (int) (Color.green(mMutedColor) * 0.9),
                    (int) (Color.blue(mMutedColor) * 0.9));
//            mPhotoView.setBackgroundTintList(
//                    ColorStateList.valueOf(mExtra_color));
//
//            // check if we should used curved motion and load an appropriate transition
//

        }
        mStatusBarColorDrawable.setColor(color);
        mDrawInsetsFrameLayout.setInsetBackground(mStatusBarColorDrawable);
    }

    static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }
final View draw_insets_frame_layout = (View) mRootView.findViewById(R.id.draw_insets_frame_layout);
        final TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
        final TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        final TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        final View fabView = (View) mRootView.findViewById(R.id.share_fab);
        bodyView.setTypeface(Config.typefaceMedium);
        titleView.setTypeface(Config.typefaceRegular);
        bylineView.setTypeface(Config.typefaceRegular);
      //  bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));
        final ViewGroup detailBottomCtr = (ViewGroup) mRootView.findViewById(R.id.detailBottomCtr);
        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            titleView.setContentDescription(titleView
                    .getText());
            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <i>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</i>"));
            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));
            bodyView.setContentDescription(bodyView
            .getText());




            Glide.with(getActivity())
                    .load(mCursor.getString(ArticleLoader.Query.PHOTO_URL)).asBitmap()
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
                                                    if(titleAndFabColors!=null) {

                                                    }
                                                }

                                                if (backgroundAndContentColors != null) {
                                                    mMutedColor = backgroundAndContentColors
                                                            .getRgb();


                                                } else {
                                                    mMutedColor = palette.getDarkMutedColor
                                                            (0xFF333333);
                                                }
                                                draw_insets_frame_layout.setBackgroundColor(mMutedColor);
                                                if(titleView!=null) {
                                                    if(darkMutedSwatch!=null) {
                                                        titleView.setTextColor(darkMutedSwatch.getRgb());
                                                    }else{
                                                        titleView.setTextColor(mMutedColor);
                                                    }
                                                    fabView.setBackgroundColor(mMutedColor);
                                                    bylineView.setTextColor(mMutedColor);
                                                    bodyView.setTextColor(mMutedColor);
                                                }
                                             //   detailBottomCtr.setBackgroundColor(mMutedColor);
                                                updateStatusBar();
                                                anim();
//
//                                                Animator animatator = ObjectAnimator.ofInt(getView().findViewById(R.id.detailBottomCtr),"scrollY",500).setDuration(300);
//                                                animatator.start();
                                            }
                                        });
                            }
                            return false;
                        }
                    })
                    .into(mPhotoView);

//            mScrollView.scrollBy(
//                    0,100
//            );


        } else {
            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A" );
            bodyView.setText("N/A");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
    }

    public int getUpButtonFloor() {
        if (mPhotoContainerView == null || mPhotoView.getHeight() == 0) {
            return Integer.MAX_VALUE;
        }

        // account for parallax
        return mIsCard
                ? (int) mPhotoContainerView.getTranslationY() + mPhotoView.getHeight() - mScrollY
                : mPhotoView.getHeight() - mScrollY;
    }
}
