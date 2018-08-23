package com.github.rstockbridge.showstats;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.rstockbridge.showstats.appmodels.UserStatistics;
import com.github.rstockbridge.showstats.ui.BarChartMakerShowDistribution;
import com.github.rstockbridge.showstats.ui.PieChartMaker;
import com.github.rstockbridge.showstats.ui.TextUtil;

public final class StatsFragment extends Fragment {

    private static final String ARG_USER_STATISTICS = "statistics";

    @NonNull
    public static StatsFragment newInstance(@NonNull final UserStatistics statistics) {
        final Bundle args = new Bundle();
        args.putParcelable(ARG_USER_STATISTICS, statistics);

        final StatsFragment fragment = new StatsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private UserStatistics statistics;

    private BarChart barChart;
    private PieChart pieChart;

    private TextUtil textUtil;

    private TextView longestShowGapLabel;
    private TextView shortestShowGapLabel;

    private TextView longestArtistGapArtistLabel;
    private TextView shortestArtistGapArtistLabel;
    private TextView longestArtistGapLabel;
    private TextView shortestArtistGapLabel;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_stats, container, false);
        initializeUI(v);

        textUtil = new TextUtil(getResources());

        statistics = getArguments().getParcelable(ARG_USER_STATISTICS);

        displayStats();

        return v;
    }

    private void initializeUI(@NonNull final View v) {
        barChart = v.findViewById(R.id.bar_chart);

        longestShowGapLabel = v.findViewById(R.id.longest_show_gap);
        shortestShowGapLabel = v.findViewById(R.id.shortest_show_gap);

        longestArtistGapArtistLabel = v.findViewById(R.id.longest_artist_gap_artist);
        longestArtistGapLabel = v.findViewById(R.id.longest_artist_gap);
        shortestArtistGapArtistLabel = v.findViewById(R.id.shortest_artist_gap_artist);
        shortestArtistGapLabel = v.findViewById(R.id.shortest_artist_gap);

        pieChart = v.findViewById(R.id.pie_chart);
    }

    private void displayStats() {
        final BarChartMakerShowDistribution barChartMaker = new BarChartMakerShowDistribution(barChart, statistics.getDistributionByMonth());
        barChartMaker.displayBarChart();

        final PieChartMaker pieChartMaker = new PieChartMaker(pieChart, statistics.getTopVenueVisits());
        pieChartMaker.displayPieChart();

        displayShowGaps();
        displayArtistGaps();
    }

    private void displayShowGaps() {
        longestShowGapLabel.setText(textUtil.getGapText(R.string.longest, statistics.getLongestShowGap()));
        shortestShowGapLabel.setText(textUtil.getGapText(R.string.shortest, statistics.getShortestShowGap()));
    }

    private void displayArtistGaps() {
        longestArtistGapArtistLabel.setText(textUtil.getArtistText(statistics.getLongestArtistGapArtists(), true));
        longestArtistGapLabel.setText(textUtil.getGapText(R.string.longest, statistics.getLongestArtistGap()));
        shortestArtistGapArtistLabel.setText(textUtil.getArtistText(statistics.getShortestArtistGapArtists(), true));
        shortestArtistGapLabel.setText(textUtil.getGapText(R.string.shortest, statistics.getShortestArtistGap()));
    }
}
