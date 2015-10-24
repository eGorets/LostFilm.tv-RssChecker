package tv.lostfilm.com.lostfilmtvchecker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

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

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        final RssAdapter mAdapter = new RssAdapter(getApplicationContext());

        final Observable<ArrayList<RssItem>> rss = Observable.create(new Observable.OnSubscribe<ArrayList<RssItem>>() {
            @Override
            public void call(Subscriber<? super ArrayList<RssItem>> subscriber) {
                readRss(subscriber);
            }
        });

        Action1<ArrayList<RssItem>> onNextAction = new Action1<ArrayList<RssItem>>() {
            @Override
            public void call(ArrayList<RssItem> rssItems) {
                mAdapter.addAll(rssItems);
            }
        };

        rss.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNextAction);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
    }


    private void readRss(Subscriber<? super ArrayList<RssItem>> subscriber) {
        try {
            URL url = new URL("http://www.lostfilm.tv/rssdd.xml");
            RssFeed feed = RssReader.read(url);

            ArrayList<RssItem> rssItems = feed.getRssItems();
            subscriber.onNext(rssItems);

        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
    }
}
