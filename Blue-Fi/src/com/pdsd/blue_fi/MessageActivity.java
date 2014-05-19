package com.pdsd.blue_fi;

import java.util.List;

import com.pdsd.blue_fi.BluetoothChatService;
import com.pdsd.blue_fi.DeviceActivity.AddressType;
import com.pdsd.blue_fi.MessageAdapter;
import com.pdsd.blue_fi.Database;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint({ "NewApi", "HandlerLeak" })
public class MessageActivity extends Activity{
	
	// Debugging.
	static final String TAG = "MessageActivity";

	// Global variables.
	Bundle extras;
	BluetoothAdapter bluetoothAdapter;
    BluetoothChatService chatService;
    Button sendButton;
    EditText outEditText;
    Handler handler;
    ListView messageListView;
    MessageAdapter messageAdapter;
    PairedDevicesAdapter p;
	SharedPreferences preferences;
	String address, name, connectedDeviceName;
    StringBuffer outStringBuffer;
    Database db;
    AddressType addressType;
    
    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE = 1;

	@Override
	protected void onCreate( Bundle savedInstanceState ){
		super.onCreate( savedInstanceState );
		Log.d( TAG, "onCreate()" );
		setContentView( R.layout.activity_message );

		extras = getIntent().getExtras();
		if( extras != null ){
		    name = extras.getString( MainActivity.DEVICE_NAME );
		    address = extras.getString( MainActivity.DEVICE_ADDRESS );
		    addressType = DeviceActivity.AddressType.getAddressType( address );
		}
		
		outEditText = (EditText) findViewById( R.id.edit_text_out );
		sendButton = (Button) findViewById( R.id.button_send );
        messageListView = (ListView)findViewById( R.id.message_list );
        messageListView.setPadding( 0, 0, 0, sendButton.getHeight() );
        sendButton.setOnClickListener( new OnClickListener(){
            public void onClick( View v ){
        		Log.d( TAG, "onClick()" );
                // Send a message using content of the edit text widget
                TextView view = (TextView)findViewById( R.id.edit_text_out );
                String message = view.getText().toString();
                sendMessage( message );
                view.setText( "" );
            }
        });

        db = new Database( this );

        // Initialize the buffer for outgoing messages
        outStringBuffer = new StringBuffer( "" );

	    // The Handler that gets information back from the BluetoothChatService
	    handler = new Handler(){
	        @Override
	        public void handleMessage( Message msg ){
                Log.d( TAG, "handleMessage: " + msg.toString() );
                String message;
	            switch( msg.what ){
	            case BluetoothChatService.MESSAGE_STATE_CHANGE:
	                Log.d( TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1 );
	                switch( msg.arg1 ){
	                case BluetoothChatService.STATE_CONNECTED:
	        			Toast.makeText( getApplicationContext(), "Connected.", Toast.LENGTH_SHORT ).show();
	                    break;
	                case BluetoothChatService.STATE_CONNECTING:
	        			Toast.makeText( getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT ).show();
	                    break;
	                case BluetoothChatService.STATE_LISTEN:
	                case BluetoothChatService.STATE_NONE:
	                    break;
	                }
	                break;
	            case BluetoothChatService.MESSAGE_WRITE:
	                byte[] writeBuf = (byte[])msg.obj;
	                // construct a string from the buffer
	                String writeMessage = new String( writeBuf );
	                message = "Me:\n" + writeMessage;
	                messageAdapter.add( message, MessageAdapter.ME );
	        		messageAdapter.notifyDataSetChanged();
	        		db.addMessage( new DbMessage( message, MessageAdapter.ME ), address );
	                break;
	            case BluetoothChatService.MESSAGE_READ:
	                byte[] readBuf = (byte[]) msg.obj;
	                // construct a string from the valid bytes in the buffer
	                String readMessage = new String( readBuf, 0, msg.arg1 );
	                message = name + ":\n" + readMessage;
	                messageAdapter.add( message, MessageAdapter.OTHER );
	        		messageAdapter.notifyDataSetChanged();
	        		db.addMessage( new DbMessage( message, MessageAdapter.OTHER ), address );
	                break;
	            case BluetoothChatService.MESSAGE_DEVICE_NAME:
	                // save the connected device's name
	                //connectedDeviceName = msg.getData().getString( address.substring( 0, address.length() - 17 ) );
	                Toast.makeText( getApplicationContext(), "Connected to " + name, Toast.LENGTH_SHORT ).show();
	                break;
	            case BluetoothChatService.MESSAGE_TOAST:
	                Toast.makeText( getApplicationContext(), msg.getData().getString( BluetoothChatService.TOAST ), Toast.LENGTH_SHORT ).show();
	                break;
	            }
	        }
	    };
	    
        // Initialize the BluetoothChatService to perform bluetooth connections
        chatService = new BluetoothChatService( this, handler );
        
		switch( addressType ){
		case Bluetooth:
			bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if( bluetoothAdapter == null ) {
			    // Device does not support Bluetooth.
				Toast.makeText( getApplicationContext(), R.string.bluetooth_not_supported, Toast.LENGTH_LONG ).show();
			}
			else{
				// Bluetooth is supported.
				if( !bluetoothAdapter.isEnabled() ){
					bluetoothAdapter.enable();
				}
				connectDevice();
			}
			setTitle( name );
			break;
		case UdpBroadcast:
			break;
		case Wifi:
			break;
		case Unknown:
			break;
		}
        
        messageAdapter = new MessageAdapter( this );
        messageListView.setAdapter( messageAdapter );
        List<DbMessage> list = db.getAllMessages( address );
        for( DbMessage message : list ){
        	messageAdapter.add( message.message, message.owner );
        }
		messageAdapter.notifyDataSetChanged();

	}
	
    public void ensureDiscoverable() {
        Log.d( TAG, "ensureDiscoverable()" );
        if (bluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d( TAG, "onActivityResult " + resultCode );
        switch( requestCode ){
        case BluetoothChatService.REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice();
            }
            break;
        case BluetoothChatService.REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if( resultCode == Activity.RESULT_OK ){
                // Bluetooth is now enabled, so set up a chat session
            }
            else{
                // User did not enable Bluetooth or an error occurred
                Log.d( TAG, "BT not enabled" );
                Toast.makeText( this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT ).show();
                finish();
            }
        }
    }
	
	@Override
	public boolean onCreateOptionsMenu( Menu menu ){
        Log.d( TAG, "onCreateOptionsMenu()" );

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.message, menu );
		return true;
	}

    @Override
    public boolean onOptionsItemSelected( MenuItem item ){
        Log.d( TAG, "onOptionsItemSelected()" );
        switch( item.getItemId() ){
        case R.id.connect_scan:
        	connectDevice();
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }

    public void connectDevice(){
        Log.d( TAG, "connectDevice( " + address + " )" );
    	if( BluetoothAdapter.checkBluetoothAddress( address ) ){
        	Log.d( TAG, "Address is good." );
            // Get the BluetoothDevice object
    		BluetoothDevice device = bluetoothAdapter.getRemoteDevice( address );
            // Attempt to connect to the device
            chatService.connect( device );
    	}
    	else{
        	Log.d( TAG, "Address is bad." );
    	}
    }

    public void sendMessage(String message) {
        Log.d( TAG, "sendMessage()" );
        // Check that we're actually connected before trying anything
        if( chatService.getState() != BluetoothChatService.STATE_CONNECTED ){
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if( message.length() > 0 ){
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            chatService.write( send );

            // Reset out string buffer to zero and clear the edit text field
            outStringBuffer.setLength( 0 );
            outEditText.setText( outStringBuffer);
        }
    }
}
