package com.example.bluetoothmodule;


import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
    public static final String TAG = "bluetoothModule";
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    
    private TreadmillService mTreadmillService = null;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothDevice mDevice = null;
	private TextView TVDisplayState;   
	private TextView TVUint;
	private TextView TVSpeed; 
	private TextView TVIncline;
	private TextView TVSportTime;
	private TextView TVSportDistance;
	private TextView TVSportCalories;
	
	/**
	 * 與  Treadmill Service 通訊
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
            	Log.i("Chandler","ACTION_GATT_SERVICES_DISCOVERED");
            }
            //================
            if (action.equals(TreadmillService.ACTION_DATA_AVAILABLE)) {
            	
            }
            //================
            if (action.equals(TreadmillService.DEVICE_DOES_NOT_SUPPORT_UART)){
        		
            }
            //================
            if (action.equals(TreadmillService.DISPLAY_STATE_CHARACTERISTIC_DATA)){
            	final int Value = intent.getIntExtra(TreadmillService.EXTRA_DATA, 0);
              	 runOnUiThread(new Runnable() {
                     public void run() {
                    	 TVDisplayState.setText("DISPLAY_STATE:"+ Value);
                     }
                 });
            }
            //================
            if (action.equals(TreadmillService.UNIT_CHARACTERISTIC_DATA)){
            	final int Value = intent.getIntExtra(TreadmillService.EXTRA_DATA, 0);
            	 runOnUiThread(new Runnable() {
                   public void run() {
                	   TVUint.setText("Unit:"+ Value);
                   }
               });
            }
            //================
            if (action.equals(TreadmillService.SPEED_CHARACTERISTIC_DATA)){
            	final int Value = intent.getIntExtra(TreadmillService.EXTRA_DATA, 0);
             	 runOnUiThread(new Runnable() {
                    public void run() {
                    	TVSpeed.setText("Speed:"+ Value);
                    }
                });
            }
            
            //================
            if (action.equals(TreadmillService.INCLINE_CHARACTERISTIC_DATA)){
            	final int Value = intent.getIntExtra(TreadmillService.EXTRA_DATA, 0);
            	 runOnUiThread(new Runnable() {
                   public void run() {
                	   TVIncline.setText("Incline:"+ Value);
                   }
               });
            }
            //================
            if (action.equals(TreadmillService.SPORT_TIME_CHARACTERISTIC_DATA)){
            	final int Value = intent.getIntExtra(TreadmillService.EXTRA_DATA, 0);
           	 	runOnUiThread(new Runnable() {
                  public void run() {
                	  TVSportTime.setText("SportTime:"+ Value);
                  }
              });
            }
            //================
            if (action.equals(TreadmillService.SPORT_DISTANCE_CHARACTERISTIC_DATA)){
            	final long Value = intent.getLongExtra(TreadmillService.EXTRA_DATA, 0);
           	 	runOnUiThread(new Runnable() {
                  public void run() {
                	  TVSportDistance.setText("SportDistance:"+ Value);
                  }
              });
            }
            //================
            if (action.equals(TreadmillService.SPORT_CALORIES_CHARACTERISTIC_DATA)){
            	final long Value = intent.getLongExtra(TreadmillService.EXTRA_DATA, 0);
           	 	runOnUiThread(new Runnable() {
                  public void run() {
                	  TVSportCalories.setText("SportCalories:"+ Value);
                  }
              });
            }
            //================
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
        setGUI();
        TreadmillServiceInit();
        
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}
	
	private void setGUI() {

		TVDisplayState = (TextView) findViewById(R.id.display_state_views);
		TVUint = (TextView) findViewById(R.id.unit_views);
		TVSpeed = (TextView) findViewById(R.id.speed_views);
		TVIncline = (TextView) findViewById(R.id.incline_views);
		TVSportTime = (TextView) findViewById(R.id.sport_time_views);
		TVSportDistance = (TextView) findViewById(R.id.sport_distance_views);
		TVSportCalories = (TextView) findViewById(R.id.sport_calories_views);
	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_SELECT_DEVICE:
        	//When the DeviceListActivity return, with the selected device address
        	if (resultCode == Activity.RESULT_OK && data != null) {
                String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mTreadmillService);
                //((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                mTreadmillService.connect(deviceAddress);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();
            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        default:
            Log.e(TAG, "wrong request code");
            break;
        }
    }
	
	//============================================================
	// 建立跟Treadmill Service 廣播
	//============================================================
    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
        	mTreadmillService = ((TreadmillService.LocalBinder) rawBinder).getService();
        		Log.d(TAG, "onServiceConnected mService= " + mTreadmillService);
        		if (!mTreadmillService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }
        }

        public void onServiceDisconnected(ComponentName classname) {
        	mTreadmillService = null;
        }
    };
	
	
    private void TreadmillServiceInit() {
        Intent bindIntent = new Intent(this, TreadmillService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(TreadmillStatusChangeReceiver, makeGattUpdateIntentFilter());
    }
    
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TreadmillService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(TreadmillService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(TreadmillService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(TreadmillService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(TreadmillService.DEVICE_DOES_NOT_SUPPORT_UART);
        intentFilter.addAction(TreadmillService.DISPLAY_STATE_CHARACTERISTIC_DATA);
        intentFilter.addAction(TreadmillService.UNIT_CHARACTERISTIC_DATA);
        intentFilter.addAction(TreadmillService.SPEED_CHARACTERISTIC_DATA);
        intentFilter.addAction(TreadmillService.INCLINE_CHARACTERISTIC_DATA); 
        intentFilter.addAction(TreadmillService.SPORT_TIME_CHARACTERISTIC_DATA);
        intentFilter.addAction(TreadmillService.SPORT_DISTANCE_CHARACTERISTIC_DATA);
        intentFilter.addAction(TreadmillService.SPORT_CALORIES_CHARACTERISTIC_DATA);
        
        return intentFilter;
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
