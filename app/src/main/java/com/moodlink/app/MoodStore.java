package com.moodlink.app;

import android.content.Context;
import android.content.SharedPreferences;

final class MoodStore {
    private static final String DEFAULT_SERVER_URL = "https://mood-link.yy675908.chatgpt.site";
    private static final String PREFS = "mood_link";
    private static final String SERVER_URL = "server_url";
    private static final String DEVICE_TOKEN = "device_token";
    private static final String LAST_STATUS = "last_status";

    private MoodStore() {}

    static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static String serverUrl(Context context) {
        return prefs(context).getString(SERVER_URL, DEFAULT_SERVER_URL);
    }

    static String token(Context context) {
        return prefs(context).getString(DEVICE_TOKEN, "");
    }

    static boolean isConfigured(Context context) {
        return !serverUrl(context).trim().isEmpty() && !token(context).trim().isEmpty();
    }

    static void saveConnection(Context context, String serverUrl, String token) {
        String normalized = serverUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        prefs(context).edit()
                .putString(SERVER_URL, normalized)
                .putString(DEVICE_TOKEN, token.trim())
                .apply();
    }

    static String lastStatus(Context context) {
        return prefs(context).getString(LAST_STATUS, "点一下就好");
    }

    static void setLastStatus(Context context, String value) {
        prefs(context).edit().putString(LAST_STATUS, value).apply();
    }
}
