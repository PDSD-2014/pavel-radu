package com.pdsd.blue_fi;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class SaveToSDCard extends Activity {
	
	// Debugging.
	static final String TAG = "SaveToSDCard";
	
	String s, content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Log.d( TAG, "onCreate()" );
		setContentView(R.layout.save_file_in);
		Bundle b = getIntent().getExtras();
    	s = b.getString( "filepath" );// calea
    	content = b.getString( "content" );// conþinutul
		
		///////////////////////
		//String filename = "filename.txt";//cred ca va trebui trimis si asta printr-un mesaj?!... Done.
		///
		File file = new File( s );
		Log.d( TAG, s );
		FileOutputStream fos;
		byte[] data = new String( content ).getBytes();
		try{
			if( !file.exists() )
				file.createNewFile();
		    fos = new FileOutputStream(file);
		    fos.write(data);
		    fos.flush();
		    fos.close();
		    finish();
		}
		catch( FileNotFoundException e ){
		    Log.d( TAG, "FileNotFoundException " + s );
		}
		catch( IOException e ){
		    Log.d( TAG, "IOException" );
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        Log.d( TAG, "onCreateOptionsMenu()" );

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        Log.d( TAG, "onOptionsItemSelected()" );
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
		

}
