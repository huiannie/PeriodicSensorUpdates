package com.itp226.periodicsensorupdates;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

// This BroadcastReceiver will send user a notification
// to alert the user that a certain sensor condition is met.
public class AlertReceiver extends BroadcastReceiver {
    private static String myId = AlertReceiver.class.getSimpleName();
    private static int ReceiverId = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(myId, "Alert broadcast received");

        // TODO: Configure the alertText to something suitable
        String alertText = "Sensor exceeds threshold";
        // Send a notification to alert the user
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification;
        Notification.Builder builder= new Notification.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(alertText)
                        .setAutoCancel(true);
        if (Build.VERSION.SDK_INT>=16) {
            notification = builder.build();
        }
        else {
            notification = builder.getNotification();
        }
        manager.notify(ReceiverId, notification);
    }
}