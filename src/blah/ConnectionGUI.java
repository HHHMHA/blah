package blah;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

final class ConnectionGUI {
	private ClientToServer server;
	private CheckBox autoConnectCheckBox;
	private TextField serverIPInputField;
	private Button connectToServerButton,
	               serverIPSearchButton;
	private Scene connectionScene;
	private Stage connectionStage;
	private HBox topLevelControlsHBox;
	private VBox controlsVBox;
	private BlahGUI blahGUI;
	private Thread checkConnectionThread = null,
	               autoConnectThread = null;
	private final Alert connectionFailedAlert = Util.createAlert( AlertType.ERROR, "Could not connect! please try again.", connectionStage, ButtonType.OK ), 
	                    connectionLostAlert,
	                    wrongFormatAlert = Util.createAlert( AlertType.ERROR, "You should enter an ip only.", connectionStage, ButtonType.OK );
	
	public ConnectionGUI( BlahGUI blahGUI ) {
		this.blahGUI = blahGUI;
		connectionLostAlert = Util.createAlert( AlertType.ERROR, "Connection to server lost! Please try again.", blahGUI.getWindow(), ButtonType.OK );
		server = new ClientToServer();
		createControls();
		setupLayout();
	}
	
	public ClientToServer getServer() {
		return server;
	}
	
	public void show() {
		connectionStage.show();
	}
	
	public void showAndWait() {
		connectionStage.showAndWait();
	}
	
	public void exit() {
		if ( checkConnectionThread != null )
			checkConnectionThread.interrupt();
		if ( autoConnectThread != null )
			autoConnectThread.interrupt();
		server.disconnect();
	}
	
	private void createControls() {
		createAutoConnectCheckBox();
		createServerIPInputField();
		createConnectToServerButton();
		createServerIPSearchButton();
	}
	
	private void createAutoConnectCheckBox() {
		autoConnectCheckBox = new CheckBox();
		autoConnectCheckBox.setTooltip( new Tooltip( "Automatically retry connecting to server" ) );
		autoConnectCheckBox.setText( "Automatically retry connecting to server" );
		
		autoConnectCheckBox.setOnAction( e -> autoConnectCheckBoxAction() );
	}
	
	private void autoConnectCheckBoxAction() {
		if ( Util.isIP( serverIPInputField.getText() ) ) {
			if ( autoConnectCheckBox.isSelected() ) {
				autoConnectThread = new Thread( () -> autoConnectToServer() );
				autoConnectThread.start();
			}
			else {
				autoConnectThread.interrupt();
			}
		}
		else
			autoConnectCheckBox.setSelected( false );
	}
	
	private void autoConnectToServer() {
		while ( true ) {
			if ( !server.isConnected() )
				Platform.runLater( () -> {
					connectToServerButton.fire();
				});
			try {
				Thread.sleep( 6000 );
			} catch ( InterruptedException e1 ) {
				break;
			}
		}
	}
	
	private void createServerIPInputField() {
		serverIPInputField = new TextField();
		serverIPInputField.setPromptText( "Enter the IP of the server or press search if it's on the same LAN" );
	}
	
	private void createConnectToServerButton() {
		connectToServerButton = new Button( "Connect" );
		connectToServerButton.setCursor( Cursor.HAND );
		connectToServerButton.setDefaultButton( true );

		connectToServerButton.setOnAction( e -> connectToServerButtonAction() );
	}
	
	private void connectToServerButtonAction() {
		if ( !Util.isIP( serverIPInputField.getText() ) ) {
			wrongFormatAlert.showAndWait();
			return;
		}

		if ( !connectToServer() ) {
			blahGUI.setDisconnectedFromServer();
			if ( !autoConnectCheckBox.isSelected() )
				connectionFailedAlert.showAndWait();
			return;
		}
		
		blahGUI.setConnectedToServer();
		connectionStage.close();
		
		checkConnectionThread = new Thread( () -> checkConnection() );
		checkConnectionThread.start();
	}
	
	private void checkConnection() {
		while ( true ) {
			try {
				if ( !server.isConnected() ) {
					if ( autoConnectThread != null )
						autoConnectThread.interrupt();
					Platform.runLater( () -> {
						blahGUI.setDisconnectedFromServer();
						connectionLostAlert.showAndWait();
					});
					break;
				}
				Thread.sleep( 5000 );
			} catch ( InterruptedException e ) {
				server.disconnect();
				break;
			}
		}
	}
	
	private boolean connectToServer() {
		server.disconnect();
		return server.connect( serverIPInputField.getText() );
	}
	
	private void createServerIPSearchButton() {
		serverIPSearchButton = new Button( "Search" );
		serverIPSearchButton.setCursor( Cursor.HAND );
		
		serverIPSearchButton.setOnAction( e -> {
			new Thread( () -> getServerAddress() ).start();
		});
	}
	

	private void getServerAddress() {
		try {
			Platform.runLater( () -> serverIPSearchButton.setDisable( true ) );
			
			MulticastSocket serverMulticastAddressSocket = new MulticastSocket( Util.MULTICAST_SERVER_PORT );
			serverMulticastAddressSocket.setSoTimeout( Util.SO_TIMEOUT_MILLI_SECONDS );
			serverMulticastAddressSocket.joinGroup( InetAddress.getByName( Util.MULTICAST_ADDRESS ) );
			
			byte[] serverIPAddress = new byte[ 4 ];	
			DatagramPacket serverIPAddressPacket = new DatagramPacket( serverIPAddress, serverIPAddress.length );
			
			serverMulticastAddressSocket.receive( serverIPAddressPacket );
			String serverAddress = InetAddress.getByAddress( serverIPAddressPacket.getData() ).getHostAddress();
			Platform.runLater( () -> serverIPInputField.setText( serverAddress ) );
			
			serverMulticastAddressSocket.leaveGroup( InetAddress.getByName( Util.MULTICAST_ADDRESS ) );
			serverMulticastAddressSocket.close();
			Platform.runLater( () -> serverIPSearchButton.setDisable( false ) );
		} catch ( SocketTimeoutException e1 ) {
			Platform.runLater( () -> serverIPSearchButton.setDisable( false ) );
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		}
	}
	
	private void setupLayout() {
		createTopLevelControlsHBox( serverIPInputField, connectToServerButton, serverIPSearchButton );
		createControlsVBox( topLevelControlsHBox, autoConnectCheckBox );
		setupWindow();
	}
	
	private void createTopLevelControlsHBox( Node... elements ) {
		topLevelControlsHBox = new HBox();
		topLevelControlsHBox.setSpacing( 20 );
		topLevelControlsHBox.getChildren().addAll( elements );
	}
	
	private void createControlsVBox( Node... elements ) {
		controlsVBox = new VBox();
		controlsVBox.setPadding( new Insets( 10 ) );
		controlsVBox.setSpacing( 20 );
		controlsVBox.setBackground( new Background( new BackgroundImage( new Image( "blah/res/Background.png" ), null, null, null, null ) ) );
		controlsVBox.getChildren().addAll( elements );
	}
	
	private void setupWindow() {
		connectionScene = new Scene( controlsVBox );
		connectionScene.getStylesheets().add( "blah/res/style.css" );
		connectionStage = new Stage( StageStyle.DECORATED );
		connectionStage.initOwner( blahGUI.getWindow() );
		connectionStage.initModality( Modality.WINDOW_MODAL );
		connectionStage.setScene( connectionScene );
		connectionStage.setResizable( false );
		connectionStage.setTitle( "Connect to server" );
		connectionStage.getIcons().add( Util.ICON );
	}
}