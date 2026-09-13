package de.danberg.wachwerk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONObject;

public class TodoReminderReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent source) {
        MainActivity.createNotificationChannels(context);
        String todoId = source.getStringExtra("todoId");
        String text = source.getStringExtra("text");
        boolean daily = source.getBooleanExtra("daily", false);
        if (daily) {
            text = openTodoSummary(context);
            if (text == null) return;
            todoId = "daily";
            TodoReminderScheduler.restore(context);
        }
        if (todoId == null) todoId = "todo";
        if (text == null || text.isBlank()) text = "Du hast noch eine offene Aufgabe.";
        Intent open = new Intent(context, MainActivity.class)
            .putExtra("openTodos", true)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(context, 53000 + Math.abs(todoId.hashCode() % 10000), open,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new Notification.Builder(context, MainActivity.TODO_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(Color.rgb(155, 245, 177))
            .setContentTitle(daily ? "MACH · Offene To-dos" : "MACH · Aufgabe fällig")
            .setContentText(text)
            .setStyle(new Notification.BigTextStyle().bigText(text))
            .setCategory(Notification.CATEGORY_REMINDER)
            .setPriority(Notification.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(content)
            .build();
        context.getSystemService(NotificationManager.class).notify(54000 + Math.abs(todoId.hashCode() % 10000), notification);
    }

    private static String openTodoSummary(Context context) {
        try {
            String json = context.getSharedPreferences("wachwerk_native", Context.MODE_PRIVATE).getString("todos_json", "[]");
            JSONArray todos = new JSONArray(json);
            String first = ""; int open = 0;
            for (int i = 0; i < todos.length(); i++) {
                JSONObject todo = todos.optJSONObject(i);
                if (todo == null || todo.optBoolean("done", false)) continue;
                open++; if (first.isEmpty()) first = todo.optString("text", "Offene Aufgabe");
            }
            if (open == 0) return null;
            return open == 1 ? first : first + " · noch " + (open - 1) + " weitere offen";
        } catch (Exception ignored) { return null; }
    }
}
