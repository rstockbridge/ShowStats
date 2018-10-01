package com.github.rstockbridge.showstats;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.rstockbridge.showstats.appmodels.Show;
import com.github.rstockbridge.showstats.ui.TextUtil;

import java.util.List;

public final class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.Holder> {

    @NonNull
    private final Context context;

    @NonNull
    private final List<Show> shows;

    @NonNull
    private final TextUtil textUtil;

    RecyclerViewAdapter(@NonNull Context context, @NonNull final List<Show> shows) {
        this.context = context;
        this.shows = shows;

        textUtil = new TextUtil(context.getResources());
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        return new Holder(inflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, final int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return shows.size();
    }

    final class Holder extends RecyclerView.ViewHolder {

        private final TextView dateLabel;
        private final TextView venueLabel;
        private final TextView artistsLabel;

        Holder(@NonNull final LayoutInflater inflater, @NonNull final ViewGroup parent) {
            super(inflater.inflate(R.layout.show_row, parent, false));

            dateLabel = itemView.findViewById(R.id.date);
            venueLabel = itemView.findViewById(R.id.venue);
            artistsLabel = itemView.findViewById(R.id.artists);
        }

        void bind(final int position) {
            final Show show = shows.get(position);

            dateLabel.setText(textUtil.getDateText(show.getEventDate(), false));
            venueLabel.setText(textUtil.getVenueText(show.getVenueName(), false));
            artistsLabel.setText(textUtil.getArtistListTextWithHeader(show.getArtistNames()));

            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent intent = NoteActivity.newIntent(context, show.getId());
                    context.startActivity(intent);
                }
            });
        }
    }
}
