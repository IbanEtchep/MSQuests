package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.database.DbAccess;
import org.jdbi.v3.core.Jdbi;

import java.util.Map;

public abstract class Repository<T, U> {

    protected final DbAccess dbAccess;

    public Repository(DbAccess dbAccess) {
        this.dbAccess = dbAccess;
    }

    public Jdbi getJdbi() {
        return dbAccess.getJdbi();
    }

    public abstract U get(T id);

    public  abstract void add(U entity);

    public abstract void update(U entity);

    public abstract Map<T, U> getAll();
}
