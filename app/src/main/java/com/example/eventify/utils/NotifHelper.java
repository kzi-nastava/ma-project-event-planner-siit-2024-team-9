package com.example.eventify.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.eventify.R;
import com.example.eventify.activities.MainActivity;

public class NotifHelper {
    public static final String CHANNEL_ID = "eventify_notifications";

    public static void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Eventify", NotificationManager.IMPORTANCE_DEFAULT);
            ch.setDescription("Real-time obaveštenja");
            ctx.getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    public static void show(Context ctx, String title, String message, @Nullable String deepLink) {
        // 0) Da li je korisnik globalno isključio notifikacije za app?
        if (!NotificationManagerCompat.from(ctx).areNotificationsEnabled()) {
            // Korisnik je isključio notifikacije u settings → elegantno odustani
            return;
        }

        // 1) Runtime permission za Tiramisu+ (API 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            int granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS);
            if (granted != PackageManager.PERMISSION_GRANTED) {
                // Nema dozvolu → ne šalji; UI neka zatraži dozvolu ranije (vidi dole)
                return;
            }
        }

        ensureChannel(ctx);

        Intent intent = (deepLink != null && !deepLink.isEmpty())
                ? new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
                : new Intent(ctx, MainActivity.class);

        PendingIntent pi = PendingIntent.getActivity(
                ctx, (int) System.currentTimeMillis(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(ctx).notify((int) System.currentTimeMillis(), b.build());
    }
}

