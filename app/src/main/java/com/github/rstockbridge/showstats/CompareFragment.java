package com.github.rstockbridge.showstats;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.rstockbridge.showstats.api.RetrofitInstance;
import com.github.rstockbridge.showstats.api.SetlistfmService;
import com.github.rstockbridge.showstats.api.models.Setlist;
import com.github.rstockbridge.showstats.api.models.SetlistData;
import com.github.rstockbridge.showstats.api.models.User;
import com.github.rstockbridge.showstats.appmodels.UserStatistics;
import com.github.rstockbridge.showstats.ui.BarChartMakerUserTotalShows;
import com.github.rstockbridge.showstats.ui.MessageUtil;
import com.github.rstockbridge.showstats.ui.TextUtil;
import com.github.rstockbridge.showstats.utility.ActivityFragmentUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class CompareFragment extends Fragment {

    private static final String ARG_USER1_STATISTICS = "user1Statistics";

    @NonNull
    public static CompareFragment newInstance(@NonNull final UserStatistics user1Statistics) {
        final Bundle args = new Bundle();
        args.putParcelable(ARG_USER1_STATISTICS, user1Statistics);

        final CompareFragment fragment = new CompareFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private UserStatistics user1Statistics;
    private UserStatistics user2Statistics;

    private List<String> commonArtists;
    private List<String> commonShows;
    private List<String> commonVenues;

    private EditText user2IdText;

    private ScrollView scrollview;
    private BarChart barChart;

    private TextUtil textUtil;

    private TextView commonArtistsLabel;
    private TextView commonVenuesLabel;
    private TextView commonShowsLabel;

    private TextView averageShowGapLabel1;
    private TextView averageShowGapLabel2;


    @Nullable
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_compare, container, false);
        initializeUI(v);

        textUtil = new TextUtil(getResources());

        user1Statistics = getArguments().getParcelable(ARG_USER1_STATISTICS);

        return v;
    }

    private void initializeUI(@NonNull final View v) {
        user2IdText = v.findViewById(R.id.edit_user2_userId_text);
        final Button submit = v.findViewById(R.id.submit_button);

        user2IdText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
                // method intentionally left blank
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                submit.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(final Editable s) {
                //method intentionally left blank
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                submit.setEnabled(false);
                scrollview.setVisibility(View.INVISIBLE);

                if (TextUtil.getText(user2IdText).equals(user1Statistics.getUserId())) {
                    MessageUtil.makeToast(getActivity(), getString(R.string.same_user));
                } else {
                    makeUserNetworkCall(TextUtil.getText(user2IdText));
                }
            }
        });

        scrollview = v.findViewById(R.id.scrollview);
        barChart = v.findViewById(R.id.bar_chart);

        commonArtistsLabel = v.findViewById(R.id.common_artists);
        commonVenuesLabel = v.findViewById(R.id.common_venues);
        commonShowsLabel = v.findViewById(R.id.common_shows);

        averageShowGapLabel1 = v.findViewById(R.id.average_show_gap1);
        averageShowGapLabel2 = v.findViewById(R.id.average_show_gap2);
    }

    private void makeUserNetworkCall(@NonNull final String userId) {
        final SetlistfmService service = RetrofitInstance.getRetrofitInstance().create(SetlistfmService.class);
        final Call<User> call = service.verifyUserId(userId);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull final Call<User> call, @NonNull final Response<User> response) {
                if (!ActivityFragmentUtil.isActivityValid(CompareFragment.this, getActivity())) {
                    return;
                }

                if (response.isSuccessful()) {
                    processSuccessfulUserResponse(response.body());
                } else {
                    processUnsuccessfulUserResponse(response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull final Call<User> call, @NonNull final Throwable t) {
                if (!ActivityFragmentUtil.isActivityValid(CompareFragment.this, getActivity())) {
                    return;
                }

                MessageUtil.makeToast(getActivity(), getString(R.string.wrong_message));
            }
        });
    }

    private void processSuccessfulUserResponse(@NonNull final User user) {
        if (user.getUserId().equals(TextUtil.getText(user2IdText))) {
            final ArrayList<Setlist> storedSetlists = new ArrayList<>();
            makeSetlistsNetworkCall(user.getUserId(), 1, storedSetlists);
        } else {
            MessageUtil.makeToast(getActivity(), getString(R.string.unresolveable_userId_message));
        }
    }

    private void processUnsuccessfulUserResponse(@Nullable final ResponseBody errorBody) {
        try {
            if (errorBody != null && errorBody.string().contains(getString(R.string.unknown_userId))) {
                MessageUtil.makeToast(getActivity(), getString(R.string.unknown_userId_message));
            } else {
                MessageUtil.makeToast(getActivity(), getString(R.string.wrong_message));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeSetlistsNetworkCall(
            @NonNull final String userId,
            final int pageIndex,
            @NonNull final ArrayList<Setlist> storedSetlists) {

        final SetlistfmService service = RetrofitInstance.getRetrofitInstance().create(SetlistfmService.class);
        final Call<SetlistData> call = service.getSetlistData(userId, pageIndex);

        call.enqueue(new Callback<SetlistData>() {
            @Override
            public void onResponse(@NonNull final Call<SetlistData> call, @NonNull final Response<SetlistData> response) {
                if (!ActivityFragmentUtil.isActivityValid(CompareFragment.this, getActivity())) {
                    return;
                }

                if (response.isSuccessful()) {
                    final SetlistData setlistData = response.body();
                    storedSetlists.addAll(setlistData.getSetlists());

                    if (pageIndex < setlistData.getNumberOfPages()) {
                        makeSetlistsNetworkCall(userId, pageIndex + 1, storedSetlists);
                    } else {
                        user2Statistics = new UserStatistics(userId, storedSetlists);
                        displayStats();
                    }
                } else {
                    MessageUtil.makeToast(getActivity(), getString(R.string.no_setlist_data));
                }
            }

            @Override
            public void onFailure(@NonNull final Call<SetlistData> call, @NonNull final Throwable t) {
                if (!ActivityFragmentUtil.isActivityValid(CompareFragment.this, getActivity())) {
                    return;
                }

                MessageUtil.makeToast(getActivity(), getString(R.string.wrong_message));
            }
        });
    }

    private void displayStats() {
        scrollview.setVisibility(View.VISIBLE);

        final BarChartMakerUserTotalShows barChartMaker = new BarChartMakerUserTotalShows(
                barChart, user1Statistics, user2Statistics);
        barChartMaker.displayBarChart();

        calculateCommonStatistics();

        displayCommonArtists();
        displayCommonVenues();
        displayCommonShows();
        displayShowGaps();
    }

    private void calculateCommonStatistics() {
        calculateCommonArtists();
        calculateCommonVenues();
        calculateCommonShows();
    }

    private void calculateCommonArtists() {
        commonArtists = new ArrayList<>();

        for (final String user1ArtistId : user1Statistics.getArtistIds()) {
            if (user2Statistics.getArtistIds().contains(user1ArtistId)) {
                commonArtists.add(user1Statistics.getArtistNameFromId(user1ArtistId));
            }

            Collections.sort(commonArtists);
        }
    }

    private void calculateCommonVenues() {
        commonVenues = new ArrayList<>();

        for (final String user1Venue : user1Statistics.getVenues()) {
            if (user2Statistics.getVenues().contains(user1Venue)) {
                commonVenues.add(user1Venue);
            }

            Collections.sort(commonVenues);
        }
    }

    private void calculateCommonShows() {
        commonShows = new ArrayList<>();

        for (final String show : user1Statistics.getShows()) {

            if (user2Statistics.getShows().contains(show)) {
                commonShows.add(show);
            }

            Collections.sort(commonShows);
        }
    }

    private void displayShowGaps() {
        averageShowGapLabel1.setText(textUtil.getUserGapText(user1Statistics.getUserId(), user1Statistics.getAverageShowGap()));
        averageShowGapLabel2.setText(textUtil.getUserGapText(user2Statistics.getUserId(), user2Statistics.getAverageShowGap()));
    }

    private void displayCommonArtists() {
        commonArtistsLabel.setText(textUtil.getArtistText(commonArtists, false));
    }

    private void displayCommonShows() {
        commonShowsLabel.setText(textUtil.getArtistText(commonShows, false));
    }

    private void displayCommonVenues() {
        commonVenuesLabel.setText(textUtil.getArtistText(commonVenues, false));
    }
}
