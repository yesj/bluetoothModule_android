package com.example.bluetoothmodule;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


public class MainActivity extends Activity {
    public static final String TAG = "bluetoothModule";
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    
    private BluetoothAdapter mBtAdapter = null;
    
    
	/**
	 * »P  Treadmill Service ³q°T
	 */
    private final BroadcastReceiver TreadmillStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

           //================
            if (action.equals(TreadmillService.ACTION_GATT_CONNECTED)) {
                Log.d(TAG, "UART_CONNECT_MSG");

            }
            //================
            if (action.equals(TreadmillService.ACTION_GATT_DISCONNECTED)) {
            	Log.i("Chandler","ACTION_GATT_DISCONNECTED");

            }
            //================
            if (action.equals(TreadmillService.ACTION_GATT_SERVICES_DISCOVERED)) {

            }
            //================
            if (action.equals(TreadmillService.ACTION_DATA_AVAILABLE)) {
            	
            }
            //================
            if (action.equals(TreadmillService.DEVICE_DOES_NOT_SUPPORT_UART)){
        		
            }
            //================
            if (action.equals(TreadmillService.ACTION_GATT_SERVICES_HEART_RATE_DISCOVERED)){

            }
            //================
            if (action.equals(TreadmillService.ACTION_SERIVCES_HEART_RATE)){

            }
            
        }
    }; 
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	/**
	 * Handler Disconnect & Connect button
	 */
	public void onSelectClicked(final View view) {
        if (!mBtAdapter.isEnabled()) {
            showBLEDialog();
        } else {
		Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
		startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
        }
	}	
	
	private void showBLEDialog() {
		final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
