package blah;

enum ServerString {
	
	DATABASE_ERROR( "Error Connecting with the database: " ),
	RETRY( "Press R to retry and any other key to exit: " ),
	RETRY_KEY( "R" ),
	EXIT( "EXIT" ),
	WRONG_COMMAND_FORMAT( "GO TO HELL!!!" ),
	BLOCKING_MESSAGE( "Blocking " ),
	COMMAND_ADD_USER( "ADDUSER" ),
	COMMAND_DELETE_USER( "DELUSER" ),
	COMMAND_BLOCK_IP( "BLOCKIP" ),
	COMMAND_UNBLOCK_IP( "UNBLKIP" ),
	COMMAND_SHOW_BLOCKED_IPS( "SHOWBLK" ),
	COMMAND_ACTIVE_CLIENTS( "CLIENTS" ),
	COMMAND_DOES_NOT_EXIST( "Command does not exist!" );
	
	
	private String value;
	
	private ServerString( String value ) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	public static ServerString getServerString( String value ) {
		try {
			return valueOf( value );
		}
		catch ( Exception e ) {
			return COMMAND_DOES_NOT_EXIST;
		}
	}
}
