package com.github.rstockbridge.showstats.screens.tabbed;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.rstockbridge.showstats.R;
import com.github.rstockbridge.showstats.api.models.City;
import com.github.rstockbridge.showstats.appmodels.User1StatisticsHolder;
import com.github.rstockbridge.showstats.appmodels.UserStatistics;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Set;

public final class MapFragment extends Fragment {

    private MapView mapView;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = v.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        mapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap map) {
                final UserStatistics statistics = User1StatisticsHolder.getSharedInstance().getStatistics();

                if (statistics != null) {
                    displayMap(map, statistics.getCities());
                }
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void displayMap(final GoogleMap map, @NonNull final Set<City> cities) {
        final LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (final City city : cities) {
            final LatLng latLng = new LatLng(city.getLatitude(), city.getLongitude());
            map.addMarker(new MarkerOptions().position(latLng).title(city.getName()));
            builder.include(latLng);
        }

        final LatLngBounds bounds = builder.build();
        final int screenHeight = getResources().getDisplayMetrics().heightPixels;
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, (int) (0.1 * screenHeight)));
        map.getUiSettings().setMapToolbarEnabled(false);
    }
}
