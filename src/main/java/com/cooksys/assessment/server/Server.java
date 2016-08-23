package com.cooksys.assessment.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server implements Runnable {
	private Logger log = LoggerFactory.getLogger(Server.class);
	
	private int port;
	private ExecutorService executor;
	private ClientHandler handler;
	private static ArrayList<ClientHandler> clients = new ArrayList<>();
	
	public Server(int port, ExecutorService executor) {
		super();
		this.port = port;
		this.executor = executor;
	}

	public void run() {
		log.info("Server Started Successfully:  Awaiting Connections");
		ServerSocket ss;
		try {
			ss = new ServerSocket(this.port);
			while (true) {
				Socket socket = ss.accept();
				handler = new ClientHandler(socket);
				executor.execute(handler);
				clients.add(handler);
			}
		} catch (IOException e) {
			log.error("Server Run Error Noobsauce.", e);
		}
	}

	public static ArrayList<ClientHandler> getClients() {
		return clients;
	}

	public static ArrayList<String> getUsers() {
		ArrayList<String> users = new ArrayList<>();
		for (ClientHandler c : clients) {
			users.add(c.getUser());
		}
		return users;
	}
	
	public static void removeClient(ClientHandler c) {
		clients.remove(c);
	}
	
	public static void msgAll(String msg) throws IOException {
		for (ClientHandler c : clients) {
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(c.getSocket().getOutputStream()));
			writer.write(msg);
			writer.flush();
		}
	}
	
	public static void msgOne(String user, String msg) throws IOException {
		for (ClientHandler c : clients) {
			if ( c.getUser().equals(user)) {
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(c.getSocket().getOutputStream()));
				writer.write(msg);
				writer.flush();
			}
		}
	}
}
