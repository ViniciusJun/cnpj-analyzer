package com.novasemp.cnpjmobile.util;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.UUID;

public class SessionManager {
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "CNPJAnalyzerPrefs";
    private static final String KEY_SESSION_ID = "session_id";

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getSessionId() {
        String sessionId = prefs.getString(KEY_SESSION_ID, null);
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
            prefs.edit().putString(KEY_SESSION_ID, sessionId).apply();
        }
        return sessionId;
    }
}
