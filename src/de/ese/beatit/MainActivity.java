package de.ese.beatit;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.ese.beatit.pulsereader.SetupBluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity {
	
	private PulseControl pulseCtrl = null;
	private ScheduledExecutorService schedExec = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // init pulseCtrl
        pulseCtrl = new PulseControl();
        
        // init scheduled executor service
        schedExec = Executors.newSingleThreadScheduledExecutor();
        
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
    
    /** Called when the user clicks the Start button */
    public void startCtrl(View view) {
    	
    	// start exec
        schedExec.scheduleAtFixedRate(pulseCtrl, 0, 1, TimeUnit.SECONDS);
        // dissable start button
        Button btnStart = (Button) findViewById(R.id.start_button);
        btnStart.setEnabled(false);
        // enable stop button
        Button btnStop = (Button) findViewById(R.id.stop_button);
        btnStop.setEnabled(true);
    }
    
    /** Called when the user clicks the Stop button */
    public void stopCtrl(View view) {
    	
    	// stop exec
        //try {
        	schedExec.shutdownNow();
			//schedExec.awaitTermination(100, TimeUnit.MILLISECONDS);
		//} catch (InterruptedException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
        // dissable stop button
        Button btnStop = (Button) findViewById(R.id.stop_button);
        btnStop.setEnabled(false);
        // enable stop button
        Button btnStart = (Button) findViewById(R.id.start_button);
        btnStart.setEnabled(true);
    }
}
