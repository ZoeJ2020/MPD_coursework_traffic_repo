package org.me.gcu.johnston_zoe_s2336647.view;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.me.gcu.johnston_zoe_s2336647.model.Location;
import org.me.gcu.johnston_zoe_s2336647.model.Situation;
import org.me.gcu.johnston_zoe_s2336647.viewmodel.TrafficViewModel;

import java.util.ArrayList;

// SiuationMapFragment handles displaying icons of selected situations on map
public class SituationMapFragment extends SupportMapFragment implements OnMapReadyCallback {

    private GoogleMap map;

    //    middle of Scotland, used when there is nothing to show
    private static final LatLng SCOTLAND = new LatLng(56.4907, -4.2026);

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        TrafficViewModel viewModel = new ViewModelProvider(requireActivity()).get(TrafficViewModel.class);

        //    tapping a marker's icon opens that situation's detail screen
        map.setOnInfoWindowClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof Situation) {
                viewModel.select((Situation) tag);
            }
        });

        viewModel.getMapSituations().observe(getViewLifecycleOwner(), list -> drawSituations(list));
    }

    //    one marker per situation (in the middle of its stretch) and a line between its from and to points
    private void drawSituations(ArrayList<Situation> list) {
        map.clear();

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        LatLng onlyPosition = null;
        int pointCount = 0;

        if (list != null) {
            for (Situation s : list) {
                ArrayList<Location> locations = s.getLocations();
                if (locations.isEmpty()) {
                    continue;
                }

                Location first = locations.get(0);
                LatLng start = new LatLng(first.getLatitude(), first.getLongitude());
                LatLng position = start;

                //    pin in the middle of the stretch when there are two points, so it sits on the line
                LatLng end = null;
                if (locations.size() >= 2) {
                    Location second = locations.get(1);
                    end = new LatLng(second.getLatitude(), second.getLongitude());
                    position = new LatLng((start.latitude + end.latitude) / 2,
                            (start.longitude + end.longitude) / 2);
                }

                //    roadworks are orange pins, incidents are red pins
                boolean roadworks = "roadworks".equals(s.getCategory());
                float pinColour = roadworks ? BitmapDescriptorFactory.HUE_ORANGE : BitmapDescriptorFactory.HUE_RED;

                Marker marker = map.addMarker(new MarkerOptions()
                        .position(position)
                        .title(s.getFormattedType())
                        .snippet(s.getDescription())
                        .icon(BitmapDescriptorFactory.defaultMarker(pinColour)));
                if (marker != null) {
                    marker.setTag(s);
                }

                bounds.include(start);
                onlyPosition = start;
                pointCount++;

                if (end != null) {
                    //    roadworks lines use the delay colours, incident lines are red
                    int lineColour = roadworks
                            ? DelayStyles.findColour(requireContext(), s.getImpact().getDelayType())
                            : Color.RED;

                    map.addPolyline(new PolylineOptions().add(start, end).width(8f).color(lineColour));
                    bounds.include(end);
                    pointCount++;
                }
            }
        }

        if (pointCount == 0) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(SCOTLAND, 6f));
        } else if (pointCount == 1) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(onlyPosition, 14f));
        } else {
            fitToBounds(bounds.build());
        }
    }

    //    zooms so every marker and line end is in view, waiting until the map has a size
    private void fitToBounds(LatLngBounds bounds) {
        View mapView = getView();
        if (mapView == null) {
            return;
        }

        mapView.post(() -> {
            try {
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            } catch (IllegalStateException e) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(SCOTLAND, 6f));
            }
        });
    }
}