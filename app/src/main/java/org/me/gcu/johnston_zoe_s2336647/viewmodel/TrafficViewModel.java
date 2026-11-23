package org.me.gcu.johnston_zoe_s2336647.viewmodel;

import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import org.me.gcu.johnston_zoe_s2336647.model.Location;
import org.me.gcu.johnston_zoe_s2336647.model.Situation;
import org.me.gcu.johnston_zoe_s2336647.model.TrafficCache;
import org.me.gcu.johnston_zoe_s2336647.model.TrafficDownloader;
import org.me.gcu.johnston_zoe_s2336647.model.TrafficXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class TrafficViewModel extends AndroidViewModel {

    private static final String INCIDENTS_URL =
            "https://datex2.trafficscotland.org/rest/2.3/publications/UnplannedEvents/Content.xml";
    private static final String ROADWORKS_URL =
            "https://datex2.trafficscotland.org/rest/2.3/publications/CurrentRoadworks/Content.xml";

    // time between auto refresh. set to minute for testing purposes.
    private static final long REFRESH_INTERVAL_MS = 60 * 1000;

    private final MutableLiveData<ArrayList<Situation>> situations = new MutableLiveData<>();

    private final MutableLiveData<Situation> selectedSituation = new MutableLiveData<Situation>(null);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    // banner at top of list (updated/offline messages)
    private final MutableLiveData<String> updateNotice = new MutableLiveData<>("");
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // user search query, display in "___ results for ___"
    private final MutableLiveData<String> roadQuery = new MutableLiveData<>("");

    // nearby search. 0 means nearby search is off, otherwise save the radius in km
    private final MutableLiveData<Integer> radiusKm = new MutableLiveData<>(0);

    // nearby search measure point - set to GCU for emulator purpose.
    private double originLat = 55.8668;
    private double originLon = -4.2500;
    private String originLabel = "Glasgow Caledonian University";

    // the line above the list, e.g. "___ results for ___ within ___ km of ___"
    private final MutableLiveData<String> statusText = new MutableLiveData<>("");

    // the list the screen shows, either everything or only the search matches
    private final MediatorLiveData<ArrayList<Situation>> visibleSituations = new MediatorLiveData<>();

    // the situations the map is currently showing (all, search results, or one)
    private final MutableLiveData<ArrayList<Situation>> mapSituations = new MutableLiveData<>();

    // schedules the repeating refresh on the main thread
    private final Handler handler = new Handler(Looper.getMainLooper());

    // the repeating refresh - load now, then ask the handler to run this again later
    private final Runnable refreshTask = new Runnable() {
        @Override
        public void run() {
            load();
            handler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    private String clientId = "";
    private String clientKey = "";

    public TrafficViewModel(@NonNull Application application) {
        super(application);
        readCredentials();

        // rebuild the visible list whenever the data or a search changes
        visibleSituations.addSource(situations, list -> applyFilters());
        visibleSituations.addSource(roadQuery, query -> applyFilters());
        visibleSituations.addSource(radiusKm, radius -> applyFilters());

        // load now (auto-update at start-up), then again every REFRESH_INTERVAL_MS
        load();
        handler.postDelayed(refreshTask, REFRESH_INTERVAL_MS);
    }

    //    get methods

    public LiveData<Situation> getSelectedSituation() { return selectedSituation; }

    public LiveData<String> getError() { return error; }

    public LiveData<String> getUpdateNotice() { return updateNotice; }

    public String getRoadQuery() { return roadQuery.getValue(); }

    public LiveData<String> getStatusText() { return statusText; }

    public LiveData<ArrayList<Situation>> getVisibleSituations() { return visibleSituations; }

    public LiveData<ArrayList<Situation>> getMapSituations() { return mapSituations; }

    //    set methods

    public void select(Situation situation) {
        selectedSituation.setValue(situation);
    }

    public void clearSelection() {
        selectedSituation.setValue(null);
    }

    public void setRoadQuery(String query) {
        roadQuery.setValue(query == null ? "" : query.trim());
    }

    public void setRadiusKm(int km) {
        radiusKm.setValue(km);
    }

    public void setOrigin(double lat, double lon, String label) {
        originLat = lat;
        originLon = lon;
        originLabel = label;
        applyFilters();
    }

    public void setMapSituations(ArrayList<Situation> list) {
        mapSituations.setValue(list);
    }

    private void readCredentials() {
        try {
            Application app = getApplication();
            Bundle metaData = app.getPackageManager()
                    .getApplicationInfo(app.getPackageName(), PackageManager.GET_META_DATA).metaData;
            clientId = metaData.getString("org.me.gcu.johnston_zoe_s2336647.CLIENT_ID");
            clientKey = metaData.getString("org.me.gcu.johnston_zoe_s2336647.CLIENT_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("TRAFFIC", "Could not read credentials", e);
        }
    }

    // fetches both feeds. If a download fails it falls back to the last saved copy,
    // and if there is nothing saved it keeps the list already on screen
    public void load() {
        executor.execute(() -> {
            ArrayList<Situation> combined = new ArrayList<>();
            ArrayList<String> failedFeeds = new ArrayList<>();
            boolean[] usedSavedData = {false};
            long[] savedAt = {0};

            ArrayList<Situation> incidents = loadFeed(INCIDENTS_URL, "incident", usedSavedData, savedAt);
            if (incidents == null) {
                failedFeeds.add("incidents");
            } else {
                combined.addAll(incidents);
            }

            ArrayList<Situation> roadworks = loadFeed(ROADWORKS_URL, "roadworks", usedSavedData, savedAt);
            if (roadworks == null) {
                failedFeeds.add("roadworks");
            } else {
                combined.addAll(roadworks);
            }

            //    nothing could be loaded, so keep the data that is already on screen
            if (failedFeeds.size() == 2 && situations.getValue() != null) {
                updateNotice.postValue("Could not update. Showing the data already loaded.");
                error.postValue("Could not load: " + String.join(", ", failedFeeds));
                return;
            }

            situations.postValue(combined);

            if (failedFeeds.size() == 2) {
                updateNotice.postValue("No data yet. Check your internet connection.");
            } else if (usedSavedData[0]) {
                updateNotice.postValue("Offline: showing data saved at " + formatTime(savedAt[0]));
            } else if (!failedFeeds.isEmpty()) {
                updateNotice.postValue("Only some of the data could be updated.");
            } else {
                updateNotice.postValue("Updated at " + formatTime(System.currentTimeMillis()));
            }

            if (!failedFeeds.isEmpty()) {
                error.postValue("Could not load: " + String.join(", ", failedFeeds));
            }
        });
    }

    private ArrayList<Situation> loadFeed(String url, String category, boolean[] usedSavedData, long[] savedAt) {
        try {
            String xml = TrafficDownloader.download(url, clientId, clientKey);
            ArrayList<Situation> list = new TrafficXmlParser().parseSituations(xml, category);

            //    only data that parsed properly is saved
            TrafficCache.save(getApplication(), category, xml);

            return list;
        } catch (IOException | XmlPullParserException e) {
            Log.e("TRAFFIC", "Load failed for " + category, e);
            return loadSavedFeed(category, usedSavedData, savedAt);
        }
    }

    // if download failed, try the last saved copy of this feed
    private ArrayList<Situation> loadSavedFeed(String category, boolean[] usedSavedData, long[] savedAt) {
        String saved = TrafficCache.load(getApplication(), category);
        if (saved == null) {
            return null;
        }

        try {
            ArrayList<Situation> list = new TrafficXmlParser().parseSituations(saved, category);
            usedSavedData[0] = true;

            //    remember the oldest saved time
            long time = TrafficCache.savedTime(getApplication(), category);
            if (savedAt[0] == 0 || time < savedAt[0]) {
                savedAt[0] = time;
            }

            return list;
        } catch (Exception e) {
            Log.e("TRAFFIC", "Saved copy of " + category + " could not be read", e);
            return null;
        }
    }

    private String formatTime(long millis) {
        return new SimpleDateFormat("HH:mm", Locale.UK).format(new Date(millis));
    }

    // applies filters based on searching terminology (within X km of location, matching certain roads etc)
    private void applyFilters() {
        ArrayList<Situation> all = situations.getValue();
        if (all == null) {
            return;
        }

        String query = roadQuery.getValue();
        Integer radius = radiusKm.getValue();
        boolean searchingRoad = query != null && !query.isEmpty();
        boolean searchingNearby = radius != null && radius > 0;

        ArrayList<Situation> result = new ArrayList<>();

        for (Situation s : all) {
            s.setDistanceKm(null);

            if (searchingRoad && !matchesRoad(s, query)) {
                continue;
            }

            if (searchingNearby) {
                Float distance = distanceFromOrigin(s);
                if (distance == null || distance > radius) {
                    continue;
                }
                s.setDistanceKm(distance);
            }

            result.add(s);
        }

        if (searchingNearby) {
            result.sort((a, b) -> Float.compare(a.getDistanceKm(), b.getDistanceKm()));
        }

        String text = result.size() + (result.size() == 1 ? " result" : " results");
        if (searchingRoad) {
            text += " for road " + query.toUpperCase();
        }
        if (searchingNearby) {
            text += " within " + radius + " km of " + originLabel;
        }
        statusText.setValue(text);

        visibleSituations.setValue(result);
    }

    // ensure strict match - roads can have very similar names.
    private boolean matchesRoad(Situation s, String query) {
        String wanted = query.replace(" ", "");

        //    first choice: the road names from the location data
        for (Location l : s.getLocations()) {
            for (String road : l.getRoadNames()) {
                if (road.replace(" ", "").equalsIgnoreCase(wanted)) {
                    return true;
                }
            }
        }

        //    fallback: the road written in the description, not as part of a longer road number
        if (s.getDescription() != null) {
            Pattern pattern = Pattern.compile("(?<![A-Za-z0-9])" + Pattern.quote(wanted) + "(?![A-Za-z0-9])", Pattern.CASE_INSENSITIVE);
            return pattern.matcher(s.getDescription()).find();
        }

        return false;
    }

    // distance in km to the closest of the situation's points
    private Float distanceFromOrigin(Situation s) {
        Float nearest = null;
        float[] results = new float[1];

        for (Location l : s.getLocations()) {
            android.location.Location.distanceBetween(originLat, originLon, l.getLatitude(), l.getLongitude(), results);
            float km = results[0] / 1000f;
            if (nearest == null || km < nearest) {
                nearest = km;
            }
        }

        return nearest;
    }

    @Override
    protected void onCleared() {
        //    stop the repeating refresh, otherwise it would keep running after the ViewModel is gone
        handler.removeCallbacks(refreshTask);
        executor.shutdown();
    }
}