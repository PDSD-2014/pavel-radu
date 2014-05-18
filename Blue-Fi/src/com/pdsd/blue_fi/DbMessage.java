package com.pdsd.blue_fi;

public class DbMessage{

    // Debugging
    static final String TAG = "DbMessage";

	// Declarations.
	int id;
	String owner;
	String message;

	public DbMessage(){
		
	}

	public DbMessage( String _message, String _owner ){
		message = _message;
		owner = _owner;
	}
 
    @Override
    public String toString() {
        return "DbMessage [id=" + id + ", owner=" + owner + ", message=" + message + "]";
    }

}
