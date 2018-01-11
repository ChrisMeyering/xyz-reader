package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ShareCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.squareup.picasso.Picasso;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    private int count = 0;
    private Cursor mCursor;
    private long mItemId;
    private View mRootView;

    private ImageView mPhotoView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    public ArticleDetailFragment() {
    }


    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

//        setHasOptionsMenu(true);
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader((int)mItemId, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        mRootView.setVisibility(View.INVISIBLE);
        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);
        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });
        return mRootView;
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }
    private void bindViews() {
        count++;
        Log.i(TAG, "bindViews #" + count);
        final TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
        final TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        final TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);

//        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {
            new BindViewsTask().execute();
            /*
            mRootView.setVisibility(View.VISIBLE);
            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            Date publishedDate = parsePublishedDate();
            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));
            bodyView.post(new Runnable() {
                @Override
                public void run() {
                    bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)
                            .replaceAll("\\.(\r\n|\n)", ".<br />")));
                    ;
                }
            });
            Picasso.with(getContext())
                    .load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
                    .into(mPhotoView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            Bitmap bitmap = ((BitmapDrawable) mPhotoView.getDrawable()).getBitmap();
                            if (bitmap != null) {
                                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(@NonNull Palette p) {
                                        int primaryDark = getActivity().getResources().getColor(R.color.blue_700);
                                        int primary = getActivity().getResources().getColor(R.color.blue_500);
//                                        getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
                                        mRootView.findViewById(R.id.meta_bar).setBackgroundColor(p.getMutedColor(primary));
                                    }
                                });
                            }
                        }

                        @Override
                        public void onError() {

                        }
                    });


            final Handler h =  new Handler();
            Thread t = new Thread() {
                public void run() {
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
                                Date publishedDate = parsePublishedDate();
                                bylineView.setText(Html.fromHtml(
                                        DateUtils.getRelativeTimeSpanString(
                                                publishedDate.getTime(),
                                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                                + " by <font color='#ffffff'>"
                                                + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                                + "</font>"));
                                bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)
                                        .replaceAll("\\.(\r\n|\n)", ".<br />")));
                                ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                                        .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                                            @Override
                                            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                                                Bitmap bitmap = imageContainer.getBitmap();
                                                if (bitmap != null) {
                                                    Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                                        @Override
                                                        public void onGenerated(@NonNull Palette p) {
                                                            int primaryDark = getResources().getColor(R.color.blue_700);
                                                            int primary = getResources().getColor(R.color.blue_500);
//                                        getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
                                                            mRootView.findViewById(R.id.meta_bar).setBackgroundColor(p.getMutedColor(primary));


                                                        }
                                                    });
                                                    mPhotoView.setImageBitmap(imageContainer.getBitmap());
//                                ((ProgressBar) mRootView.findViewById(R.id.pb_loading_details))
//                                        .animate().alpha(0).setDuration(300).start();

                                                    mRootView.animate().alpha(1).setDuration(300).start();
                                                }
                                            }

                                            @Override
                                            public void onErrorResponse(VolleyError volleyError) {

                                            }
                                        });
                            } finally {
                                Log.i(TAG, "bindViews for article " + titleView.getText() + " complete");
                            }
                        }
                    });
                }
            };
            t.run();
            */
//            bodyView.setText("This is some text");
//            bodyView.setText(Html.fromHtml(
//                    mCursor.getString(ArticleLoader.Query.BODY)).toString());
//                            .replaceAll("\\.(\r\n|\n)", ".<br />")));

        } else {

            titleView.setText("N/A");
            bylineView.setText("N/A" );
            bodyView.setText("N/A");
        }

    }

    private void showLoader() {
        mRootView.setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.pb_loading_details).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.group_article_detail).setVisibility(View.INVISIBLE);
    }

    private void showDetails() {
        mRootView.setAlpha(0);
        mRootView.findViewById(R.id.pb_loading_details).setVisibility(View.INVISIBLE);
        mRootView.findViewById(R.id.group_article_detail).setVisibility(View.VISIBLE);
        mRootView.animate().alpha(1).setDuration(500);
    }

    private class BindViewsTask extends AsyncTask<Void, Void, Void> {
        String title;
        String byLine;
        String body;
        String photoUrl;

        TextView titleView;
        TextView bylineView;
        TextView bodyView;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoader();
            titleView = (TextView) mRootView.findViewById(R.id.article_title);
            bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
            bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            title = mCursor.getString(ArticleLoader.Query.TITLE);
            Date publishedDate = parsePublishedDate();
            byLine = DateUtils.getRelativeTimeSpanString(
                    publishedDate.getTime(),
                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL).toString()
                    + " by <font color='#ffffff'>"
                    + mCursor.getString(ArticleLoader.Query.AUTHOR)
                    + "</font>";
            body = mCursor.getString(ArticleLoader.Query.BODY)
                    .replaceAll("\\.(\r\n|\n)", ".<br />");
            photoUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            titleView.setText(title);
            bylineView.setText(Html.fromHtml(byLine));
            bodyView.setText(Html.fromHtml(body));
            Picasso.with(getContext())
                    .load(photoUrl)
                    .into(mPhotoView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            Bitmap bitmap = ((BitmapDrawable) mPhotoView.getDrawable()).getBitmap();
                            if (bitmap != null) {
                                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(@NonNull Palette p) {
                                        int primary = getResources().getColor(R.color.blue_500);
                                        mRootView.findViewById(R.id.meta_bar).setBackgroundColor(p.getMutedColor(primary));
                                    }
                                });
                            }
                            showDetails();

                        }

                        @Override
                        public void onError() {

                        }
                    });
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), i);
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

}
