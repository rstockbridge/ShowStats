package com.github.rstockbridge.showstats.appmodels;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.github.rstockbridge.showstats.api.models.Coordinates;
import com.github.rstockbridge.showstats.api.models.Setlist;
import com.github.rstockbridge.showstats.api.models.SetlistArtist;
import com.github.rstockbridge.showstats.api.models.SetlistVenue;
import com.github.rstockbridge.showstats.utility.MapUtil;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.threeten.bp.temporal.ChronoUnit.DAYS;

public final class UserStatistics {

    private static final int NUMBER_OF_MONTHS = 12;

    @NonNull
    private List<Setlist> setlists;

    @NonNull
    private String userId;

    @NonNull
    private int[] distributionByMonth = new int[NUMBER_OF_MONTHS];

    @Nullable
    private Integer longestShowGap = null;
    @Nullable
    private Integer shortestShowGap = null;

    @NonNull
    private List<String> longestArtistGapArtists = new ArrayList<>();
    @NonNull
    private List<String> shortestArtistGapArtists = new ArrayList<>();

    @Nullable
    private Integer longestArtistGap = null;
    @Nullable
    private Integer shortestArtistGap = null;
    @Nullable
    private Integer averageShowGap = null;

    @NonNull
    private Map<String, Integer> topVenueVisits = new HashMap<>();

    @NonNull
    private Set<String> artistIds = new HashSet<>();

    @NonNull
    private Map<String, String> artistIdNameMap = new HashMap<>();

    @NonNull
    private List<Show> shows = new ArrayList<>();

    @NonNull
    private Set<String> venueNames = new HashSet<>();

    @NonNull
    private Set<SetlistVenue> venues = new HashSet<>();

    private int numberOfShows;

    public UserStatistics(@NonNull final String userId, @NonNull final List<Setlist> setlists) {
        this.userId = userId;
        this.setlists = setlists;

        calculateDistributionByMonth();
        calculateShowGaps();
        calculateArtistGaps();
        calculateTopVenueVisits();
        constructArtistIds();
        constructArtistIdNameMap();
        constructShows();
        constructVenues();
        calculateNumberOfShows();
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    @NonNull
    public int[] getDistributionByMonth() {
        return distributionByMonth;
    }

    @Nullable
    public Integer getLongestShowGap() {
        return longestShowGap;
    }

    @Nullable
    public Integer getShortestShowGap() {
        return shortestShowGap;
    }

    @NonNull
    public List<String> getLongestArtistGapArtists() {
        return longestArtistGapArtists;
    }

    @NonNull
    public List<String> getShortestArtistGapArtists() {
        return shortestArtistGapArtists;
    }

    @Nullable
    public Integer getLongestArtistGap() {
        return longestArtistGap;
    }

    @Nullable
    public Integer getShortestArtistGap() {
        return shortestArtistGap;
    }

    @NonNull
    public Map<String, Integer> getTopVenueVisits() {
        return topVenueVisits;
    }

    @NonNull
    public Set<String> getArtistIds() {
        return artistIds;
    }

    @NonNull
    public String getArtistNameFromId(final String artistId) {
        return artistIdNameMap.get(artistId);
    }

    @NonNull
    public List<Show> getShows() {
        return shows;
    }

    @NonNull
    public Set<String> getVenueNames() {
        return venueNames;
    }

    @NonNull
    public Set<SetlistVenue> getVenues() {
        return venues;
    }

    public int getNumberOfShows() {
        return numberOfShows;
    }

    @Nullable
    public Integer getAverageShowGap() {
        return averageShowGap;
    }


    // methods to calculate statistics

    private void calculateDistributionByMonth() {

        final List<List<Pair<String, LocalDate>>> processedVenuesByMonth = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_MONTHS; i++) {
            processedVenuesByMonth.add(new ArrayList<Pair<String, LocalDate>>());
        }

        for (final Setlist setlist : setlists) {
            final int month = setlist.getEventDate().getMonthValue();
            final List<Pair<String, LocalDate>> monthProcessedVenues = processedVenuesByMonth.get(month - 1);

            final Pair<String, LocalDate> venueDatePair =
                    new Pair<>(setlist.getVenue().getName(), setlist.getEventDate());

            // multiple shows appearing at the same venue on the same day only count as one visit
            if (!monthProcessedVenues.contains(venueDatePair)) {
                monthProcessedVenues.add(venueDatePair);
                distributionByMonth[NUMBER_OF_MONTHS - month]++; // invert for display purposes
            }
        }
    }

