package blah;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public final class ClientToServer {
	private Socket socket;
	private ObjectInputStream inputFromServer = null;
	private ObjectOutputStream outputToServer = null;
	private InetAddress serverIP;
	
	public ClientToServer() {};
	
	public boolean connect( String serverIP ) {
		socket = new Socket();
		try {
			this.serverIP = InetAddress.getByName( serverIP );
			socket.setSoTimeout( 3000 );
			socket.connect( new InetSocketAddress( this.serverIP, Util.SERVER_PORT ), Util.CONNECTION_TIMEOUT_MILLI_SECONDS );
			InputStream inputStream = socket.getInputStream();
			OutputStream outputStream = socket.getOutputStream();
			outputToServer = new ObjectOutputStream( outputStream );
			outputToServer.flush();
			inputFromServer = new ObjectInputStream( inputStream );
			return true;
		} catch ( IOException e ) {
			return false;
		}
	}
	
	public void disconnect() {
		try {
			socket.close();
		} catch ( Exception e ) { }
	}
	
	public boolean login( String username, String password ) {
		try {
			return (boolean) sendRequest( new Request( RequestCode.LOGIN, username, password ) );
		} catch ( ClassNotFoundException | IOException e ) {
			return false;
		}
	}
	
	public boolean logout() {
		try {
			return (boolean) sendRequest( new Request( RequestCode.LOGOUT ) );
		} catch ( IOException | ClassNotFoundException e ) {
			return false;
		}
	}

	public boolean isConnected() {
		if ( socket == null )
			return false;
		try {
			
			return (boolean) sendRequest( new Request( RequestCode.CHECK_CONNECTION ) );
		} catch ( ClassNotFoundException | IOException e ) {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Message> getPendingMessages() {
		try {
			return (ArrayList<Message>) sendRequest( new Request( RequestCode.PENDING_MESSAGES ) );
		} catch ( ClassNotFoundException | IOException e ) {
			ArrayList<Message> errorMessageList = new ArrayList<>();
			errorMessageList.add( Message.getErrorMessage() );
			return errorMessageList;
		}
	}
	
	/**
	 * If an error occurs the first user code will be {@link Util#USERS_CODE_ERROR_FETCHING}
	 * @return an ArrayList of strings each string is divided into two parts the first part is the code
	 * which you can get using the {@link Util#CODE_INDEX} each code has different meaning
	 * the second part is the username which you can get by using the start index {@link Util#USERNAME_INDEX}
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<String> fetchAvailableUsers() {
		ArrayList<String> availableUsers = new ArrayList<>();
		try {
			return ( (ArrayList<String>) sendRequest( new Request( RequestCode.USERS ) ) );
		} catch ( ClassNotFoundException | IOException e ) {
			availableUsers.add( Util.USERS_CODE_ERROR_FETCHING + "" );
			return availableUsers;
		}
	}
	
	public ArrayList<Message> updateChatMessages( String with ) {
		return fetchMessages( with, false );
	}
	
	public ArrayList<Message> fetchChatMessagesWith( String with ) {
		return fetchMessages( with, true );
	}
	
	public Message sendMessage( String sender, String receiver, String content ) {
		try {
			long MID = (long) sendRequest( new Request( RequestCode.MID ) );
			Message message = new Message( MID, sender, receiver, content, null, false, false );
			boolean isRead = (boolean) sendRequest( new Request( RequestCode.SEND, message ) );
			if ( isRead )
				message.setRead();
			return message;
		} catch ( ClassNotFoundException | IOException e ) {
			System.out.println( e.getMessage() );
			return Message.getErrorMessage();
		}
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<Message> fetchMessages( String with, boolean update ) {
		ArrayList<Message> messages = new ArrayList<>();
		try {
			// respond so we don't get caught in a loop if each user generating an update message
			messages = update ? (ArrayList<Message>)sendRequest( new Request( RequestCode.GET_MESSAGES_WITH_NOTIFY, with ) ) : (ArrayList<Message>)sendRequest( new Request( RequestCode.GET_MESSAGES_WITHOUT_NOTIFY, with ) );
			return messages;
		} catch ( ClassNotFoundException | IOException e ) {
			messages.add( Message.getErrorMessage() );
			return messages;
		}
	}
	
	private synchronized Object sendRequest( Request request ) throws IOException, ClassNotFoundException {
		if ( outputToServer == null || inputFromServer == null )
			throw new IOException( "Not Connected to server yet" );
		outputToServer.writeObject( request );
		outputToServer.flush();
		return inputFromServer.readObject();
	}
	
}
