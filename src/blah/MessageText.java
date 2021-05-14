package blah;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;

public class MessageText extends HBox {
	Message message;
	boolean isSelected = false;
	private TextArea textArea;
	private boolean isSender;
	private ContextMenu contextMenu;
	
	/**
	 * The main (and only) constructor to create a MessageText object.
	 * @param message - The Message object to show and contain.
	 * @param isSender - true if the message is sent by the user which constructed the object. used to display the message on the right side and
	 * to ignore the isRead property.
	 */
	public MessageText( Message message, boolean isSender ) {
		this.isSender = isSender;
		this.message = message;
		textArea = new TextArea( message.CONTENT );
		textArea.setEditable( false );
		textArea.setPrefColumnCount( maxColumn( message.CONTENT.split( "\n" ) ) );
		textArea.setPrefRowCount( maxRow( message.CONTENT.split( "\n" ) ) );
		textArea.pseudoClassStateChanged( Util.SENDER, isSender );
		textArea.pseudoClassStateChanged( Util.READ, message.isRead() && isSender );
		
		getChildren().add( textArea );
		setAlignment( isSender ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT );
		setOnMouseClicked( e -> {
			if ( e.getButton() == MouseButton.PRIMARY ) {
				isSelected = !isSelected;
				String color = isSelected ? "rgba( 50, 50, 200, 0.5 );" : "transparent;";
				setStyle( "-fx-background-color: " + color );
			}
			else if ( contextMenu != null && e.getButton() == MouseButton.SECONDARY ) {
				contextMenu.show( this, e.getSceneX(), e.getScreenY() );
			}
		});
		
		
		textArea.setTooltip( new Tooltip( message.DATESENT ) );
		
		setPadding( new Insets( 5 ) );
	}
	
	public final void setRead() {
		message.setRead();
		textArea.pseudoClassStateChanged( Util.READ, isSender );
	}
	
	public final void setContextMenu( ContextMenu value ) {
		contextMenu = value;
	}
	
	public final ContextMenu getContextMenu() {
		return contextMenu;
	}
	
	private int maxColumn( String[] message ) {
		int maxCol = 1;
		for ( String s : message )
			maxCol = Math.max( maxCol, s.length() );
		return Math.min( 30, maxCol );
	}

	private int maxRow( String[] message ) {
		return Math.min( 10, message.length );
	}
}