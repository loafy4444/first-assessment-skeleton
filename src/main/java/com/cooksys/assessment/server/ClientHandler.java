package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;
	private int ID;
	private String user;
	private Handler handler;

	public ClientHandler(Socket socket, Handler handler) {
		super();
		this.socket = socket;
		this.handler = handler;
		ID = socket.getPort();
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

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				String response = mapper.writeValueAsString(message);

				switch (message.getCommand()) {

				case "connect":
					if (!handler.validateUserName(message.getUsername())) {
						log.info("<{}> <{}> attempted to connect with an invalid username and was auto-disconnected.", message.getTimeStamp(),
								message.getUsername());
						message.setCommand("invaliduser");
						message.setContents(message.getCommand());
						response = mapper.writeValueAsString(message);
						handler.msgOne(this.ID, response);
						handler.removeClient(this);
						this.socket.close();
						break;
					} else if (handler.duplicateUserName(message.getUsername())) {
						log.info("<{}> <{}> attempted to connect with a duplicate username and was auto-disconnected.", message.getTimeStamp(),
								message.getUsername());
						message.setCommand("duplicateusername");
						message.setContents(message.getCommand());
						response = mapper.writeValueAsString(message);
						handler.msgOne(this.ID, response);
						handler.removeClient(this);
						this.socket.close();
						break;
					} else {
						log.info("<{}> <{}> connected", message.getTimeStamp(), message.getUsername());
						this.user = message.getUsername();
						message.setContents(message.getCommand());
						response = mapper.writeValueAsString(message);
						handler.msgAll(response);
						log.info("Current Users Logged In: " + handler.getUsers());
						break;
					}

				case "disconnect":
					log.info("<{}> <{}> disconnected", message.getTimeStamp(), message.getUsername());
					message.setContents(message.getCommand());
					response = mapper.writeValueAsString(message);
					handler.msgAll(response);
					handler.removeClient(this);
					this.socket.close();
					log.info("Current Users Logged In: " + handler.getUsers());
					break;

				case "echo":
					log.info("<{}> echoed message: <{}>", message.getUsername(), message.getContents());
					handler.msgOne(this.ID, response);
					break;

				case "broadcast":
					log.info("<{}> broadcast message: <{}>", message.getUsername(), message.getContents());
					handler.msgAll(response);
					break;

				case "users":
					message.setContents(handler.usersCom());
					log.info("<{}> <{}> requested user data", message.getTimeStamp(), message.getUsername());
					response = mapper.writeValueAsString(message);
					handler.msgOne(this.ID, response);
					break;

				case "@":
					if (!handler.getUsers().contains(message.getTargetUser())) {
						message.setCommand("usernotfound");
						message.setContents(message.getCommand());
						log.info("<{}>  attempted to whisper <{}> but it failed due to user not found.", message.getUsername(),
								message.getTargetUser());
						response = mapper.writeValueAsString(message);
						handler.msgOne(ID, response);
						break;
					}
					log.info("<{}> whispered <{}>: <{}>", message.getUsername(), message.getTargetUser(), message.getContents());
					if (message.getTargetUser().equals(message.getUsername())) {
						handler.msgOne(this.ID, response);
					} else {
						handler.msgOne(this.ID, response);
						handler.msgOne(handler.getUserID(message.getTargetUser()), response);
					}
					break;
				default:
					log.error("Not sure what the crap {} did but they broke their client.", message.getUsername());
				}
			}

		} catch (IOException e) {
			try {
				log.info("<{}> disconnected due to error. ", user);
				handler.msgAll(user + " disconnected due to error.");
				handler.removeClient(this);
				this.socket.close();
				log.info("Current Users Logged In: " + handler.getUsers());
				log.error("Something went wrong :/", e);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

}
