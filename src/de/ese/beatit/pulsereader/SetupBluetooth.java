package de.ese.beatit.pulsereader;

import java.util.ArrayList;

import de.ese.beatit.R;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

/*
 * Main Activity
 * Setting up the environment
 * Initialize the bluetooth settings
 * Create list of available Bluetooth LE devices
 */

public class SetupBluetooth extends ListActivity {

	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
	private ArrayAdapter<BluetoothDevice> mDeviceAdapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup_connection);
		
		// setup the list view
		mDeviceAdapter = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1, mDeviceList);
		setListAdapter(mDeviceAdapter);
		
		// Check if BTLE is available on hardware
		// Even when BTLE is set as required
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast message = Toast.makeText(this,
					"Your hardware doesn't support BT Low Energy...",
					Toast.LENGTH_LONG);
			message.show();
			finish();
		}

		// Setting up the Bluetooth hardware
		mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		mBluetoothAdapter = mBluetoothManager.getAdapter();

		// if Bluetooth is disabled
		// activate it without the users permission
		if (!mBluetoothAdapter.isEnabled() || mBluetoothAdapter == null) {
			mBluetoothAdapter.enable();
		}
		

		// start scanning for BTLE devices
		ScanForDevices scanner = new ScanForDevices(mBluetoothAdapter, this);
		scanner.startScan(mDeviceAdapter);
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
}
