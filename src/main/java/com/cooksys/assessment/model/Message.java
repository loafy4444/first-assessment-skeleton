package com.cooksys.assessment.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {

	//  TODO add Date timeStamp to Message as the message is received from the user
	//  This will allow the ClientHandler to simply pull that out for use in the run method as message.getTimeStamp()
	private Date timestamp = new Date();
	private SimpleDateFormat dStr = new SimpleDateFormat("MM.dd.yy zzz hh:mm:ss a ");
	private String dateStr;
	private String username;
	private String command;
	private String contents;

	public Message() {
		timestamp = new Date();
	}
	
	public String getTimeStamp() {
		dateStr = dStr.format(timestamp);
		return dateStr;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		switch(command){
			case "echo":
				this.contents = (this.getTimeStamp() + " <" + username + "> (echo): " + contents);
				break;
			case "broadcast":
				this.contents = (this.getTimeStamp() + " <" + username + "> (all): " + contents);
				break;
			case "@":
				this.contents = (this.getTimeStamp() + " <" + username + "> (whisper): " + contents);
				break;
			case "users":
				this.contents = (this.getTimeStamp() + ": currently connect users: \n " + contents);
				break;
			case "connect": //  TODO Needed?
				this.contents = (this.getTimeStamp() + " <" + username + "> has connected.");
				break;
			case "disconnect": //  TODO Needed?
				this.contents = (this.getTimeStamp() + " <" + username + "> has disconnected.");
				break;
		}
	}

}
