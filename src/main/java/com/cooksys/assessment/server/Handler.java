package com.cooksys.assessment.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Handler {
	
	private ArrayList<ClientHandler> clients = new ArrayList<>();
	
	public ArrayList<ClientHandler> getClients() {
		return clients;
	}

	public ArrayList<String> getUsers() {
		ArrayList<String> users = new ArrayList<>();
		for (ClientHandler c : clients) {
			users.add(c.getUser());
		}
		return users;
	}
	
	public ArrayList<Integer> getUserIDs() {
		ArrayList<Integer> IDs = new ArrayList<>();
		for (ClientHandler c : clients) {
			IDs.add(c.getID());
		}
		return IDs;
	}
	
	public int getUserID (String user) {
		for (ClientHandler c : clients) {
			if(c.getUser() .equals(user)) {
				return c.getID();
			}
		}
		return 0;
	}
	
	public void addClient(ClientHandler c){
		clients.add(c);
	}
	
	public void removeClient(ClientHandler c) {
		clients.remove(c);
	}
	
	public void msgAll(String msg) throws IOException {
		for (ClientHandler c : clients) {
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(c.getSocket().getOutputStream()));
			writer.write(msg);
			writer.flush();
		}
	}
	
	public void msgOne(int ID, String msg) throws IOException {
		for (ClientHandler c : clients) {
			if ( c.getID() == ID) {
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(c.getSocket().getOutputStream()));
				writer.write(msg);
				writer.flush();
			}
		}
	}
	
	public boolean validateUserName(String username) {
		if (username.matches(".*([^a-zA-Z, \\d]).*") 
//				|| username.startsWith("@") 
				|| Character.isDigit(username.charAt(0))  ) {
//				|| username.contains(" ") ) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean duplicateUserName(String username) {
		for (String user: getUsers()) {
			if (username.equalsIgnoreCase(user)){
				return true;
			} 
		}
		return false;
	}
	

}
