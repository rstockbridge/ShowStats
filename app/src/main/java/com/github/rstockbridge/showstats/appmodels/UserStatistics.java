package com.github.rstockbridge.showstats.appmodels;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.github.rstockbridge.showstats.api.models.Setlist;
import com.github.rstockbridge.showstats.api.models.SetlistArtist;
import com.github.rstockbridge.showstats.api.models.SetlistVenue;
import com.github.rstockbridge.showstats.utility.MapUtil;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.threeten.bp.temporal.ChronoUnit.DAYS;

public final class UserStatistics implements Parcelable {

    private static final int NUMBER_OF_MONTHS = 12;

    @NonNull
    private String userId;

    @NonNull
    private List<Setlist> setlists;

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

    @NonNull
    private Map<String, Integer> topVenueVisits = new HashMap<>();

    public UserStatistics(@NonNull final String userId, @NonNull final List<Setlist> setlists) {
        this.userId = userId;
        this.setlists = setlists;

        calculateDistributionByMonth();
        calculateShowGaps();
        calculateArtistGaps();
        calculateTopVenueVisits();
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

    private void calculateDistributionByMonth() {
        for (final Setlist setlist : setlists) {
            final int setlistMonth = setlist.getEventDate().getMonthValue();
            distributionByMonth[NUMBER_OF_MONTHS - setlistMonth]++; // invert for display purposes
        }
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

        for (final Artist artist : constructArtistIndexedData(setlists)) {
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

    private List<Artist> constructArtistIndexedData(final List<Setlist> setlists) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(this.userId);
        dest.writeTypedList(this.setlists);
        dest.writeIntArray(this.distributionByMonth);
        dest.writeValue(this.longestShowGap);
        dest.writeValue(this.shortestShowGap);
        dest.writeStringList(this.longestArtistGapArtists);
        dest.writeStringList(this.shortestArtistGapArtists);
        dest.writeValue(this.longestArtistGap);
        dest.writeValue(this.shortestArtistGap);
        dest.writeInt(this.topVenueVisits.size());
        for (Map.Entry<String, Integer> entry : this.topVenueVisits.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeValue(entry.getValue());
        }
    }

    private UserStatistics(final Parcel in) {
        this.userId = in.readString();
        this.setlists = in.createTypedArrayList(Setlist.CREATOR);
        this.distributionByMonth = in.createIntArray();
        this.longestShowGap = (Integer) in.readValue(Integer.class.getClassLoader());
        this.shortestShowGap = (Integer) in.readValue(Integer.class.getClassLoader());
        this.longestArtistGapArtists = in.createStringArrayList();
        this.shortestArtistGapArtists = in.createStringArrayList();
        this.longestArtistGap = (Integer) in.readValue(Integer.class.getClassLoader());
        this.shortestArtistGap = (Integer) in.readValue(Integer.class.getClassLoader());
        int topVenueVisitsSize = in.readInt();
        this.topVenueVisits = new HashMap<>(topVenueVisitsSize);
        for (int i = 0; i < topVenueVisitsSize; i++) {
            String key = in.readString();
            Integer value = (Integer) in.readValue(Integer.class.getClassLoader());
            this.topVenueVisits.put(key, value);
        }
    }

    public static final Parcelable.Creator<UserStatistics> CREATOR = new Parcelable.Creator<UserStatistics>() {
        @Override
        public UserStatistics createFromParcel(final Parcel source) {
            return new UserStatistics(source);
        }

        @Override
        public UserStatistics[] newArray(final int size) {
            return new UserStatistics[size];
        }
    };
}
