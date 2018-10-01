package com.github.rstockbridge.showstats.ui;

import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.EditText;

import com.github.rstockbridge.showstats.R;
import com.github.rstockbridge.showstats.appmodels.Show;

import java.util.List;

public final class TextUtil {

    private static final String notApplicable = "n/a";
    private static final String newlineHtml = "<br>";

    @NonNull
    private final Resources resources;

    public TextUtil(@NonNull final Resources resources) {
        this.resources = resources;
    }

    @NonNull
    public Spanned getGapText(@StringRes int id, @Nullable final Integer gap) {
        String partialText;

        if (gap != null) {
            partialText = resources.getQuantityString(R.plurals.day_plural, gap, gap);
        } else {
            partialText = notApplicable;
        }

        return fromHtml(resources.getString(id, partialText));
    }

    @NonNull
    public Spanned getArtistListTextWithHeader(@NonNull final List<String> list) {
        String partialText = "";

        if (list.size() > 0) {
            if (list.size() > 1) {
                partialText += newlineHtml;
            }

            partialText += TextUtil.formatList(list);
        } else {
            partialText = notApplicable;
        }

        return fromHtml(resources.getQuantityString(R.plurals.artist_plural, list.size(), partialText));
    }

    @NonNull
    public Spanned getListText(@NonNull final List<String> list) {
        String text = "";

        if (list.size() > 0) {
            text += TextUtil.formatList(list);
        } else {
            text = notApplicable;
        }

        return fromHtml(text);
    }

    @NonNull
    private static String formatList(@NonNull final List<String> list) {
        if (list.size() == 1) {
            return list.get(0);
        } else {
            final StringBuilder result = new StringBuilder();

            for (int i = 0; i < list.size() - 1; i++) {
                final String item = list.get(i);
                result.append(item).append(newlineHtml);
            }
            result.append(list.get(list.size() - 1));

            return result.toString();
        }
    }

    @NonNull
    public Spanned getUserGapText(@NonNull final String user, @Nullable final Integer gap) {
        String partialText;

        if (gap != null) {
            partialText = resources.getQuantityString(R.plurals.day_plural, gap, gap);
        } else {
            partialText = notApplicable;
        }

        return fromHtml(resources.getString(R.string.user, user, partialText));
    }

    @NonNull
    public Spanned getCommonShowsText(@NonNull final List<Show> shows) {
        if (shows.size() > 0) {
            final SpannableStringBuilder resultBuilder = new SpannableStringBuilder();

            for (int i = 0; i < shows.size() - 1; i++) {
                final Show show = shows.get(i);

                final Spanned eventDate = getDateText(show.getEventDate(), true);
                final Spanned venueName = getVenueText(show.getVenueName(), true);
                final Spanned artists = getArtistListTextWithHeader(show.getArtistNames());

                resultBuilder.append(TextUtils.concat(eventDate, venueName, artists));

                if (shows.size() > 1) {
                    resultBuilder.append(TextUtils.concat(fromHtml(newlineHtml), fromHtml(newlineHtml)));
                }
            }

            final Show show = shows.get(shows.size() - 1);

            final Spanned eventDate = getDateText(show.getEventDate(), true);
            final Spanned venueName = getVenueText(show.getVenueName(), true);
            final Spanned artists = getArtistListTextWithHeader(show.getArtistNames());

            resultBuilder.append(TextUtils.concat(eventDate, venueName, artists));

            return resultBuilder;
        } else {
            return fromHtml(notApplicable);
        }
    }

    @NonNull
    public Spanned getDateText(@NonNull final String date, final boolean useNewline) {
        String partialText = resources.getString(R.string.date, date);

        if (useNewline) {
            partialText += newlineHtml;
        }

        return fromHtml(partialText);
    }

    @NonNull
    public Spanned getVenueText(@NonNull final String venue, final boolean useNewline) {
        String partialText = resources.getString(R.string.venue, venue);

        if (useNewline) {
            partialText += newlineHtml;
        }

        return fromHtml(partialText);
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
