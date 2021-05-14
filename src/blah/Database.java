package blah;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Database {
	private static Connection connection;
	private static Statement statement;
	private static ResultSet users;
	/**
	 * This function is used to initiate the connection with the database and it should always be the first method to call.
	 * @throws SQLException if a database access error occurs
	 */
	public static final void connect() throws SQLException {
		connection = DriverManager.getConnection( "jdbc:ucanaccess://database.accdb" );
		statement = connection.createStatement();
		statement.executeUpdate( "UPDATE User SET online = false;" );
		retriveUsers();
	}
	
	public static final void markMessagesAsRead( String sender, String receiver ) throws SQLException {
		statement.executeUpdate( "UPDATE Message SET Received = true WHERE Receiver = \"" + receiver + "\" AND Sender = \"" + sender + "\";" );
	}
	
	public static final ArrayList<Message> getUserMesseges( String sender, String receiver ) throws SQLException {
		checkConnectionWithDatabase();
		ResultSet rows = getMessagesQuery( sender, receiver ).executeQuery();
		return getMessagesFromDatabase( rows );
	}
	
	private static final PreparedStatement getMessagesQuery( String sender, String receiver ) throws SQLException {
		PreparedStatement preparedStatment = connection.prepareStatement( "SELECT * from Message WHERE (Sender = ? and Receiver = ?) or (Sender = ? and Receiver = ?) ORDER BY DateSent;" );
		preparedStatment.setString( 1, sender );
		preparedStatment.setString( 2, receiver );
		preparedStatment.setString( 4, sender );
		preparedStatment.setString( 3, receiver );
		return preparedStatment;
	}
	
	private static final ArrayList<Message> getMessagesFromDatabase( ResultSet rows ) throws SQLException {
		ArrayList<Message> messages = new ArrayList<>();
		while ( rows.next() ) {
			Calendar gc = new Calendar.Builder().setInstant( rows.getTimestamp( "DateSent" ).getTime() ).build();
			String date = gc.get( Calendar.DAY_OF_MONTH ) + "/" + gc.get( Calendar.MONTH ) + "/" + gc.get( Calendar.YEAR ) + " " + gc.get( Calendar.HOUR ) + ":" + 
					gc.get( Calendar.MINUTE ) + ":" + gc.get( Calendar.SECOND ) + " " + (gc.get( Calendar.AM_PM ) == 0 ? "AM" : "PM");
			Message m = new Message( rows.getInt( "MID" ), rows.getString( "Sender" ), rows.getString( "Receiver" ), rows.getString( "Content" ), date, rows.getBoolean( "Received" ), rows.getBoolean( "Deleted" ) );
			if ( !m.isDeleted() ) 
				messages.add( m  );
		}
		return messages;
	}
	
	public static final void addUser( String username, String password ) {
		password = hash( password );
		try {
			checkConnectionWithDatabase();
			if ( !username.matches( "([A-Z]|[a-z]).*" ) )
				throw new SQLException( "username must start with an English letter and not excced 30 characters" );
			retriveUsers();
			users.next();
			users.moveToInsertRow();
			users.updateString( "username", username );
			users.updateString( "password", password );
			users.insertRow();
			users.close();
			retriveUsers();
		} catch ( SQLException e ) {
			retriveUsers();
			e.printStackTrace();
			System.out.println( e.getMessage() );
		}
	}
	
	private static final String hash( String password ) {
		String securePassword = "";
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			byte[] temp = messageDigest.digest( password.getBytes() );
			StringBuffer sb = new StringBuffer();
            for( int i=0; i < temp.length; i++ ) {
                sb.append( Integer.toString( ( temp[i] & 0xff ) + 0x100, 16 ).substring( 1 ) );
            }
            securePassword = sb.toString();
		} catch ( NoSuchAlgorithmException e1 ) {
			e1.printStackTrace();
			return password;
		}
		return securePassword;
	}

	public static final void deleteUser( String username ) {
		try {
			checkConnectionWithDatabase();
			while ( users.next() ) {
				if ( users.getString( "username" ).equals( username ) ) {
					users.deleteRow();
					break;
				}
			}
			retriveUsers();
		} catch ( SQLException e ) {
			retriveUsers();
			e.printStackTrace();
		}
		
	}

	private static final void retriveUsers() {
		try {
			users = connection.createStatement( ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE ).executeQuery( "SELECT * FROM user;" );
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
	}
	
	public static final boolean logUserOn( String username, String password ) {
		return logUser( username, password, true );
	}
	
	public static final boolean logUserOff( String username ) {
		return logUser( username, null, false );
	}
	
	private static final boolean logUser( String username, String password, boolean logOn )  {
		if ( logOn )
			password = hash( password );
		try {
			checkConnectionWithDatabase();
			retriveUsers();
			while ( users.next() ) {
				if ( users.getString( "username" ).equals( username ) && ( !logOn || ( users.getString( "password" ).equals( password ) && !users.getBoolean( "online" ) ) ) ) {
					users.updateBoolean( "online", logOn );
					users.updateRow();
					users.refreshRow();
					retriveUsers();
					return true;
				}
			}
			retriveUsers();
			return false;
		} catch ( SQLException e ) {
			retriveUsers();
			e.printStackTrace();
			return false;
		}
	}
	
	public static final ArrayList<String> getOtherUsersForChat( String username ) {
		ArrayList<String> userArrayList = new ArrayList<>();
		try {
			checkConnectionWithDatabase();
			retriveUsers();
			while ( users.next() )
				if ( !users.getString( "username" ).equals( username ) ) {
					String online = users.getBoolean( "online" ) ? "1" : "0";
					userArrayList.add( online + users.getString( "username" ) );
				}
			return userArrayList;
		} catch ( SQLException e ) {
			e.printStackTrace();
			return null;
		}
	}

	public static final void addMessage( Message message ) {	
		try {
			checkConnectionWithDatabase();
			PreparedStatement ps = connection.prepareStatement( "INSERT into Message(Sender, Receiver, Content, Received, Deleted, DateSent) VALUES( ?, ?, ?, ?, ?, ? )" );
			ps.setString( 1, message.SENDER );
			ps.setString( 2, message.RECEIVER );
			ps.setString( 3, message.CONTENT );
			ps.setBoolean( 4, message.isRead() );
			ps.setBoolean( 5, message.isDeleted() );
			ps.setTimestamp( 6, new Timestamp( message.CALENDAR.getTimeInMillis() ) );
			ps.executeUpdate();
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
	}
	
	public static final long getAvailableMID() {
		long MID = Message.ERROR;
		try {
			ResultSet singleRow = statement.executeQuery( "SELECT max(MID) + 1 FROM Message;");
			singleRow.next();
			MID = singleRow.getLong( 1 );
			return MID;
		} catch ( SQLException e ) {
			return MID;
		}
	}
	
	private static final void checkConnectionWithDatabase() throws SQLException {
		if ( connection == null || !connection.isValid( 3 ) )
			throw new SQLException( "not connected to the database." );
	}
}