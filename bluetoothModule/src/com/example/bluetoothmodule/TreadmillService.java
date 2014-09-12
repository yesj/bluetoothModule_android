/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bluetoothmodule;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;
import java.util.UUID;


/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class TreadmillService extends Service {
    private final static String TAG = TreadmillService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;

    public final static String ACTION_GATT_CONNECTED =
    		"com.example.bluetoothmodule.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
    		"com.example.bluetoothmodule.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
    		"com.example.bluetoothmodule.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
    		"com.example.bluetoothmodule.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetoothmodule.EXTRA_DATA";
    public final static String DEVICE_DOES_NOT_SUPPORT_UART =
            "com.example.bluetoothmodule.DEVICE_DOES_NOT_SUPPORT_UART";
    public final static String DISPLAY_STATE_CHARACTERISTIC_DATA =
            "com.example.bluetoothmodule.DISPLAY_STATE_CHARACTERISTIC_DATA";
    public final static String UNIT_CHARACTERISTIC_DATA =
            "com.example.bluetoothmodule.UNIT_CHARACTERISTIC_DATA";
    public final static String SPEED_CHARACTERISTIC_DATA =
            "com.example.bluetoothmodule.SPEED_CHARACTERISTIC_DATA";
    public final static String INCLINE_CHARACTERISTIC_DATA =
            "com.example.bluetoothmodule.INCLINE_CHARACTERISTIC_DATA";
    public final static String SPORT_TIME_CHARACTERISTIC_DATA =
            "com.example.bluetoothmodule.SPORT_TIME_CHARACTERISTIC_DATA";
    public final static String SPORT_DISTANCE_CHARACTERISTIC_DATA =
            "com.example.bluetoothmodule.SPORT_DISTANCE_CHARACTERISTIC_DATA";
    public final static String SPORT_CALORIES_CHARACTERISTIC_DATA =
            "com.example.bluetoothmodule.SPORT_CALORIES_CHARACTERISTIC_DATA";
    public final static String HEART_RATE_CHARACTERISTIC_DATA =
            "com.example.bluetoothmodule.HEART_RATE_CHARACTERISTIC_DATA";
    
	private static final int FIRST_BITMASK = 0x01;

    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_TREADMILL_SERVICE = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    private static final UUID UUID_DISPLAY_STATE_CHARACTERISTIC =	UUID.fromString("6e400004-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID UUID_UNIT_CHARACTERISTIC = 			UUID.fromString("6e400005-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID UUID_SPEED_CHARACTERISTIC =			UUID.fromString("6e400006-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID UUID_INCLINE_CHARACTERISTIC =			UUID.fromString("6e400007-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID UUID_SPORT_TIME_CHARACTERISTIC =		UUID.fromString("6e400008-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID UUID_SPORT_DISTANCE_CHARACTERISTIC =	UUID.fromString("6e400009-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID UUID_SPORT_CALORIES_CHARACTERISTIC =	UUID.fromString("6e40000A-b5a3-f393-e0a9-e50e24dcca9e");

    private final static UUID HR_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_HR_CHARACTERISTIC = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb");

    
    
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                //mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> listGattService;
            	listGattService = gatt.getServices();  
                for (BluetoothGattService listService : listGattService) {
                	if(listService.getUuid().equals(UUID_TREADMILL_SERVICE)) {
                		EnableNotification();
                	} else if(listService.getUuid().equals(HR_SERVICE_UUID)) {
                		Enable_HeartRate_CHARACTERISTIC_Notification();
                	}
                }
            	Log.w(TAG, "mBluetoothGatt = " + mBluetoothGatt );
                
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	if(UUID_UNIT_CHARACTERISTIC.equals(characteristic.getUuid())) {
            		int Value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            		broadcastUpdate(UNIT_CHARACTERISTIC_DATA, Value);
            	}
            }
        }
        
        
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
        	if (TX_CHAR_UUID.equals(characteristic.getUuid())) {
        		broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        		//Log.w("Chandler", "ACTION_DATA_AVAILABLE : " + characteristic.getValue());
        	} 
        	if(UUID_DISPLAY_STATE_CHARACTERISTIC.equals(characteristic.getUuid())) {
        		int Value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
				//Log.w("Chandler", "DISPLAY_STATE_CHARACTERISTIC_DATA : " + hrValue); 
				broadcastUpdate(DISPLAY_STATE_CHARACTERISTIC_DATA, Value);
        	}
        	
        	if(UUID_SPEED_CHARACTERISTIC.equals(characteristic.getUuid())) {
        		int Value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
				broadcastUpdate(SPEED_CHARACTERISTIC_DATA, Value);
        	}
        	
        	if(UUID_INCLINE_CHARACTERISTIC.equals(characteristic.getUuid())) {
        		int Value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        		broadcastUpdate(INCLINE_CHARACTERISTIC_DATA, Value);
        	}

        	if(UUID_SPORT_TIME_CHARACTERISTIC.equals(characteristic.getUuid())) {
        		int Value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
        		broadcastUpdate(SPORT_TIME_CHARACTERISTIC_DATA, Value);
        	}
        	
        	if(UUID_SPORT_DISTANCE_CHARACTERISTIC.equals(characteristic.getUuid())) {
        		long Value = byteArrayToLong(characteristic.getValue());
        		broadcastUpdate(SPORT_DISTANCE_CHARACTERISTIC_DATA, Value);
        	}
        	
        	if(UUID_SPORT_CALORIES_CHARACTERISTIC.equals(characteristic.getUuid())) {
        		long Value = byteArrayToLong(characteristic.getValue());
        		Log.w("Chandler", "UUID_SPORT_CALORIES : " + Value); 
        		broadcastUpdate(SPORT_CALORIES_CHARACTERISTIC_DATA, Value);	
        	}
        	
        	if(UUID_HR_CHARACTERISTIC.equals(characteristic.getUuid())) {
        		int Value = 0;
				if (isHeartRateInUINT16(characteristic.getValue()[0])) {
					Value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1);
				} else {
					Value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
				}
				broadcastUpdate(HEART_RATE_CHARACTERISTIC_DATA, Value);	
        	}
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        
        intent.putExtra(EXTRA_DATA, characteristic.getValue());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    
    private void broadcastUpdate(final String action,
            final int data) {
    		final Intent intent = new Intent(action);

    		intent.putExtra(EXTRA_DATA, data);
    		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
            final long data) {
    		final Intent intent = new Intent(action);

    		intent.putExtra(EXTRA_DATA, data);
    		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void EnableNotification() {
    	new Thread(new Runnable(){
    	    @Override
    	    public void run() {
    	            try{
    	            	Thread.sleep(600);
    	            	Enable_SportDistance_CHARACTERISTIC_Notification();
    	            	Thread.sleep(600);
    	            	Enable_SporCalories_CHARACTERISTIC_Notification();
    	            	Thread.sleep(600);
    	            	enableTXNotification();
    	                Thread.sleep(600);
    	                Enable_DISPLAY_STATE_CHARACTERISTIC_Notification();
    	                Thread.sleep(600);
    	                Read_Uint_CHARACTERISTIC_Data();
    	                Thread.sleep(600);
    	                Enable_SPEED_CHARACTERISTIC_Notification();
    	                Thread.sleep(600);
    	                Enable_INCLINE_CHARACTERISTIC_Notification();
    	                Thread.sleep(600);
    	                Enable_SportTime_CHARACTERISTIC_Notification();
    	            } 
    	            catch(Exception e){
    	                e.printStackTrace();
    	            }
    	    }            
    	}).start();
    }
    
    public class LocalBinder extends Binder {
    	TreadmillService getService() {
            return TreadmillService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                //mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        //mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        Log.w(TAG, "mBluetoothGatt closed");
        mBluetoothDeviceAddress = null;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enable TXNotification
     *
     * @return 
     */
    private void enableTXNotification() { 
    	BluetoothGattService RxService = mBluetoothGatt.getService(UUID_TREADMILL_SERVICE);
    	if (RxService == null) {
            showMessage("Rx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
    	BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            showMessage("Tx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        	Log.i("Chandler","RX_SERVICE_UUID");
	        mBluetoothGatt.setCharacteristicNotification(TxChar,true);
	        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
	        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
	        mBluetoothGatt.writeDescriptor(descriptor);
    }

    private void Enable_DISPLAY_STATE_CHARACTERISTIC_Notification() { 
    	BluetoothGattService TreadmillService = mBluetoothGatt.getService(UUID_TREADMILL_SERVICE);
    	if (TreadmillService == null) {
            showMessage("Rx service not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
    	BluetoothGattCharacteristic Characteristic = TreadmillService.getCharacteristic(UUID_DISPLAY_STATE_CHARACTERISTIC);
        if (Characteristic == null) {
            showMessage("DISPLAY STATE charateristic not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        	Log.i("Chandler","UUID_DISPLAY_STATE_CHARACTERISTIC");
	        mBluetoothGatt.setCharacteristicNotification(Characteristic,true);
	        BluetoothGattDescriptor descriptor = Characteristic.getDescriptor(CCCD);
	        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
	        mBluetoothGatt.writeDescriptor(descriptor);
    }    
    
    private void Enable_SPEED_CHARACTERISTIC_Notification() { 
    	BluetoothGattService TreadmillService = mBluetoothGatt.getService(UUID_TREADMILL_SERVICE);
    	if (TreadmillService == null) {
            showMessage("TREADMILL_SERVICE service not found!");
            return;
        }
    	BluetoothGattCharacteristic Characteristic = TreadmillService.getCharacteristic(UUID_SPEED_CHARACTERISTIC);
        if (Characteristic == null) {
            showMessage("SPEED charateristic not found!");
            return;
        }
	        mBluetoothGatt.setCharacteristicNotification(Characteristic,true);
	        BluetoothGattDescriptor descriptor = Characteristic.getDescriptor(CCCD);
	        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
	        mBluetoothGatt.writeDescriptor(descriptor);
    }
    
    private void Read_Uint_CHARACTERISTIC_Data() {
    	BluetoothGattService TreadmillService = mBluetoothGatt.getService(UUID_TREADMILL_SERVICE);
    	if (TreadmillService == null) {
            showMessage("TREADMILL_SERVICE service not found!");
            return;
        }
    	BluetoothGattCharacteristic Characteristic = TreadmillService.getCharacteristic(UUID_UNIT_CHARACTERISTIC);
        if (Characteristic == null) {
            showMessage("DISPLAY STATE charateristic not found!");
            return;
        }
        readCharacteristic(Characteristic);
    }

    private void Enable_INCLINE_CHARACTERISTIC_Notification() {
    	BluetoothGattService TreadmillService = mBluetoothGatt.getService(UUID_TREADMILL_SERVICE);
    	if (TreadmillService == null) {
            showMessage("TREADMILL_SERVICE service not found!");
            return;
        }
    	BluetoothGattCharacteristic Characteristic = TreadmillService.getCharacteristic(UUID_INCLINE_CHARACTERISTIC);
        if (Characteristic == null) {
            showMessage("SPEED charateristic not found!");
            return;
        }
	        mBluetoothGatt.setCharacteristicNotification(Characteristic,true);
	        BluetoothGattDescriptor descriptor = Characteristic.getDescriptor(CCCD);
	        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
	        mBluetoothGatt.writeDescriptor(descriptor);	
    }

    private void Enable_SportTime_CHARACTERISTIC_Notification() {
    	BluetoothGattService TreadmillService = mBluetoothGatt.getService(UUID_TREADMILL_SERVICE);
    	if (TreadmillService == null) {
            showMessage("TREADMILL_SERVICE service not found!");
            return;
        }
    	BluetoothGattCharacteristic Characteristic = TreadmillService.getCharacteristic(UUID_SPORT_TIME_CHARACTERISTIC);
        if (Characteristic == null) {
            showMessage("SPEED charateristic not found!");
            return;
        }
	        mBluetoothGatt.setCharacteristicNotification(Characteristic,true);
	        BluetoothGattDescriptor descriptor = Characteristic.getDescriptor(CCCD);
	        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
	        mBluetoothGatt.writeDescriptor(descriptor);		
    }

    private void Enable_SportDistance_CHARACTERISTIC_Notification() {
    	BluetoothGattService TreadmillService = mBluetoothGatt.getService(UUID_TREADMILL_SERVICE);
    	if (TreadmillService == null) {
            showMessage("TREADMILL_SERVICE service not found!");
            return;
        }
    	BluetoothGattCharacteristic Characteristic = TreadmillService.getCharacteristic(UUID_SPORT_DISTANCE_CHARACTERISTIC);
        if (Characteristic == null) {
            showMessage("SPEED charateristic not found!");
            return;
        }
	        mBluetoothGatt.setCharacteristicNotification(Characteristic,true);
	        BluetoothGattDescriptor descriptor = Characteristic.getDescriptor(CCCD);
	        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
	        mBluetoothGatt.writeDescriptor(descriptor);	
    }
    
    private void Enable_SporCalories_CHARACTERISTIC_Notification() {
    	BluetoothGattService TreadmillService = mBluetoothGatt.getService(UUID_TREADMILL_SERVICE);
    	if (TreadmillService == null) {
            showMessage("TREADMILL_SERVICE service not found!");
            return;
        }
    	BluetoothGattCharacteristic Characteristic = TreadmillService.getCharacteristic(UUID_SPORT_CALORIES_CHARACTERISTIC);
        if (Characteristic == null) {
            showMessage("SportCalories charateristic not found!");
            return;
        }
        Log.i("Chandler","Hr charateristic");
	        mBluetoothGatt.setCharacteristicNotification(Characteristic,true);
	        BluetoothGattDescriptor descriptor = Characteristic.getDescriptor(CCCD);
	        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
	        mBluetoothGatt.writeDescriptor(descriptor);	
    }
    
    private void Enable_HeartRate_CHARACTERISTIC_Notification() {
    	BluetoothGattService TreadmillService = mBluetoothGatt.getService(HR_SERVICE_UUID);
    	if (TreadmillService == null) {
            showMessage("HR_SERVICE service not found!");
            return;
        }
    	Log.i("Chandler","Hr_SERVICE_UUID");
    	BluetoothGattCharacteristic Characteristic = TreadmillService.getCharacteristic(UUID_HR_CHARACTERISTIC);
        if (Characteristic == null) {
            showMessage("Hr charateristic not found!");
            return;
        }
	        mBluetoothGatt.setCharacteristicNotification(Characteristic,true);
	        BluetoothGattDescriptor descriptor = Characteristic.getDescriptor(CCCD);
	        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
	        mBluetoothGatt.writeDescriptor(descriptor);	
    }
    
    public void writeRXCharacteristic(byte[] value)
    {
    	BluetoothGattService RxService = mBluetoothGatt.getService(UUID_TREADMILL_SERVICE);
    	showMessage("mBluetoothGatt null"+ mBluetoothGatt);
    	if (RxService == null) {
            showMessage("Rx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
    	BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
        if (RxChar == null) {
            showMessage("Rx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        RxChar.setValue(value);
    	boolean status = mBluetoothGatt.writeCharacteristic(RxChar);
    	
        Log.d(TAG, "write TXchar - status=" + status); 
        Log.d("Chandler", "write TXchar - status=" + status); 
    }
    
    private void showMessage(String msg) {
        Log.e(TAG, msg);
    }
    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
    

    public static long byteArrayToLong(byte[] byteArray) {
    	long Number = 0;
    	long NumberInt1,NumberInt2,NumberInt3,NumberInt4;
    	
    	NumberInt1 = byteArray [0] & 0xFF;
    	NumberInt2 = byteArray [1] & 0xFF;
    	NumberInt3 = byteArray [2] & 0xFF;
    	NumberInt4 = byteArray [3] & 0xFF;
    	Number = (NumberInt4 << 24) + (NumberInt3 << 16) + (NumberInt2 << 8) + NumberInt1;
    	return  Number;  
    }
    
    /**
     * INT 值轉成4字節的byte數組
     * @param num
     * @return
     */
    public static byte[] int2byteArray(int num) {
    	byte[] result = new byte[4];
    	result[0] = (byte)(num >>> 24);
    	result[1] = (byte)(num >>> 16);
    	result[2] = (byte)(num >>> 8); 
    	result[3] = (byte)(num );
    	return result;
    }
 
	/**
	 * This method will check if Heart rate value is in 8 bits or 16 bits
	 */
	private boolean isHeartRateInUINT16(byte value) {
		if ((value & FIRST_BITMASK) != 0)
			return true;
		return false;
	}
}