    private void calculateShowGaps() {
        // longestShowGap/shortestShowGap/averageShowGap remain null if either only one show or
        // if the only shows are on the same day at the same venue

        if (setlists.size() > 1) {
            int sumOfShowGaps = 0;

            for (int i = 0; i < setlists.size() - 1; i++) {
                final Setlist setlist1 = setlists.get(i);
                final Setlist setlist2 = setlists.get(i + 1);

                final LocalDate date1 = setlist1.getEventDate();
                final LocalDate date2 = setlist2.getEventDate();
                int gap = (int) DAYS.between(date2, date1);

                final SetlistVenue venue1 = setlist1.getVenue();
                final SetlistVenue venue2 = setlist2.getVenue();

                if (gap > 0 || (gap == 0 && !venue1.equals(venue2))) {
                    sumOfShowGaps += gap;

                    if (longestShowGap == null || gap > longestShowGap) {
                        longestShowGap = gap;
                    }
                    if (shortestShowGap == null || gap < shortestShowGap) {
                        shortestShowGap = gap;
                    }
                }
            }

            averageShowGap = sumOfShowGaps / (setlists.size() - 1); // integer division on purpose
        }
    }

    private void calculateArtistGaps() {
        // longestArtistGap/shortestArtistGap remain null if either only one show or
        // if the only shows are on the same day at the same venue

        for (final Artist artist : constructArtistIndexedData()) {
            final List<ArtistSetlist> artistSetlists = artist.getSetlists();

            if (artistSetlists.size() > 1) {
                for (int i = 0; i < artistSetlists.size() - 1; i++) {
                    final ArtistSetlist artistSetlist1 = artistSetlists.get(i);
                    final ArtistSetlist artistSetlist2 = artistSetlists.get(i + 1);

                    final LocalDate date1 = artistSetlist1.getEventDate();
                    final LocalDate date2 = artistSetlist2.getEventDate();
                    int gap = (int) DAYS.between(date2, date1);

                    final String venue1 = artistSetlist1.getVenue();
                    final String venue2 = artistSetlist2.getVenue();

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

    private List<Artist> constructArtistIndexedData() {
        final List<Artist> result = new ArrayList<>();

        for (final Setlist setlist : setlists) {
            final SetlistArtist setlistArtist = setlist.getArtist();
            boolean isNewArtist = true;

            for (final Artist artist : result) {
                if ((setlistArtist.getMbid().equals(artist.getId()))) {
                    artist.addSetlist(new ArtistSetlist(setlist.getEventDate(), setlist.getVenue().getName()));
                    isNewArtist = false;
                }
            }

            if (isNewArtist) {
                result.add(new Artist(setlistArtist.getMbid(), setlistArtist.getName(),
                        new ArtistSetlist(setlist.getEventDate(), setlist.getVenue().getName())));
            }
        }

        return result;
    }

    private void calculateTopVenueVisits() {
        final int NUMBER_OF_TOP_VENUES = 5;
        final Map<String, Integer> venueVisits = calculateVenueVisits();

        int i = 0;
        final Iterator<Map.Entry<String, Integer>> iterator = venueVisits.entrySet().iterator();
        while (i < venueVisits.size() && i < NUMBER_OF_TOP_VENUES) {
            final Map.Entry<String, Integer> pair = iterator.next();
            topVenueVisits.put(pair.getKey(), pair.getValue());
            i++;
        }
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

    private void constructArtistIds() {
        for (final Setlist setlist : setlists) {
            artistIds.add(setlist.getArtist().getMbid());
        }
    }

    private void constructArtistIdNameMap() {
        for (final Artist artist : constructArtistIndexedData()) {
            artistIdNameMap.put(artist.getId(), artist.getName());
        }
    }

    private void constructShows() {
        for (final Setlist setlist : setlists) {
            final String eventDate = setlist.getEventDate().toString();
            final String venueName = setlist.getVenue().getName();
            final String artistId = setlist.getArtist().getMbid();
            final String artist = setlist.getArtist().getName();
            final Show show = new Show(eventDate, venueName);

            if (!shows.contains(show)) {
                shows.add(show);
            }

            shows.get(shows.indexOf(show)).addArtist(artistId, artist);
        }
    }

    private void constructVenues() {
        for (final Setlist setlist : setlists) {
            final String venueName = setlist.getVenue().getName();

            if (!venueNames.contains(venueName)) {
                venueNames.add(setlist.getVenue().getName());
                venues.add(setlist.getVenue());
            }
        }
    }

    private void calculateNumberOfShows() {
        numberOfShows = shows.size();
    }
}
