package com.pdsd.blue_fi;

import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.pdsd.blue_fi.PairedDevicesAdapter;

// This is the main activity.

public class MainActivity extends Activity {

    // Debugging
    static final String TAG = "MainActivity";
    
    // Constants.
    public static final String DEVICE_NAME = "com.pdsd.blue_fi.device_name";
    public static final String DEVICE_ADDRESS = "com.pdsd.blue_fi.device_address";

	// Global variables.
	public int nOfDevicesPaired;
	SharedPreferences preferences;
	BluetoothAdapter bluetoothAdapter;
    PairedDevicesAdapter pairedDevicesAdapter;
    Set<BluetoothDevice> pairedDevices;
    ListView pairedListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        Log.d( TAG, "onCreate()" );
		
		// Declarations.
		LinearLayout layout;

		// Default actions.
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		// Global variables at app-level.
		preferences = this.getSharedPreferences( "com.pdsd.blue_fi", Context.MODE_PRIVATE );
		nOfDevicesPaired = preferences.getInt( "com.pdsd.blue_fi.nOfDevicesPaired", 0 );

		// Programmed.
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if ( bluetoothAdapter == null ) {
		    // Device does not support Bluetooth.
			Toast.makeText( getApplicationContext(), R.string.bluetooth_not_supported, Toast.LENGTH_LONG ).show();
		}
		else{
			// Bluetooth is supported.
			Toast.makeText( getApplicationContext(), R.string.bluetooth_supported, Toast.LENGTH_SHORT ).show();
			if (!bluetoothAdapter.isEnabled()) {
				bluetoothAdapter.enable();
			}
		}
        pairedDevices = bluetoothAdapter.getBondedDevices();
 
        pairedDevicesAdapter = new PairedDevicesAdapter( this );

        // Find and set up the ListView for paired devices
        pairedListView = (ListView)findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Get a set of currently paired devices
        pairedDevices = bluetoothAdapter.getBondedDevices();
        
        if (pairedDevices.size() > 0) {
            //findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
            	pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
			layout = (LinearLayout)findViewById( R.id.after_first_pair_layout );
			layout.setVisibility( View.VISIBLE );
			layout = (LinearLayout)findViewById( R.id.please_pair_layout );
			layout.setVisibility( View.GONE );
			pairedDevicesAdapter.add( "com.pdsd.blue_fi.pair_buttons" );
        }
        else{
			layout = (LinearLayout)findViewById( R.id.please_pair_layout );
			layout.setVisibility( View.VISIBLE );
			layout = (LinearLayout)findViewById( R.id.after_first_pair_layout );
			layout.setVisibility( View.GONE );
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        Log.d( TAG, "onCreateOptionsMenu()");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            Log.d( TAG, "onItemClick()" );
            // Cancel discovery because it's costly and we're about to connect
        	bluetoothAdapter.cancelDiscovery();

            goToDeviceActivity( v );
        }
    };

	
	public void pairWithBluetooth( View view ){
        Log.d( TAG, "pairWithBluetooth()");
		Intent intent = new Intent( this, BluetoothPairActivity.class );
		startActivity( intent );
	}
	
	public void pairWithWifi( View view ){
        Log.d( TAG, "pairWithWifi()" );
		Intent intent = new Intent( this, WifiPairActivity.class );
		startActivity( intent );
	}

	public void goToDeviceActivity( View view ){
        Log.d( TAG, "goToDeviceActivity()" );
		Intent intent = new Intent( this, DeviceActivity.class );
		String device = ((TextView)view).getText().toString();
		String []parts = device.split( "\n" );
		intent.putExtra( DEVICE_NAME, parts[0] );
		intent.putExtra( DEVICE_ADDRESS, parts[1] );
        setResult(Activity.RESULT_OK, intent);
		startActivity( intent );
	}
}
