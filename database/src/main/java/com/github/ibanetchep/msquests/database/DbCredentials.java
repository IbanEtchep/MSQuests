package com.github.ibanetchep.msquests.database;

public record DbCredentials(String host, String user, String pass, String dbName, int port) {

	public String toURI() {
		return "jdbc:mysql://" + host + ":" + port + "/" + dbName;
	}
	
}

