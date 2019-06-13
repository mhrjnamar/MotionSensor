package com.mhrjnamar.motionsensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("")
                .setContentText("")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        String type = intent.getExtras().getString("Type");
        notificationBuilder.setContentText(type);
        notificationManager.notify(1, notificationBuilder.build());

//        if (ActivityTransitionResult.hasResult(intent)) {
//            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
//            for (ActivityTransitionEvent event : result.getTransitionEvents()) {
//                // chronological sequence of events....
//            }
//        }
    }
}
