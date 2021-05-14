package blah;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class BlahServer extends Thread {
	ArrayList<InetAddress> clientsIP = new ArrayList<>();
	ArrayList<ClientHandler> clientsThread = new ArrayList<>();
	private static final Scanner input = new Scanner( System.in );
	private ServerSocket server;
	private ArrayList<InetAddress> blockedIP = new ArrayList<>();
	
	public static void main(String[] args) throws IOException {
		new BlahServer().start();
	}
	
	@Override
	public void run() {
		try {
			server = new ServerSocket( Util.SERVER_PORT );
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		}
		
		while ( true ) {
			try {
				Database.connect();
				new ServerThread().start();
				new MultiCastThread().start();
	            handleRequests();
			} catch ( IOException e ) {
				for ( Thread c : clientsThread )
					c.interrupt();
				e.printStackTrace();
				System.exit( 1 );
			} catch ( SQLException e ) {
				System.out.println( ServerString.DATABASE_ERROR + e.getMessage() );
				System.out.println( ServerString.RETRY );
				if ( input.next().equalsIgnoreCase( ServerString.RETRY_KEY.toString() ) )
					continue;
				System.exit( 1 );
			}
		}
	}
	
	private final void handleRequests() throws IOException {
		while ( true ) {
			Socket clientSocket = server.accept();
			if ( !blockedIP.contains( clientSocket.getInetAddress() ) ) {
				ClientHandler client = new ClientHandler( clientSocket, this );
				clientsThread.add( client );
				client.start();
				clientsIP.add( clientSocket.getInetAddress() );
			}
			else
				clientSocket.close();
		}
	}
	
	private final class ServerThread extends Thread {
		@Override
		public void run() {
			while ( true ) {
				String call = input.nextLine();
				execute( call );
				Thread.yield();
			}
		}
		
		private void execute( String call ) {
			if ( call.equalsIgnoreCase( ServerString.EXIT.toString() ) ) {
				for ( Thread c : clientsThread )
					c.interrupt();
				System.exit( 0 );
			}
			else if ( call.length() < 7 ) {
				System.out.println( ServerString.WRONG_COMMAND_FORMAT );
				return;
			}
			
			String command = call.substring( 0, 7 ).toUpperCase();
			String args = call.substring( 7 );
			args = args.trim();
			
			switch ( ServerString.getServerString( command ) ) {
			case COMMAND_ACTIVE_CLIENTS:
				for ( InetAddress c : clientsIP )
					System.out.println( c );
				break;
				
			case COMMAND_ADD_USER:
				System.out.println( "Password for " + args );
				String password = input.nextLine();
				Database.addUser( args, password );
				break;
				
			case COMMAND_BLOCK_IP:
				blockIP( args );
				break;
				
			case COMMAND_DELETE_USER:
				Database.deleteUser( args );
				break;
				
			case COMMAND_SHOW_BLOCKED_IPS:
				for ( InetAddress ip : blockedIP )
					System.out.println( ip );
				break;
				
			case COMMAND_UNBLOCK_IP:
				try {
					blockedIP.remove( InetAddress.getByName( args ) );
					break;
				} catch ( UnknownHostException e ) {
					System.out.println( "Unknown Host!" );
					break;
				}

			default:
				System.out.println( ServerString.COMMAND_DOES_NOT_EXIST );
				break;
			}
		}
		
		private void blockIP( String ip ) {
			try {
				InetAddress host = InetAddress.getByName( ip );
				blockedIP.add( host );
				
				if ( isConnected( host ) ) {
					kick( host );
				}
				
			} catch ( UnknownHostException e ) {
				System.out.println( "Unknown Host!" );
			}
		}
		
		private boolean isConnected( InetAddress host ) {
			return clientsIP.contains( host );
		}
		
		private void kick( InetAddress host ) {
			clientsIP.remove( host );
			for ( Thread c : clientsThread ) {
				if ( c.getName().equals( host.toString() ) ) {
					System.out.println( "Blocking " + host.getHostAddress() );
					c.interrupt();
				}
			}
		}
	}

	private final class MultiCastThread extends Thread {
		private MulticastSocket mc;
		private InetAddress serverIP, groupAddress;
		public MultiCastThread() {
			try {
				serverIP = InetAddress.getLocalHost();
				mc = new MulticastSocket( Util.MULTICAST_SERVER_PORT );
				groupAddress = InetAddress.getByName( Util.MULTICAST_ADDRESS );
				mc.joinGroup( groupAddress );
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		@Override
		public void run() {
			DatagramPacket addressPacket = new DatagramPacket( serverIP.getAddress(), serverIP.getAddress().length, groupAddress, Util.MULTICAST_SERVER_PORT );
			while ( true ) {
				try {
					mc.send( addressPacket );
					Thread.sleep( 1000 );
				} catch ( IOException | InterruptedException e ) {
					e.printStackTrace();
				}
			}	
		}
	}
}