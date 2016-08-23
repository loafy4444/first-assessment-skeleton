package com.cooksys.assessment.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {

	private Date timestamp = new Date();
	private SimpleDateFormat dStr = new SimpleDateFormat("MM.dd.yy zzz hh:mm:ss a ");
	private String dateStr;
	private String username;
	private String command;
	private String contents;
	private String targetUser;

	public String getTargetUser() {
		return targetUser;
	}

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
		if (command.charAt(0) == '@') {
			this.command = "@";
			targetUser = command.substring(1);
		} else {
			this.command = command;
		}
		
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
				this.contents = (this.getTimeStamp() + ": currently connected users: \n " + contents);
				break;
			case "connect":
				this.contents = (this.getTimeStamp() + " <" + username + "> has connected to the coolest server in the whole class.");
				break;
			case "disconnect":
				this.contents = (this.getTimeStamp() + " <" + username + "> has disconnected because they are a sad sad soul.");
				break;
		}
	}

}
