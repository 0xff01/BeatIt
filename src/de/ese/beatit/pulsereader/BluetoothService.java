package de.ese.beatit.pulsereader;

import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BluetoothService extends Service {

	BluetoothDevice mBluetoothDevice;
	BluetoothGatt mBluetoothGatt;
	Context context;

	// notificator for newly measured data by the GATT
	private final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";

	// UUIDs of needed services and values
	private static final UUID HEART_RATE_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
	private static final UUID HEART_RATE_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");

	// current pulse rate returned by the getter
	private int mCurrentPulseRate = 0;
	
	// Constructor
	public BluetoothService(BluetoothDevice mBluetoothDevice, Context context) {
		this.mBluetoothDevice = mBluetoothDevice;
		this.context = context;
		mBluetoothGatt = mBluetoothDevice.connectGatt(context, false, btleGattCallback);
	}

	// Callback for handling connection status
	BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {

			if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
				gatt.discoverServices();
			} else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
				gatt.connect();
			} else if (status != BluetoothGatt.GATT_SUCCESS) {
				gatt.disconnect();
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			
			// everytime a new value is measured by the pulse band the value is send
			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			// not needed because we get the values periodically by onCharacteristicsChanged
		}

		@Override
		public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {

			// try get heartrate from previously selected device
			getHeartRate();
		}

		public void getHeartRate() {
			
			BluetoothGattService heartRateService = mBluetoothGatt.getService(HEART_RATE_SERVICE);

			// check if Heart Rate Service is available on device;
			if (heartRateService == null) {
				return;
			}

			BluetoothGattCharacteristic heartRateMeasurement = heartRateService.getCharacteristic(HEART_RATE_MEASUREMENT);
			
			// check if a value is already available;
			if (heartRateMeasurement == null) {
				return;
			}

			// set notificator
			mBluetoothGatt.setCharacteristicNotification(heartRateMeasurement, true);
		}
	};

	private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
		
		mCurrentPulseRate = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
		
		// write current rate to console for debugging reasons
		Log.v("meins", "characteristic.getStringValue(0) = " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1));
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int getCurrentPulseRate() {
		return mCurrentPulseRate;
	}
}