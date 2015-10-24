package tv.lostfilm.com.lostfilmtvchecker;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import tv.lostfilm.com.android_rss_reader_library.RssItem;

/**
 * Created by Gorets on 23.10.2015.
 */
public class RssAdapter extends RecyclerView.Adapter<RssAdapter.ViewHolder> {
    private static final String TAG = RssAdapter.class.getCanonicalName();

    private ArrayList<RssItem> rssItems;
    private Context context;

    public RssAdapter(Context context) {
        this.context = context;
        this.rssItems = new ArrayList<>();
    }

    @Override
    public RssAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adaper_view, null);
        return new ViewHolder(view);
    }

    private static final String PATTERN = "\\\"(http.*?)\\\"";

    @Override
    public void onBindViewHolder(final RssAdapter.ViewHolder holder, int position) {
        final RssItem rssItem = rssItems.get(position);

        holder.titleView.setText(rssItem.getTitle());
        Pattern p = Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(rssItem.getDescription());
        if (m.find()) {
            String imageUrl = m.group(1);
            Picasso.with(context)
                    .load(StringEscapeUtils.unescapeHtml4(imageUrl))
//                    .centerInside()
//                    .fit()
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            holder.imageView.setImageBitmap(bitmap);

                            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    Palette.Swatch swatch = palette.getDarkMutedSwatch();
                                    if (swatch != null) {
                                        holder.cardView.setCardBackgroundColor(Color.HSVToColor(swatch.getHsl()));
                                        holder.titleView.setTextColor(swatch.getTitleTextColor());
                                    }
                                }
                            });
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

    public void addAll(ArrayList<RssItem> rssItems) {
        this.rssItems.clear();
        this.rssItems.addAll(rssItems);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return rssItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.titleView)
        TextView titleView;
        @Bind(R.id.imageView)
        ImageView imageView;
        @Bind(R.id.card_view)
        CardView cardView;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
