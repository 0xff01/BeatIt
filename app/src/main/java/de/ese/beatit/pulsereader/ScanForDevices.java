package de.ese.beatit.pulsereader;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.widget.ArrayAdapter;

public class ScanForDevices {

	private Handler mHandler = new Handler();
	private int delayMillis = 5000; // scan for 5 sec
	private BluetoothAdapter mBluetoothAdapter;
	private Activity mActivity;
	private ArrayAdapter<BluetoothDevice> mDeviceAdapter;

	// constructor
	public ScanForDevices(BluetoothAdapter mBluetoothAdapter, Activity mActivity) {
		this.mBluetoothAdapter = mBluetoothAdapter;
		this.mActivity = mActivity;
	}

	// start scanning for BTLE devices for 'delayMillis' ms
	public void startScan(ArrayAdapter<BluetoothDevice> mDeviceAdapter) {

		this.mDeviceAdapter = mDeviceAdapter;

		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				mBluetoothAdapter.stopLeScan(callback);
			}

		}, delayMillis);

		mBluetoothAdapter.startLeScan(callback);
	}

	// for each found BTLE device add an entry to the list view
	private BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// prevent items to be added multiple times
					if (mDeviceAdapter.getPosition(device) < 0) {
						mDeviceAdapter.add(device);
						mDeviceAdapter.notifyDataSetChanged();
					}
				}
			});
		}
	};
}