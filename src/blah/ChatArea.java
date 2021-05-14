package blah;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class ChatArea {
	static final String DEFAULT_HEADER_TEXT = "Chat";
	private TitledPane chatHeader;
	private ScrollPane chatBodyScrollPane;
	private VBox chatBody;
	private BlahGUI blahGUI;
	private ConnectionGUI connectionGUI;
	
	public ChatArea( BlahGUI blahGUI, ConnectionGUI connectionGUI ) {
		this.connectionGUI = connectionGUI;
		this.blahGUI = blahGUI;
		chatHeader = new TitledPane();
		chatBodyScrollPane = new ScrollPane();
		initChatArea();
	}
	
	public void setNotChatting() {
		chatHeader.setText( DEFAULT_HEADER_TEXT );
		clearChatMessages();
	}
	
	public void setChattingWith( String user ) {
		chatHeader.setText( user );
		chatBody.getChildren().clear();
		ArrayList<Message> chatMessagesWithUser = connectionGUI.getServer().fetchChatMessagesWith( user );
		setChatMessages( chatMessagesWithUser );
	}
	
	public void setChatMessages(ArrayList<Message> messages) {
		clearChatMessages();
		for ( Message message : messages ) {
			MessageText messageBox = new MessageText( message, message.SENDER.equals( blahGUI.getUsername() ) );
			addMessageToChat( messageBox );
		}
	}
	
	public void clearChatMessages() {
		chatBody.getChildren().clear();
	}
	
	public void addMessageToChat( MessageText messageBox ) {
		if ( !chatHeader.getText().equals( DEFAULT_HEADER_TEXT ) ) {
			chatBody.getChildren().add( messageBox );
		}
	}
	
	public void removeMessageFromChat( MessageText messageBox ) {
		chatBody.getChildren().remove( messageBox );			
	}
	
	public String getCurrentChatUsername() {
		return chatHeader.getText();
	}
	
	public StringProperty getCurrentChatUsernameTextProperty() {
		return chatHeader.textProperty();
	}
	
	public void scrollToBottom() {
		// creating a delay so when something is added to the pane the scroll bars get updated before this code executes
		new Thread( () -> {
			try {
				Thread.sleep( 30 );
			} catch ( InterruptedException e1 ) {}
			Platform.runLater( ()  -> {
				chatBodyScrollPane.setVvalue( chatBodyScrollPane.getVmax() );
			});
		}).start();
	}
	
	public Node getNode() {
		return chatHeader;
	}
	
	private void initChatArea() {
		VBox chatBox = createAndSetupChatBox();
		//chatBoxScrollPane.setVvalue( chatBoxScrollPane.getVmax() );
		ButtonPane scrollToBottomButton = createScrollToBottomButton();
		setupChatBoxScrollPane( chatBox );
		addAndSetupChatElements( chatBodyScrollPane, scrollToBottomButton );
	}
	
	private VBox createAndSetupChatBox() {
		chatBody = new VBox();
		chatBody.prefWidthProperty().bind( blahGUI.widthProperty().subtract( 20 ) );
		chatBody.setId( "chat-box" );
		return chatBody;
	}
	
	private ButtonPane createScrollToBottomButton() {
		ButtonPane scrollToBottomButton = Util.createButtonPane( "", "blah/res/Arrow", "Scroll to bottom", getScrollToBottomAction() );
		hideButtonWhenChatScrolledToBottom( scrollToBottomButton );
		return scrollToBottomButton;
	}
	
	private javafx.event.EventHandler<? super MouseEvent> getScrollToBottomAction() {
		return e -> chatBodyScrollPane.setVvalue( chatBodyScrollPane.getVmax() );
	}
	
	private void hideButtonWhenChatScrolledToBottom( ButtonPane button ) {
		button.visibleProperty().bind( Bindings.createBooleanBinding( () -> {
			return chatBodyScrollPane.getVvalue() != chatBodyScrollPane.getVmax();
		}, chatBodyScrollPane.heightProperty(), chatBody.heightProperty(), chatBodyScrollPane.vvalueProperty(), chatBodyScrollPane.vmaxProperty() ) );
	}
		
	private void setupChatBoxScrollPane( Node chatBox ) {
		chatBodyScrollPane.setContent( chatBox );
		chatBodyScrollPane.setHbarPolicy( ScrollBarPolicy.NEVER );
		chatBodyScrollPane.prefHeightProperty().bind( blahGUI.heightProperty().subtract( 100 + blahGUI.getBottomAreaHeight() ) );
		chatBodyScrollPane.setVvalue( chatBodyScrollPane.getVmax() );
	}
	
	private void addAndSetupChatElements( Node... chatNodes ) {
		StackPane chatStackPane = new StackPane();
		chatStackPane.setAlignment( Pos.BOTTOM_CENTER );
		chatStackPane.getChildren().addAll( chatNodes );
		chatHeader.setText( DEFAULT_HEADER_TEXT );
		chatHeader.setId( "chat-pane" );
		chatHeader.setContent( chatStackPane );
	}
}
