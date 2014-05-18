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

public class BluetoothPairActivity extends Activity{

    // Debugging
	static final String TAG = "BluetoothPairActivity";

	// Global variables.
	BluetoothAdapter bluetoothAdapter;
    ListView pairedListView;
    PairedDevicesAdapter pairedDevicesAdapter;
    Set<BluetoothDevice> pairedDevices;
	SharedPreferences preferences;

	@Override
	protected void onCreate( Bundle savedInstanceState ){
        Log.d( TAG, "onCreate()" );
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_bluetooth_pair );

		// Global variables at app-level.
		preferences = this.getSharedPreferences( "com.pdsd.blue_fi", Context.MODE_PRIVATE );

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
			if( !bluetoothAdapter.isEnabled() ){
				bluetoothAdapter.enable();
			}
		}

        setResult( Activity.RESULT_CANCELED );

        Button scanButton = (Button)findViewById( R.id.scan_button );
        scanButton.setOnClickListener( new OnClickListener(){
            public void onClick( View v ){
    			if( !bluetoothAdapter.isEnabled() ){
    				bluetoothAdapter.enable();
    			}
                doDiscovery();
            }
        });

        // Find and set up the ListView for paired devices
        pairedListView = (ListView)findViewById( R.id.paired_devices );
        pairedDevicesAdapter = new PairedDevicesAdapter( this );
        pairedListView.setAdapter( pairedDevicesAdapter );
        pairedListView.setOnItemClickListener( deviceClickListener );
  
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter( BluetoothDevice.ACTION_FOUND );
        this.registerReceiver( mReceiver, filter );

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter( BluetoothAdapter.ACTION_DISCOVERY_FINISHED );
        this.registerReceiver( mReceiver, filter );

        // If there are paired devices, add each one to the ArrayAdapter
        pairedDevices = bluetoothAdapter.getBondedDevices();
        if( pairedDevices.size() > 0 ){
            //findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for( BluetoothDevice device : pairedDevices ){
            	pairedDevicesAdapter.add( device.getName() + "\n" + device.getAddress() );
            }
        }
        else{
            String noDevices = getResources().getText( R.string.none_paired ).toString();
            pairedDevicesAdapter.add( noDevices );
        }
        pairedDevicesAdapter.add( null ); // This right here is a line break.
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        Log.d( TAG, "onCreateOptionsMenu()" );
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.bluetooth_pair, menu );
		return true;
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d( TAG, "onDestroy()" );

        // Make sure we're not doing discovery anymore
        if( bluetoothAdapter != null ){
        	bluetoothAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver( mReceiver );
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery(){
        Log.d( TAG, "doDiscovery()" );

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility( true );

        // Turn on sub-title for new devices
        //findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if( bluetoothAdapter.isDiscovering() ){
        	bluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        bluetoothAdapter.startDiscovery();
		Toast.makeText( getApplicationContext(), R.string.discovery, Toast.LENGTH_SHORT ).show();
    }
	
	public void goToDeviceActivity( View view ){
        Log.d( TAG, "goToDeviceActivity()" );
		Intent intent = new Intent( this, DeviceActivity.class );
		String device = ((TextView)view).getText().toString();
		String []parts = device.split( "\n" );
		intent.putExtra( MainActivity.DEVICE_NAME, parts[0] );
		intent.putExtra( MainActivity.DEVICE_ADDRESS, parts[1] );
        setResult(Activity.RESULT_OK, intent);
		startActivity( intent );
	}

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener deviceClickListener = new OnItemClickListener(){
        public void onItemClick( AdapterView<?> av, View v, int arg2, long arg3 ){
            Log.d( TAG, "onItemClick()" );
            // Cancel discovery because it's costly and we're about to connect
        	bluetoothAdapter.cancelDiscovery();
  
            // Set result and finish this Activity
            goToDeviceActivity( v );
        }
    };

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive( Context context, Intent intent ){
            Log.d( TAG, "BroadcastReceiver.onReceive()" );
            String action = intent.getAction();

            // When discovery finds a device
            if( BluetoothDevice.ACTION_FOUND.equals( action ) ){
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );
                // If it's already paired, skip it, because it's been listed already
                if( device.getBondState() != BluetoothDevice.BOND_BONDED ){
                    pairedDevicesAdapter.add( device.getName() + "\n" + device.getAddress() );
                }
            // When discovery is finished, change the Activity title
            }
            else if( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals( action ) ){
                if( pairedDevicesAdapter.getCount() <= pairedDevices.size() ){
                	String noDevices = getResources().getText( R.string.none_found ).toString();
                    if( pairedDevicesAdapter.getItem( pairedDevicesAdapter.getCount() - 1 ) == null || !pairedDevicesAdapter.getItem( pairedDevicesAdapter.getCount() - 1 ).equals( noDevices ) )
                    	pairedDevicesAdapter.add( noDevices );
                }
        		pairedDevicesAdapter.notifyDataSetChanged();
        		Toast.makeText( getApplicationContext(), R.string.discovery_finished, Toast.LENGTH_SHORT ).show();
            }
        }
    };
}
