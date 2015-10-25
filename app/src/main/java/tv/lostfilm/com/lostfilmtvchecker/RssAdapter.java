package tv.lostfilm.com.lostfilmtvchecker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import tv.lostfilm.com.android_rss_reader_library.RssItem;
import tv.lostfilm.com.lostfilmtvchecker.utils.ParserUtils;

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

    @Override
    public void onBindViewHolder(final RssAdapter.ViewHolder holder, int position) {
        final RssItem rssItem = rssItems.get(position);

        holder.titleView.setText(rssItem.getTitle());

        final String imageUrl = ParserUtils.getClearImageAddress(rssItem);
        if (!TextUtils.isEmpty(imageUrl)) {
            Picasso.with(context)
                    .load(StringEscapeUtils.unescapeHtml4(imageUrl))
                    .fit()
                    .placeholder(new ColorDrawable(context.getResources().getColor(android.R.color.transparent)))
                    .centerInside()
                    .into(holder.imageView, new Callback.EmptyCallback() {
                        @Override
                        public void onSuccess() {
                            final Bitmap bitmap = ((BitmapDrawable) holder.imageView.getDrawable()).getBitmap();
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
                    });
        }

        holder.downloadView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchTorrentIntent(rssItem);
            }
        });
    }

    private void launchTorrentIntent(RssItem rssItem) {
        final Intent i = new Intent(Intent.ACTION_VIEW);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setType("application/x-bittorrent");
        i.setData(Uri.parse(rssItem.getLink()));
        try {
            context.startActivity(Intent.createChooser(i, "Choose app:"));
        } catch (Exception e) {
            Toast.makeText(context, "Torrent client not found", Toast.LENGTH_SHORT).show();
        }
    }

    public void addAll(ArrayList<RssItem> rssItems) {
        this.rssItems.clear();
        this.rssItems.addAll(rssItems);
        notifyDataSetChanged();
    }

    public ArrayList<RssItem> getContent() {
        return rssItems;
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
        @Bind(R.id.downloadView)
        ImageView downloadView;
        @Bind(R.id.card_view)
        CardView cardView;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
