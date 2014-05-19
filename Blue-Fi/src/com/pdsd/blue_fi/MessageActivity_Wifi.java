package com.pdsd.blue_fi;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import com.pdsd.blue_fi.DeviceActivity.AddressType;
import com.pdsd.blue_fi.MessageAdapter;
import com.pdsd.blue_fi.Database;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

@SuppressLint({ "NewApi", "HandlerLeak" })
public class MessageActivity_Wifi extends Activity{
	
	// Debugging.
	static final String TAG = "MessageActivity";

	// Global variables.
	Bundle extras;
    Button sendButton;
    EditText outEditText;
    ListView messageListView;
    MessageAdapter messageAdapter;
	SharedPreferences preferences;
	String address, name, connectedDeviceName, receivedMessage;
    StringBuffer outStringBuffer;
    Database db;
    AddressType addressType;
    DatagramSocket sendSocket, receiveSocket;
    DatagramPacket sendPacket, receivePacket;
    InetAddress broadcastAddress;
    WifiManager wifi;
    int localSendPort, localReceivePort, remoteSendPort, remoteReceivePort;
	Intent intent;
    
    // Intent request codes
    static final int REQUEST_SEAT = 1;

	@Override
	protected void onCreate( Bundle savedInstanceState ){
		super.onCreate( savedInstanceState );
		Log.d( TAG, "onCreate()" );
		setContentView( R.layout.activity_message );

		extras = getIntent().getExtras();
		if( extras != null ){
		    name = extras.getString( MainActivity.DEVICE_NAME );
		    address = extras.getString( MainActivity.DEVICE_ADDRESS );
		    try{
		    broadcastAddress = InetAddress.getByName( extras.getString( WifiPairActivity.BROADCAST_ADDRESS ) );
		    }
		    catch( UnknownHostException e ){
		    	Log.d( TAG, "UnknownHostException" );
		    }
		    addressType = DeviceActivity.AddressType.getAddressType( address );
		}
		
		localSendPort = 40095;
		localReceivePort = 38785;
		remoteSendPort = 34065;
		remoteReceivePort = 56452;
		
        intent = new Intent( this, PlaceChooser.class );
        startActivityForResult( intent, REQUEST_SEAT );

		setTitle( name );
		
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
    
		switch( addressType ){
		case UdpBroadcast:
			break;
		case Wifi:
			break;
		default:
			break;
		}
		
		wifi = (WifiManager)getSystemService( Context.WIFI_SERVICE );
		if( wifi != null ){
		    WifiManager.MulticastLock lock = wifi.createMulticastLock("BlueFi");
		    lock.acquire();
		}
        
        messageAdapter = new MessageAdapter( this );
        messageListView.setAdapter( messageAdapter );
        List<DbMessage> list = db.getAllMessages( address );
        for( DbMessage message : list ){
        	messageAdapter.add( message.message, message.owner );
        }
		messageAdapter.notifyDataSetChanged();
	}

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d( TAG, "onActivityResult " + resultCode );
        if( resultCode == REQUEST_SEAT )
        	if( data.getStringExtra( "choice" ).equals( "two" ) ){
        		int aux;
        		aux = localSendPort;
        		localSendPort = remoteSendPort;
        		remoteSendPort = aux;
        		aux = localReceivePort;
        		localReceivePort = remoteReceivePort;
        		remoteReceivePort = aux;
        	}
        try{
        	sendSocket = new DatagramSocket( localSendPort );
        	receiveSocket = new DatagramSocket( localReceivePort );
			Log.d( TAG, "send " + Integer.valueOf( sendSocket.getLocalPort() ).toString() );
			Log.d( TAG, "receive " + Integer.valueOf( receiveSocket.getLocalPort() ).toString() );
			sendSocket.setBroadcast( true );
        }
        catch( Exception e ){
        	Log.d( TAG, "Exception", e );
        }
		receiveMessage();
    }

    public void connectDevice(){
        Log.d( TAG, "connectDevice( " + address + " )" );
    }

    class SendThread implements Runnable{
    	public String message;
    	public SendThread( String _message ){
    		message = _message;
    	}
    	@Override
    	public void run(){
            Log.d( TAG, "SendThread()" );
    		try{
				sendPacket = new DatagramPacket( message.getBytes(), message.length(), broadcastAddress, remoteReceivePort );
				sendSocket.send( sendPacket );
	        }
	        catch( SocketException e ){
	        	Log.d( TAG, "SocketException ", e );
	        }
	        catch( IOException e ){
	        	Log.d( TAG, "IOException ", e );
	        }
    		
    	}
    }

    class ReceiveThread implements Runnable{
    	public String message;
    	public ReceiveThread(){
    	}
    	@Override
    	public void run(){
            Log.d( TAG, "ReceiveThread()" );
    		
            while( true ){
	    		byte[] buf = new byte[1024];
	            try{
	    			receivePacket = new DatagramPacket( buf, buf.length );
	    			receiveSocket.receive( receivePacket );
	    			receivedMessage = receivePacket.getData().toString();
	    			messageAdapter.add( name + ": \n" + receivedMessage, MessageAdapter.OTHER );
	        		db.addMessage( new DbMessage( name + ": \n" + receivedMessage, MessageAdapter.OTHER ), broadcastAddress.toString() );
	    			messageAdapter.notifyDataSetChanged();
	            }
	            catch( IOException e ){
	            	Log.d( TAG, "IOException" );
	            }
            }
    		
    	}
    }
    
    public void sendMessage( String message ){
        Log.d( TAG, "sendMessage()" );
        Runnable r = new SendThread( message );
        new Thread( r ).start();
        messageAdapter.add( "Me: \n" + message, MessageAdapter.ME );
		db.addMessage( new DbMessage( "Me: \n" + message, MessageAdapter.ME ), broadcastAddress.toString() );
    }
    
    public void receiveMessage(){
        Log.d( TAG, "receiveMessage()" );
        ReceiveThread r = new ReceiveThread();
        Thread t = new Thread( r );
        t.start();
    }
}
