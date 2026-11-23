package org.me.gcu.johnston_zoe_s2336647.model;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

// TraficCache class - saves most recent downloaded traffic data for use offline
public class TrafficCache {

//    save different category (incident/roadworks) as seperate feeds due to seperate content
    private static File fileFor(Context context, String category) {
        return new File(context.getFilesDir(), "feed_" + category + ".xml");
    }

    //    save download
    public static void save(Context context, String category, String xml) {
        try {
            Files.write(fileFor(context, category).toPath(), xml.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.e("CACHE", "Could not save " + category, e);
        }
    }

    //    loads most recent download - if none are found, return null to avoid crashing
    public static String load(Context context, String category) {
        File file = fileFor(context, category);
        if (!file.exists()) {
            return null;
        }

        try {
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.e("CACHE", "Could not read " + category, e);
            return null;
        }
    }

    //    when the saved copy was written, in milliseconds
    public static long savedTime(Context context, String category) {
        return fileFor(context, category).lastModified();
    }
}