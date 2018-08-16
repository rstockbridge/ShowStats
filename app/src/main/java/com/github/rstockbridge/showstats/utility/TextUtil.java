package com.github.rstockbridge.showstats.utility;

import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.Html;
import android.text.Spanned;

import com.github.rstockbridge.showstats.R;

import java.util.List;

public final class TextUtil {

    @NonNull
    private Resources resources;

    public TextUtil(@NonNull final Resources resources) {
        this.resources = resources;
    }

    @NonNull
    public Spanned getGapText(@StringRes int id, @Nullable final Integer gap) {
        String partialText;

        if (gap != null) {
            partialText = resources.getQuantityString(R.plurals.day_plural, gap, gap);
        } else {
            partialText = "n/a";
        }

        return TextUtil.fromHtml(resources.getString(id, partialText));
    }

    @NonNull
    public Spanned getArtistText(@Nullable final Integer gap, @NonNull final List<String> artists) {
        String partialText;

        if (gap != null) {
            partialText = TextUtil.formatArtistsText(artists);
        } else {
            partialText = "n/a";
        }

        return TextUtil.fromHtml(resources.getQuantityString(R.plurals.artist_plural, artists.size(), partialText));
    }

    @NonNull
    private static String formatArtistsText(@NonNull final List<String> artists) {
        if (artists.size() == 1) {
            return artists.get(0);
        } else {
            final StringBuilder result = new StringBuilder();

            for (final String artist : artists) {
                result.append("<br>").append(artist);
            }

            return result.toString();
        }
    }

    @SuppressWarnings("deprecation")
    @NonNull
    private static Spanned fromHtml(@NonNull final String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(text);
        }
    }
}
