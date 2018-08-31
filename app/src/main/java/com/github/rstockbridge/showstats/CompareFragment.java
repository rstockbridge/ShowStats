package com.github.rstockbridge.showstats;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.rstockbridge.showstats.api.RetrofitInstance;
import com.github.rstockbridge.showstats.api.SetlistfmService;
import com.github.rstockbridge.showstats.api.models.Setlist;
import com.github.rstockbridge.showstats.api.models.SetlistData;
import com.github.rstockbridge.showstats.api.models.User;
import com.github.rstockbridge.showstats.appmodels.Show;
import com.github.rstockbridge.showstats.appmodels.User1StatisticsHolder;
import com.github.rstockbridge.showstats.appmodels.User2StatisticsHolder;
import com.github.rstockbridge.showstats.appmodels.UserStatistics;
import com.github.rstockbridge.showstats.ui.BarChartMakerUserTotalShows;
import com.github.rstockbridge.showstats.ui.MessageUtil;
import com.github.rstockbridge.showstats.ui.TextUtil;
import com.github.rstockbridge.showstats.utility.ActivityFragmentUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class CompareFragment extends Fragment {

    private List<String> commonArtists;
    private List<String> commonVenues;
    private List<Show> commonShows;

    private EditText user2IdText;

    private Button clear;
    private Button submit;

    private ProgressBar progressBar;
    private ScrollView scrollView;
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

        final UserStatistics user1Statistics = User1StatisticsHolder.getSharedInstance().getStatistics();
        final UserStatistics user2Statistics = User2StatisticsHolder.getSharedInstance().getStatistics();

        if (user1Statistics != null && user2Statistics != null) {
            displayStats(user1Statistics, user2Statistics);
        }

        return v;
    }

    private void initializeUI(@NonNull final View v) {
        user2IdText = v.findViewById(R.id.edit_user2_userId_text);
        clear = v.findViewById(R.id.clear_button);
        submit = v.findViewById(R.id.submit_button);

        user2IdText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
                // method intentionally left blank
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                clear.setEnabled(s.length() > 0);
                submit.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(final Editable s) {
                //method intentionally left blank
            }
        });

        user2IdText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    setViewsForInProgress();
                    makeUserNetworkCall(TextUtil.getText(user2IdText));
                    handled = true;
                }
                return handled;
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                user2IdText.setText("");
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                setViewsForInProgress();

                final UserStatistics user1Statistics = User1StatisticsHolder.getSharedInstance().getStatistics();

                if (TextUtil.getText(user2IdText).equals(user1Statistics.getUserId())) {
                    setViewsForUnsuccessfulResponse();
                    MessageUtil.makeToast(getActivity(), getString(R.string.same_user));
                } else {
                    makeUserNetworkCall(TextUtil.getText(user2IdText));
                }
            }
        });

        progressBar = v.findViewById(R.id.progress_bar);
        scrollView = v.findViewById(R.id.scroll_view);
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

                setViewsForUnsuccessfulResponse();
                MessageUtil.makeToast(getActivity(), getString(R.string.wrong_message));
            }
        });
    }

    private void processSuccessfulUserResponse(@NonNull final User user) {
        if (user.getUserId().equals(TextUtil.getText(user2IdText))) {
            final ArrayList<Setlist> storedSetlists = new ArrayList<>();
            makeSetlistsNetworkCall(user.getUserId(), 1, storedSetlists);
        } else {
            setViewsForUnsuccessfulResponse();
            MessageUtil.makeToast(getActivity(), getString(R.string.unresolveable_userId_message));
        }
    }

    private void processUnsuccessfulUserResponse(@Nullable final ResponseBody errorBody) {
        try {
            setViewsForUnsuccessfulResponse();

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
                        setViewsForSuccessfulResponse();
                        final UserStatistics user1Statistics = User1StatisticsHolder.getSharedInstance().getStatistics();

                        User2StatisticsHolder.getSharedInstance().setStatistics(new UserStatistics(userId, storedSetlists));
                        final UserStatistics user2Statistics = User2StatisticsHolder.getSharedInstance().getStatistics();

                        displayStats(user1Statistics, user2Statistics);
                    }
                } else {
                    setViewsForUnsuccessfulResponse();
                    MessageUtil.makeToast(getActivity(), getString(R.string.no_setlist_data));
                }
            }

            @Override
            public void onFailure(@NonNull final Call<SetlistData> call, @NonNull final Throwable t) {
                if (!ActivityFragmentUtil.isActivityValid(CompareFragment.this, getActivity())) {
                    return;
                }

                setViewsForUnsuccessfulResponse();
                MessageUtil.makeToast(getActivity(), getString(R.string.wrong_message));
            }
        });
    }

    private void displayStats(@NonNull final UserStatistics user1Statistics, @NonNull final UserStatistics user2Statistics) {
        final BarChartMakerUserTotalShows barChartMaker = new BarChartMakerUserTotalShows(
                barChart, user1Statistics, user2Statistics);
        barChartMaker.displayBarChart();

        calculateCommonStatistics(user1Statistics, user2Statistics);

        displayCommonArtists();
        displayCommonVenues();
        displayCommonShows();
        displayShowGaps(user1Statistics, user2Statistics);
    }

    private void calculateCommonStatistics(@NonNull final UserStatistics user1Statistics, @NonNull final UserStatistics user2Statistics) {
        calculateCommonArtists(user1Statistics, user2Statistics);
        calculateCommonVenues(user1Statistics, user2Statistics);
        calculateCommonShows(user1Statistics, user2Statistics);
    }

    private void calculateCommonArtists(@NonNull final UserStatistics user1Statistics, @NonNull final UserStatistics user2Statistics) {
        commonArtists = new ArrayList<>();

        final Set<String> commonArtistIds = new HashSet<>(user1Statistics.getArtistIds());
        commonArtistIds.retainAll(user2Statistics.getArtistIds());

        for (final String commonArtistId : commonArtistIds) {
            commonArtists.add(user1Statistics.getArtistNameFromId(commonArtistId));
        }

        Collections.sort(commonArtists);
    }

    private void calculateCommonVenues(@NonNull final UserStatistics user1Statistics, @NonNull final UserStatistics user2Statistics) {
        final Set<String> commonVenuesAsSet = new HashSet<>(user1Statistics.getVenueNames());
        commonVenuesAsSet.retainAll(user2Statistics.getVenueNames());

        commonVenues = new ArrayList<>(commonVenuesAsSet);
        Collections.sort(commonVenues);
    }

    private void calculateCommonShows(@NonNull final UserStatistics user1Statistics, @NonNull final UserStatistics user2Statistics) {
        commonShows = new ArrayList<>();

        final List<Show> user1Shows = user1Statistics.getShows();
        final List<Show> user2Shows = user2Statistics.getShows();

        for (final Show user1Show : user1Shows) {
            if (user2Shows.contains(user1Show)) {
                final Show user2Show = user2Shows.get(user2Shows.indexOf(user1Show));
                final Show commonShow = new Show(user1Show.getEventDate(), user1Show.getVenueName());

                for (final String user1ArtistId : user1Show.getArtistIds()) {
                    if (user2Show.getArtistIds().contains(user1ArtistId)) {
                        final String user1Artist = user1Statistics.getArtistNameFromId(user1ArtistId);
                        commonShow.addArtist(user1ArtistId, user1Artist);
                    }
                }

                if (commonShow.getArtistIds().size() > 0) {
                    commonShows.add(commonShow);
                }
            }
        }
    }

    private void displayShowGaps(@NonNull final UserStatistics user1Statistics, @NonNull final UserStatistics user2Statistics) {
        averageShowGapLabel1.setText(textUtil.getUserGapText(user1Statistics.getUserId(), user1Statistics.getAverageShowGap()));
        averageShowGapLabel2.setText(textUtil.getUserGapText(user2Statistics.getUserId(), user2Statistics.getAverageShowGap()));
    }

    private void displayCommonArtists() {
        commonArtistsLabel.setText(textUtil.getListText(commonArtists, false));
    }

    private void displayCommonShows() {
        commonShowsLabel.setText(textUtil.getCommonShowsText(commonShows));
    }

    private void displayCommonVenues() {
        commonVenuesLabel.setText(textUtil.getListText(commonVenues, false));
    }

    private void setViewsForInProgress() {
        user2IdText.setEnabled(false);
        clear.setEnabled(false);
        submit.setEnabled(false);

        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.INVISIBLE);
    }

    private void setViewsForSuccessfulResponse() {
        user2IdText.setEnabled(true);
        clear.setEnabled(true);
        submit.setEnabled(true);

        progressBar.setVisibility(View.INVISIBLE);
        scrollView.setVisibility(View.VISIBLE);
    }

    private void setViewsForUnsuccessfulResponse() {
        user2IdText.setEnabled(true);
        clear.setEnabled(true);
        submit.setEnabled(true);

        progressBar.setVisibility(View.INVISIBLE);
        scrollView.setVisibility(View.INVISIBLE);
    }
}
