package com.github.rstockbridge.showstats;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.rstockbridge.showstats.api.models.Setlist;
import com.github.rstockbridge.showstats.api.models.SetlistArtist;
import com.github.rstockbridge.showstats.api.models.SetlistVenue;
import com.github.rstockbridge.showstats.appmodels.Artist;
import com.github.rstockbridge.showstats.appmodels.ArtistSetlist;
import com.github.rstockbridge.showstats.utility.BarChartMaker;
import com.github.rstockbridge.showstats.utility.MapUtil;
import com.github.rstockbridge.showstats.utility.PieChartMaker;
import com.github.rstockbridge.showstats.utility.TextUtil;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.threeten.bp.temporal.ChronoUnit.DAYS;

public final class StatsFragment extends Fragment {

    private static final String ARG_SETLISTS = "setlists";

    @NonNull
    public static StatsFragment newInstance(@NonNull final ArrayList<Setlist> setlists) {
        final Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_SETLISTS, setlists);

        final StatsFragment fragment = new StatsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private List<Setlist> setlists;
    private List<Artist> artists;

    private BarChart barChart;
    private PieChart pieChart;

    private TextUtil textUtil;

    @Nullable
    private Integer longestShowGap = null;
    @Nullable
    private Integer shortestShowGap = null;

    private TextView longestShowGapLabel;
    private TextView shortestShowGapLabel;

    private List<String> longestArtistGapArtists;
    private List<String> shortestArtistGapArtists;

    @Nullable
    private Integer longestArtistGap = null;
    @Nullable
    private Integer shortestArtistGap = null;

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

        setlists = getArguments().getParcelableArrayList(ARG_SETLISTS);
        constructArtistIndexedData();

        calculateShowGaps();
        calculateArtistGaps();

        displayStats();

