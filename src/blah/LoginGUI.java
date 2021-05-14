package blah;

import java.util.ArrayList;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

final class LoginGUI {
	private BlahGUI blahGUI;
	private ConnectionGUI connectionGUI;
	private TextField usernameInputField;
	private PasswordField passwordInputField;
	private Button loginButton,
                   closeButton,
                   logoutButton;
	private Stage loginStage;
	private Scene loginScene;
	private String username = "";
	private final String LOGGEDIN_AS_MESSAGE = "You are now logged in as: ",
	                     NOT_LOGGEDIN_MESSAGE = "Please enter the username and password: ";
	private Label loggedinMessage;
	private Thread retrivePendingMessagesThread = new Thread();
	private Alert loginFailedAlert = Util.createAlert( AlertType.ERROR, "Could not log in.", loginStage, ButtonType.OK ),
                  wrongCredentialsAlert = Util.createAlert( AlertType.ERROR, "username and password can not be empty!", loginStage, ButtonType.OK );
	private HBox buttonsHBox;
	private GridPane mainLayoutGridPane;
	private double mouseOriginalPressX, mouseOriginalPressY;
	
	public LoginGUI( BlahGUI blahGUI, ConnectionGUI connectionGUI ) {
		this.blahGUI = blahGUI;
		this.connectionGUI = connectionGUI;
		createControls();
		setupLayout();
	}
	
	public void setDisconnectedFromServer() {
		username = "";
		retrivePendingMessagesThread.interrupt();
		loggedinMessage.setText( NOT_LOGGEDIN_MESSAGE );
	}
	
	public void show() {
		loginStage.show();
	}
	
	public void showAndWait() {
		loginStage.showAndWait();
	}
	
	public String getUsername() {
		return username;
	}
	
	private void createControls() {
		createLoggedinMessage();
		createpasswordInputField();
		createUsernameInputField();
		createloginButton();
		createLogoutButton();
		createCancelButton();
	}
	
	private void createUsernameInputField() {
		usernameInputField = new TextField();
		usernameInputField.setPromptText( "Username" );
		usernameInputField.setId( "username-input-field" );
	}
	
	private void createpasswordInputField() {
		passwordInputField = new PasswordField();
		passwordInputField.setPromptText( "Password" );
		passwordInputField.setId( "password-input-field" );
	}
	
	private void createloginButton() {
		loginButton = new Button( "Login" );
		loginButton.setCursor( Cursor.HAND );
		loginButton.setDefaultButton( true );
		
		loginButton.setOnAction( e -> {
			if ( connectionGUI.getServer().isConnected() && credentialsEntered() )
				login();
			else
				wrongCredentialsAlert.showAndWait();
		});
	}
	
	private boolean credentialsEntered() {
		return usernameInputField.getText() != null && passwordInputField.getText() != null
				&& !usernameInputField.getText().equals( "" ) && !passwordInputField.getText().equals( "" );
	}
	
	private void login() {
		username = usernameInputField.getText();
	    String password = passwordInputField.getText();
		if ( !connectionGUI.getServer().login( username, password ) ) {
			loginFailedAlert.showAndWait();
			username = "";
			return;
		}

		loggedinMessage.setText( LOGGEDIN_AS_MESSAGE + username );
		loginStage.close();
		blahGUI.setLoggedin();
		usernameInputField.clear();
		passwordInputField.clear();
		
		retrivePendingMessagesThread.interrupt();
		retrivePendingMessagesThread = new Thread( () -> retrivePendingMessages() );
		retrivePendingMessagesThread.start();
	}
	
