package de.danberg.wachwerk;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

public final class QuestReminderScheduler {
    private static final String PREFS = "wachwerk_quest_reminder";
    private static final int REQUEST_CODE = 67001;

    private QuestReminderScheduler() {}

    public static void sync(Context context, boolean enabled, String time, String title, String detail, String date, boolean done) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("enabled", enabled).putString("time", time == null ? "10:00" : time)
            .putString("title", title == null ? "Deine Tagesquest wartet" : title)
            .putString("detail", detail == null ? "" : detail).putString("date", date == null ? "" : date)
            .putBoolean("done", done).apply();
        schedule(context, prefs);
    }

    public static void restore(Context context) { schedule(context, context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)); }

    static void schedule(Context context, SharedPreferences prefs) {
        cancel(context);
        if (!prefs.getBoolean("enabled", false)) return;
        String time = prefs.getString("time", "10:00");
        if (time == null || !time.matches("(?:[01]\\d|2[0-3]):[0-5]\\d")) time = "10:00";
        String[] parts = time.split(":");
        Calendar at = Calendar.getInstance();
        at.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0])); at.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
        at.set(Calendar.SECOND, 0); at.set(Calendar.MILLISECOND, 0);
        if (prefs.getBoolean("done", false)) at.add(Calendar.DAY_OF_YEAR, 1);
        else if (at.getTimeInMillis() <= System.currentTimeMillis() + 1_500L) at.add(Calendar.DAY_OF_YEAR, 1);
        Intent intent = new Intent(context, QuestReminderReceiver.class).setAction("de.danberg.wachwerk.QUEST_REMINDER");
        PendingIntent pending = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager manager = context.getSystemService(AlarmManager.class);
        try { manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.getTimeInMillis(), pending); }
        catch (SecurityException denied) { manager.set(AlarmManager.RTC_WAKEUP, at.getTimeInMillis(), pending); }
    }

    private static void cancel(Context context) {
        AlarmManager manager = context.getSystemService(AlarmManager.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, REQUEST_CODE,
            new Intent(context, QuestReminderReceiver.class).setAction("de.danberg.wachwerk.QUEST_REMINDER"), PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pending != null) manager.cancel(pending);
    }
}
