package com.pdsd.blue_fi;

import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pdsd.blue_fi.PairedDevicesAdapter;

//This is the Bluetooth pairing activity.

public class BluetoothPairActivity extends Activity {

    // Debugging
    private static final String TAG = "BluetoothPairActivity";

	// Global variables.
	SharedPreferences preferences;
	BluetoothAdapter bluetoothAdapter;
    PairedDevicesAdapter pairedDevicesAdapter;
    Set<BluetoothDevice> pairedDevices;
    ListView pairedListView;
	
	// Constants.
	public final static String GO_TO_BLUETOOTH_ACTIVITY = "com.pdsd.blue_fi.bluetoothID";
    public static String DEVICE_ADDRESS = "com.pdsd.blue_fi.device_address";

	@Override
	protected void onCreate( Bundle savedInstanceState ){
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_bluetooth_pair );

		// Global variables at app-level.
		preferences = this.getSharedPreferences( "com.pdsd.blue_fi", Context.MODE_PRIVATE );

		// Intent intent = getIntent(); TODO: Do we need this?
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if ( bluetoothAdapter == null ) {
		    // Device does not support Bluetooth.
			Toast.makeText( getApplicationContext(), R.string.bluetooth_not_supported, Toast.LENGTH_LONG ).show();
			findViewById( R.id.bluetooth_pair_layout ).setVisibility( View.GONE );
		}
		else{
			// Bluetooth is supported.
			Toast.makeText( getApplicationContext(), R.string.bluetooth_supported, Toast.LENGTH_SHORT ).show();
			findViewById( R.id.bluetooth_pair_layout ).setVisibility( View.VISIBLE );
			if (!bluetoothAdapter.isEnabled()) {
				bluetoothAdapter.enable();
			}
		}

		// Extra
        setResult( Activity.RESULT_CANCELED );

        Button scanButton = (Button)findViewById( R.id.scan_button );
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
    			if (!bluetoothAdapter.isEnabled()) {
    				bluetoothAdapter.enable();
    			}
                doDiscovery();
            }
        });

        // Get a set of currently paired devices
        pairedDevices = bluetoothAdapter.getBondedDevices();
 
        pairedDevicesAdapter = new PairedDevicesAdapter( this );

        // Find and set up the ListView for paired devices
        pairedListView = (ListView)findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
  
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            //findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
            	pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedDevicesAdapter.add(noDevices);
        }
        pairedDevicesAdapter.add( null ); // This right here is a line break.
        Log.d( TAG, "onCreate()" );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bluetooth_pair, menu);
        Log.d( TAG, "onCreateOptionsMenu()" );
		return true;
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (bluetoothAdapter != null) {
        	bluetoothAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
        Log.d( TAG, "onDestroy()" );
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
		Toast.makeText( getApplicationContext(), R.string.discovery, Toast.LENGTH_SHORT ).show();

        // Turn on sub-title for new devices
        //findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (bluetoothAdapter.isDiscovering()) {
        	bluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        bluetoothAdapter.startDiscovery();
        Log.d( TAG, "doDiscovery()" );
    }
	
	public void goToDeviceActivity( View view ){
		Intent intent = new Intent( this, DeviceActivity.class );
		intent.putExtra( DEVICE_ADDRESS, ((TextView)view).getText() );
		startActivity( intent );
        Log.d( TAG, "goToDeviceActivity()" );
	}

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
        	bluetoothAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra( DEVICE_ADDRESS, address );

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            goToDeviceActivity( v );
            Log.d( TAG, "onItemClick()" );
        }
    };

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if( BluetoothDevice.ACTION_FOUND.equals( action ) ){
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
        		Toast.makeText( getApplicationContext(), R.string.discovery_finished, Toast.LENGTH_SHORT ).show();
                if( pairedDevicesAdapter.getCount() <= pairedDevices.size() ){
                	String noDevices = getResources().getText( R.string.none_found ).toString();
                	pairedDevicesAdapter.add( noDevices );
                }
            }
            Log.d( TAG, "BroadcastReceiver.onReceive()" );
        }
    };
}
