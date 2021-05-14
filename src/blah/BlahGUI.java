package blah;

import java.util.ArrayList;
import javafx.animation.*;
import javafx.application.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.shape.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.stage.*;
import javafx.util.Duration;

public final class BlahGUI extends Application {
	private ConnectionGUI connectionGUI;
	private LoginGUI loginGUI;
	private ChatArea chatArea;
	private Pane frame = new Pane();
	private Timeline showFrameAnimation = createFrameAnimationTopDown(), 
            hideFrameAnimation = createFrameAnimationBottomUp();
	private StackPane background = new StackPane(), 
			          frameBox = new StackPane();
	private HBox bottomBox = new HBox(),
            controlsBox = new HBox();
	private BorderPane foreground = new BorderPane();
	private ButtonPane openConnectionGUIButton,
                       exitApplicationButton,
                       maximizeButton,
                       minimizeButton,
                       openLoginGUIButton;

	private MenuButtonPane showAvailableUsersMenuButton;
	private Button sendMessageButton;
	private TextArea messageTypingArea;
	private Stage mainStage;
	private Scene mainScene = new Scene( background );
	private double deltaMouseX, 
	               deltaMouseY, 
	               stageX, 
	               stageY;
	
	// sending files, images, sounds, videos with support to drag and drop
	// implement message deleting
	// implement message forwarding
	// next to each user name show how many unread messages there is
	// add emoji button
	// add voice chat
	// make it work with multiple nics
	// implement encryption for messages
	// when server closes handle the Exceptions
	// make sure all Threads use Platform.runLater
	// backup of database
	// Data integrity for database
	// Create a database if it doesn't exists
	// Implement Compression Algorithm when sending images
	// cache of messages and users
	// give the custom buttons disable and other methods
	// Create a method to create an Alert
	// Banning with settings (specific times)
	// Report abuse
	// create effects on the background when the mouse moves
	// option to save login credential
	// Groups with admins, rights, ...
	// public polls
	// videos, images, web pages open in the app itself
	// mute notifications option
	// search messages.
	// Use StringBuilder when needed
	// Consider moving the styles to the css sheet
	// Might need to create objects first then set them up to avoid null
	// Create/Throw Exceptions for stuff like getCurrentChatUsername if it's chat ..etc
	// hash the password before sending to server
	// fix the problem of only one server is allowed in lan
	// when chatHeader is collapsed don't set messages read
	// when searching for server make the client ask for it and the server respond instead if constantly sending
	// make user online status get updated automatically
	// add sound for keyboard typing and other effects
	
	// init all class fields in place and get rid of nulls
	// Refactor Again and again
	// Add equal method and hashkey to classes like message
	// Follow SRP, DIP, DRY
	// make a father class for messages (rename MessageText to MessageBubble or GUI)
	
	
	public static void main( String[] args ) {
		launch( args );
	}
	
	@Override
	public void start( Stage primaryStage ) {
		mainStage = primaryStage;
		setupMainScene();
		setupMainStage();
		connectionGUI = new ConnectionGUI( this );
		loginGUI = new LoginGUI( this, connectionGUI );
		chatArea = new ChatArea( this, connectionGUI );
		createControls();
		setupLayout();
		mainStage.show();
	}
	
	@Override
	public void stop() {
		// Make a loop and put all the Threads in an array list
		setDisconnectedFromServer();
		connectionGUI.exit();
		System.exit( 0 );
	}

	public Window getWindow() {
		return mainStage;
	}
	
	public void setConnectedToServer() {
		openConnectionGUIButton.setIdleImage( Util.CONNECT_3 );
	}
	
	public void setDisconnectedFromServer() {
		loginGUI.setDisconnectedFromServer();
		chatArea.setNotChatting();
		openConnectionGUIButton.setIdleImage( Util.CONNECT_1 );
		showAvailableUsersMenuButton.getItems().clear();
	}
	
	public void setLoggedin() {
		setLoggedout();
	}
	
	public void setLoggedout() {
		chatArea.setNotChatting();
		showAvailableUsersMenuButton.getItems().clear();
	}
	
	public void addMessageToChat( MessageText messageBox ) {
		chatArea.addMessageToChat( messageBox );
	}
	
	public void removeMessageFromChat( MessageText messageBox ) {
		chatArea.removeMessageFromChat( messageBox );
	}
	
