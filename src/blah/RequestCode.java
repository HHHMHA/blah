package blah;

import java.io.Serializable;

//0 check connection
//1 login
//2 logout
//3 get a list of all other users
//4 see if there is pending messages
//5 send a message
//6 and 7 get a list of all messages of user x

/**
 * Mapping for RequestID, use when Creating a Request object and pass as the first argument to the constructor
 * @author MHA
 */
public enum RequestCode implements Serializable {
	/**
	 * Check if the server is online, requires no further arguments.
	 * The sever will return a true boolean if it is online.
	 */
	CHECK_CONNECTION, 
	/**
	 * Logs in the user, requires the username and the password.
	 */
	LOGIN,
	/**
	 * Logout the user, requires no further arguments.
	 */
	LOGOUT, 
	/**
	 * Get a list of all other users, Requires no further arguments.
	 */
	USERS, 
	/**
	 * Get a list of messages sent to this user, Requires no further arguments.
	 */
	PENDING_MESSAGES, 
	/**
	 * Send a message to a user, requires the message as an argument.
	 */
	SEND, 
	/**
	 * Retrieve the first available MID from the database
	 */
	MID,
	/**
	 * Get the messages between the current user and an extra argument which is the other user.
	 * It will also generates a request telling the other user to get the messages also in case they where updated.
	 */
	GET_MESSAGES_WITH_NOTIFY, 
	/**
	 * Get the messages between the current user and an extra argument which is the other user.
	 * Use only if the messages aren't updates eg: used as a response to the GET_MESSAGES_WITH_NOTIFY so we don't get caught in a loop of updating the messages.
	 */
	GET_MESSAGES_WITHOUT_NOTIFY;
}
