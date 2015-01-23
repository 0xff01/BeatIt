package de.ese.beatit.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WearActionReceiver extends BroadcastReceiver {

    public static final String NOTIFICATION_ID_STRING = "NotificationId";
    public static final String WEAR_ACTION = "WearAction";

    public static final int PLAY_PAUSE = 1;
    public static final int VOL_UP = 2;
    public static final int VOL_DOWN = 3;
    public static final int SKIP = 4;


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null) {

            int notificationId = intent.getIntExtra(NOTIFICATION_ID_STRING, 0);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(notificationId);

            int action = intent.getIntExtra(WEAR_ACTION, 0);

            switch (action) {
                case PLAY_PAUSE:
                    GoogleWear.instance.onPlayPausePressed();
                    break;
                case VOL_UP:
                    GoogleWear.instance.onVolUpPressed();
                    break;
                case VOL_DOWN:
                    GoogleWear.instance.onVolDownPressed();
                    break;
                case SKIP:
                    GoogleWear.instance.onSkipPressed();
                    break;
            }
        }
    }
}