package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.database.DbAccess;
import org.jdbi.v3.core.Jdbi;

public abstract class Repository {

    protected final DbAccess dbAccess;

    public Repository(DbAccess dbAccess) {
        this.dbAccess = dbAccess;
    }

    public Jdbi getJdbi() {
        return dbAccess.getJdbi();
    };
}
