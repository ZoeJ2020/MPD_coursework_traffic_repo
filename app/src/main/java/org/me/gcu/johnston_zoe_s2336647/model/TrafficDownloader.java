package org.me.gcu.johnston_zoe_s2336647.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class TrafficDownloader {

    // stop trying after 10 seconds, so a lost connection fails
    private static final int TIMEOUT_MS = 10000;

    // download XML from specific traffic link - return errors if no XML found or connection fails
    public static String download(String urlString, String clientId, String clientKey) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestProperty("Authorization",
                Base64.getEncoder().encodeToString((clientId + ":" + clientKey).getBytes()));

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            connection.disconnect();
            throw new IOException("HTTP " + responseCode);
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            String text = result.toString();
            int start = text.indexOf("<?");
            if (start < 0) {
                throw new IOException("No XML found in response");
            }
            return text.substring(start);
        } finally {
            connection.disconnect();
        }
    }
}