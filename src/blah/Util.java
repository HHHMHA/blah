package blah;

import java.nio.file.Paths;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.media.AudioClip;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import tray.animations.AnimationType;
import tray.notification.NotificationType;
import tray.notification.TrayNotification;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;
/**
 * A class to contain resources and methods used by multiple classes in this package.
 * @author MHA
 */
final class Util {
	static final Image BACKGROUND_IMAGE = new Image( "blah/res/Background.png" ),
			           BANNER_IMAGE = new Image( "blah/res/Banner.png" ),
	                   ICON = new Image( "blah/res/Blah.png" ),
                       CONNECT_1 = new Image( "blah/res/Connect_1.png" ), 
                       CONNECT_3 = new Image( "blah/res/Connect_3.png" ),
                       SEND_BUTTON_MASK = new Image( "blah/res/send.png" );
	
	static final String STYLESHEET_PATH = "blah/res/style.css",
	                    IP_REGIX_STRING = "\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}",
	                    MULTICAST_ADDRESS = "225.0.0.0",
						TITLE = "Blah";
	
	static final char USERS_CODE_ERROR_FETCHING = '2',
			          USERS_CODE_ONLINE = '1';
	
	static final int SERVER_PORT = 8880,
	                 MULTICAST_SERVER_PORT = 8885,
	                 SO_TIMEOUT_MILLI_SECONDS = 2000,
	                 CONNECTION_TIMEOUT_MILLI_SECONDS = 3000,
	                 FRAME_HEIGHT_PIXELS = 50,
	                 Y_POSITION_TO_HIDE_WINDOW_PIXELS = -FRAME_HEIGHT_PIXELS + 1,
	                 MAX_ROWS_TO_SHOW_IN_MESSAGE_TYPING_AREA = 4,
	                 MIN_PROGRAM_HEIGHT_PIXELS = 720,
	                 CODE_INDEX = 0,
	                 USERNAME_INDEX = 1;
	
	static final PseudoClass SENDER = PseudoClass.getPseudoClass( "sender" ), 
                             READ = PseudoClass.getPseudoClass( "read" );
	
	static final AudioClip NOTIFICATION_SOUND = new AudioClip( Paths.get( "blah/res/notify.wav" ).toUri().toString() ); // fix the audio can't be found when it's inside the jar
	static Tooltip errorTooltip;
	static final Duration HALF_SECOND = new Duration( 500 );
	
	static {
		errorTooltip = new Tooltip( "Connection Error!" ); 
		errorTooltip.setShowDuration( new Duration( 500 ) );
		errorTooltip.setAutoHide( true );
	}
	
	static Alert createAlert( AlertType alertType, String contentText, Window owner, ButtonType... buttons ) {
		Alert alert = new Alert( alertType, contentText, buttons );
		alert.getDialogPane().setBackground( new Background( new BackgroundImage( new Image( "blah/res/Background.png" ), null, null, null, null ) ) );
		alert.setHeaderText( null );
		alert.initModality( Modality.WINDOW_MODAL );
		alert.initOwner( owner );
		alert.getDialogPane().getScene().getStylesheets().add( STYLESHEET_PATH );
		( (Stage) alert.getDialogPane().getScene().getWindow() ).getIcons().add( ICON );
		return alert;
	}
	
	static final ButtonPane createButtonPane( String title, String imagePathWithoutUnderSocreAndNumber, String TooltipText, EventHandler<? super MouseEvent> event ) {
		ButtonPane button = new ButtonPane( title, imagePathWithoutUnderSocreAndNumber + "_1.png", imagePathWithoutUnderSocreAndNumber + "_2.png", imagePathWithoutUnderSocreAndNumber + "_3.png", null, null );
		if ( TooltipText != null )
			button.setTooltip( new Tooltip( TooltipText ) );
		button.setOnAction( event );
		return button;
	}
	
	static final MenuButtonPane createMenuButtonPane( String title, String imagePathWithoutUnderSocreAndNumber, String TooltipText, EventHandler<? super MouseEvent> event ) {
		MenuButtonPane button = new MenuButtonPane( title, imagePathWithoutUnderSocreAndNumber + "_1.png", imagePathWithoutUnderSocreAndNumber + "_2.png", imagePathWithoutUnderSocreAndNumber + "_3.png", null, null );
		button.setTooltip( new Tooltip( TooltipText ) );
		button.setOnAction( event );
		return button;
	}
	
	static boolean isIP( String value ) {
		return value.matches( IP_REGIX_STRING );
	}
	
	static boolean isWhiteSpace( String value ) {
		return value.matches( "\\s*" );
	}
	
	static TrayNotification createTrayNotification( String title, String message ) {
		TrayNotification tray = new TrayNotification();
		tray.setTitle( title );
		tray.setMessage( message );
		tray.setNotificationType( NotificationType.INFORMATION );
		tray.setAnimationType( AnimationType.POPUP );
		tray.setImage( Util.ICON );
		return tray;
	}
}
