package com.pdsd.blue_fi;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MessageAdapter extends BaseAdapter{
	
	// Debugging.
    static final String TAG = "MessageAdapter";
	
    // Constants.
    static final String ME = "com.pdsd.blue_fi.me";
    static final String OTHER = "com.pdsd.blue_fi.other";
    
	Activity context;
	List<String> data;
	List<String> ownership;
	
	public MessageAdapter( Activity _context ){
		data = new ArrayList<String>();
		ownership = new ArrayList<String>();
		context = _context;
	}

	@Override
    public View getView( int position, View convertView, ViewGroup parent ){

		// Declarations.
        LayoutInflater layoutInflater;
        String string;
		View customView;

        layoutInflater = (LayoutInflater)context.getLayoutInflater();
        string = data.get( position );
        if( ownership.get( position ).equals( ME ) ){
	        customView = layoutInflater.inflate( R.layout.right_message, parent, false);
        	((TextView)customView.findViewById( R.id.right_message_text )).setText( string );
        }
        else{
	        customView = layoutInflater.inflate( R.layout.left_message, parent, false);
        	((TextView)customView.findViewById( R.id.left_message_text )).setText( string );
        }
        return customView;
    }

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.indexOf( data.get( position ) );
	}

	@Override
	public long getItemId(int position) {
		return data.indexOf( data.get( position ) );
	}

	public void add( String object, String who ){
		Log.d( TAG, "add( " + object + ", " + who + " );" );
		data.add( object );
		ownership.add( who );
	}

}
