package blah;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ButtonPane extends StackPane {
	private Image idleImg, onMouseEnteredImg, onMouseClickedImg;
	protected ImageView currentImg;
	private AudioClip onMouseEnteredAudioClip, onMouseClickedAudioClip;
	private Label text;
	private Tooltip tooltip;
	private EventHandler<? super MouseEvent> mouseEventHandler = null;
	
	public ButtonPane( String text, String idleImgPath, String onMouseEnteredImgPath, String onMouseClickedImgPath, String onMouseEnteredAudioClipPath, String onMouseClickedAudioClipPath ) {
		tooltip = null;
		idleImg = new Image( idleImgPath );
		onMouseEnteredImg = new Image( onMouseEnteredImgPath );
		onMouseClickedImg = new Image( onMouseClickedImgPath );
		
		if ( onMouseEnteredAudioClipPath != null )
			onMouseEnteredAudioClip = new AudioClip( onMouseEnteredAudioClipPath );
		
		if ( onMouseClickedAudioClipPath != null )
			onMouseClickedAudioClip = new AudioClip( onMouseClickedAudioClipPath );
		
		currentImg = new ImageView( this.idleImg );
		this.text = new Label( text );
		this.text.setFont( Font.font( "Consolas", FontWeight.BOLD, 16 ) );
		setMaxSize( currentImg.getFitWidth(), currentImg.getFitHeight() );
		
		getChildren().addAll( currentImg, this.text );
		setCursor( Cursor.HAND );
		setOnMouseEntered( e -> {
			if ( tooltip != null )
				tooltip.show( this, e.getScreenX() - tooltip.getWidth() / 2, currentImg.localToScreen( currentImg.getX(), currentImg.getY() ).getY() + getHeight() + 5 );
			
			currentImg.setImage( this.onMouseEnteredImg );
			//setCursor( Cursor.HAND );
			this.text.setTextFill( Color.YELLOW );
			if ( this.onMouseEnteredAudioClip != null )
				this.onMouseEnteredAudioClip.play();
		});
		
		setOnMouseMoved( e -> {
			currentImg.setImage( this.onMouseEnteredImg );
			if ( tooltip != null )
				tooltip.setX( e.getScreenX() - tooltip.getWidth() / 2 );
			//setCursor( Cursor.HAND );
			this.text.setTextFill( Color.YELLOW );
			if ( this.onMouseEnteredAudioClip != null )
				this.onMouseEnteredAudioClip.play();
		});
		
		setOnMousePressed( e -> {
			if ( tooltip != null )
				tooltip.hide();
			currentImg.setImage( this.onMouseClickedImg );
			this.text.setTextFill( Color.YELLOW );
			if ( this.onMouseClickedAudioClip != null )
				this.onMouseClickedAudioClip.play();
		});
		
		setOnMouseReleased( e -> {
			if ( tooltip != null )
				tooltip.hide();
			//setCursor( Cursor.DEFAULT );
			currentImg.setImage( this.idleImg );
			this.text.setTextFill( Color.WHITE );
		});
		
		setOnMouseExited( e -> {
			//setCursor( Cursor.DEFAULT );
			if ( tooltip != null )
				tooltip.hide();
			currentImg.setImage( this.idleImg );
			this.text.setTextFill( Color.WHITE );
		});
	}
	
	public void setOnAction( EventHandler<? super MouseEvent> event ) {
		mouseEventHandler = event;
		setOnMouseClicked( mouseEventHandler );
	}
	
	public final void setIdleImage( Image idleImg ) {
		checkArgumentIfNullAndThrowNullException( idleImg );
		this.idleImg = idleImg;
		currentImg.setImage( idleImg );
	}
	
	public final double getX() {
		return currentImg.getX();
	}
	
	public final double getY() {
		return currentImg.getY();
	}
	
	public final Image getOnMouseEnteredImg() {
		return onMouseEnteredImg;
	}

	public final void setOnMouseEnteredImg( Image onMouseEnteredImg ) {
		checkArgumentIfNullAndThrowNullException( onMouseEnteredImg );
		this.onMouseEnteredImg = onMouseEnteredImg;
	}

	public final Image getOnMouseClickedImg() {
		return onMouseClickedImg;
	}

	public final void setOnMouseClickedImg( Image onMouseClickedImg ) {
		checkArgumentIfNullAndThrowNullException( onMouseClickedImg );
		this.onMouseClickedImg = onMouseClickedImg;
	}

	public final AudioClip getOnMouseEnteredClp() {
		return onMouseEnteredAudioClip;
	}

	public final void setOnMouseEnteredClp( AudioClip onMouseEnteredClp ) {
		this.onMouseEnteredAudioClip = onMouseEnteredClp;
	}

	public final AudioClip getOnMouseClickedClp() {
		return onMouseClickedAudioClip;
	}

	public final void setOnMouseClickedClp( AudioClip onMouseClickedClp ) {
		this.onMouseClickedAudioClip = onMouseClickedClp;
	}

	public final Label getText() {
		return text;
	}

	public final void setText( Label text ) {
		checkArgumentIfNullAndThrowNullException( text );
		this.text = text;
	}

	public final Image getIdleImg() {
		return idleImg;
	}

	public final Tooltip getTooltip() {
		return tooltip;
	}
	
	public final void setTooltip( Tooltip tooltip ) {
		this.tooltip = tooltip;
		this.tooltip.setAutoHide( true );
	}
	
	public void fire() {
		if ( mouseEventHandler != null )
			mouseEventHandler.handle( new MouseEvent( this, null, MouseEvent.MOUSE_CLICKED, currentImg.getX(), currentImg.getY(), currentImg.getX(), currentImg.getY(), MouseButton.PRIMARY, 1, false, false, false, false, true, false, false, false, false, false, null ) );
	}
	
	private final void checkArgumentIfNullAndThrowNullException( Object argument ) {
		if ( argument == null )
			throw new NullPointerException( "Argument Can't be null" );
	}
}