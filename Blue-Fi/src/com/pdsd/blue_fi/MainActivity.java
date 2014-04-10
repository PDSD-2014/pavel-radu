package com.pdsd.blue_fi;

import java.lang.reflect.Array;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;

// This is the main activity.

public class MainActivity extends Activity {
	
	// Constants.
	public final static String GO_TO_BLUETOOTH_ACTIVITY = "com.example.myfirstapp.GO_TO_BLUETOOTH_ACTIVITY";
	public final static String GO_TO_WIFI_ACTIVITY = "com.example.myfirstapp.GO_TO_WIFI_ACTIVITY";

	// Global variables.
	public int nOfDevicesPaired;
	SharedPreferences preferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		// Declarations.
		LinearLayout layout;
		int i;

		// Default actions.
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		// Global variables at app-level.
		preferences = this.getSharedPreferences( "com.pdsd.blue_fi", Context.MODE_PRIVATE );
		nOfDevicesPaired = preferences.getInt( "com.pdsd.blue_fi.nOfDevicesPaired", 0 );

		// Programmed.
		int[] paired_devices = { R.id.paired_device1,  R.id.paired_device2 };
		if( nOfDevicesPaired == 0 ){
			layout = (LinearLayout)findViewById( R.id.please_pair_layout );
			layout.setVisibility( View.VISIBLE );
			layout = (LinearLayout)findViewById( R.id.after_first_pair_layout );
			layout.setVisibility( View.GONE );
		}
		else{
			layout = (LinearLayout)findViewById( R.id.after_first_pair_layout );
			layout.setVisibility( View.VISIBLE );
			layout = (LinearLayout)findViewById( R.id.please_pair_layout );
			layout.setVisibility( View.GONE );
			for( i = 0; i < max( nOfDevicesPaired, Array.getLength( paired_devices ) ); i += 1 ){
				layout = (LinearLayout)findViewById( paired_devices[i] );
				layout.setVisibility( View.VISIBLE );
			}
		}
	}

	private int max(int a, int b) {
		if( a < b )
			return b;
		return a;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void pairWithBluetooth( View view ){
		Intent intent = new Intent( this, BluetoothPairActivity.class );
		startActivity( intent );
	}
	
	public void pairWithWifi( View view ){
		Intent intent = new Intent( this, WifiPairActivity.class );
		startActivity( intent );
	}
	
	public void expand( View view ){
	}
	
	public void goToBluetoothActivity( View view ){
		Intent intent = new Intent( this, BluetoothActivity.class );
		intent.putExtra( GO_TO_BLUETOOTH_ACTIVITY, view.getId() );
		startActivity( intent );
	}
	
	public void goToWifiActivity( View view ){
		Intent intent = new Intent( this, WifiActivity.class );
		intent.putExtra( GO_TO_WIFI_ACTIVITY, view.getId() );
		startActivity( intent );
	}
	
}
