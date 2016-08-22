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
	
	public Message(String username, String command, String contents) {
		timestamp = new Date();
		this.username = username;
		this.command = command;
		this.contents = contents;
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
		this.contents = contents;
	}

}
