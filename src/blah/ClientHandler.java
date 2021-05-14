package blah;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public final class ClientHandler extends Thread {
	private final Socket clientSocket;
	private String username = "", currentChat = "";
	private ObjectInputStream inputFromClient;
	private ObjectOutputStream outputToClient;
	private final BlahServer server;
	private ArrayList<Message> pendingMessages = new ArrayList<>();
	private final InetAddress clientIPAddress;
	
	public ClientHandler( Socket clientSocket, BlahServer server ) {
		this.clientSocket = clientSocket;
		this.server = server;
		clientIPAddress = clientSocket.getInetAddress();
	}

	@Override
	public void run() {
		Thread.currentThread().setName( clientSocket.getInetAddress().toString() );
		try {
			outputToClient = new ObjectOutputStream( clientSocket.getOutputStream() );
			outputToClient.flush();
			inputFromClient = new ObjectInputStream( clientSocket.getInputStream() );
			while ( true ) {
				Request r = (Request)inputFromClient.readObject();
				serve( r );
				Thread.sleep( 50 );
			}
		} 
		catch ( Exception e ) {
			System.out.println( "Logging " + username + " at " + getName() + " off." );
			server.clientsIP.remove( clientIPAddress );
			server.clientsThread.remove( this );
			if ( username != null && !username.equals( "" ) )
				Database.logUserOff( username );
			try {
				clientSocket.close();
			} catch ( IOException e1 ) {
				
			}
		}
	}
	
	private void serve( Request request ) throws IOException {
		switch( request.requestID ) {
		case CHECK_CONNECTION:
			outputToClient.writeObject( true );
			outputToClient.flush();
			break;
		case LOGIN:
			if ( username != null && !username.equals( "" ) )
				Database.logUserOff( username );
			username = request.username;
			outputToClient.writeObject( Database.logUserOn( request.username, request.password ) ); 
			outputToClient.flush();
			break;
		case LOGOUT:
			if ( username != null && !username.equals( "" ) )
				outputToClient.writeObject( Database.logUserOff( username ) );
			else
				outputToClient.writeObject( false );
			username = "";
			currentChat = "";
			pendingMessages.clear();
			outputToClient.flush();
			break;
		case USERS:
			outputToClient.writeObject( Database.getOtherUsersForChat( username ) ); 
			outputToClient.flush();
			break;
		case PENDING_MESSAGES:
			outputToClient.writeObject( pendingMessages );
			outputToClient.flush();
			pendingMessages.clear();
			pendingMessages = new ArrayList<>();
			break;
		case SEND:
			Message message = request.message;
			for ( ClientHandler client : server.clientsThread ) {
				if ( client.username.equals( message.RECEIVER ) ) {
					client.pendingMessages.add( message );
					if ( client.currentChat.equals( username ) ) {
						outputToClient.writeObject( true ); 
						message.setRead();
					}
					else
						outputToClient.writeObject( false );
					outputToClient.flush();
					Database.addMessage( message );
					return;
				}
			}
			Database.addMessage( message );
			outputToClient.writeObject( false ); 
			outputToClient.flush();
			break;
		case MID:
			outputToClient.writeObject( Database.getAvailableMID() );
			outputToClient.flush();
			break;
		case GET_MESSAGES_WITH_NOTIFY:
			currentChat = request.currentChatUsername;
			for ( ClientHandler client : server.clientsThread )
				if ( client.username.equals( request.currentChatUsername ) )
					client.pendingMessages.add( new Message( Message.UPDATE, username, null, null, null, false, true ) );
		case GET_MESSAGES_WITHOUT_NOTIFY:
			pendingMessages.clear();
			pendingMessages = new ArrayList<>();
			try {
				Database.markMessagesAsRead( request.currentChatUsername, username );
				outputToClient.writeObject( Database.getUserMesseges( username, request.currentChatUsername ) );
			} catch ( SQLException e ) {
				ArrayList<Message> error = new ArrayList<>();
				error.add( new Message( Message.ERROR, request.currentChatUsername, username, "SERVER MESSAGE!!!\n An error occured please wait a few seconds", null, true, true ) );
				outputToClient.writeObject( error );
			} 
			outputToClient.flush();
			break;
		}
	}
}
