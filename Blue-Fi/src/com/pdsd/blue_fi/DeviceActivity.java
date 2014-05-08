package com.pdsd.blue_fi;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

// This is the detailed Bluetooth activity of a certain device.

public class DeviceActivity extends Activity {

    // Debugging
    private static final String TAG = "BluetoothActivity";
	
	// Constants.
    public static String DEVICE_ADDRESS = "com.pdsd.blue_fi.device_address";
    
    public enum AddressType{
    	Bluetooth,
    	Wifi,
    	Unknown;
    	public static AddressType getAddressType( String address ){
    		if( address == null )
    			return AddressType.Unknown;
    		else if( address.contains( ":" ) )
    			return AddressType.Bluetooth;
    		else if( address.contains( "." ) )
    			return AddressType.Wifi;
    		else
    			return AddressType.Unknown;
    	}
    }

	// Global variables.
	SharedPreferences preferences;
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
			TextView t = (TextView)findViewById( R.id.device_title );
			t.setText( address );
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
	
	public void goToCallActivity( View view ){
		Intent intent = new Intent( this, CallActivity.class );
		intent.putExtra( DEVICE_ADDRESS, address );
		startActivity( intent );
        Log.d( TAG, "goToCallActivity()" );
	}

	public void goToFileShareActivity( View view ){
		Intent intent = new Intent( this, FileShareActivity.class );
		intent.putExtra( DEVICE_ADDRESS, address );
		startActivity( intent );
        Log.d( TAG, "goToFileShareActivity()" );
	}

	public void goToMessageActivity( View view ){
		Intent intent = new Intent( this, MessageActivity.class );
		intent.putExtra( DEVICE_ADDRESS, address );
		startActivity( intent );
        Log.d( TAG, "goToMessageActivity()" );
	}
	
}
