package com.cooksys.assessment.server;


import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Handler {

	private List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

	public void addClient(ClientHandler c) {
		clients.add(c);
	}

	public void removeClient(ClientHandler c) {
		clients.remove(c);
	}

	public boolean checkIP(InetAddress ip) {
		synchronized (clients) {
			for (ClientHandler c : clients) {
				if (ip.equals(c.getUserIP())) {
					return true;
				}
			}
		}
		return false;
	}

	public ArrayList<String> getUsers() {
		ArrayList<String> users = new ArrayList<>();
		synchronized (clients) {
			for (ClientHandler c : clients) {
				users.add(c.getUser());
			}
		}
		return users;
	}

	public Socket getUserSocket(String user) {
		synchronized (clients) {
			for (ClientHandler c : clients) {
				if (c.getUser().equals(user)) {
					return c.getSocket();
				}
			}
		}
		return null;
	}

	public void msgAll(String msg) throws IOException {
		synchronized (clients) {
			for (ClientHandler c : clients) {
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(c.getSocket().getOutputStream()));
				writer.write(msg);
				writer.close();
			}
		}
	}

	public void msgOne(Socket socket, String msg) throws IOException {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		writer.write(msg);
		writer.close();

	}

	public String usersCom() {
		String users = "";
		for (ClientHandler c : clients) {
			users += "\n" + c.getUser();
		}
		return users;
	}

	public boolean validateUserName(String username) {   //  Validates that username does not contain spaces or special characters
		if (username.matches(".*([^\\w]).*") || Character.isDigit(username.charAt(0))) {
			return false;
		} else {
			return true;
		}
	}

	public boolean duplicateUserName(String username) {  //  Check if username is already in use on server
		for (String user : getUsers()) {
			if (username.equalsIgnoreCase(user)) {
				return true;
			}
		}
		return false;
	}

}
