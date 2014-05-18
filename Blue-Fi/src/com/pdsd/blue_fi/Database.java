package com.pdsd.blue_fi;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
 

import com.pdsd.blue_fi.DbMessage;
 

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
 
public class Database extends SQLiteOpenHelper{

    // Debugging
    static final String TAG = "Database";
 
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "MessageDB";
    
    MessageDigest digester;
   
    public Database( Context context ){
        super( context, DATABASE_NAME, null, DATABASE_VERSION );  
    }
 
    @Override
    public void onCreate( SQLiteDatabase db ){
    	Log.d( TAG, "onCreate()" );
        // SQL statement to create message table
        String CREATE_MESSAGE_TABLE = "CREATE TABLE IF NOT EXISTS messages(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                "owner TEXT, "+
                "message TEXT )";
 
        // create messages table
        db.execSQL( CREATE_MESSAGE_TABLE );
    }
    
    public void initDigester( MessageDigest digester, String address, String md5 ){
    	try{
    		digester = MessageDigest.getInstance( "MD5" );
    	}
    	catch( NoSuchAlgorithmException e ){
    		Log.d( TAG, "No Such Algorithm" );
    	}
    	digester.update( address.getBytes() );
    	try{
    		md5 = new String( digester.digest(), "UTF-8" );
    	}
    	catch( UnsupportedEncodingException e ){
    		Log.d( TAG, "UnsupportedEncodingException" );
    	}
    }
    
    public void createDatabase( SQLiteDatabase db, String address ){
    	String md5 = new String();
    	initDigester( digester, address, md5 );
        // SQL statement to create message table
        String CREATE_MESSAGE_TABLE = "CREATE TABLE IF NOT EXISTS messages" + md5 + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                "owner TEXT, "+
                "message TEXT )";
 
        // create messages table
        db.execSQL( CREATE_MESSAGE_TABLE );
    }
 
    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ){
    	Log.d( TAG, "onUpgrade()" );
        // Drop older messages table if existed
        db.execSQL( "DROP TABLE IF EXISTS messages" );
 
        // create fresh messages table
        this.onCreate( db );
    }
    //---------------------------------------------------------------------
 
    /**
     * CRUD operations (create "add", read "get", update, delete) message + get all messages + delete all messages
     */
 
    // Messages table name
    private static final String TABLE_MESSAGES = "messages";
 
    // Messages Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_OWNER = "owner";
    private static final String KEY_MESSAGE = "message";
 
    private static final String[] COLUMNS = {KEY_ID, KEY_OWNER, KEY_MESSAGE};
 
    public void addMessage( DbMessage message, String address ){
    	String md5 = new String();
    	initDigester( digester, address, md5 );
        Log.d( TAG, "addMessage( " + message.toString() + " )" );
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        this.onCreate( db );
        
        createDatabase( db, address );
 
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put( KEY_OWNER, message.owner );
        values.put( KEY_MESSAGE, message.message );
 
        // 3. insert
        db.insert( TABLE_MESSAGES + md5, // table
                null, //nullColumnHack
                values ); // key/value -> keys = column names/ values = column values
 
        // 4. close
        db.close(); 
    }
 
    public DbMessage getMessage( int id, String address ){
    	String md5 = new String();
    	initDigester( digester, address, md5 );
 
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        this.onCreate( db );
        createDatabase( db, address );
 
        // 2. build query
        Cursor cursor = 
                db.query( TABLE_MESSAGES + md5, // a. table
                COLUMNS, // b. column names
                " id = ?", // c. selections 
                new String[]{ String.valueOf(id) }, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null ); // h. limit
 
        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();
 
        // 4. build message object
        DbMessage message = new DbMessage();
        message.id = Integer.parseInt( cursor.getString( 0 ) );
        message.owner = cursor.getString( 1 );
        message.message = cursor.getString( 2 );
 
        Log.d( "getMessage( " + id + " )", message.toString() );
 
        // 5. return message
        return message;
    }
 
    // Get All Messages
    public List<DbMessage> getAllMessages( String address ){
    	String md5 = new String();
    	initDigester( digester, address, md5 );
        List<DbMessage> messages = new LinkedList<DbMessage>();
        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_MESSAGES + md5;
 
        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery( query, null );
        createDatabase( db, address );
 
        // 3. go over each row, build message and add it to list
        DbMessage message = null;
        if (cursor.moveToFirst()) {
            do {
            	message = new DbMessage();
                message.id = Integer.parseInt( cursor.getString( 0 ) );
                message.owner = cursor.getString( 1 );
                message.message = cursor.getString( 2 );
 
                // Add message to messages
            	messages.add(message);
            } while (cursor.moveToNext());
        }
 
        Log.d( "getAllMessages()", messages.toString() );
 
        // return messages
        return messages;
    }
 
     // Updating single message
    public int updateMessage( DbMessage message, String address ){
    	String md5 = new String();
    	Log.d( TAG, "updateMessage()" );
    	initDigester( digester, address, md5 );
 
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        createDatabase( db, address );
 
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put( "owner", message.owner );
        values.put( "message", message.message );
 
        // 3. updating row
        int i = db.update(TABLE_MESSAGES + md5, //table
                values, // column/value
                KEY_ID+" = ?", // selections
                new String[] { String.valueOf( message.id ) }); //selection args
 
        // 4. close
        db.close();
 
        return i;

    }
 
    // Deleting single message
    public void deleteMessage( DbMessage message, String address ){
    	String md5 = new String();
    	initDigester( digester, address, md5 );
 
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        createDatabase( db, address );
 
        // 2. delete
        db.delete(TABLE_MESSAGES + md5,
                KEY_ID+" = ?",
                new String[] { String.valueOf( message.id ) });
 
        // 3. close
        db.close();
 
        Log.d( TAG, "deleteMessage( " + message.toString() + " )" );
 
    }
}