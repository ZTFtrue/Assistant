package com.ztftrue.tool;

import static android.app.NotificationManager.IMPORTANCE_DEFAULT;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.net.Uri;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

class CreateNotification {
    String CHANNEL_ID = "通知";
    int NOTIFICATION_ID = 1;

    public void createForegroundNotification(Context context, String title, String subTitle) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.cut_screen)
                .setContentTitle(title)
                .setWhen(System.currentTimeMillis())
                .setSubText(subTitle)
                .setSilent(true)
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+ "://" +context.getPackageName()+"/"+R.raw.silent))
//                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        createNotificationChannel(context);
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
        }
    }

    public void cancelNotification(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(NOTIFICATION_ID);
        notificationManagerCompat.cancelAll();
    }

//    public void updateNotification(context: Context, title: String, subTitle: String) {
//        with(NotificationManagerCompat.from(context)) {
//            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
//                .setSmallIcon(R.drawable.cut_screen)
//                .setContentTitle(subTitle)
//                .setSubText(title)
//                .setPriority(NotificationCompat.PRIORITY_LOW)
//            if (ActivityCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.POST_NOTIFICATIONS
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                return
//            }
//            notify(NOTIFICATION_ID, builder.build())
//        }
//    }

    private void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        String name = "Screen captured";
        String descriptionText = "Screen captured";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, IMPORTANCE_DEFAULT);
        channel.setDescription(descriptionText);
        var audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        channel.setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + context.getPackageName() + "/" + R.raw.silent), audioAttributes);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

}