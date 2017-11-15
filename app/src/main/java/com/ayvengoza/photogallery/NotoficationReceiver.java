package com.ayvengoza.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by ayven on 15.11.2017.
 */

public class NotoficationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotoficationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "receive result: " + getResultCode());
        if(getResultCode() != Activity.RESULT_OK){
            return;
        }

        int requestCode = intent.getIntExtra(PollService.REQUEST_CODE, 0);
        Notification notification = (Notification) intent.getParcelableExtra(PollService.NOTIFICATION);

        NotificationManagerCompat notificationManage =
                NotificationManagerCompat.from(context);
        notificationManage.notify(requestCode, notification);
    }
}
