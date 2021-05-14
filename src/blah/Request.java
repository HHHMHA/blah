package blah;

import java.io.Serializable;

// 0 check connection
// 1 login
// 2 logout
// 3 get a list of all other users
// 4 see if there is pending messages
// 5 send a message
// 6 and 7 get a list of all messages of user x

public final class Request implements Serializable {
	private static final long serialVersionUID = 1L;
	public final RequestCode requestID;
	public final String username, password, currentChatUsername;
	public final Message message;
	public Request( RequestCode requestID ) {
		this.requestID = requestID;
		this.username = null;
		this.password = null;
		this.currentChatUsername = null;
		message = null;
	}
	
	public Request( RequestCode requestID, String username, String password ) {
		this.requestID = requestID;
		this.username = username;
		this.password = password;
		this.currentChatUsername = null;
		message = null;
	}
	
	public Request( RequestCode requestID, String currentChatUsername ) {
		this.requestID = requestID;
		this.username = null;
		this.password = null;
		this.currentChatUsername = currentChatUsername;
		this.message = null;
	}
	
	public Request( RequestCode requestID, Message message ) {
		this.requestID = requestID;
		this.username = null;
		this.password = null;
		this.currentChatUsername = null;
		this.message = message;
	}
}