	public void clearChatMessages() {
		chatArea.clearChatMessages();
	}
	
	public void setChatMessages( ArrayList<Message> messages ) {
		chatArea.setChatMessages( messages );
	}
	
	public String getCurrentChatUsername() {
		return chatArea.getCurrentChatUsername();
	}
	
	public String getUsername() {
		return loginGUI.getUsername();
	}
	
	public double getBottomAreaHeight() {
		return bottomBox.getHeight();
	}
	
	public ReadOnlyDoubleProperty widthProperty() {
		return mainScene.widthProperty();
	}

	public ReadOnlyDoubleProperty heightProperty() {
		return mainScene.heightProperty();
	}
	
	private void createControls() {
		exitApplicationButton = Util.createButtonPane( "", "blah/res/Exit", "Exit", e -> {
			if ( e.getButton() == MouseButton.PRIMARY )
				Platform.exit();
		});
		
		maximizeButton = Util.createButtonPane( "", "blah/res/Max", "Fullscreen Mode", e -> {
			if ( e.getButton() == MouseButton.PRIMARY ) {
				mainStage.setFullScreen( !mainStage.isFullScreen() );
			}
		});
		
		minimizeButton = Util.createButtonPane( "", "blah/res/Min", "Minimize", e -> {
			// Works alongside mainStage minimize behavior
			if ( e.getButton() == MouseButton.PRIMARY ) {
				mainStage.setIconified( true );
				stageX = mainStage.getX();
				stageY = mainStage.getY();
			}
		});
		
		openLoginGUIButton = Util.createButtonPane( "", "blah/res/login", "Login", e -> {
			if ( e.getButton() == MouseButton.PRIMARY )
				loginGUI.showAndWait();
		});
		
		openConnectionGUIButton = Util.createButtonPane( "", "blah/res/Connect", "Connect to server", e -> {
			if ( e.getButton() == MouseButton.PRIMARY )
				connectionGUI.showAndWait();
		});
		
		showAvailableUsersMenuButton = Util.createMenuButtonPane( "", "blah/res/User", "Available users", e -> {
			if ( e.getButton() == MouseButton.PRIMARY )
				showAvailableUsersMenu();
		});
		
		createMessageTypingTextArea();
		createSendMessageButton();
	}
	
	private void showAvailableUsersMenu() {
		ArrayList<String> availableUsers = connectionGUI.getServer().fetchAvailableUsers();
		if ( Util.USERS_CODE_ERROR_FETCHING  == availableUsers.get( 0 ).charAt( Util.CODE_INDEX ) ) {
			Util.errorTooltip.show( mainStage );
			return;
		}
		fillAvailableUsersMenu( availableUsers );
	}
	
	private void fillAvailableUsersMenu( ArrayList<String> availableUsers ) {
		showAvailableUsersMenuButton.getItems().clear();
		for ( int i = 0; i < availableUsers.size(); ++i ) {
			MenuItem userMenuItem = new MenuItem( availableUsers.get( i ).substring( Util.USERNAME_INDEX ) );
			if ( Util.USERS_CODE_ONLINE == availableUsers.get( i ).charAt( Util.CODE_INDEX )  )
				setUserOnline( userMenuItem );
			userMenuItem.setOnAction( e -> chatArea.setChattingWith( userMenuItem.getText() ) );
			showAvailableUsersMenuButton.getItems().add( userMenuItem );
		}
	}
	
	private void setUserOnline( MenuItem userMenuItem ) {
		Circle online = new Circle( 5 );
		online.setFill( Color.GREEN );
		online.setStroke( Color.WHITE );
		userMenuItem.setGraphic( online );
	}
	
	private void createMessageTypingTextArea() {
		messageTypingArea = new TextArea();
		IntegerBinding maxRowsInMessageTypingArea = Bindings.createIntegerBinding( () ->
			(messageTypingArea.getParagraphs().size() > Util.MAX_ROWS_TO_SHOW_IN_MESSAGE_TYPING_AREA) ? Util.MAX_ROWS_TO_SHOW_IN_MESSAGE_TYPING_AREA : messageTypingArea.getParagraphs().size(), messageTypingArea.getParagraphs() );
		messageTypingArea.prefRowCountProperty().bind( maxRowsInMessageTypingArea );
		
		messageTypingArea.setOnKeyPressed( e -> {
			if ( !e.isShiftDown() && e.getCode() == KeyCode.ENTER ) {
				// get rid of pressed Enter
				messageTypingArea.deletePreviousChar();
				
				sendMessageButton.fire();
			}
			else if ( e.isShiftDown() && e.getCode() == KeyCode.ENTER )
				messageTypingArea.appendText( "\n" );
		});
	}
	
