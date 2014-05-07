package com.pdsd.blue_fi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.annotation.SuppressLint;
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
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.pdsd.blue_fi.PairedDevicesAdapter;

//This is the Wi-Fi pairing activity.

@SuppressLint("NewApi")
public class WifiPairActivity extends Activity{

    // Debugging
    private static final String TAG = "WifiPairActivity";

	// Global variables.
	SharedPreferences preferences;
	Channel channel;
	WifiP2pManager manager;
	boolean isWifiP2pEnabled;
	boolean p2p_unsupported;
	BroadcastReceiver broadcastReceiver;
	WifiManager wifiManager;
	IntentFilter intentFilter;
	List<WifiP2pDevice> peers;
    PairedDevicesAdapter pairedDevicesAdapter;
    ListView pairedListView;
	
	// Constants.
	public final static String GO_TO_WIFI_ACTIVITY = "com.pdsd.blue_fi.wifiID";
    public static String DEVICE_ADDRESS = "com.pdsd.blue_fi.device_address";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi_pair);
		intentFilter = new IntentFilter();
		
		// Global variables at app-level.
		preferences = this.getSharedPreferences( "com.pdsd.blue_fi", Context.MODE_PRIVATE );
		
	    //  Indicates a change in the Wi-Fi P2P status.
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

	    // Indicates a change in the list of available peers.
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

	    // Indicates the state of Wi-Fi P2P connectivity has changed.
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

	    // Indicates this device's details have changed.
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

	    manager = (WifiP2pManager)getSystemService( Context.WIFI_P2P_SERVICE );
	    channel = manager.initialize(this, getMainLooper(), null);
	    isWifiP2pEnabled = false;
	    Button scanButton = (Button)findViewById( R.id.wifi_scan_button );
	    wifiManager = (WifiManager)this.getSystemService( Context.WIFI_SERVICE );
	    wifiManager.setWifiEnabled( true );
		p2p_unsupported = false;
		peers = new ArrayList<WifiP2pDevice>();
        pairedDevicesAdapter = new PairedDevicesAdapter( this );

        // Find and set up the ListView for paired devices
        pairedListView = (ListView)findViewById(R.id.wifi_paired_devices);
        pairedListView.setAdapter( pairedDevicesAdapter );
        pairedListView.setOnItemClickListener(mDeviceClickListener);

    	String noDevices = getResources().getText( R.string.none_paired ).toString();
    	pairedDevicesAdapter.add( noDevices );

        pairedDevicesAdapter.add( null ); // This right here is a line break.

	    scanButton.setOnClickListener( new OnClickListener() {
            public void onClick( View v ){
                doDiscovery();
            }
        } );

        Log.d( TAG, "onCreate()" );
	}
    
    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        initiateBroadcastReceiver();
        registerReceiver( broadcastReceiver, intentFilter );
        Log.d( TAG, "onResume()" );
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver( broadcastReceiver );
        Log.d( TAG, "onPause()" );
    }
    
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
        	//bluetoothAdapter.cancelDiscovery();

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

	public void goToDeviceActivity( View view ){
		Intent intent = new Intent( this, DeviceActivity.class );
		intent.putExtra( DEVICE_ADDRESS, ((TextView)view).getText() );
		startActivity( intent );
        Log.d( TAG, "goToDeviceActivity()" );
	}

    public void connect() {
        // Picking the first device found on the network.
        WifiP2pDevice device = (WifiP2pDevice)peers.get(0);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        manager.connect( channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
    			Toast.makeText( getApplicationContext(), "Connection failed. Retry!", Toast.LENGTH_SHORT ).show();
            }
        });
        Log.d( TAG, "connect()" );
    }
    
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {

        // InetAddress from WifiP2pInfo struct.
        try {
			InetAddress groupOwnerAddress = InetAddress.getByName( info.groupOwnerAddress.getHostAddress() );
			Log.d( TAG, groupOwnerAddress.toString() );
        } catch (UnknownHostException e) {
            Log.d( TAG, "Unknown Host Exception");
		}

        // After the group negotiation, we can determine the group owner.
        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a server thread and accepting
            // incoming connections.
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case,
            // you'll want to create a client thread that connects to the group
            // owner.
        }
        Log.d( TAG, "onConnectionInfoAvailable()" );
    }

    public void doDiscovery(){
        manager.discoverPeers( channel, new WifiP2pManager.ActionListener() {

	        @Override
	        public void onSuccess() {
	            // Code for when the discovery initiation is successful goes here.
	            // No services have actually been discovered yet, so this method
	            // can often be left blank.  Code for peer discovery goes in the
	            // onReceive method, detailed below.
	            Log.d( TAG, "WifiP2pManager.onSuccess()" );
	        }

	        @Override
	        public void onFailure(int reasonCode) {
	            // Code for when the discovery initiation fails goes here.
	            // Alert the user that something went wrong.
	        	String errorName;
	        	if( reasonCode == WifiP2pManager.P2P_UNSUPPORTED ){
	        		errorName = "P2P_UNSUPPORTED";
	        		p2p_unsupported = true;
	        	}
	        	else if( reasonCode == WifiP2pManager.ERROR )
	        		errorName = "ERROR";
	        	else if( reasonCode == WifiP2pManager.BUSY )
	        		errorName = "BUSY";
	        	else
	        		errorName = "Unknown Error";
	            Log.d( TAG, "WifiP2pManager.onFailure(" + errorName + ")" );
	        }

        });

    	String noDevices = getResources().getText( R.string.none_found ).toString();
    	pairedDevicesAdapter.add( noDevices );

    	Log.d( TAG, "doDiscovery()" );
    }
    
    private PeerListListener peerListListener = new PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            // Out with the old, in with the new.
            peers.clear();
            peers.addAll(peerList.getDeviceList());

            // If an AdapterView is backed by this data, notify it
            // of the change.  For instance, if you have a ListView of available
            // peers, trigger an update.
            // TODO: ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
            if (peers.size() == 0) {
                Log.d( TAG, "No devices found");
                return;
            }
            Log.d( TAG, "PeerListListener.onPeersAvailable()" );
        }
    };
    
    public void initiateBroadcastReceiver(){
	    broadcastReceiver = new BroadcastReceiver(){
	
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            String action = intent.getAction();
	            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
	                // Determine if Wifi P2P mode is enabled or not, alert
	                // the Activity.
	                // Check to see if Wi-Fi is enabled and notify appropriate activity
	                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
	                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
	                    isWifiP2pEnabled = true;
	        			Toast.makeText( getApplicationContext(), R.string.wifi_p2p_enabled, Toast.LENGTH_SHORT ).show();
	                } else {
	                    isWifiP2pEnabled = false;
	        			Toast.makeText( getApplicationContext(), R.string.wifi_p2p_disabled, Toast.LENGTH_SHORT ).show();
	                }
	            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
	
	    			Toast.makeText( getApplicationContext(), R.string.wifi_peer_list_changed, Toast.LENGTH_SHORT ).show();
	                // The peer list has changed!  We should probably do something about
	                // that.
	                // Call WifiP2pManager.requestPeers() to get a list of current peers
	    			manager.requestPeers( channel, peerListListener );
	
	            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
	
	    			Toast.makeText( getApplicationContext(), R.string.wifi_connection_state_changed, Toast.LENGTH_SHORT ).show();

	                if (manager == null) {
	                    return;
	                }

	                NetworkInfo networkInfo = (NetworkInfo) intent
	                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

	                if (networkInfo.isConnected()) {

	                    // We are connected with the other device, request connection
	                    // info to find group owner IP

	                   //TODO: manager.requestConnectionInfo( channel, connectionListener);
	                }
	                // Connection state changed!  We should probably do something about
	                // that.
	                // Respond to new connection or disconnections
	
	            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
	                // Respond to this device's wifi state changing
	    			Toast.makeText( getApplicationContext(), R.string.wifi_this_device_changed, Toast.LENGTH_SHORT ).show();
	/*                DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
	                        .findFragmentById(R.id.frag_list);
	                fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
	                        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));*/
	
	            }
	            Log.d( TAG, "BroadcastReceiver.onReceive()" );
	        }

	    };
    }
    
}
