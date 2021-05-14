package blah;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Message implements Serializable {
	private static final long serialVersionUID = -919075327975565207L;
	public final long MID;
	public final String SENDER, RECEIVER, CONTENT, DATESENT;
	private boolean read, deleted;
	public final GregorianCalendar CALENDAR;
	public static final long UPDATE = -1, ERROR = -2;
	/**
	 * The only constructor for a message with all the properties
	 * @param MID - A long identifier for the message which is set by the database automatically can have two special
	 * values Message.UPDATE which indicate that the client should re-retrieve past messages because they may have been updated (this is what is used with the GET_MESSAGES_WITH_NOTIFY request),
	 * and Message.ERROR indicating an error from the server.
	 * @param SENDER - a string representing the username of the sender.
	 * @param RECEIVER - a string representing the username of the receiver.
	 * @param CONTENT - a string representing the content of the message it self.
	 * @param DATESENT - a string representing the date on which the message was created and sent, if null it will get the current datetime value.
	 * you should only provide this when retrieving messages from the database.
	 * @param read - a boolean which indicated if the message has been read by the receiver.
	 * @param deleted - a boolean which indicates if the message was deleted, and it should not be displayed.
	 */
	public Message( long MID, String SENDER, String RECEIVER, String CONTENT, String DATESENT, boolean read, boolean deleted ) {
		this.MID = MID;
		this.SENDER = SENDER;
		this.RECEIVER = RECEIVER;
		this.CONTENT = CONTENT;
		this.read = read;
		this.deleted = deleted;
		CALENDAR = new GregorianCalendar();
		if ( DATESENT == null || DATESENT.equals( "" ) )
			this.DATESENT = CALENDAR.get( Calendar.DAY_OF_MONTH ) + "/" + CALENDAR.get( Calendar.MONTH ) + "/" + CALENDAR.get( Calendar.YEAR ) + " " + CALENDAR.get( Calendar.HOUR ) + ":" + 
					CALENDAR.get( Calendar.MINUTE ) + ":" + CALENDAR.get( Calendar.SECOND ) + " " + (CALENDAR.get( Calendar.AM_PM ) == 0 ? "AM" : "PM");
		else
			this.DATESENT = DATESENT;
	}

	public final boolean isDeleted() {
		return deleted;
	}

	public final boolean isRead() {
		return read;
	}

	public final void setRead() {
		read = true;
	}
	
	public final static Message getErrorMessage() {
		return new Message( Message.ERROR, null, null, "SERVER MESSAGE!!!\n An error occured please wait a few seconds", null, true, false );
	}
	
	public final static Message getUpdateMessage( String sender ) {
		return new Message( Message.UPDATE, sender, null, null, null, false, true );
	}
}
