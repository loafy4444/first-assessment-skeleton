package com.cooksys.assessment.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server implements Runnable {
	private Logger log = LoggerFactory.getLogger(Server.class);

	private int port;
	private ExecutorService executor;
	private ClientHandler chandler;
	private Handler handler;

	public Server(int port, ExecutorService executor) {
		super();
		this.port = port;
		this.executor = executor;
	}

	//  TODO Add IP Limits based on socket connections.
	//  TODO Catch user names here prior to submitting to clientHandler?
	//  TODO Catch socket disconnects here?

	public void run() {
		log.info("Server Started Successfully:  Awaiting Connections");
		handler = new Handler();
		ServerSocket ss;
		try {
			ss = new ServerSocket(this.port);
			while (true) {
				Socket socket = ss.accept();
				chandler = new ClientHandler(socket, handler);
				executor.execute(chandler);
				handler.addClient(chandler);
			}
		} catch (IOException e) {
			log.error("Server Run Error Noobsauce.", e);
		}
	}
}
