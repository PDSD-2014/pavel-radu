package com.pdsd.blue_fi;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;

// This is the detailed Bluetooth activity of a certain device.

public class BluetoothActivity extends Activity {
	
	// Constants.
	public final static String GO_BACK = "com.example.myfirstapp.GO_BACK";

	// Global variables.
	SharedPreferences preferences;
	BluetoothAdapter bluetoothAdapter;

	// Global variables at app-level.


	@Override
	protected void onCreate(Bundle savedInstanceState ){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth);

		// Global variables at app-level.
		preferences = this.getSharedPreferences( "com.pdsd.blue_fi", Context.MODE_PRIVATE );

		Intent intent = getIntent();
		String id = intent.getStringExtra( MainActivity.GO_TO_BLUETOOTH_ACTIVITY );
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if ( bluetoothAdapter == null ) {
		    // Device does not support Bluetooth
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bluetooth, menu);
		return true;
	}

	public void goBack( View view ){
		Intent intent = new Intent( this, MainActivity.class );
		intent.putExtra( GO_BACK, view.getId() );
		startActivity( intent );		
	}
}