	private void createSendMessageButton() {
		sendMessageButton = new Button();
		sendMessageButton.setId( "sendbtn" );
		sendMessageButton.setTooltip( new Tooltip( "Send" ) );
		sendMessageButton.setPrefWidth( Util.SEND_BUTTON_MASK.getWidth() );
		sendMessageButton.setPrefHeight( Util.SEND_BUTTON_MASK.getHeight() );
		sendMessageButton.setClip( new ImageView( Util.SEND_BUTTON_MASK ) );
		BooleanBinding isChattingAndNotEmptyMessage = Bindings.createBooleanBinding( () -> ( !Util.isWhiteSpace( messageTypingArea.getText() ) &&
				!chatArea.getCurrentChatUsername().equals( ChatArea.DEFAULT_HEADER_TEXT )), 
				chatArea.getCurrentChatUsernameTextProperty(), 
				messageTypingArea.textProperty() );
		sendMessageButton.disableProperty().bind( isChattingAndNotEmptyMessage.not() );
		sendMessageButton.setOnAction( e -> {
			if ( !connectionGUI.getServer().isConnected() ) {
				Util.errorTooltip.show( mainStage );
				return;
			}
			if ( trySendingTypedMessage() )
				messageTypingArea.clear();
		});
	}
	
	private boolean trySendingTypedMessage() {
		Message sentMessage = connectionGUI.getServer().sendMessage( loginGUI.getUsername(), chatArea.getCurrentChatUsername(), messageTypingArea.getText() );
		if ( sentMessage.MID == Message.ERROR ) {
			Util.errorTooltip.show( mainStage );
			return false;
		}
		chatArea.addMessageToChat( new MessageText( sentMessage, true ) );
		chatArea.scrollToBottom();
		return true;
	}
	
	private void setupLayout() {
		setupBackground();
		setupForeground();
		setupControlsBox( openLoginGUIButton, showAvailableUsersMenuButton, openConnectionGUIButton, minimizeButton, maximizeButton, exitApplicationButton );
		
		ImageView bannerImage = new ImageView( Util.BANNER_IMAGE );
		bannerImage.fitWidthProperty().bind( mainStage.widthProperty() );
		
		setupWindow( bannerImage, controlsBox );
		setupFrame( frameBox );
		setupBottomBox( messageTypingArea, sendMessageButton );
		setupMainScene();
		setupMainStage();
	}
	
	private void setupBackground() {
		ImageView backgroundImage = new ImageView( Util.BACKGROUND_IMAGE );
		backgroundImage.fitWidthProperty().bind( mainStage.widthProperty() );
		backgroundImage.fitHeightProperty().bind( mainStage.heightProperty() );
		background.getChildren().addAll( backgroundImage, foreground );
	}
	
	private void setupForeground() {
		foreground.setTop( frame );
		foreground.setCenter( chatArea.getNode() );
		foreground.setBottom( bottomBox );
		foreground.setMinHeight( Util.MIN_PROGRAM_HEIGHT_PIXELS );
		foreground.setPrefHeight( Util.MIN_PROGRAM_HEIGHT_PIXELS );
		BorderPane.setAlignment( chatArea.getNode(), Pos.TOP_CENTER );
	}
	
	private void setupControlsBox( Node... children ) {
		controlsBox.getChildren().clear();
		controlsBox.setMaxHeight( Util.FRAME_HEIGHT_PIXELS );
		controlsBox.setMinHeight( Util.FRAME_HEIGHT_PIXELS );
		controlsBox.setAlignment( Pos.TOP_RIGHT );
		controlsBox.setSpacing( 10 );
		controlsBox.getChildren().addAll( children );
	}
	
	private void setupWindow( Node... children ) {		
		frameBox.setOnMousePressed( mouse -> saveInitialMousePosition( mouse ) );
		frameBox.setOnMouseDragged( mouse -> {
			if ( shouldMoveStage( mouse ) ) {
				mainStage.setX( mouse.getScreenX() - deltaMouseX );
				mainStage.setY( mouse.getScreenY() - deltaMouseY );
			}
		});
		frameBox.visibleProperty().bind( frameBox.layoutYProperty().lessThanOrEqualTo( Util.Y_POSITION_TO_HIDE_WINDOW_PIXELS ).not() );
		frameBox.getChildren().addAll( children );
		//window.setLayoutY( 0 );
	}
	
