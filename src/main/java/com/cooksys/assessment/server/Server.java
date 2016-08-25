package com.cooksys.assessment.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Server implements Runnable {
	private Logger log = LoggerFactory.getLogger(Server.class);

	private int port;
	private ExecutorService executor;
	private ClientHandler chandler;
	private Handler handler;
	private Message msg;
	private ObjectMapper mapper;

	public Server(int port, ExecutorService executor) {
		super();
		this.port = port;
		this.executor = executor;
	}

	public void run() {
		log.info("Server Started Successfully:  Awaiting Connections");
		handler = new Handler();
		mapper = new ObjectMapper();
		
		ServerSocket ss;
		
		try {
			ss = new ServerSocket(this.port);
			while (true) {
				
				Socket socket = ss.accept();
				
				if (handler.checkIP(socket.getInetAddress())) {
					msg = new Message();
					msg.setCommand("duplicateip");
					msg.setContents(msg.getCommand());
					String response = mapper.writeValueAsString(msg);
					handler.msgThis(socket, response);
					socket.close();
				
				} else {
					
					chandler = new ClientHandler(socket, handler, mapper);
					executor.execute(chandler);
					handler.addClient(chandler);
				
				}
			
			}
		
		} catch (IOException e) {
			
			log.error("Server Run Error Noobsauce.", e);
		
		}
	
	}

}
