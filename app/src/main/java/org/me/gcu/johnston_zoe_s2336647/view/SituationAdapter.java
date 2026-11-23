package org.me.gcu.johnston_zoe_s2336647.view;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.me.gcu.johnston_zoe_s2336647.R;
import org.me.gcu.johnston_zoe_s2336647.model.Situation;

import java.util.ArrayList;
import java.util.Locale;

// RecyclerView holds found situations in scrollable list and handles clicks etc.
public class SituationAdapter extends RecyclerView.Adapter<SituationAdapter.Holder> {

    private ArrayList<Situation> items = new ArrayList<>();

    private Situation selected = null;

    // fragment listens for when a situation is tapped
    public interface OnSituationClickListener {
        void onSituationClick(Situation situation);
    }

    private OnSituationClickListener clickListener;

    public void setOnSituationClickListener(OnSituationClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setItems(ArrayList<Situation> newItems) {
        items = newItems;
        notifyDataSetChanged();
    }

    // called when the selection changes, null means nothing is selected
    public void setSelected(Situation situation) {
        selected = situation;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_situation, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Situation s = items.get(position);
        holder.typeText.setText(s.getFormattedType());
        holder.descriptionText.setText(s.getDescription());
        holder.iconImage.setImageResource(SituationIcons.findIcon(s));

        // calculate situation distance away from location when using nearby search
        if (s.getDistanceKm() == null) {
            holder.distanceText.setVisibility(View.GONE);
        } else {
            holder.distanceText.setVisibility(View.VISIBLE);
            holder.distanceText.setText(String.format(Locale.UK, "%.1f km away", s.getDistanceKm()));
        }

        // coloured delay chip for roadworks, hidden when there is no delay
        DelayStyles.applyChip(holder.delayText, s.getImpact().getDelayType());

        // highlight the situation that is open on the detail screen, using the theme's
        // highlight colour for light/dark mode
        if (s == selected) {
            TypedValue highlight = new TypedValue();
            holder.itemView.getContext().getTheme()
                    .resolveAttribute(android.R.attr.colorControlHighlight, highlight, true);
            holder.itemView.setBackgroundColor(highlight.data);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        // tell the listener which situation this row is showing when it is tapped
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onSituationClick(s);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView typeText;
        TextView descriptionText;
        ImageView iconImage;
        TextView distanceText;
        TextView delayText;

        Holder(@NonNull View itemView) {
            super(itemView);
            typeText = itemView.findViewById(R.id.typeText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            iconImage = itemView.findViewById(R.id.iconImage);
            distanceText = itemView.findViewById(R.id.distanceText);
            delayText = itemView.findViewById(R.id.delayText);
        }
    }

}