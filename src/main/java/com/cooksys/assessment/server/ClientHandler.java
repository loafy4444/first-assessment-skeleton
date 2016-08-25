package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;
	private int ID;
	private InetAddress userIP;
	private String user;
	private Handler handler;
	private ObjectMapper mapper;

	public ClientHandler(Socket socket, Handler handler, ObjectMapper mapper) {
		super();
		this.socket = socket;
		this.handler = handler;
		this.mapper = mapper;
		ID = socket.getPort();
		userIP = socket.getInetAddress();
	}

	public InetAddress getUserIP() {
		return userIP;
	}

	public Socket getSocket() {
		return socket;
	}

	public int getID() {
		return ID;
	}

	public String getUser() {
		return user;
	}

	public void run() {
		try {

			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				String response = mapper.writeValueAsString(message);

				switch (message.getCommand()) {

				case "connect":
					if (!handler.validateUserName(message.getUsername())) {  //  Validates that username does not contain spaces or special characters
						log.info("<{}> <{}> attempted to connect with an invalid username and was auto-disconnected.", message.getTimeStamp(),
								message.getUsername());
						message.setCommand("invaliduser");
						message.setContents(null);
						response = mapper.writeValueAsString(message);
						handler.msgOne(socket, response);
						handler.removeClient(this);
						this.socket.close();
						break;
					} else if (handler.duplicateUserName(message.getUsername())) {  //  Check if username is already in use on server
						log.info("<{}> <{}> attempted to connect with a duplicate username and was auto-disconnected.", message.getTimeStamp(),
								message.getUsername());
						message.setCommand("duplicateusername");
						message.setContents(null);
						response = mapper.writeValueAsString(message);
						handler.msgOne(socket, response);
						handler.removeClient(this);
						this.socket.close();
						break;
					} else {
						log.info("<{}> <{}> connected", message.getTimeStamp(), message.getUsername());
						this.user = message.getUsername();
						message.setContents(null);
						response = mapper.writeValueAsString(message);
						handler.msgAll(response);
						log.info("Current Users Logged In: " + handler.getUsers());
						break;
					}

				case "disconnect":
					log.info("<{}> <{}> disconnected", message.getTimeStamp(), message.getUsername());
					message.setContents(null);
					response = mapper.writeValueAsString(message);
					handler.msgAll(response);
					handler.removeClient(this);
					this.socket.close();
					log.info("Current Users Logged In: " + handler.getUsers());
					break;

				case "echo":
					log.info("<{}> echoed message: <{}>", message.getUsername(), message.getContents());
					handler.msgOne(socket, response);
					break;

				case "broadcast":
					log.info("<{}> broadcast message: <{}>", message.getUsername(), message.getContents());
					handler.msgAll(response);
					break;

				case "users":
					message.setContents(handler.usersCom());
					log.info("<{}> <{}> requested user data", message.getTimeStamp(), message.getUsername());
					response = mapper.writeValueAsString(message);
					handler.msgOne(socket, response);
					break;

				case "@":
					if (!handler.getUsers().contains(message.getTargetUser())) {  //  Check is target user is a valid user
						message.setCommand("usernotfound");
						message.setContents(null);
						log.info("<{}>  attempted to whisper <{}> but it failed due to user not found.", message.getUsername(),
								message.getTargetUser());
						response = mapper.writeValueAsString(message);
						handler.msgOne(socket, response);
						break;
					}
					log.info("<{}> whispered <{}>: <{}>", message.getUsername(), message.getTargetUser(), message.getContents());
					if (message.getTargetUser().equals(message.getUsername())) {  //  If user is whispering themselves only whispers once
						handler.msgOne(socket, response);
					} else {   //  If user is whispering someone else sends the message to both users (not needed but I hate not seeing that the message was sent)
						handler.msgOne(socket, response);
						handler.msgOne(handler.getUserSocket(message.getTargetUser()), response);
					}
					break;
				default:  //  Somehow the user sent an invalid command because their client sucks
					log.error("Not sure what the crap {} did but they broke their client./n"
							+ "Command Sent: <{}>\nContents Sent: <{}>" , message.getUsername(), message.getCommand(), message.getContents());
				}
			}

		} catch (IOException e) {
			try {  //Usually overloading issue... still need to check for possible solutions
				log.info("<{}> disconnected due to error. ", user);
				handler.msgAll(user + " disconnected due to error.");
				handler.removeClient(this);
				this.socket.close();
				log.info("Current Users Logged In: " + handler.getUsers());
				log.error("Something went wrong in the ClientHandler.... Because, users.", e);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

}
