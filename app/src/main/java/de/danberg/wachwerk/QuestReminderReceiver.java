package de.danberg.wachwerk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;

public class QuestReminderReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("wachwerk_quest_reminder", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("enabled", false) || prefs.getBoolean("done", false)) return;
        MainActivity.createNotificationChannels(context);
        Intent open = new Intent(context, MainActivity.class).putExtra("openQuests", true)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(context, 67002, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        String title = prefs.getString("title", "Deine Tagesquest wartet");
        String detail = prefs.getString("detail", "Nimm dir kurz Zeit für deine Tagesquest.");
        Notification notification = new Notification.Builder(context, MainActivity.QUEST_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification).setColor(Color.rgb(255, 211, 71))
            .setContentTitle("MACH · Tagesquest") .setContentText(title)
            .setStyle(new Notification.BigTextStyle().bigText(title + "\n" + detail))
            .setCategory(Notification.CATEGORY_REMINDER).setAutoCancel(true).setContentIntent(content).build();
        context.getSystemService(NotificationManager.class).notify(67003, notification);
        QuestReminderScheduler.restore(context);
    }
}
