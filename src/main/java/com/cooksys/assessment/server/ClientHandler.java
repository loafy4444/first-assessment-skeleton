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

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
		ID = socket.getPort();
	}

	public int getID() {
			return ID;
	}
	
	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			
			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				
				//  TODO Add additional command functionality base here.
								
				switch (message.getCommand()) {
					
					case "connect":
						log.info("<{}> <{}> connected", message.getTimeStamp(), message.getUsername());
						Server.addUser(ID, message.getUsername());
						log.info("Current Users Logged In: " + Server.getUsers());

						//  TODO add function to notify all users of connection
						break;
					
					case "disconnect":
						log.info("<{}> <{}> disconnected",  message.getTimeStamp(), message.getUsername());
						this.socket.close();
						Server.removeUser(ID, message.getUsername());
						log.info("Current Users Logged In: " + Server.getUsers());
						
						//  TODO add function to notify all users of disconnect
						break;
					
					case "echo":
						log.info("<{}> <{}> echoed message: <{}>", message.getTimeStamp(), message.getUsername(), message.getContents());
						String echoResponse = mapper.writeValueAsString(message);
						writer.write(echoResponse);
						writer.flush();
						break;
					
					case "broadcast":
						log.info("<{}> <{}> broadcast message: <{}>", message.getTimeStamp(), message.getUsername(), message.getContents());
						String broadcastResponse = mapper.writeValueAsString(message);
						writer.write(broadcastResponse);
						writer.flush();
						break;
					
					case "@":
						log.info("<{}> <{}> set a personal message to \'not yet defined\': <{}>", message.getTimeStamp(), message.getUsername(), message.getContents());
						String pmResponse = mapper.writeValueAsString(message);
						writer.write(pmResponse);
						writer.flush();
						break;
					
					case "users":
						message.setContents(Server.getUsers());;
						log.info("<{}> <{}> requested user data", message.getTimeStamp(), message.getUsername());
						String usersComResponse = mapper.writeValueAsString(message);
						writer.write(usersComResponse);
						writer.flush();
						break;
				
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}
}
