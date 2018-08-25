package com.github.rstockbridge.showstats;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.rstockbridge.showstats.appmodels.User1StatisticsHolder;
import com.github.rstockbridge.showstats.appmodels.UserStatistics;

public final class ShowsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_shows, container, false);

        final RecyclerView recyclerView = v.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        final UserStatistics statistics = User1StatisticsHolder.getSharedInstance().getStatistics();

        if (statistics != null) {
            final RecyclerViewAdapter adapter = new RecyclerViewAdapter(getActivity(), statistics.getShows());
            recyclerView.setAdapter(adapter);
        }

        return v;
    }
}
