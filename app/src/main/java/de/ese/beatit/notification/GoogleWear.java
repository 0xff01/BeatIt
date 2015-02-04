package de.ese.beatit.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;

import de.ese.beatit.R;
import de.ese.beatit.mp3.MP3PlayerListener;
import de.ese.beatit.mp3.Track;

public class GoogleWear implements MP3PlayerListener {

    public static GoogleWear instance;

    private final int TRACK_INFO_NOTIFICATION_ID = 1;
    private final int PLAYER_INFO_NOTIFICATION_ID = 2;

    private Context context;

    private NotificationCompat.Action volUpAction;
    private NotificationCompat.Action volDownAction;
    private NotificationCompat.Action skipAction;

    private PendingIntent playPauseIntent;
    private PendingIntent volUpPIntent;
    private PendingIntent volDownPIntent;
    private PendingIntent skipPIntent;

    private NotificationCompat.Builder trackBuilder, playerBuilder;

    private NotificationManagerCompat notification_manager;

    @Override
    public void onTrackChanged(Track track) {
        setTrackDescription(track.getName(), track.getArtist());
    }

    @Override
    public void onPlaybackTimeChanged(double time) {

    }

    @Override
    public void onPlay() {
        setPlaying(true);
    }

    @Override
    public void onPause() {
        setPlaying(false);
    }

    public interface WearListener {

        public void onVolumeUp();
        public void onVolumeDown();
        public void skip();
        public void onPlayPressed();
    }

    private WearListener listener;

    public GoogleWear(WearListener l){
        listener = l;
        instance = this;
    }

    public void init(Context context){

        Log.v("wear", "init");

        this.context = context;

        // notification manager
        notification_manager = NotificationManagerCompat.from(context);

        // init track info page
        trackBuilder = new NotificationCompat.Builder(context);
        trackBuilder.setSmallIcon(R.drawable.note);
        trackBuilder.setContentTitle("Not playing");
        trackBuilder.setContentText("--");
        trackBuilder.setDefaults(Notification.DEFAULT_ALL);
        trackBuilder.setAutoCancel(true);

        Intent actionIntent = new Intent(context, WearActionReceiver.class);
        actionIntent.putExtra(WearActionReceiver.NOTIFICATION_ID_STRING, TRACK_INFO_NOTIFICATION_ID);
        actionIntent.putExtra(WearActionReceiver.WEAR_ACTION, WearActionReceiver.PLAY_PAUSE);

        playPauseIntent = PendingIntent.getBroadcast(context, 1, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.drawable.pause45,
                        "play-pause", playPauseIntent)
                        .build();

        Intent volUpIntent = new Intent(context, WearActionReceiver.class);
        volUpIntent.putExtra(WearActionReceiver.NOTIFICATION_ID_STRING, TRACK_INFO_NOTIFICATION_ID);
        volUpIntent.putExtra(WearActionReceiver.WEAR_ACTION, WearActionReceiver.VOL_UP);

        volUpPIntent = PendingIntent.getBroadcast(context, 2, volUpIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        volUpAction =
                new NotificationCompat.Action.Builder(R.drawable.round69,
                        "Vol-Up", volUpPIntent)
                        .build();

        Intent volDownIntent = new Intent(context, WearActionReceiver.class);
        volDownIntent.putExtra(WearActionReceiver.NOTIFICATION_ID_STRING, TRACK_INFO_NOTIFICATION_ID);
        volDownIntent.putExtra(WearActionReceiver.WEAR_ACTION, WearActionReceiver.VOL_DOWN);

        volDownPIntent = PendingIntent.getBroadcast(context, 3, volDownIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        volDownAction =
                new NotificationCompat.Action.Builder(R.drawable.rounded61,
                        "Vol-Down", volDownPIntent)
                        .build();

        Intent skipIntent = new Intent(context, WearActionReceiver.class);
        skipIntent.putExtra(WearActionReceiver.NOTIFICATION_ID_STRING, TRACK_INFO_NOTIFICATION_ID);
        skipIntent.putExtra(WearActionReceiver.WEAR_ACTION, WearActionReceiver.SKIP);

        skipPIntent = PendingIntent.getBroadcast(context, 4, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        skipAction =
                new NotificationCompat.Action.Builder(R.drawable.fast47,
                        "Skip-Track", skipPIntent)
                        .build();

        Bitmap Melody = BitmapFactory.decodeResource(context.getResources(), R.drawable.music_background);

        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender()
                        .setHintHideIcon(true)
                        .setBackground(Melody)
                        .addAction(action)
                        .addAction(skipAction)
                        .addAction(volUpAction)
                        .addAction(volDownAction)
                        .setContentAction(0);
        trackBuilder.extend(wearableExtender);

        notification_manager.notify(TRACK_INFO_NOTIFICATION_ID, trackBuilder.build());
    }

    public void setTrackDescription(String title, String artist){
        trackBuilder.setContentTitle(title);
        trackBuilder.setContentText(artist);
        notification_manager.notify(TRACK_INFO_NOTIFICATION_ID, trackBuilder.build());
    }

    public void setPlaying(boolean playing){

        if(playing){

            // show pause button
            NotificationCompat.Action action =
                    new NotificationCompat.Action.Builder(R.drawable.pause45,
                            "play-pause", playPauseIntent)
                            .build();

            Bitmap Melody = BitmapFactory.decodeResource(context.getResources(), R.drawable.music_background);

            NotificationCompat.WearableExtender wearableExtender =
                    new NotificationCompat.WearableExtender()
                            .setHintHideIcon(true)
                            .setBackground(Melody)
                            .addAction(action)
                            .addAction(skipAction)
                            .addAction(volUpAction)
                            .addAction(volDownAction)
                            .setContentAction(0);

            trackBuilder.extend(wearableExtender);
            notification_manager.notify(TRACK_INFO_NOTIFICATION_ID, trackBuilder.build());

            Log.v("wear","now playing");

        } else {

            // show play button
            NotificationCompat.Action action =
                    new NotificationCompat.Action.Builder(R.drawable.right246,
                            "play-pause", playPauseIntent)
                            .build();

            Bitmap Melody = BitmapFactory.decodeResource(context.getResources(), R.drawable.music_background);

            NotificationCompat.WearableExtender wearableExtender =
                    new NotificationCompat.WearableExtender()
                            .setHintHideIcon(true)
                            .setBackground(Melody)
                            .addAction(action)
                            .addAction(skipAction)
                            .addAction(volUpAction)
                            .addAction(volDownAction)
                            .setContentAction(0);

            trackBuilder.extend(wearableExtender);
            notification_manager.notify(TRACK_INFO_NOTIFICATION_ID, trackBuilder.build());

            Log.v("wear","now pausing");

        }
    }
    public void onPlayPausePressed() {
        listener.onPlayPressed();
    }

    public void onVolUpPressed(){
        listener.onVolumeUp();
        notification_manager.notify(TRACK_INFO_NOTIFICATION_ID, trackBuilder.build());
    }

    public void onVolDownPressed(){
        listener.onVolumeDown();
        notification_manager.notify(TRACK_INFO_NOTIFICATION_ID, trackBuilder.build());
    }

    public void onSkipPressed(){
        listener.skip();
    }
}
