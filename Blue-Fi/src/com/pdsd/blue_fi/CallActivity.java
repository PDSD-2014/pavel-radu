package com.pdsd.blue_fi;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.pdsd.blue_fi.BluetoothChatService;
import com.pdsd.blue_fi.DeviceActivity.AddressType;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint({ "NewApi", "HandlerLeak" })
public class CallActivity extends Activity{
	
	// Debugging.
	static final String TAG = "CallActivity";

	// Global variables.
	Bundle extras;
	BluetoothAdapter bluetoothAdapter;
    BluetoothChatService chatService;
    Button sendButton;
    Handler handler;
	SharedPreferences preferences;
	String address, name, connectedDeviceName;
    StringBuffer outStringBuffer;
    MediaRecorder recorder;
    MediaPlayer player, ringRing;
    String recordedFileName, playedFileName, delimiter, content;
    Timer timer;

    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE = 1;
    
    static final int ALL_GOOD = 0;
    static final int NOT_CONNECTED = 1;
    
    void ring(){
        Log.d( TAG, "ring()" );
	    ringRing = MediaPlayer.create( this, R.raw.ringtone );
	    ringRing.setLooping( true );
	    ringRing.start();
    }
    void stopRing(){
        Log.d( TAG, "stopRing()" );
        if( ringRing != null && ringRing.isPlaying() ){
	        ringRing.release();
	        ringRing = null;
        }
    }
    
    void startPlaying() {
        Log.d( TAG, "startPlaying()" );
        if( !player.isPlaying() ){
	        player = new MediaPlayer();
	        try {
	        	player.setLooping( true );
	            player.setDataSource( playedFileName );
	            player.setDisplay( null );
	            player.prepare();
	            player.start();
	        } catch( IOException e ){
	            Log.d( TAG, "prepare() failed" );
	        }
        }
    }

    void stopPlaying() {
        Log.d( TAG, "stopPlaying()" );
        if( player != null && player.isPlaying() ){
	        player.release();
	        player = null;
        }
    }

    void startRecording() {
        Log.d( TAG, "startRecording()" );
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile( recordedFileName );
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e( TAG, "prepare() failed" );
        }

