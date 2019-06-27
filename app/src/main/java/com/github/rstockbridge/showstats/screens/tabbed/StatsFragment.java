package com.github.rstockbridge.showstats.screens.tabbed;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.rstockbridge.showstats.R;
import com.github.rstockbridge.showstats.appmodels.User1StatisticsHolder;
import com.github.rstockbridge.showstats.appmodels.UserStatistics;
import com.github.rstockbridge.showstats.ui.BarChartMakerShowDistribution;
import com.github.rstockbridge.showstats.ui.PieChartMaker;
import com.github.rstockbridge.showstats.ui.TextUtil;

public final class StatsFragment extends Fragment {

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

        final UserStatistics statistics = User1StatisticsHolder.getSharedInstance().getStatistics();

        if (statistics != null) {
            displayStats(statistics);
        }

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

    private void displayStats(@NonNull final UserStatistics statistics) {
        final BarChartMakerShowDistribution barChartMaker = new BarChartMakerShowDistribution(barChart, statistics.getDistributionByMonth());
        barChartMaker.displayBarChart(getActivity());

        final PieChartMaker pieChartMaker = new PieChartMaker(pieChart, statistics.getTopVenueVisits());
        pieChartMaker.displayPieChart();

        displayShowGaps(statistics);
        displayArtistGaps(statistics);
    }

    private void displayShowGaps(@NonNull final UserStatistics statistics) {
        longestShowGapLabel.setText(textUtil.getGapText(R.string.longest, statistics.getLongestShowGap()));
        shortestShowGapLabel.setText(textUtil.getGapText(R.string.shortest, statistics.getShortestShowGap()));
    }

    private void displayArtistGaps(@NonNull final UserStatistics statistics) {
        longestArtistGapArtistLabel.setText(textUtil.getArtistListTextWithHeader(statistics.getLongestArtistGapArtists()));
        longestArtistGapLabel.setText(textUtil.getGapText(R.string.longest, statistics.getLongestArtistGap()));
        shortestArtistGapArtistLabel.setText(textUtil.getArtistListTextWithHeader(statistics.getShortestArtistGapArtists()));
        shortestArtistGapLabel.setText(textUtil.getGapText(R.string.shortest, statistics.getShortestArtistGap()));
    }
}
