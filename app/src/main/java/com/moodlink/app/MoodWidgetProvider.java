package com.moodlink.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver.PendingResult;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MoodWidgetProvider extends AppWidgetProvider {
    static final String ACTION_SEND_MOOD = "com.moodlink.app.SEND_MOOD";
    static final String EXTRA_MOOD = "mood";
    static final String EXTRA_LABEL = "label";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final int[][] MOODS = {
            {R.id.mood_calm, 101}, {R.id.mood_happy, 102}, {R.id.mood_tired, 103},
            {R.id.mood_irritable, 104}, {R.id.mood_sad, 105}, {R.id.mood_company, 106}
    };
    private static final String[] KEYS = {"calm", "happy", "tired", "irritable", "sad", "company"};
    private static final String[] LABELS = {"平静", "开心", "疲惫", "烦躁", "难过", "想被陪"};

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        updateWidgets(context, manager, appWidgetIds);
    }

    static void updateWidgets(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.mood_widget);
            views.setTextViewText(R.id.widget_status, MoodStore.lastStatus(context));

            for (int i = 0; i < MOODS.length; i++) {
                Intent intent = new Intent(context, MoodWidgetProvider.class);
                intent.setAction(ACTION_SEND_MOOD);
                intent.putExtra(EXTRA_MOOD, KEYS[i]);
                intent.putExtra(EXTRA_LABEL, LABELS[i]);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        MOODS[i][1],
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                views.setOnClickPendingIntent(MOODS[i][0], pendingIntent);
            }

            Intent setup = new Intent(context, MainActivity.class);
            PendingIntent setupIntent = PendingIntent.getActivity(
                    context, 200, setup, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_status, setupIntent);
            manager.updateAppWidget(id, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (!ACTION_SEND_MOOD.equals(intent.getAction())) return;

        if (!MoodStore.isConfigured(context)) {
            Toast.makeText(context, "先打开一次“Mood”完成配对", Toast.LENGTH_LONG).show();
            Intent setup = new Intent(context, MainActivity.class);
            setup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(setup);
            return;
        }

        String mood = intent.getStringExtra(EXTRA_MOOD);
        String label = intent.getStringExtra(EXTRA_LABEL);
        if (mood == null || label == null) return;

        MoodStore.setLastStatus(context, "正在同步“" + label + "”…");
        refresh(context);
        PendingResult pendingResult = goAsync();
        EXECUTOR.execute(() -> {
            try {
                sendMood(context.getApplicationContext(), mood, label);
            } finally {
                pendingResult.finish();
            }
        });
    }

    private static void sendMood(Context context, String mood, String label) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(MoodStore.serverUrl(context) + "/api/device/mood");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(12000);
            connection.setReadTimeout(12000);
            connection.setDoOutput(true);
            connection.setRequestProperty("OAI-Sites-Authorization", "Bearer " + MoodStore.token(context));
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            byte[] payload = ("{\"mood\":\"" + mood + "\",\"intensity\":5,\"source\":\"android-widget\"}")
                    .getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(payload.length);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(payload);
            }

            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) throw new IllegalStateException("HTTP " + code);

            String time = new SimpleDateFormat("HH:mm", Locale.CHINA).format(new Date());
            MoodStore.setLastStatus(context, label + " · 已同步 " + time);
            showResult(context, "收到，已经传过去了");
        } catch (Exception error) {
            MoodStore.setLastStatus(context, "没传过去 · 点这里检查配对");
            showResult(context, "这次没传过去，点右上角状态检查一下");
        } finally {
            if (connection != null) connection.disconnect();
            refresh(context);
        }
    }

    private static void showResult(Context context, String message) {
        new Handler(Looper.getMainLooper()).post(
                () -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

    private static void refresh(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, MoodWidgetProvider.class));
        updateWidgets(context, manager, ids);
    }
}
