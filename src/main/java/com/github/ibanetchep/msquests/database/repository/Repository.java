package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.database.DbAccess;

public abstract class Repository {

    protected final DbAccess dbAccess;

    public Repository(DbAccess dbAccess) {
        this.dbAccess = dbAccess;
    }

}