	private void retrivePendingMessages() {
		while ( true ) {
			try {
				ArrayList<Message> messages = connectionGUI.getServer().getPendingMessages();
				for ( Message m : messages ) {
					if ( m.MID == Message.ERROR ) {
						addTemporaryErrorMessage();
					} else if ( m.MID == Message.UPDATE && m.SENDER.equals( blahGUI.getCurrentChatUsername() ) ) {
						Platform.runLater( () -> {
							blahGUI.clearChatMessages();
							ArrayList<Message> updatedMessages = connectionGUI.getServer().updateChatMessages( m.SENDER );
							blahGUI.setChatMessages( updatedMessages );
						});
						break;
					} else if ( m.SENDER.equals( blahGUI.getCurrentChatUsername() ) && !m.isDeleted() ) {
						Platform.runLater( () -> blahGUI.addMessageToChat( new MessageText( m, false ) ) );
					} else if ( !m.SENDER.equals( blahGUI.getCurrentChatUsername() ) && !m.isDeleted() ) {
						Platform.runLater( () -> Util.createTrayNotification( m.SENDER, m.CONTENT ).showAndDismiss( Util.HALF_SECOND ) );
						Thread.sleep( 300 );
						Util.NOTIFICATION_SOUND.play();
					}
				}
				Thread.sleep( 500 );
			} catch ( InterruptedException e ) {
				break;
			}
		}
	}
	
	private void addTemporaryErrorMessage() throws InterruptedException {
		MessageText errorMessageText = new MessageText( Message.getErrorMessage(), false );
		Platform.runLater( () -> blahGUI.addMessageToChat( errorMessageText ) );
		Thread.sleep( 5000 );
		Platform.runLater( () -> blahGUI.removeMessageFromChat( errorMessageText ) );
	}
	
	private void createLogoutButton() {
		logoutButton = new Button( "Logout" );
		logoutButton.setOnAction( e -> logoutButtonAction() );
	}
	
	private void logoutButtonAction() {
		if ( !connectionGUI.getServer().isConnected() ) {
			Util.errorTooltip.show( loginStage );
			return;
		}
		
		if ( connectionGUI.getServer().logout() ) {
			username = "";
			loggedinMessage.setText( NOT_LOGGEDIN_MESSAGE );
			retrivePendingMessagesThread.interrupt();
			blahGUI.setLoggedout();
		}
	}
	
	private void createCancelButton() {
		closeButton = new Button( "Close" );
		closeButton.setCancelButton( true );
		closeButton.setOnAction( e -> loginStage.close() );
	}
	
	private void createLoggedinMessage() {
		loggedinMessage = new Label();
		loggedinMessage.setTextFill( Color.WHITE );
	}
	
	private void setupLayout() {
		createButtonsHBox( closeButton, loginButton, logoutButton );
		createMainLayoutGridPane();
		setupWindow();
	}
	
	private void createButtonsHBox( Node... elements ) {
		buttonsHBox = new HBox();
		buttonsHBox.setSpacing( 30 );
		buttonsHBox.getChildren().addAll( elements );
	}
	
	private void createMainLayoutGridPane() {
		mainLayoutGridPane = new GridPane();
		mainLayoutGridPane.setVgap( 20 );
		mainLayoutGridPane.setPadding( new Insets( 20 ) );
		mainLayoutGridPane.add( loggedinMessage, 0, 0 );
		mainLayoutGridPane.add( usernameInputField, 0, 1 );
		mainLayoutGridPane.add( passwordInputField, 0, 2 );
		mainLayoutGridPane.add( buttonsHBox, 0, 3 );
		mainLayoutGridPane.setBackground( new Background(
				new BackgroundImage( Util.BACKGROUND_IMAGE, null, null, null, null ) ) );
		GridPane.setHalignment( buttonsHBox, HPos.CENTER );
	}
	
	private void setupWindow() {
		loginScene = new Scene( mainLayoutGridPane );
		loginScene.getStylesheets().add( Util.STYLESHEET_PATH );
		loginStage = new Stage( StageStyle.DECORATED );
		loginStage.initOwner( blahGUI.getWindow() );
		loginStage.initModality( Modality.WINDOW_MODAL );
		loginStage.setScene( loginScene );
		loginStage.setResizable( false );
		loginStage.setTitle( "Login" );
		loginStage.getIcons().add( Util.ICON );

		loginScene.setOnMousePressed( e -> {
			mouseOriginalPressX = e.getScreenX() - loginStage.getX();
			mouseOriginalPressY = e.getScreenY() - loginStage.getY();
		} );
		loginScene.setOnMouseDragged( e -> {
			loginStage.setX( e.getScreenX() - mouseOriginalPressX );
			loginStage.setY( e.getScreenY() - mouseOriginalPressY );
		} );
	}
}