        return v;
    }

    private void initializeUI(final View v) {
        barChart = v.findViewById(R.id.bar_chart);

        longestShowGapLabel = v.findViewById(R.id.longest_show_gap);
        shortestShowGapLabel = v.findViewById(R.id.shortest_show_gap);

        longestArtistGapArtistLabel = v.findViewById(R.id.longest_artist_gap_artist);
        longestArtistGapLabel = v.findViewById(R.id.longest_artist_gap);
        shortestArtistGapArtistLabel = v.findViewById(R.id.shortest_artist_gap_artist);
        shortestArtistGapLabel = v.findViewById(R.id.shortest_artist_gap);

        pieChart = v.findViewById(R.id.pie_chart);
    }

    private void constructArtistIndexedData() {
        artists = new ArrayList<>();

        for (final Setlist setlist : setlists) {
            final SetlistArtist setlistArtist = setlist.getArtist();
            boolean isNewArtist = true;

            for (final Artist artist : artists) {
                if ((setlistArtist.getMbid() != null && setlistArtist.getMbid().equals(artist.getId()))) {
                    artist.addSetlist(new ArtistSetlist(setlist.getEventDate(), setlist.getVenue().getName()));
                    isNewArtist = false;
                }
            }

            if (isNewArtist) {
                artists.add(new Artist(setlistArtist.getMbid(), setlistArtist.getName(),
                        new ArtistSetlist(setlist.getEventDate(), setlist.getVenue().getName())));
            }
        }
    }

    private void displayStats() {
        final BarChartMaker barChartMaker = new BarChartMaker(barChart, calculateDistributionByMonth());
        barChartMaker.displayBarChart();

        final PieChartMaker pieChartMaker = new PieChartMaker(pieChart, calculateTopVenueVisits());
        pieChartMaker.displayPieChart();

        displayShowGaps();
        displayArtistGaps();
    }

    @NonNull
    private int[] calculateDistributionByMonth() {
        final int NUMBER_OF_MONTHS = 12;
        final int[] result = new int[NUMBER_OF_MONTHS];

        for (final Setlist setlist : setlists) {
            final int setlistMonth = setlist.getEventDate().getMonthValue();
            result[NUMBER_OF_MONTHS - setlistMonth]++; // invert for display purposes
        }

        return result;
    }

    @NonNull
    private List<Pair<Integer, String>> calculateTopVenueVisits() {
        final int NUMBER_OF_TOP_VENUES = 5;
        final List<Pair<Integer, String>> topVenueVisits = new ArrayList<>();
        final Map<String, Integer> venueVisits = calculateVenueVisits();

        int i = 0;
        final Iterator<Map.Entry<String, Integer>> iterator = venueVisits.entrySet().iterator();
        while (i < venueVisits.size() && i < NUMBER_OF_TOP_VENUES) {
            final Map.Entry<String, Integer> pair = iterator.next();
            topVenueVisits.add(new Pair<>(pair.getValue(), pair.getKey()));
            i++;
        }

        return topVenueVisits;
    }

    @NonNull
    private Map<String, Integer> calculateVenueVisits() {
        final Map<String, Integer> result = new HashMap<>();
        final List<Pair<String, LocalDate>> processedVenues = new ArrayList<>();

        for (final Setlist setlist : setlists) {
            final String venueName = setlist.getVenue().getName();

            if (!result.containsKey(venueName)) {
                result.put(venueName, 1);
            } else {
                final Pair<String, LocalDate> venueDatePair = new Pair<>(venueName, setlist.getEventDate());

                // multiple shows appearing at the same venue on the same day only count as one visit
                if (!processedVenues.contains(venueDatePair)) {
                    processedVenues.add(venueDatePair);
                    result.put(venueName, result.get(venueName) + 1);
                }
            }
        }

        return MapUtil.sortMapByValues(result);
    }

    private void calculateShowGaps() {
        // longestShowGap/shortestShowGap remain null if the only shows
        // are on the same day at the same venue

        if (setlists.size() > 1) {
            for (int i = 0; i < setlists.size() - 1; i++) {
                final Setlist setlist1 = setlists.get(i);
                final Setlist setlist2 = setlists.get(i + 1);

                final LocalDate date1 = setlist1.getEventDate();
                final LocalDate date2 = setlist2.getEventDate();
                int gap = (int) DAYS.between(date2, date1);

                final SetlistVenue venue1 = setlist1.getVenue();
                final SetlistVenue venue2 = setlist2.getVenue();

                if (gap > 0 || (gap == 0 && !venue1.equals(venue2))) {
                    if (longestShowGap == null || gap > longestShowGap) {
                        longestShowGap = gap;
                    }
                    if (shortestShowGap == null || gap < shortestShowGap) {
                        shortestShowGap = gap;
                    }
                }
            }
        }
    }

    private void calculateArtistGaps() {
        // longestArtistGap/shortestArtistGap remain null if only gap is 0
        // (i.e. there are shows on the same day)

        longestArtistGapArtists = new ArrayList<>();
        shortestArtistGapArtists = new ArrayList<>();

        for (final Artist artist : artists) {
            final List<ArtistSetlist> setlists = artist.getSetlists();

            if (setlists.size() > 1) {
                for (int i = 0; i < setlists.size() - 1; i++) {
                    final ArtistSetlist setlist1 = setlists.get(i);
                    final ArtistSetlist setlist2 = setlists.get(i + 1);

                    final LocalDate date1 = setlist1.getEventDate();
                    final LocalDate date2 = setlist2.getEventDate();
                    int gap = (int) DAYS.between(date2, date1);

                    final String venue1 = setlist1.getVenue();
                    final String venue2 = setlist2.getVenue();

                    if (gap > 0 || (gap == 0 && !venue1.equals(venue2))) {
                        if (longestArtistGap == null || gap > longestArtistGap) {
                            longestArtistGap = gap;
                            longestArtistGapArtists = new ArrayList<>(Arrays.asList(artist.getName()));
                        } else if (gap == longestArtistGap) {
                            longestArtistGapArtists.add(artist.getName());
                        }

                        if (shortestArtistGap == null || gap < shortestArtistGap) {
                            shortestArtistGap = gap;
                            shortestArtistGapArtists = new ArrayList<>(Arrays.asList(artist.getName()));
                        } else if (gap == shortestArtistGap) {
                            shortestArtistGapArtists.add(artist.getName());
                        }
                    }
                }
            }
        }

        Collections.sort(longestArtistGapArtists);
        Collections.sort(shortestArtistGapArtists);
    }

    private void displayShowGaps() {
        longestShowGapLabel.setText(textUtil.getGapText(R.string.longest, longestShowGap));
        shortestShowGapLabel.setText(textUtil.getGapText(R.string.shortest, shortestShowGap));
    }

    private void displayArtistGaps() {
        longestArtistGapArtistLabel.setText(textUtil.getArtistText(longestArtistGap, longestArtistGapArtists));
        longestArtistGapLabel.setText(textUtil.getGapText(R.string.longest, longestArtistGap));
        shortestArtistGapArtistLabel.setText(textUtil.getArtistText(shortestArtistGap, shortestArtistGapArtists));
        shortestArtistGapLabel.setText(textUtil.getGapText(R.string.shortest, shortestArtistGap));
    }
}
