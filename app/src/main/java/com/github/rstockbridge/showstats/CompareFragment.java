package com.github.rstockbridge.showstats;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.rstockbridge.showstats.api.RetrofitWrapper;
import com.github.rstockbridge.showstats.api.SetlistfmService;
import com.github.rstockbridge.showstats.api.models.Setlist;
import com.github.rstockbridge.showstats.api.models.SetlistData;
import com.github.rstockbridge.showstats.api.models.User;
import com.github.rstockbridge.showstats.appmodels.Show;
import com.github.rstockbridge.showstats.appmodels.User1StatisticsHolder;
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
    private Button goButton;

    private ProgressBar progressBar;
    private ScrollView scrollView;
    private BarChart barChart;

    private TextUtil textUtil;

    private TextView commonArtistsLabel;
    private TextView commonVenuesLabel;
    private TextView commonShowsLabel;

    private TextView averageShowGapLabel1;
    private TextView averageShowGapLabel2;

    private boolean networkCallIsInProgress;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_compare, container, false);

        initializeUI(v);

        setNetworkCallInProgress(false);

        textUtil = new TextUtil(getResources());

        return v;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeUI(@NonNull final View v) {
        user2IdText = v.findViewById(R.id.edit_user2_userId_text);
        goButton = v.findViewById(R.id.go_button);

        user2IdText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
                // method intentionally left blank
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                goButton.setEnabled(s.length() > 0);
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
                    processSubmittedUser2();

                    handled = true;
                }
                return handled;
            }
        });

        user2IdText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (user2IdText.getRight() - user2IdText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        user2IdText.setText("");
                        return true;
                    }
                }
                return false;
            }

        });


        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                processSubmittedUser2();
            }
        });
        goButton.setEnabled(false);

        progressBar = v.findViewById(R.id.progress_bar);
        scrollView = v.findViewById(R.id.scroll_view);
        barChart = v.findViewById(R.id.bar_chart);

        scrollView.setVisibility(View.INVISIBLE);

        commonArtistsLabel = v.findViewById(R.id.common_artists);
        commonVenuesLabel = v.findViewById(R.id.common_venues);
        commonShowsLabel = v.findViewById(R.id.common_shows);

        averageShowGapLabel1 = v.findViewById(R.id.average_show_gap1);
        averageShowGapLabel2 = v.findViewById(R.id.average_show_gap2);
    }

    private void processSubmittedUser2() {
        final UserStatistics user1Statistics = User1StatisticsHolder.getSharedInstance().getStatistics();

        if (TextUtil.getText(user2IdText).equals(user1Statistics.getUserId())) {
            goButton.setEnabled(false);
            MessageUtil.makeToast(getActivity(), getString(R.string.same_user));
        } else {
            makeUserNetworkCall(TextUtil.getText(user2IdText));
        }
    }

    private void makeUserNetworkCall(@NonNull final String userId) {
        setNetworkCallInProgress(true);

        final SetlistfmService service = RetrofitWrapper.getRetrofitInstance().create(SetlistfmService.class);
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
                    setNetworkCallInProgress(false);
                    processUnsuccessfulUserResponse(response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull final Call<User> call, @NonNull final Throwable t) {
                setNetworkCallInProgress(false);

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
            setNetworkCallInProgress(false);
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
            @NonNull final String user2Id,
            final int pageIndex,
            @NonNull final ArrayList<Setlist> user2storedSetlists) {

        final SetlistfmService service = RetrofitWrapper.getRetrofitInstance().create(SetlistfmService.class);
        final Call<SetlistData> call = service.getSetlistData(user2Id, pageIndex);

        call.enqueue(new Callback<SetlistData>() {
            @Override
            public void onResponse(@NonNull final Call<SetlistData> call, @NonNull final Response<SetlistData> response) {
                if (!ActivityFragmentUtil.isActivityValid(CompareFragment.this, getActivity())) {
                    return;
                }

                if (response.isSuccessful()) {
                    final SetlistData user2SetlistData = response.body();
                    user2storedSetlists.addAll(user2SetlistData.getSetlists());

                    if (pageIndex < user2SetlistData.getNumberOfPages()) {
                        makeSetlistsNetworkCall(user2Id, pageIndex + 1, user2storedSetlists);
                    } else {
                        setNetworkCallInProgress(false);
                        final UserStatistics user1Statistics = User1StatisticsHolder.getSharedInstance().getStatistics();
                        displayStats(user1Statistics, new UserStatistics(user2Id, user2storedSetlists));
                    }
                } else {
                    setNetworkCallInProgress(false);
                    MessageUtil.makeToast(getActivity(), getString(R.string.no_setlist_data));
                }
            }

            @Override
            public void onFailure(@NonNull final Call<SetlistData> call, @NonNull final Throwable t) {
                setNetworkCallInProgress(false);

                if (!ActivityFragmentUtil.isActivityValid(CompareFragment.this, getActivity())) {
                    return;
                }

                MessageUtil.makeToast(getActivity(), getString(R.string.wrong_message));
            }
        });
    }

    private void displayStats(@NonNull final UserStatistics user1Statistics, @NonNull final UserStatistics user2Statistics) {
        final BarChartMakerUserTotalShows barChartMaker = new BarChartMakerUserTotalShows(
                barChart, user1Statistics, user2Statistics);
        barChartMaker.displayBarChart(getActivity());

        calculateCommonStatistics(user1Statistics, user2Statistics);

        scrollView.setVisibility(View.VISIBLE);

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
                final Show commonShow = new Show(user1Show.getId(), user1Show.getEventDate(), user1Show.getVenueName());

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

    private void setNetworkCallInProgress(final boolean inProgress) {
        networkCallIsInProgress = inProgress;
        syncUI();
    }

    private void syncUI() {
        if (networkCallIsInProgress) {
            user2IdText.setEnabled(false);
            goButton.setEnabled(false);

            progressBar.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.INVISIBLE);
        } else {
            user2IdText.setEnabled(true);
            if (user2IdText.getText().length() > 0) {
                goButton.setEnabled(true);
            }

            progressBar.setVisibility(View.INVISIBLE);
        }
    }

}
