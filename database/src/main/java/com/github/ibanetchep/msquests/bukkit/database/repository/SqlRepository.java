package com.github.ibanetchep.msquests.bukkit.database.repository;

import com.github.ibanetchep.msquests.bukkit.database.DbAccess;
import org.jdbi.v3.core.Jdbi;

public abstract class SqlRepository {

    protected final DbAccess dbAccess;

    public SqlRepository(DbAccess dbAccess) {
        this.dbAccess = dbAccess;
    }

    public Jdbi getJdbi() {
        return dbAccess.getJdbi();
    };
}
