package tv.lostfilm.com.lostfilmtvchecker;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import tv.lostfilm.com.android_rss_reader_library.RssFeed;
import tv.lostfilm.com.android_rss_reader_library.RssItem;
import tv.lostfilm.com.android_rss_reader_library.RssReader;
import tv.lostfilm.com.lostfilmtvchecker.utils.ParserUtils;

public class MainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.refreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.adView)
    AdView mAdView;

    private Observable<ArrayList<RssItem>> rss;
    private RssAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    public static final String RSS_DOMAIN = "http://www.lostfilm.tv/rssdd.xml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdapter = new RssAdapter(getApplicationContext());

        rss = Observable.create(new Observable.OnSubscribe<ArrayList<RssItem>>() {
            @Override
            public void call(Subscriber<? super ArrayList<RssItem>> subscriber) {
                readRss(subscriber);
            }
        });

        rss.doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "Error: " + throwable.toString());
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<RssItem>>() {
                    @Override
                    public void call(ArrayList<RssItem> rssItems) {
                        swipeRefreshLayout.setRefreshing(false);
                        mAdapter.addAll(rssItems);
                    }
                });

        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);

        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int pos = mLayoutManager.findFirstCompletelyVisibleItemPosition();
                RssItem rssItem = mAdapter.getContent().get(pos);

                final String imageUrl = ParserUtils.getClearImageAddress(rssItem);
                if (!TextUtils.isEmpty(imageUrl)) {
                    Picasso.with(MainActivity.this)
                            .load(StringEscapeUtils.unescapeHtml4(imageUrl))
                            .into(new Target() {
                                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    swipeRefreshLayout.setBackground(new BitmapDrawable(bitmap));
                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {

                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            });
                }
            }
        });
        onRefresh();
    }

    private void readRss(Subscriber<? super ArrayList<RssItem>> subscriber) {
        try {
            URL url = new URL(RSS_DOMAIN);
            RssFeed feed = RssReader.read(url);

            ArrayList<RssItem> rssItems = feed.getRssItems();
            subscriber.onNext(rssItems);

        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        rss.repeat();
    }

}
