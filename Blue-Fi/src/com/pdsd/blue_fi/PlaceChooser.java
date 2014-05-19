package com.pdsd.blue_fi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class PlaceChooser extends Activity {

	public void check1( View v ){

    	Intent intent = new Intent();
        intent.putExtra( "choice", "one" );
		setResult( MessageActivity_Wifi.REQUEST_SEAT, intent );
		finish();
		
	}

	public void check2( View v ){

    	Intent intent = new Intent();
        intent.putExtra( "choice", "two" );
		setResult( MessageActivity_Wifi.REQUEST_SEAT, intent );
		finish();
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_place_chooser);
	}
}
