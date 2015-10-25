package tv.lostfilm.com.lostfilmtvchecker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Gorets on 25.10.2015.
 */
public class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getCanonicalName();

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // init silent app tracker
        AnalyticsTrackers.initialize(this);
        Tracker tracker = AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-1657105281106558/7856409622");

        AdRequest adRequest2 = new AdRequest.Builder()
                .build();

        mInterstitialAd.loadAd(adRequest2);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }
}
