package com.github.ibanetchep.msquests.database;

import java.io.File;

public record DbCredentials(String type, String host, String user, String pass, String dbName, int port,
                            File dataFolder) {

    public String toURI() {
		return "jdbc:mysql://" + host + ":" + port + "/" + dbName;
	}
	
}