        recorder.start();
    }

    void stopRecording() {
        Log.d( TAG, "stopRecording()" );
        recorder.stop();
        recorder.release();
        recorder = null;
    }
    
    public class Update extends TimerTask{
    	public void run(){
    		Log.d( TAG, "Call update" );
    		sendfile();
    	}
    }
    
	@Override
	protected void onCreate( Bundle savedInstanceState ){
        Log.d( TAG, "onCreate()" );
		super.onCreate( savedInstanceState );
		Log.d( TAG, "onCreate()" );
		setContentView( R.layout.activity_call );

		extras = getIntent().getExtras();
		if( extras != null ){
		    name = extras.getString( MainActivity.DEVICE_NAME );
		    address = extras.getString( MainActivity.DEVICE_ADDRESS );
		}

        player = new MediaPlayer();
        recorder = new MediaRecorder();
        // Initialize the buffer for outgoing messages
        outStringBuffer = new StringBuffer( "" );

        recordedFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        recordedFileName += "/com.pdsd.blue_fi.recorded.3gp";
        playedFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        playedFileName += "/com.pdsd.blue_fi.played.3gp";
        delimiter = "-";
        
        ( (LinearLayout)findViewById( R.id.call_screen_layout ) ).setVisibility( View.VISIBLE );
        ( (LinearLayout)findViewById( R.id.answer_decline_layout ) ).setVisibility( View.GONE );
        ( (ImageView)findViewById( R.id.call_button ) ).setVisibility( View.VISIBLE );
        ( (ImageView)findViewById( R.id.end_call_button ) ).setVisibility( View.GONE );
        ( (ImageView)findViewById( R.id.end_calling_button ) ).setVisibility( View.GONE );

        ( (TextView)findViewById( R.id.call_id ) ).setText( name );
        ( (TextView)findViewById( R.id.call_id2 ) ).setText( name );

        ( (TextView)findViewById( R.id.call_state ) ).setText( "Idle" );
        ( (TextView)findViewById( R.id.call_state2 ) ).setText( "Idle" );
        
	    // The Handler that gets information back from the BluetoothChatService
	    handler = new Handler(){
	        @Override
	        public void handleMessage( Message msg ){
                Log.d( TAG, "handleMessage( " + msg.what + " )" );
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
	                // TODO: writeMessage is voice data
	                
	                break;
	            case BluetoothChatService.MESSAGE_READ:
	                byte[] readBuf = (byte[]) msg.obj;
	                // construct a string from the valid bytes in the buffer
	                String readMessage = new String( readBuf, 0, msg.arg1 );
	                Log.d( TAG, "handleMessage: " + readMessage );
	                // TODO: readMessage is voice data
	                String[] parts = readMessage.split( delimiter );
	                if( parts.length < 2 )
	                	Log.d( TAG, "parts.length < 2, it is " + parts.length + " " + parts[0] );
	                else{
	                	if( parts[0].equals( "call" ) ){
	                		switch( parts[1] ){
	                		case "update":
	                			if( parts.length > 2 ){
		                			content = new String( parts[2] );
			                		for( int i = 3; i < parts.length; i += 1 )
			                			content += delimiter + parts[i];
		                			savefile( null );
	                			}
	                			break;
	                		case "call":
	                			receiveCall();
	                			break;
	                		case "end_call":
	                	        ( (LinearLayout)findViewById( R.id.call_screen_layout ) ).setVisibility( View.VISIBLE );
	                	        ( (LinearLayout)findViewById( R.id.answer_decline_layout ) ).setVisibility( View.GONE );
	                	        ( (ImageView)findViewById( R.id.call_button ) ).setVisibility( View.VISIBLE );
	                	        ( (ImageView)findViewById( R.id.end_call_button ) ).setVisibility( View.GONE );
	                	        ( (ImageView)findViewById( R.id.end_calling_button ) ).setVisibility( View.GONE );
	                	        ( (TextView)findViewById( R.id.call_state ) ).setText( "Idle" );
	                	        ( (TextView)findViewById( R.id.call_state2 ) ).setText( "Idle" );
	                			break;
	                		case "end_calling":
	                	        ( (LinearLayout)findViewById( R.id.call_screen_layout ) ).setVisibility( View.VISIBLE );
	                	        ( (LinearLayout)findViewById( R.id.answer_decline_layout ) ).setVisibility( View.GONE );
	                	        ( (ImageView)findViewById( R.id.call_button ) ).setVisibility( View.VISIBLE );
	                	        ( (ImageView)findViewById( R.id.end_call_button ) ).setVisibility( View.GONE );
	                	        ( (ImageView)findViewById( R.id.end_calling_button ) ).setVisibility( View.GONE );
	                	        ( (TextView)findViewById( R.id.call_state ) ).setText( "Idle" );
	                	        ( (TextView)findViewById( R.id.call_state2 ) ).setText( "Idle" );
	                	        stopRing();
	                			break;
	                		case "answer":
	                	        ( (LinearLayout)findViewById( R.id.call_screen_layout ) ).setVisibility( View.VISIBLE );
	                	        ( (LinearLayout)findViewById( R.id.answer_decline_layout ) ).setVisibility( View.GONE );
	                	        ( (ImageView)findViewById( R.id.call_button ) ).setVisibility( View.GONE );
	                	        ( (ImageView)findViewById( R.id.end_call_button ) ).setVisibility( View.VISIBLE );
	                	        ( (ImageView)findViewById( R.id.end_calling_button ) ).setVisibility( View.GONE );
	                	        ( (TextView)findViewById( R.id.call_state ) ).setText( "Connected." );
	                	        ( (TextView)findViewById( R.id.call_state2 ) ).setText( "Connected." );
	                	        startRecording();
	                	        startPlaying();
	                			// timer = new Timer();
	                			// timer.schedule( new Update(), 0, 1000 );
	                			break;
	                		case "decline":
	                	        ( (LinearLayout)findViewById( R.id.call_screen_layout ) ).setVisibility( View.VISIBLE );
	                	        ( (LinearLayout)findViewById( R.id.answer_decline_layout ) ).setVisibility( View.GONE );
	                	        ( (ImageView)findViewById( R.id.call_button ) ).setVisibility( View.VISIBLE );
	                	        ( (ImageView)findViewById( R.id.end_call_button ) ).setVisibility( View.GONE );
	                	        ( (ImageView)findViewById( R.id.end_calling_button ) ).setVisibility( View.GONE );
	                	        ( (TextView)findViewById( R.id.call_state ) ).setText( "Idle" );
	                	        ( (TextView)findViewById( R.id.call_state2 ) ).setText( "Idle" );
	                			break;
	                		}
	                	}
	                }
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
				//connectDevice();
			}
			setTitle( name );
			break;
		case Wifi:
			break;
		case Unknown:
			break;
		}

	}
	
	public void sendfile(){ 
        Log.d( TAG, "sendfile()" );
    	content = FileSender.async( recordedFileName );
		sendMessage( "call" + delimiter + "update" + delimiter + content );
    }

    public void savefile( View view ){ 
        Log.d( TAG, "savefile()" );
    	Intent intent1 = new Intent( this, SaveToSDCard.class );//somesave activity
    	Bundle b = new Bundle();
    	b.putString( "content", content );
    	b.putString( "filepath", playedFileName );
    	//put into your intent
    	intent1.putExtras( b );
        startActivity( intent1 );
    }
	
    public void ensureDiscoverable() {
        Log.d( TAG, "ensureDiscoverable()" );
        if( bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE ){
            Intent discoverableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE );
            discoverableIntent.putExtra( BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300 );
            startActivity( discoverableIntent );
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
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

    public int sendMessage(String message) {
        Log.d( TAG, "sendMessage()" );
        // Check that we're actually connected before trying anything
        if( chatService.getState() != BluetoothChatService.STATE_CONNECTED ){
            return NOT_CONNECTED;
        }

        // Check that there's actually something to send
        if( message.length() > 0 ){
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            chatService.write( send );

            // Reset out string buffer to zero and clear the edit text field
            outStringBuffer.setLength( 0 );
        }
        return ALL_GOOD;
    }

    public void receiveCall(){
	    Log.d( TAG, "receiveCall()" );
        ( (LinearLayout)findViewById( R.id.call_screen_layout ) ).setVisibility( View.GONE );
        ( (LinearLayout)findViewById( R.id.answer_decline_layout ) ).setVisibility( View.VISIBLE );
        ( (ImageView)findViewById( R.id.call_button ) ).setVisibility( View.GONE );
        ( (ImageView)findViewById( R.id.end_call_button ) ).setVisibility( View.GONE );
        ( (ImageView)findViewById( R.id.end_calling_button ) ).setVisibility( View.GONE );
        ( (TextView)findViewById( R.id.call_state ) ).setText( "Receiving call..." );
        ( (TextView)findViewById( R.id.call_state2 ) ).setText( "Receiving call..." );
        ring();
    }
    
	public void answer( View v ){
	    Log.d( TAG, "answer()" );
        if( sendMessage( "call" + delimiter + "answer" ) == ALL_GOOD ){
	        ( (LinearLayout)findViewById( R.id.call_screen_layout ) ).setVisibility( View.VISIBLE );
	        ( (LinearLayout)findViewById( R.id.answer_decline_layout ) ).setVisibility( View.GONE );
	        ( (ImageView)findViewById( R.id.call_button ) ).setVisibility( View.GONE );
	        ( (ImageView)findViewById( R.id.end_call_button ) ).setVisibility( View.VISIBLE );
	        ( (ImageView)findViewById( R.id.end_calling_button ) ).setVisibility( View.GONE );
	        ( (TextView)findViewById( R.id.call_state ) ).setText( "Connected." );
	        ( (TextView)findViewById( R.id.call_state2 ) ).setText( "Connected." );
	        stopRing();
		    startRecording();
		    startPlaying();
			// timer = new Timer();
			// timer.schedule( new Update(), 0, 1000 );
        }
	}
	
	public void decline( View v ){
	    Log.d( TAG, "decline()" );
	    if( sendMessage( "call" + delimiter + "decline" ) == ALL_GOOD ){
	        ( (LinearLayout)findViewById( R.id.call_screen_layout ) ).setVisibility( View.VISIBLE );
	        ( (LinearLayout)findViewById( R.id.answer_decline_layout ) ).setVisibility( View.GONE );
	        ( (ImageView)findViewById( R.id.call_button ) ).setVisibility( View.VISIBLE );
	        ( (ImageView)findViewById( R.id.end_call_button ) ).setVisibility( View.GONE );
	        ( (ImageView)findViewById( R.id.end_calling_button ) ).setVisibility( View.GONE );
	        ( (TextView)findViewById( R.id.call_state ) ).setText( "Idle" );
	        ( (TextView)findViewById( R.id.call_state2 ) ).setText( "Idle" );
	        stopRing();
	    }
	}
	
	public void call( View v ){
	    Log.d( TAG, "call()" );
        if( sendMessage( "call" + delimiter + "call" ) == ALL_GOOD ){
	        ( (LinearLayout)findViewById( R.id.call_screen_layout ) ).setVisibility( View.VISIBLE );
	        ( (LinearLayout)findViewById( R.id.answer_decline_layout ) ).setVisibility( View.GONE );
	        ( (ImageView)findViewById( R.id.call_button ) ).setVisibility( View.GONE );
	        ( (ImageView)findViewById( R.id.end_call_button ) ).setVisibility( View.GONE );
	        ( (ImageView)findViewById( R.id.end_calling_button ) ).setVisibility( View.VISIBLE );
	        ( (TextView)findViewById( R.id.call_state ) ).setText( "Calling..." );
	        ( (TextView)findViewById( R.id.call_state2 ) ).setText( "Calling..." );
        }
	}
	
	public void end_calling( View v ){
	    Log.d( TAG, "end_calling()" );
        if( sendMessage( "call" + delimiter + "end_calling" ) == ALL_GOOD ){
	        ( (LinearLayout)findViewById( R.id.call_screen_layout ) ).setVisibility( View.VISIBLE );
	        ( (LinearLayout)findViewById( R.id.answer_decline_layout ) ).setVisibility( View.GONE );
	        ( (ImageView)findViewById( R.id.call_button ) ).setVisibility( View.VISIBLE );
	        ( (ImageView)findViewById( R.id.end_call_button ) ).setVisibility( View.GONE );
	        ( (ImageView)findViewById( R.id.end_calling_button ) ).setVisibility( View.GONE );
	        ( (TextView)findViewById( R.id.call_state ) ).setText( "Idle" );
	        ( (TextView)findViewById( R.id.call_state2 ) ).setText( "Idle" );
        }
	}
	
	public void end_call( View v ){
	    Log.d( TAG, "end_call()" );
        if( sendMessage( "call" + delimiter + "end_call" ) == ALL_GOOD ){
	        ( (LinearLayout)findViewById( R.id.call_screen_layout ) ).setVisibility( View.VISIBLE );
	        ( (LinearLayout)findViewById( R.id.answer_decline_layout ) ).setVisibility( View.GONE );
	        ( (ImageView)findViewById( R.id.call_button ) ).setVisibility( View.VISIBLE );
	        ( (ImageView)findViewById( R.id.end_call_button ) ).setVisibility( View.GONE );
	        ( (ImageView)findViewById( R.id.end_calling_button ) ).setVisibility( View.GONE );
	        ( (TextView)findViewById( R.id.call_state ) ).setText( "Idle" );
	        ( (TextView)findViewById( R.id.call_state2 ) ).setText( "Idle" );
	        stopRecording();
		    //stopPlaying();
        }
	}
}


