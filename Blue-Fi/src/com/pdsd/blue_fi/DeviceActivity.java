package com.pdsd.blue_fi;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

// This is the detailed Bluetooth activity of a certain device.

public class DeviceActivity extends Activity {

    // Debugging
    private static final String TAG = "BluetoothActivity";
	
	// Constants.
	public final static String GO_TO_BLUETOOTH_ACTIVITY = "com.pdsd.blue_fi.bluetoothID";
	public final static String GO_TO_CALL_ACTIVITY = "com.pdsd.blue_fi.GO_TO_CALL_ACTIVITY";
	public final static String GO_TO_FILE_SHARE_ACTIVITY = "com.pdsd.blue_fi.GO_TO_FILE_SHARE_ACTIVITY";
	public final static String GO_TO_MESSAGE_ACTIVITY = "com.pdsd.blue_fi.GO_TO_MESSAGE_ACTIVITY";
    public static String DEVICE_ADDRESS = "com.pdsd.blue_fi.device_address";
    
    public enum AddressType{
    	Bluetooth,
    	Wifi,
    	Unknown
    }

	// Global variables.
	SharedPreferences preferences;
	BluetoothAdapter bluetoothAdapter;
	Bundle extras;
	String address;

	@Override
	protected void onCreate(Bundle savedInstanceState ){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device);

		// Global variables at app-level.
		preferences = this.getSharedPreferences( "com.pdsd.blue_fi", Context.MODE_PRIVATE );

		extras = getIntent().getExtras();
		if (extras != null) {
		    address = extras.getString( DEVICE_ADDRESS );
			TextView t = (TextView)findViewById( R.id.bluetooth_activity_title );
			t.setText( address );
		}
	
		switch( bluetoothOrWifi( address ) ){
		case Bluetooth:
			bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if( bluetoothAdapter == null ) {
			    // Device does not support Bluetooth.
				Toast.makeText( getApplicationContext(), R.string.bluetooth_not_supported, Toast.LENGTH_LONG ).show();
			}
			else{
				// Bluetooth is supported.
				Toast.makeText( getApplicationContext(), R.string.bluetooth_supported, Toast.LENGTH_SHORT ).show();
				if( !bluetoothAdapter.isEnabled() ){
					bluetoothAdapter.enable();
				}
			}
			break;
		case Wifi:
			break;
		case Unknown:
			break;
		}
		
        Log.d( TAG, "onCreate()" );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bluetooth, menu);
        Log.d( TAG, "onCreateOptionsMenu()" );
		return true;
	}

	public AddressType bluetoothOrWifi( String address ){
		if( address == null )
			return AddressType.Unknown;
		else if( address.contains( ":" ) )
			return AddressType.Bluetooth;
		else if( address.contains( "." ) )
			return AddressType.Wifi;
		else
			return AddressType.Unknown;
	}
	
	public void goToCallActivity( View view ){
		Intent intent = new Intent( this, CallActivity.class );
		intent.putExtra( GO_TO_CALL_ACTIVITY, address );
		startActivity( intent );
        Log.d( TAG, "goToCallActivity()" );
	}

	public void goToFileShareActivity( View view ){
		Intent intent = new Intent( this, FileShareActivity.class );
		intent.putExtra( GO_TO_FILE_SHARE_ACTIVITY, address );
		startActivity( intent );
        Log.d( TAG, "goToFileShareActivity()" );
	}

	public void goToMessageActivity( View view ){
		Intent intent = new Intent( this, MessageActivity.class );
		intent.putExtra( GO_TO_MESSAGE_ACTIVITY, address );
		startActivity( intent );
        Log.d( TAG, "goToMessageActivity()" );
	}
	
}
