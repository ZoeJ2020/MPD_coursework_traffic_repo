package org.me.gcu.johnston_zoe_s2336647.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.me.gcu.johnston_zoe_s2336647.MainActivity;
import org.me.gcu.johnston_zoe_s2336647.R;
import org.me.gcu.johnston_zoe_s2336647.model.Impact;
import org.me.gcu.johnston_zoe_s2336647.model.Location;
import org.me.gcu.johnston_zoe_s2336647.model.Situation;
import org.me.gcu.johnston_zoe_s2336647.viewmodel.TrafficViewModel;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

// SituationDetailFragment handles the detail screen for the selected situation
public class SituationDetailFragment extends Fragment {

    // convert time to easily readable human format
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.UK).withZone(ZoneId.of("Europe/London"));

    public SituationDetailFragment(){
        super(R.layout.fragment_situation_detail);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        ImageView icon = view.findViewById(R.id.detailIcon);
        TextView type = view.findViewById(R.id.detailType);
        TextView category = view.findViewById(R.id.detailCategory);
        TextView description = view.findViewById(R.id.detailDescription);
        TextView direction = view.findViewById(R.id.detailDirection);
        TextView start = view.findViewById(R.id.detailStart);
        TextView end = view.findViewById(R.id.detailEnd);
        TextView impact = view.findViewById(R.id.detailImpact);
        TextView locations = view.findViewById(R.id.detailLocations);
        TextView updated = view.findViewById(R.id.detailUpdated);
        //  delay chip colored by DelayStyles
        TextView delay = view.findViewById(R.id.detailDelay);
        // placeholder message if nothing selected
        TextView placeholder = view.findViewById(R.id.detailPlaceholder);
        View content = view.findViewById(R.id.detailContent);

        TrafficViewModel viewModel = new ViewModelProvider(requireActivity()).get(TrafficViewModel.class);

        // shows only the selected situation on the map when "Show on map" button is clicked
        Button mapButton = view.findViewById(R.id.detailMapButton);
        mapButton.setOnClickListener(v -> {
            Situation chosen = viewModel.getSelectedSituation().getValue();
            if (chosen != null && requireActivity() instanceof MainActivity){
                ArrayList<Situation> justOne = new ArrayList<>();
                justOne.add(chosen);
                viewModel.setMapSituations(justOne);
                ((MainActivity) requireActivity()).showMap();
            }
        });

        viewModel.getSelectedSituation().observe(getViewLifecycleOwner(), s -> {
            if (s == null){
                placeholder.setVisibility(View.VISIBLE);
                content.setVisibility(View.GONE);
                return;
            }

            placeholder.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);

            icon.setImageResource(SituationIcons.findIcon(s));
            type.setText(s.getFormattedType());
            DelayStyles.applyChip(delay, s.getImpact().getDelayType());

            //    category, description and direction are hidden if the feed gives nothing
            showRow(category, s.getCategory() == null ? null : "Category: " + s.getCategory());
            showRow(description, s.getDescription());
            showRow(direction, s.getDirection() == null ? null : "Direction: " + s.getDirection());

            showRow(start, s.getStartTime() == null ? null : "Starts: " + formatTime(s.getStartTime()));

            //    set end time if found, if not then leave it. end times are not always given
            if (s.getEndTime() == null){
                showRow(end, "No end time given");
            } else {
                showRow(end, "Ends: " + formatTime(s.getEndTime()));
            }

            showRow(impact, buildImpactText(s.getImpact()));
            showRow(locations, buildLocationText(s));
            showRow(updated, s.getUpdatedTime() == null ? null : "Last updated: " + formatTime(s.getUpdatedTime()));
        });
    }

    //    shows the text in the row, or hides the row completely when there is no text
    private void showRow(TextView row, String text){
        if (text == null || text.isEmpty()){
            row.setVisibility(View.GONE);
        } else {
            row.setVisibility(View.VISIBLE);
            row.setText(text);
        }
    }

    // turns the feed time (2026-11-22T10:00:00Z) into UK time,
    // or keeps the original text if format doesn't match
    private String formatTime(String feedTime){
        try {
            return TIME_FORMAT.format(Instant.parse(feedTime));
        } catch (Exception e){
            return feedTime;
        }
    }

    //    handles lanes only, the delay type is shown in the coloured chip,
    //    so is not included in this impact text handles
    private String buildImpactText(Impact impact){
        if (impact == null){
            return null;
        }

        StringBuilder text = new StringBuilder();

        if (impact.getOperationalLaneCount() != null){
            if (impact.getOperationalLaneCount() == 0){
                text.append("0 lanes open\n");
            } else {
                text.append("Lanes open: ").append(impact.getOperationalLaneCount()).append("\n");
            }
        }

        if (impact.getRestrictedLaneCount() != null){
            text.append("Lanes restricted: ").append(impact.getRestrictedLaneCount()).append("\n");
        }

        return text.toString().trim();
    }

    //    one paragraph per location - role, place name, road names and coordinates
    private String buildLocationText(Situation s){
        if (s.getLocations() == null || s.getLocations().isEmpty()){
            return null;
        }

        StringBuilder text = new StringBuilder();

        for (Location l : s.getLocations()){
            text.append("from".equals(l.getRole()) ? "From: " : "To: ");

            if (l.getPlaceName() != null){
                text.append(l.getPlaceName()).append(" ");
            }

            if (l.getRoadNames() != null && !l.getRoadNames().isEmpty()){
                text.append("(").append(String.join(", ", l.getRoadNames())).append(") ");
            }

            text.append(l.getLatitude()).append(", ").append(l.getLongitude()).append("\n");
        }

        return text.toString().trim();
    }
}