package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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


	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
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
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			
			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				String response = mapper.writeValueAsString(message);
																
				switch (message.getCommand()) {
					
					case "connect":
						log.info("<{}> <{}> connected", message.getTimeStamp(), message.getUsername());
						this.user = message.getUsername();
						message.setContents(message.getCommand());
						response = mapper.writeValueAsString(message);
						Server.msgAll(response);
						log.info("Current Users Logged In: " + Server.getUsers());
						break;
					
					case "disconnect":
						log.info("<{}> <{}> disconnected",  message.getTimeStamp(), message.getUsername());
						message.setContents(message.getCommand());
						response = mapper.writeValueAsString(message);
						Server.msgAll(response);
						Server.removeClient(this);
						this.socket.close();
						log.info("Current Users Logged In: " + Server.getUsers());
						break;
					
					case "echo":
						log.info("<{}> echoed message: <{}>", message.getUsername(), message.getContents());
						Server.msgOne(user, response);
						break;
					
					case "broadcast":
						log.info("<{}> broadcast message: <{}>", message.getUsername(), message.getContents());
						Server.msgAll(response);
						break;
					
					case "users":
						message.setContents(Server.getUsers().toString());;
						log.info("<{}> <{}> requested user data", message.getTimeStamp(), message.getUsername());
						response = mapper.writeValueAsString(message);
						Server.msgOne(user, response);
						break;
						
					case "@":
						log.info("<{}> whispered <{}>: <{}>", message.getUsername(), message.getTargetUser(), message.getContents());
						if ( message.getTargetUser().equals(message.getUsername())) {
							Server.msgOne(user, response);					
						} else {
							Server.msgOne(user, response);		
							Server.msgOne(message.getTargetUser(), response);
						}
						break;
					default:
						log.error("Not sure what the crap {} did but they broke it all.", message.getUsername());
				}
			}

		} catch (IOException e) {
			try {
				log.info("<{}> disconnected due to error. ",  user);
				Server.msgAll(user + " disconnected due to error.");
				Server.removeClient(this);
				this.socket.close();
				log.info("Current Users Logged In: " + Server.getUsers());
				log.error("Something went wrong :/", e);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

}
