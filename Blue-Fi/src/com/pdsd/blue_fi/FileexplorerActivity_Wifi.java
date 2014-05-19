package com.pdsd.blue_fi;

import com.pdsd.blue_fi.DeviceActivity.AddressType;

import android.os.Bundle; 
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity; 
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent; 
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


@SuppressLint("HandlerLeak")
public class FileexplorerActivity_Wifi extends Activity {
	
	// Debugging.
	static final String TAG = "FileexplorerActivity";

	static final int REQUEST_PATH = 101;
	static final int REQUEST_CONTENT = 102;
 
	// Global variables.
	Bundle extras;
	BluetoothAdapter bluetoothAdapter;
    BluetoothChatService chatService;
    Handler handler;
	SharedPreferences preferences;
	String address, name, connectedDeviceName;
    StringBuffer outStringBuffer;
    String recordedFileName, playedFileName, delimiter, content, filepath;
	
	public String curFileName;
	public String wantedPath;
	public String curPath;
	int x = 0;///chestie stupida pentru layout
	Button sendButton;
	Button saveButton;
	
	EditText edittext;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d( TAG, "onCreate()" );
        setContentView(R.layout.activity_fileexplorer); 
        edittext = (EditText)findViewById(R.id.editText);
        sendButton = (Button)findViewById(R.id.sendButton);
        saveButton = (Button)findViewById(R.id.saveButton);
        if(x==0){
        	sendButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.GONE);
        }
        else{
        	sendButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.VISIBLE);
        	
        }
        extras = getIntent().getExtras();
		if( extras != null ){
		    name = extras.getString( MainActivity.DEVICE_NAME );
		    address = extras.getString( MainActivity.DEVICE_ADDRESS );
		}

        // Initialize the buffer for outgoing messages
        outStringBuffer = new StringBuffer( "" );
        delimiter = "-";

	    // The Handler that gets information back from the BluetoothChatService
	    handler = new Handler(){
	        @Override
	        public void handleMessage( Message msg ){
                Log.d( TAG, "handleMessage: " + msg.toString() );
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
	                //byte[] writeBuf = (byte[])msg.obj;
	                // construct a string from the buffer
	                //String writeMessage = new String( writeBuf );
	                break;
	            case BluetoothChatService.MESSAGE_READ:
	                byte[] readBuf = (byte[]) msg.obj;
	                // construct a string from the valid bytes in the buffer
	                String readMessage = new String( readBuf, 0, msg.arg1 );
	                // TODO: readMessage is file data
                	Log.d( TAG, "readMessage is " + readMessage );
	                if( readMessage.length() == 0 )
	                	Log.d( TAG, "readMessage.length() is 0" );
	                else{
		                String[] parts = readMessage.split( delimiter );
		                if( parts.length < 3 )
		                	Log.d( TAG, "Length is < 3");
	                	if( parts[0].equals( "file" ) && parts[1].equals( "send" ) ){
	                		filepath = parts[2];
	                		content = parts[3];
	                		for( int i = 4; i < parts.length; i += 1 )
	                			content += delimiter + parts[i];
                			savefile( null );
        	                Toast.makeText( getApplicationContext(), "File received.", Toast.LENGTH_SHORT ).show();
	                	}
                			/*
                		case "end_of_file":
                        	sendButton.setVisibility(View.VISIBLE);
                            saveButton.setVisibility(View.GONE);
                			break;*/
	                }
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
        
		switch( AddressType.getAddressType( address ) ){
		case Bluetooth:
			bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if( bluetoothAdapter == null ) {
			    // Device does not support Bluetooth.
				Toast.makeText( getApplicationContext(), R.string.bluetooth_not_supported, Toast.LENGTH_LONG ).show();
			}
			else{
				// Bluetooth is supported.
				// Toast.makeText( getApplicationContext(), R.string.bluetooth_supported, Toast.LENGTH_SHORT ).show();
				if( !bluetoothAdapter.isEnabled() ){
					bluetoothAdapter.enable();
				}
				connectDevice();
			}
			setTitle( name );
			break;
		case Wifi:
			break;
		case Unknown:
			break;
		}
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

    public void getfile(View view){ 
        Log.d( TAG, "getfile()" );
    	Intent intent1 = new Intent(this, FileChooser.class);
        startActivityForResult(intent1,REQUEST_PATH);
    }
 // Listen for results.
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d(TAG, "onActivityResult " + requestCode + " " + resultCode);
        switch (requestCode) {
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
                Log.d(TAG, "BT not enabled");
                Toast.makeText( this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT ).show();
                finish();
            }
            break;
            default:
            	break;
        }
    	switch( requestCode ){
    		// See which child activity is calling us back.
	        case REQUEST_PATH:
	        	if( data != null ){
	        		curFileName = data.getStringExtra("GetFileName");
	        		curPath = data.getStringExtra("GetPath");
	        		wantedPath=curPath +"/"+ curFileName;
	               	edittext.setText(wantedPath);//path of the choosen file
	        	}
	        	break;
	        case REQUEST_CONTENT:
	        	if( data != null ){
	    			content = data.getStringExtra( "content" );
	    			filepath = data.getStringExtra( "filepath" );
	    	    	sendMessage( "file" + delimiter + "send" + delimiter + filepath + delimiter + content );
	                Toast.makeText( getApplicationContext(), "File sent.", Toast.LENGTH_SHORT ).show();
	        	}
	    		break;
	    	default:
	    		break;
	        }
		}

    public void sendfile(View view){ 
        Log.d( TAG, "sendfile()" );
    	Intent intent1 = new Intent(this, FileSender.class);
    	Bundle b = new Bundle();
    	b.putString("key", wantedPath);
    	//put into your intent
    	intent1.putExtras(b);
        startActivityForResult(intent1,REQUEST_CONTENT);
    }

    public void savefile(View view){ 
        Log.d( TAG, "savefile()" );
    	Intent intent1 = new Intent(this, SaveToSDCard.class);//somesave activity
    	Bundle b = new Bundle();
    	b.putString( "content", content );
    	
    	String[] parts = filepath.split( "/" );
    	b.putString( "filepath", Environment.getExternalStorageDirectory().getPath() + "/" + parts[parts.length - 1] );
    	//put into your intent
    	intent1.putExtras(b);
        startActivity(intent1);
    }
	
	@Override
	public boolean onCreateOptionsMenu( Menu menu ){
        Log.d( TAG, "onCreateOptionsMenu()" );

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.file_share, menu);
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
	    }
	}

}