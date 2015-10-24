package tv.lostfilm.com.lostfilmtvchecker;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.refreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.adView)
    AdView mAdView;

    private Observable<ArrayList<RssItem>> rss;
    private RssAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private InterstitialAd mInterstitialAd;
    public static final String RSS_DOMAIN = "http://www.lostfilm.tv/rssdd.xml";
    private static final String PATTERN = "\\\"(http.*?)\\\"";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AnalyticsTrackers.initialize(this);
        Tracker tracker = AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-1657105281106558/7856409622");

        AdRequest adRequest2 = new AdRequest.Builder()
                .build();

        mInterstitialAd.loadAd(adRequest2);

        mAdapter = new RssAdapter(getApplicationContext());

        rss = Observable.create(new Observable.OnSubscribe<ArrayList<RssItem>>() {
            @Override
            public void call(Subscriber<? super ArrayList<RssItem>> subscriber) {
                readRss(subscriber);
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

                Pattern p = Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher m = p.matcher(rssItem.getDescription());
                if (m.find()) {
                    final String imageUrl = m.group(1);
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
        rss.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<RssItem>>() {
                    @Override
                    public void call(ArrayList<RssItem> rssItems) {
                        swipeRefreshLayout.setRefreshing(false);
                        mAdapter.addAll(rssItems);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }
}
