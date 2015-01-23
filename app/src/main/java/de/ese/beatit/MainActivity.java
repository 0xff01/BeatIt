package de.ese.beatit;

import java.util.Timer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import de.ese.beatit.beatanalyzer.BeatAnalyzerService;
import de.ese.beatit.mp3.MP3Player;
import de.ese.beatit.mp3.PlayerView;
import de.ese.beatit.pulsereader.SetupBluetooth;
import de.ese.beatit.notification.GoogleWear;
import de.ese.beatit.notification.WearActionReceiver;

public class MainActivity extends Activity {
	
	private PulseControl pulseCtrl = null;
	//private ScheduledExecutorService schedExec = null;
    private Timer pulseCtrlTimer = null;

    // Notification setup

    private int notification_id = 1;
    private final String NOTIFICATION_ID = "notification_id";

    private GoogleWear wear = null;
    int numMessages = 0;

    private NotificationCompat.Builder notification_builder;
    private NotificationManagerCompat notification_manager;

    /** the service which provides the track database **/
    private BeatAnalyzerService beatAnalayzerService = null;
	private boolean isBound = false;
    
	private ServiceConnection serviceConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	    	beatAnalayzerService = ((BeatAnalyzerService.BeatAnalyzerServiceBinder)service).getService();

	        // create player
	        mp3Player = new MP3Player(getApplicationContext());
	        beatAnalayzerService.database().setListener(mp3Player);
	        
	        playerView = new PlayerView(findViewById(R.id.player_view), mp3Player);
	        
	        // init buttons
	        final ImageButton playPause = (ImageButton)(findViewById(R.id.button_play_pause));
	        playPause.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
                    processPlayPause();
				}
			});
	        ImageButton skip = (ImageButton)(findViewById(R.id.button_skip));
	        skip.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
                    processSkip();
				}
			});
	        
	        // Tell the user about this for our demo.
	        Toast.makeText(getApplicationContext(), "Connected",
	                Toast.LENGTH_SHORT).show();
	        
	        pulseCtrl.setPlayer(mp3Player);
	        pulseCtrlTimer.scheduleAtFixedRate(pulseCtrl, 0, 1000);

            mp3Player.addMp3PlayerListener(wear);
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
	    	beatAnalayzerService = null;
	        Toast.makeText(getApplicationContext(), "Disconnected",
	        		Toast.LENGTH_SHORT).show();
	    }
	};


    /** player **/
	private MP3Player mp3Player = null;
	private PlayerView playerView = null;
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init pulseCtrl
        pulseCtrl = new PulseControl();
        
        // init scheduled executor service
        pulseCtrlTimer = new Timer();

        // connect service
        doBindService();

        wear = new GoogleWear(new GoogleWear.WearListener() {

            @Override
            public void onVolumeUp() {

            }

            @Override
            public void onVolumeDown() {

            }

            @Override
            public void skip() {
                processSkip();
            }

            @Override
            public void onPlayPressed() {
                processPlayPause();
            }
        });

        wear.init(this);
    }

	void doBindService() {
	    // Establish a connection with the service.  We use an explicit
	    // class name because we want a specific service implementation that
	    // we know will be running in our own process (and thus won't be
	    // supporting component replacement by other applications).
	    bindService(new Intent(getApplicationContext(),
	            BeatAnalyzerService.class), serviceConnection, Context.BIND_AUTO_CREATE);
	    isBound = true;
	}

	void doUnbindService() {
	    if (isBound) {
	        // Detach our existing connection.
	        unbindService(serviceConnection);
	        isBound = false;
	    }
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.connect_pulse_reader) {
        	Intent connectDeviceIntent = new Intent(MainActivity.this, SetupBluetooth.class);
        	MainActivity.this.startActivity(connectDeviceIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
        pulseCtrlTimer.cancel();
    }

    public void processPlayPause(){
        final ImageButton playPause = (ImageButton)(findViewById(R.id.button_play_pause));
        if(mp3Player.isPlaying()){
            mp3Player.pause();
            playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
        } else {
            mp3Player.play();
            playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
        }
    }


    private void processSkip() {
        if(mp3Player.isPlaying()){
            mp3Player.skip();
        }
    }
}