	private boolean shouldMoveStage( MouseEvent mouse ) {
		return !mainStage.isFullScreen() && mouseNotOnControls( mouse, openLoginGUIButton ) && mouse.getY() <= Util.FRAME_HEIGHT_PIXELS;
	}
	
	private void saveInitialMousePosition( MouseEvent e ) {
		double origionalMouseClickX = e.getScreenX();
		double origionalMouseClickY = e.getScreenY();
		deltaMouseX = origionalMouseClickX - mainStage.getX();
		deltaMouseY = origionalMouseClickY - mainStage.getY();
	}
	
	private boolean mouseNotOnControls( MouseEvent mouse, ButtonPane leftMostButton ) {
		return mouse.getScreenX() < leftMostButton.localToScreen( leftMostButton.getX(), 0 ).getX();
	}
	
	private void setupFrame( Node... children ) {
		frame.getChildren().addAll( children );
		frame.setMaxHeight( Util.FRAME_HEIGHT_PIXELS );
		frame.setOnMouseExited( e -> {
			if ( mainStage.isFullScreen() ) {
				showFrameAnimation.pause();
				hideFrameAnimation.play();
			}
		});
		
		frame.setOnMouseEntered( e -> {
			if ( mainStage.isFullScreen() ) {
				hideFrameAnimation.pause();
				showFrameAnimation.play();
			}
		});
	}
	
	private Timeline createFrameAnimationTopDown() {
		return createFrameAnimation( true );
	}
	
	private Timeline createFrameAnimationBottomUp() {
		return createFrameAnimation( false );
	}

	private Timeline createFrameAnimation( boolean isTopDown ) {
		Timeline frameAnimation = new Timeline();
		final int TOP_DOWN = 1, BOTTOM_UP = -1;
		frameAnimation.getKeyFrames().add( new KeyFrame( Duration.millis( 5 ), e -> {
			int deltaY = isTopDown ? TOP_DOWN : BOTTOM_UP;
			if ( stopAnimation( isTopDown ) )
				frameAnimation.pause();
			frameBox.setLayoutY( frameBox.getLayoutY() + deltaY );
			frame.setMaxHeight( frame.getMaxHeight() + deltaY );
		}));
		frameAnimation.setCycleCount( Timeline.INDEFINITE );
		return frameAnimation;
	}
	// make readable
	private boolean stopAnimation( boolean isTopDown ) {
		return isTopDown ? frameBox.getLayoutY() >= 0 && frame.getMaxHeight() >= Util.FRAME_HEIGHT_PIXELS : frameBox.getLayoutY() <= Util.Y_POSITION_TO_HIDE_WINDOW_PIXELS + 1 && frame.getMaxHeight() <= 2;
	}
	
	private void setupBottomBox( Node... children ) {
		bottomBox.setAlignment( Pos.BOTTOM_CENTER );
		bottomBox.setSpacing( 20 );
		bottomBox.setPadding( new Insets( 0, 0, 10, 0 ) );
		bottomBox.getChildren().addAll( children );
	}
	
	private void setupMainScene() {
		mainScene.getStylesheets().add( Util.STYLESHEET_PATH );
	}
	
	private void setupMainStage() {
		mainStage.initStyle( StageStyle.UNDECORATED );
		mainStage.setResizable( false );
		mainStage.setScene( mainScene );
		mainStage.getIcons().add( Util.ICON );
		mainStage.setTitle( Util.TITLE );
		
		mainStage.iconifiedProperty().addListener( e -> {
			if ( !mainStage.isIconified() ) {
				mainStage.setMaximized( true );
				mainStage.setHeight( 720 );
				mainStage.setWidth( 1280 );
				mainStage.setX( stageX );
				mainStage.setY( stageY );
			}
			mainStage.setFullScreen( false );
		});
		mainStage.fullScreenProperty().addListener( e -> {
			if ( !mainStage.isFullScreen() ) {
				try {
					hideFrameAnimation.pause();
					showFrameAnimation.play();
				}
				catch ( NullPointerException nullException ) {
					// not created yet
				}
			}
		});
	}
}
