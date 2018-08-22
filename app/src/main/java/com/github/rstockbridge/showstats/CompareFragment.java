package com.github.rstockbridge.showstats;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.rstockbridge.showstats.appmodels.UserStatistics;

public final class CompareFragment extends Fragment {

    private static final String ARG_FIRST_USER_STATISTICS = "firstUserStatistics";

    @NonNull
    public static CompareFragment newInstance(@NonNull final UserStatistics firstUserStatistics) {
        final Bundle args = new Bundle();
        args.putParcelable(ARG_FIRST_USER_STATISTICS, firstUserStatistics);

        final CompareFragment fragment = new CompareFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_compare, container, false);

        final UserStatistics firstUserStatistics = getArguments().getParcelable(ARG_FIRST_USER_STATISTICS);

        final TextView label = v.findViewById(R.id.compare_label);
        label.setText(firstUserStatistics.getUserId()); // temporary display until fragment is built to spec

        return v;
    }
}
