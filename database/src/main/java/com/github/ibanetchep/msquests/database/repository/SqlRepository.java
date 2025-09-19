package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.database.DbAccess;
import org.jdbi.v3.core.Jdbi;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public abstract class SqlRepository {

    protected final DbAccess dbAccess;

    public SqlRepository(DbAccess dbAccess) {
        this.dbAccess = dbAccess;
    }

    public Jdbi getJdbi() {
        return dbAccess.getJdbi();
    }

    protected <T> CompletableFuture<T> supplyAsync(Callable<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }

                throw new CompletionException(e);
            }
        }, dbAccess.getSingleThreadExecutor());
    }

    protected CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                throw (RuntimeException) e;
            }
        }, dbAccess.getSingleThreadExecutor());
    }
}
