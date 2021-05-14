package blah;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class MenuButtonPane extends ButtonPane {
	private ContextMenu contextMenu;
	public MenuButtonPane(String text, String idleImg, String onMouseEnteredImg, String onMouseClickedImg, String onMouseEnteredClp, String onMouseClickedClp) {
		super( text, idleImg, onMouseEnteredImg, onMouseClickedImg, onMouseEnteredClp, onMouseClickedClp );
		contextMenu = new ContextMenu();
		setOnMouseClicked( e2 -> {
			if ( e2.getButton() == MouseButton.PRIMARY ) {
				if ( !contextMenu.isShowing() )
					contextMenu.show( this, currentImg.localToScreen( currentImg.getX(), currentImg.getY() ).getX(), currentImg.localToScreen( currentImg.getX(), currentImg.getY() ).getY() + currentImg.getImage().getHeight() );
				else
					contextMenu.hide();
			}
		});
	}

	public final ObservableList< MenuItem > getItems() {
		return contextMenu.getItems();
	}
	
	@Override
	public void setOnAction( EventHandler<? super MouseEvent> e ) {
		setOnMouseClicked( e2 -> {
			e.handle( e2 );
			fire();
		});
	}
	
	@Override
	public void fire() {
		if ( !contextMenu.isShowing() )
			contextMenu.show( this, currentImg.localToScreen( currentImg.getX(), currentImg.getY() ).getX(), currentImg.localToScreen( currentImg.getX(), currentImg.getY() ).getY() + currentImg.getImage().getHeight() );
		else
			contextMenu.hide();
	}
}
