package com.pdsd.blue_fi;

import com.pdsd.blue_fi.BluetoothChatService;
import com.pdsd.blue_fi.DeviceActivity.AddressType;
import com.pdsd.blue_fi.MessageAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint({ "NewApi", "HandlerLeak" })
public class MessageActivity extends Activity {

	// Constants.
    public static String DEVICE_ADDRESS = "com.pdsd.blue_fi.device_address";

	// Global variables.
	SharedPreferences preferences;
	Bundle extras;
	String address;
	BluetoothAdapter bluetoothAdapter;
    BluetoothChatService chatService = null;
    StringBuffer outStringBuffer;
    EditText outEditText;
    Button sendButton;
    MessageAdapter messageAdapter;
    String connectedDeviceName;
    Handler handler;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
		extras = getIntent().getExtras();
		if (extras != null) {
		    address = extras.getString( DEVICE_ADDRESS );
		}
		
		outEditText = (EditText) findViewById(R.id.edit_text_out);
		sendButton = (Button) findViewById(R.id.button_send);
        sendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });

        // Initialize the buffer for outgoing messages
        outStringBuffer = new StringBuffer("");

		switch( AddressType.getAddressType( address ) ){
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
			setTitle( address.substring( 0, address.length() - 17 ) );
			break;
		case Wifi:
			break;
		case Unknown:
			break;
		}
		
	    // The Handler that gets information back from the BluetoothChatService
	    handler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            case BluetoothChatService.MESSAGE_STATE_CHANGE:
	                //Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
	                switch (msg.arg1) {
	                case BluetoothChatService.STATE_CONNECTED:
	        			Toast.makeText( getApplicationContext(), "Connected.", Toast.LENGTH_SHORT ).show();
	                    break;
	                case BluetoothChatService.STATE_CONNECTING:
	        			Toast.makeText( getApplicationContext(), "Connecting.", Toast.LENGTH_SHORT ).show();
	                    break;
	                case BluetoothChatService.STATE_LISTEN:
	                case BluetoothChatService.STATE_NONE:
	                    break;
	                }
	                break;
	            case BluetoothChatService.MESSAGE_WRITE:
	                byte[] writeBuf = (byte[]) msg.obj;
	                // construct a string from the buffer
	                String writeMessage = new String(writeBuf);
	                messageAdapter.add("Me:  " + writeMessage);
	                break;
	            case BluetoothChatService.MESSAGE_READ:
	                byte[] readBuf = (byte[]) msg.obj;
	                // construct a string from the valid bytes in the buffer
	                String readMessage = new String(readBuf, 0, msg.arg1);
	                messageAdapter.add(connectedDeviceName+":  " + readMessage);
	                break;
	            case BluetoothChatService.MESSAGE_DEVICE_NAME:
	                // save the connected device's name
	                connectedDeviceName = msg.getData().getString( address.substring( 0, address.length() - 17 ) );
	                Toast.makeText(getApplicationContext(), "Connected to "
	                               + connectedDeviceName, Toast.LENGTH_SHORT).show();
	                break;
	            case BluetoothChatService.MESSAGE_TOAST:
	                Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothChatService.TOAST),
	                               Toast.LENGTH_SHORT).show();
	                break;
	            }
	        }
	    };

        // Initialize the BluetoothChatService to perform bluetooth connections
        chatService = new BluetoothChatService(this, handler);
	}


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case BluetoothChatService.REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, true);
            }
            break;
        case BluetoothChatService.REQUEST_CONNECT_DEVICE_INSECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, false);
            }
            break;
        case BluetoothChatService.REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
            } else {
                // User did not enable Bluetooth or an error occurred
                //Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.message, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    private void connectDevice(Intent data, boolean secure) {
        // Get the BluetoothDevice object
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        chatService.connect( device, secure );
    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if( chatService.getState() != BluetoothChatService.STATE_CONNECTED ){
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            chatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            outStringBuffer.setLength(0);
            outEditText.setText( outStringBuffer);
        }
    }
    
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_message,
					container, false);
			return rootView;
		}
	}

}
