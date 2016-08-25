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

	public List<ClientHandler> getClients() {
		return clients;
	}

	public boolean checkIP(InetAddress ip) {
		int num = 0;
		synchronized (clients) {
			for (ClientHandler c : clients) {
				if (ip.equals(c.getUserIP())) {
					num++;
				}
			}
		}
		return num>= 3 ? true : false;
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

	public ArrayList<Integer> getUserIDs() {
		ArrayList<Integer> IDs = new ArrayList<>();
		synchronized (clients) {
			for (ClientHandler c : clients) {
				IDs.add(c.getID());
			}
		}
		return IDs;
	}

	public int getUserID(String user) {
		synchronized (clients) {
			for (ClientHandler c : clients) {
				if (c.getUser().equals(user)) {
					return c.getID();
				}
			}
		}
		return 0;
	}

	public void addClient(ClientHandler c) {
		clients.add(c);
	}

	public void removeClient(ClientHandler c) {
		clients.remove(c);
	}

	public void msgAll(String msg) throws IOException {
		synchronized (clients) {
			for (ClientHandler c : clients) {
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(c.getSocket().getOutputStream()));
				writer.write(msg);
				writer.flush();
			}
		}
	}

	public void msgOne(int ID, String msg) throws IOException {
		synchronized (clients) {
			for (ClientHandler c : clients) {
				if (c.getID() == ID) {
					PrintWriter writer = new PrintWriter(new OutputStreamWriter(c.getSocket().getOutputStream()));
					writer.write(msg);
					writer.flush();
				}
			}
		}
	}

	public void msgThis(Socket socket, String msg) throws IOException {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		writer.write(msg);
		writer.flush();

	}

	public String usersCom() {
		String users = "";
		for (ClientHandler c : clients) {
			users += "\n" + c.getUser();
		}
		return users;
	}

	public boolean validateUserName(String username) {
		if (username.matches(".*([^\\w]).*") // (".*([^a-zA-Z\\d\\s]).*")
				|| Character.isDigit(username.charAt(0))) {
			return false;
		} else {
			return true;
		}
	}

	public boolean duplicateUserName(String username) {
		for (String user : getUsers()) {
			if (username.equalsIgnoreCase(user)) {
				return true;
			}
		}
		return false;
	}

}
