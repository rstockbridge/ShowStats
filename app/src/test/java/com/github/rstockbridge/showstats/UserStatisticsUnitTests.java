package com.github.rstockbridge.showstats;

import com.github.rstockbridge.showstats.api.models.City;
import com.github.rstockbridge.showstats.api.models.Coordinates;
import com.github.rstockbridge.showstats.api.models.Setlist;
import com.github.rstockbridge.showstats.api.models.SetlistArtist;
import com.github.rstockbridge.showstats.api.models.SetlistVenue;
import com.github.rstockbridge.showstats.appmodels.UserStatistics;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public final class UserStatisticsUnitTests {

    private String userId = "rebecca";
    private UserStatistics userStatistics;

    @Before
    public void initialize() {
        final List<Setlist> setlists = new ArrayList<>();

        final Setlist setlist1 = new Setlist(
                "123abc",
                "01-01-2018",
                new SetlistArtist("artistA", "Artist A"),
                new SetlistVenue("Venue A", new City("City A", new Coordinates(42.3314, -83.0458))),
                "http://www.artistA.com");

        setlists.add(setlist1);

        userStatistics = new UserStatistics(userId, setlists);
    }

    @Test
    public void testUserId() {
        assertEquals(userStatistics.getUserId(), userId);
    }
}