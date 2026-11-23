package org.me.gcu.johnston_zoe_s2336647.view;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.me.gcu.johnston_zoe_s2336647.R;

// DelayStyles class handles the different delay types - label content, color coding etc.
public class DelayStyles {

    //    null means the feed gave no delay for this situation
    public static String findLabel(String delayType){
        String label;

        if (delayType == null || delayType.isEmpty()){
            label = null;
        } else {
            switch (delayType){
                case "delays":
                    label = "Delays";
                    break;

                case "longDelays":
                    label = "Long delays";
                    break;

                case "veryLongDelays":
                    label = "Very long delays";
                    break;

                default:
                    label = "Delay: " + delayType;
                    break;
            }
        }

        return label;
    }

    public static int findColour(Context context, String delayType){
        int colourId;

        if (delayType == null){
            colourId = R.color.delay_none;
        } else {
            switch (delayType){
                case "delays":
                    colourId = R.color.delay_short;
                    break;

                case "longDelays":
                    colourId = R.color.delay_long;
                    break;

                case "veryLongDelays":
                    colourId = R.color.delay_very_long;
                    break;

                default:
                    colourId = R.color.delay_none;
                    break;
            }
        }

        return ContextCompat.getColor(context, colourId);
    }

    //    fills a chip with the label and colour for user view, or hides it when there is no delay
    public static void applyChip(TextView chip, String delayType){
        String label = findLabel(delayType);

        if (label == null){
            chip.setVisibility(View.GONE);
        } else {
            chip.setVisibility(View.VISIBLE);
            chip.setText(label);
            chip.setBackgroundColor(findColour(chip.getContext(), delayType));
        }
    }
}