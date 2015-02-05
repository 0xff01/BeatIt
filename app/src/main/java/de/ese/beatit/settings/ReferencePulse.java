package de.ese.beatit.settings;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import de.ese.beatit.R;
import de.ese.beatit.pulsereader.ScanForDevices;

/**
 * Created by jan on 2/5/15.
 */
public class ReferencePulse extends Activity {

    private static SeekBar pulseBar;
    private static TextView selectedRefPulseTV;
    private static Button saveButton;

    private static int selectedRefPulse = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_ref_pulse);

        selectedRefPulseTV = (TextView) findViewById(R.id.selectedRefPulseDisplay);

        saveButton = (Button) findViewById(R.id.refPulse_save_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        pulseBar = (SeekBar) findViewById(R.id.ref_pulse_bar);
        pulseBar.setMax(120);

        pulseBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                selectedRefPulse = progress + 60;
                selectedRefPulseTV.setText(String.valueOf(selectedRefPulse));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
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
        return super.onOptionsItemSelected(item);
    }

    public static int getRefPulse() {
        return selectedRefPulse;
    }

}
