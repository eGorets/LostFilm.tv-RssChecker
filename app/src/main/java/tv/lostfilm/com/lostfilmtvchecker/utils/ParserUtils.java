package tv.lostfilm.com.lostfilmtvchecker.utils;

import android.support.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.lostfilm.com.android_rss_reader_library.RssItem;

/**
 * Created by Gorets on 25.10.2015.
 */
public class ParserUtils {
    private static final String TAG = ParserUtils.class.getCanonicalName();

    private static final String PATTERN = "\\\"(http.*?)\\\"";
    @Nullable
    public static String getClearImageAddress(RssItem rssItem) {
        Pattern p = Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(rssItem.getDescription());
        return m.find()? m.group(1): null;
    }
}
