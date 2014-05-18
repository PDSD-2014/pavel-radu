package com.pdsd.blue_fi;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PairedDevicesAdapter extends BaseAdapter{
	
	// Debugging.
    static final String TAG = "PairedDevicesAdapter";
	
	Activity context;
	List<String> data;
	
	public PairedDevicesAdapter( Activity _context ){
		data = new ArrayList<String>();
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
        if( string == null ){
	        customView = layoutInflater.inflate( R.layout.line_break, parent, false);
        	customView.setOnClickListener( null );
        }
        else if( string == context.getResources().getText( R.string.none_found ).toString() || string == context.getResources().getText( R.string.none_paired ).toString() ){
        	customView = layoutInflater.inflate( android.R.layout.simple_list_item_1, parent, false);
        	TextView textView = (TextView)customView.findViewById( android.R.id.text1 );
        	textView.setText( string );
        	textView.setTextColor( 0xFF808080 );
        	textView.setOnClickListener( null );
        }
        else if( string == "com.pdsd.blue_fi.pair_buttons" ){
        	customView = layoutInflater.inflate( R.layout.pair_buttons, parent, false);       	
        }
        else{
        	customView = layoutInflater.inflate( android.R.layout.simple_list_item_1, parent, false);
        	((TextView)customView.findViewById( android.R.id.text1 )).setText( string );
        }
        return customView;
    }

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public String getItem(int position) {
		return data.get( position );
	}

	@Override
	public long getItemId(int position) {
		return data.indexOf( data.get( position ) );
	}

	public void add( String object ){
		data.add( object );
	}

}
