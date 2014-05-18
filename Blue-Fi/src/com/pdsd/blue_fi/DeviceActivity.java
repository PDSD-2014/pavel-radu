package com.pdsd.blue_fi;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;

// This is the detailed Bluetooth activity of a certain device.

public class DeviceActivity extends Activity {

    // Debugging
    static final String TAG = "DeviceActivity";
    
    public enum AddressType{
    	Bluetooth,
    	Wifi,
    	Unknown;
    	public static AddressType getAddressType( String address ){
            Log.d( TAG, "AddressType.getAddressType()" );
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
	String name;

	@Override
	protected void onCreate(Bundle savedInstanceState ){
		super.onCreate(savedInstanceState);
        Log.d( TAG, "onCreate()" );
		setContentView(R.layout.activity_device);

		// Global variables at app-level.
		preferences = this.getSharedPreferences( "com.pdsd.blue_fi", Context.MODE_PRIVATE );

		extras = getIntent().getExtras();
		if (extras != null) {
		    name = extras.getString( MainActivity.DEVICE_NAME );
		    address = extras.getString( MainActivity.DEVICE_ADDRESS );
			setTitle( name );
			Log.d( TAG, address );
		}
		
	}
	
	public void goToCallActivity( View view ){
        Log.d( TAG, "goToCallActivity()" );
		Intent intent = new Intent( this, CallActivity.class );
		intent.putExtra( MainActivity.DEVICE_NAME, name );
		intent.putExtra( MainActivity.DEVICE_ADDRESS, address );
		startActivity( intent );
	}

	public void goToFileShareActivity( View view ){
        Log.d( TAG, "goToFileShareActivity()" );
		Intent intent = new Intent( this, FileexplorerActivity.class );
		intent.putExtra( MainActivity.DEVICE_NAME, name );
		intent.putExtra( MainActivity.DEVICE_ADDRESS, address );
		startActivity( intent );
	}

	public void goToMessageActivity( View view ){
        Log.d( TAG, "goToMessageActivity()" );
		Intent intent = new Intent( this, MessageActivity.class );
		intent.putExtra( MainActivity.DEVICE_NAME, name );
		intent.putExtra( MainActivity.DEVICE_ADDRESS, address );
		startActivity( intent );
	}
	
}
