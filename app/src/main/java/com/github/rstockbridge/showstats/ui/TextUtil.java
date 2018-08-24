package com.github.rstockbridge.showstats.ui;

import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.Html;
import android.text.Spanned;
import android.widget.EditText;

import com.github.rstockbridge.showstats.R;

import java.util.List;

public final class TextUtil {

    @NonNull
    private Resources resources;

    public TextUtil(@NonNull final Resources resources) {
        this.resources = resources;
    }

    @NonNull
    public static String getText(final EditText editText) {
        return editText.getText().toString();
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
    public Spanned getListText(@NonNull final List<String> list, boolean useArtistHeader) {
        String partialText = "";

        if (list.size() > 0) {
            if (useArtistHeader && list.size() > 1) {
                partialText += "<br>";
            }

            partialText += TextUtil.formatList(list);
        } else {
            partialText = "n/a";
        }

        if (useArtistHeader) {
            return TextUtil.fromHtml(resources.getQuantityString(R.plurals.artist_plural, list.size(), partialText));
        } else {
            return TextUtil.fromHtml(partialText);
        }
    }

    @NonNull
    private static String formatList(@NonNull final List<String> list) {
        if (list.size() == 1) {
            return list.get(0);
        } else {
            final StringBuilder result = new StringBuilder();

            for (int i = 0; i < list.size() - 1; i++) {
                final String item = list.get(i);
                result.append(item).append("<br>");
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
            partialText = "n/a";
        }

        return TextUtil.fromHtml(resources.getString(R.string.user, user, partialText));
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
