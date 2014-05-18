package com.pdsd.blue_fi;

import java.util.ArrayList;
import java.util.List;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.pdsd.blue_fi.PairedDevicesAdapter;

//This is the Wi-Fi pairing activity.

public class WifiPairActivity extends Activity{

    // Debugging
    static final String TAG = "WifiPairActivity";

	// Global variables.
	boolean isWifiP2pEnabled, p2p_unsupported;
	BroadcastReceiver broadcastReceiver;
	IntentFilter intentFilter;
	List<String> peers;
    ListView pairedListView;
    PairedDevicesAdapter pairedDevicesAdapter;
	SharedPreferences preferences;
	WifiManager wifiManager;
	String address;
	boolean emptyList;
	///////////////
	TextView mainText;
    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Log.d( TAG, "onCreate()" );
		setContentView(R.layout.activity_wifi_pair);
		intentFilter = new IntentFilter();
		emptyList = true;
		
		// Global variables at app-level.
		preferences = this.getSharedPreferences( "com.pdsd.blue_fi", Context.MODE_PRIVATE );

	    Button scanButton = (Button)findViewById( R.id.wifi_scan_button );
	    wifiManager = (WifiManager)this.getSystemService( Context.WIFI_SERVICE );
	    wifiManager.setWifiEnabled( true );
		peers = new ArrayList<String>();
        pairedDevicesAdapter = new PairedDevicesAdapter( this );

        // Find and set up the ListView for paired devices
        pairedListView = (ListView)findViewById(R.id.wifi_paired_devices);
        pairedListView.setAdapter( pairedDevicesAdapter );
        pairedListView.setOnItemClickListener(mDeviceClickListener);

    	String noDevices = getResources().getText( R.string.none_paired ).toString();
    	pairedDevicesAdapter.add( noDevices );

        pairedDevicesAdapter.add( null ); // This right here is a line break.
        
	    scanButton.setOnClickListener( new OnClickListener(){
            public void onClick( View v ){
            	doDiscovery();
            }
        } );

	}
    
    @Override
    public void onResume() {
    	registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    @Override
    public void onPause() {
    	unregisterReceiver(receiverWifi);
        super.onPause();
    }
    
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            Log.d( TAG, "onItemClick()" );
            // Cancel discovery because it's costly and we're about to connect
        	//bluetoothAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra( MainActivity.DEVICE_ADDRESS, address );

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            goToDeviceActivity( v );
        }
    };

	public void goToDeviceActivity( View view ){
        Log.d( TAG, "goToDeviceActivity()" );
		Intent intent = new Intent( this, DeviceActivity.class );
		intent.putExtra( MainActivity.DEVICE_ADDRESS, ((TextView)view).getText() );
		startActivity( intent );
	}

    public void connect() {
        Log.d( TAG, "connect()" );
        // Picking the first device found on the network.
    }

    public void doDiscovery(){
    	Log.d( TAG, "doDiscovery()" );
        
        Toast.makeText( this, "Scanning...", Toast.LENGTH_SHORT ).show();

    	mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();

        Toast.makeText( this, "Scan complete", Toast.LENGTH_SHORT ).show();

    }
    
    class WifiReceiver extends BroadcastReceiver {
    	
    	public String parse( String result ){
    		Log.d( TAG, "parse( " + result + " )" );
    		
    		String string, aux;
    		String[] parts;
    		String[] parts2;

    		string = new String();
    		aux = new String();
    		parts = result.split( "," );
    		
    		// SSID
    		aux = parts[0].split( ":" )[1];
    		aux = aux.substring( 1, aux.length() );
    		string += aux;

    		// BSSID
    		parts2 = parts[1].split( ":" );
    		string += "\n" + parts2[1].substring( 1, parts2[1].length() );
    		string += ":" + parts2[2];
    		string += ":" + parts2[3];
    		string += ":" + parts2[4];
    		string += ":" + parts2[5];
    		string += ":" + parts2[6];

    		// Singal strength
    		aux = parts[3].split( ":" )[1];
    		aux = aux.substring( 1, aux.length() );
    		string += "\nSignal strength: " + aux + "db";
    		
    		return string;
    	}
    	
        public void onReceive(Context c, Intent intent) {
        	if( emptyList ){
	            wifiList = mainWifi.getScanResults();
	            for(int i = 0; i < wifiList.size(); i++){
	            	String entry = parse( wifiList.get( i ).toString() );
	                pairedDevicesAdapter.add( Integer.valueOf( i + 1 ).toString() + ". " + entry );
	                emptyList = false;
	            }
	        	pairedDevicesAdapter.notifyDataSetChanged();
        	}

            if( emptyList ){
    	    	String noDevices = getResources().getText( R.string.none_found ).toString();
    	        if( pairedDevicesAdapter.getItem( pairedDevicesAdapter.getCount() - 1 ) == null || !pairedDevicesAdapter.getItem( pairedDevicesAdapter.getCount() - 1 ).equals( noDevices ) ){
    	        	pairedDevicesAdapter.add( noDevices );
    	        	pairedDevicesAdapter.notifyDataSetChanged();
    	        }
            }
           // mainText.setText(sb);///aici o sa am toate datele retelelor descoperite
            /////////
            //String networkSSID = "test";//////do some tests
            //String networkPass = "pass";

           /* WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\""; 
            //aici tre' vazut de care retea e
             //si tratat pe cazuri
            //wep
            conf.wepKeys[0] = "\"" + networkPass + "\""; 
            conf.wepTxKeyIndex = 0;
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40); 
            
            //wpa
            conf.preSharedKey = "\""+ networkPass +"\"";
            
            //open net
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            
            //add settings to manager
            
            mainWifi.addNetwork(conf);
            
            //enable so android can connect
            List<WifiConfiguration> list = mainWifi.getConfiguredNetworks();
            for( WifiConfiguration i : list ) {
                if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                     mainWifi.disconnect();
                     mainWifi.enableNetwork(i.networkId, true);
                     mainWifi.reconnect();               

                     break;
                }           
             }*/
            
        }
    }
    
}

