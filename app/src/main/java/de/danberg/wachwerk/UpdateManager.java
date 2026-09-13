package de.danberg.wachwerk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class UpdateManager {
    private static final String API = "https://api.github.com/repos/Kingdaniel4747/Mach./releases/latest";
    private static final String PREFS = "mach_updates";
    private UpdateManager() {}

    public static void check(Activity activity) {
        new Thread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(API).openConnection();
                connection.setConnectTimeout(8_000); connection.setReadTimeout(8_000);
                connection.setRequestProperty("Accept", "application/vnd.github+json");
                connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
                connection.setRequestProperty("User-Agent", "MACH-Android-Updater");
                if (connection.getResponseCode() != 200) { connection.disconnect(); return; }
                StringBuilder json = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line; while ((line = reader.readLine()) != null) json.append(line);
                } finally { connection.disconnect(); }
                JSONObject release = new JSONObject(json.toString());
                String tag = release.optString("tag_name", "").replaceFirst("^[vV]", "");
                String current = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
                if (tag.isEmpty() || compareVersions(tag, current) <= 0) return;
                String apk = findApk(release.optJSONArray("assets"));
                if (apk.isEmpty()) return;
                SharedPreferences prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                if (tag.equals(prefs.getString("dismissed", ""))) return;
                activity.runOnUiThread(() -> showPrompt(activity, tag, apk));
            } catch (Exception ignored) {}
        }, "mach-update-check").start();
    }

    private static void showPrompt(Activity activity, String tag, String apk) {
        if (activity.isFinishing()) return;
        new AlertDialog.Builder(activity).setTitle("MACH-Update verfügbar")
            .setMessage("Version " + tag + " ist bereit. Jetzt herunterladen und anschließend installieren?")
            .setNegativeButton("Später", (dialog, which) -> activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("dismissed", tag).apply())
            .setPositiveButton("Jetzt aktualisieren", (dialog, which) -> download(activity, tag, apk)).show();
    }

    private static void download(Activity activity, String tag, String url) {
        try {
            DownloadManager manager = activity.getSystemService(DownloadManager.class);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url)).setTitle("MACH " + tag)
                .setDescription("Update wird heruntergeladen").setMimeType("application/vnd.android.package-archive")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(activity, Environment.DIRECTORY_DOWNLOADS, "MACH-" + tag + ".apk");
            long id = manager.enqueue(request);
            activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putLong("download", id).apply();
            Toast.makeText(activity, "Update wird heruntergeladen", Toast.LENGTH_SHORT).show();
            new Thread(() -> waitForDownload(activity, id), "mach-update-download").start();
        } catch (Exception error) { Toast.makeText(activity, "Update konnte nicht gestartet werden", Toast.LENGTH_LONG).show(); }
    }

    private static void waitForDownload(Activity activity, long id) {
        for (int attempt = 0; attempt < 600; attempt++) {
            try { Thread.sleep(1_000L); } catch (InterruptedException interrupted) { return; }
            int status = status(activity, id);
            if (status == DownloadManager.STATUS_SUCCESSFUL) { activity.runOnUiThread(() -> install(activity, id)); return; }
            if (status == DownloadManager.STATUS_FAILED) { activity.runOnUiThread(() -> Toast.makeText(activity, "Update-Download fehlgeschlagen", Toast.LENGTH_LONG).show()); return; }
        }
    }

    public static void resumeInstall(Activity activity) {
        long id = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong("download", -1L);
        if (id > 0 && status(activity, id) == DownloadManager.STATUS_SUCCESSFUL) install(activity, id);
    }

    private static int status(Context context, long id) {
        try (Cursor cursor = context.getSystemService(DownloadManager.class).query(new DownloadManager.Query().setFilterById(id))) {
            return cursor != null && cursor.moveToFirst() ? cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)) : -1;
        } catch (Exception ignored) { return -1; }
    }

    private static void install(Activity activity, long id) {
        try {
            if (Build.VERSION.SDK_INT >= 26 && !activity.getPackageManager().canRequestPackageInstalls()) {
                activity.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + activity.getPackageName())));
                Toast.makeText(activity, "Erlaube MACH einmal, dieses Update zu installieren", Toast.LENGTH_LONG).show(); return;
            }
            Uri apk = activity.getSystemService(DownloadManager.class).getUriForDownloadedFile(id);
            if (apk == null) return;
            activity.startActivity(new Intent(Intent.ACTION_INSTALL_PACKAGE).setData(apk)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK));
            activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove("download").apply();
        } catch (Exception error) { Toast.makeText(activity, "Öffne den fertigen Download, um das Update zu installieren", Toast.LENGTH_LONG).show(); }
    }

    private static String findApk(JSONArray assets) {
        if (assets == null) return "";
        for (int i = 0; i < assets.length(); i++) { JSONObject asset = assets.optJSONObject(i); if (asset != null && asset.optString("name", "").toLowerCase(java.util.Locale.ROOT).endsWith(".apk")) return asset.optString("browser_download_url", ""); }
        return "";
    }

    static int compareVersions(String left, String right) {
        String[] a = numericVersion(left).split("[^0-9]+");
        String[] b = numericVersion(right).split("[^0-9]+");
        for (int i = 0; i < Math.max(a.length, b.length); i++) { int av = number(a, i), bv = number(b, i); if (av != bv) return Integer.compare(av, bv); }
        return 0;
    }
    private static String numericVersion(String value) { return value == null ? "" : value.replaceFirst("^[^0-9]+", ""); }
    private static int number(String[] parts, int index) { try { return index < parts.length && !parts[index].isEmpty() ? Integer.parseInt(parts[index]) : 0; } catch (Exception ignored) { return 0; } }
}
