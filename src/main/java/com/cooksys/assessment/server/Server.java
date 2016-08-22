package com.cooksys.assessment.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server implements Runnable {
	private Logger log = LoggerFactory.getLogger(Server.class);
	
	private int port;
	private ExecutorService executor;
	private ClientHandler handler;
	private static HashMap<Integer, String> users = new HashMap<>();
	
	public Server(int port, ExecutorService executor) {
		super();
		this.port = port;
		this.executor = executor;
	}

	public void run() {
		log.info("Local Server Started:  Awaiting Connections");
		ServerSocket ss;
		try {
			ss = new ServerSocket(this.port);
			while (true) {
				Socket socket = ss.accept();
				handler = new ClientHandler(socket);
				executor.execute(handler);
				
			}
		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}
	
	public static String getUsers() {
		return users.values().toString();
	}
	
	public static void addUser(int ID, String user) {
		users.put(ID, user);
	}
	
	public static void removeUser(int ID, String user) {
		users.remove(ID, user);
	}
	
	//  TODO Implement pm, echo, here?
	public void privateMessage(int ID, String msg) {
		
	}
	
	//  TODO implement broadcast and sysmsg here?
		public void broadcastMessage(String msg) {
		
	}

}
