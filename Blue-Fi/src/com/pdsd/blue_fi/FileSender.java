package com.pdsd.blue_fi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.pdsd.blue_fi.BluetoothChatService;
import com.pdsd.blue_fi.MessageAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

@SuppressLint({ "NewApi", "HandlerLeak" })
public class FileSender extends Activity{
	
	// Debugging.
	static final String TAG = "FileSender";
	
	//FileexplorerActivity smth = new FileexplorerActivity();  
	//int path = smth.x;
	EditText edittext;
	String txt = "hello";
	static String s;//here we go

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

    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE = 1;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d( TAG, "onCreate()" );
 
        Bundle b = getIntent().getExtras();
    	s = b.getString("key");

		//Get the text file
    	if( s != null ){
	    	Log.d( TAG, "Path is: " + s );
			File file = new File(s);

		//Read text from file
		StringBuilder text = new StringBuilder();

		try {
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    String line;

		    while ((line = br.readLine()) != null) {
		        text.append(line);
		        text.append('\n');
		    }
		    br.close();
		}
		catch (IOException e) {
			Log.d( TAG, "IOException", e );
		}
		
    	Intent intent = new Intent();
        intent.putExtra( "content", text.toString() );
        intent.putExtra( "filepath", s );
        setResult( FileexplorerActivity.REQUEST_CONTENT, intent );
        finish();
    	}
	}
	
	static String async( String filename ){
        Log.d( TAG, "async()" );

		//Get the text file
    	if( filename != null ){
	    	Log.d( TAG, "Path is: " + filename );
			File file = new File( filename );

			//Read text from file
			StringBuilder text = new StringBuilder();
	
			try {
			    BufferedReader br = new BufferedReader(new FileReader(file));
			    String line;
	
			    while ((line = br.readLine()) != null) {
			        text.append(line);
			        text.append('\n');
			    }
			    br.close();
			}
			catch (IOException e) {
				Log.d( TAG, "IOException", e );
			}
	        return text.toString();
    	}
    	else{
    		Log.d( TAG, "FilePath is null." );
            return null;
    	}
        
	}
     
}
