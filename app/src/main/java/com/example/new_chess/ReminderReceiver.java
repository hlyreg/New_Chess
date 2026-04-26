package com.example.new_chess;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, "chess_channel")
//                        .setSmallIcon(R.drawable.ic_chess) //TODO:!!!!!!!!!!!!!!icon
                        .setContentTitle("Play Chess ♟️")
                        .setContentText("Your daily match is waiting!")
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        if(context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context)
                    .notify(1002, builder.build());
        }
    }
}