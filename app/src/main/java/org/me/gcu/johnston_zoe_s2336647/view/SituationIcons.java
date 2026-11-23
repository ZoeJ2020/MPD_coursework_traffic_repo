package org.me.gcu.johnston_zoe_s2336647.view;
import android.util.Log;

import org.me.gcu.johnston_zoe_s2336647.R;
import org.me.gcu.johnston_zoe_s2336647.model.Situation;

// SituationIcons class decides which icon to display in situation list based on type.
public class SituationIcons {

    public static int findIcon(Situation s){
        String type = s.getRawType();
        int iconId;

//        check if rawtype is not null. if it is, log and set to default icon
        if (type == null || type.isEmpty()){
            Log.w("ICONS", "Situation has no type, using default icon");

            iconId = R.drawable.ic_default;

            return iconId;
        }

        switch (type){
            case "AbnormalTraffic":
                iconId = R.drawable.ic_traffic;
                break;


            case "VehicleObstruction":
                iconId = R.drawable.ic_vehicle;
                break;

            case "GeneralObstruction":
                iconId = R.drawable.ic_obstruction;
                break;

            case "RoadOrCarriagewayOrLaneManagement":
                iconId = R.drawable.ic_lane_management;
                break;

            case "MaintenanceWorks":
                iconId = R.drawable.ic_maintenance;
                break;

            default:
                iconId = R.drawable.ic_default;
                break;
        }

        return iconId;
    }
}
