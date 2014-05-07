package com.pdsd.blue_fi;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FileShareActivity extends Activity {

	// Constants.
	public final static String GO_TO_FILE_SHARE_ACTIVITY = "com.pdsd.blue_fi.GO_TO_FILE_SHARE_ACTIVITY";

	// Global variables.
	SharedPreferences preferences;
	Bundle extras;
	String ID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_share);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
		extras = getIntent().getExtras();
		if (extras != null) {
		    ID = extras.getString( GO_TO_FILE_SHARE_ACTIVITY );
			TextView t = (TextView)findViewById( R.id.file_share_activity_title );
			t.setText( ID );
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.file_share, menu);
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_file_share,
					container, false);
			return rootView;
		}
	}

}
