package com.github.rstockbridge.showstats;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StatsFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";

    @NonNull
    public static StatsFragment newInstance(@NonNull final String userId) {
        final Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);

        final StatsFragment fragment = new StatsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_stats, container, false);

        final String userId = getArguments().getString(ARG_USER_ID);

        final TextView label = v.findViewById(R.id.stats_label);
        label.setText(userId); // temporary display until fragment is built to spec

        return v;
    }
}
