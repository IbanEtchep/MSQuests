package com.github.ibanetchep.msquests.bukkit.document;

/** A {@code quest_group} Document that cannot be read; the message starts with the path of the faulty value. */
public class DocumentException extends RuntimeException {

    public DocumentException(String path, String message) {
        super(path.isEmpty() ? message : path + ": " + message);
    }
}
