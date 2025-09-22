package com.github.ibanetchep.msquests.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DbAccess {

	private static final Logger LOGGER = LoggerFactory.getLogger(DbAccess.class);

	private HikariDataSource dataSource;
	private Jdbi jdbi;
	private ExecutorService singleThreadExecutor;

	public void initPool(DbCredentials credentials) {
		HikariConfig config = new HikariConfig();

		config.setMaximumPoolSize(5);
		config.setMinimumIdle(1);
		config.setIdleTimeout(300000);
		config.setMaxLifetime(600000);
		config.setConnectionTimeout(10000);

		singleThreadExecutor = Executors.newSingleThreadExecutor();

		final String dbType = credentials.type();

		switch (dbType.toLowerCase()) {
			case "h2" -> {
				config.setDriverClassName("org.h2.Driver");
				File dbFile = new File(credentials.dataFolder(), "database");
				String jdbcUrl = "jdbc:h2:file:" + dbFile.getAbsolutePath();
				LOGGER.info("Testing H2 connection via DriverManager to {}", jdbcUrl);
				config.setJdbcUrl(jdbcUrl);
				LOGGER.info("Initializing H2 database connection pool to {}", jdbcUrl);
			}
			case "mysql" -> {
				config.setDriverClassName("com.mysql.cj.jdbc.Driver");
				config.setJdbcUrl(credentials.toURI());
				config.setUsername(credentials.user());
				config.setPassword(credentials.pass());

				config.addDataSourceProperty("cachePrepStmts", "true");
				config.addDataSourceProperty("prepStmtCacheSize", "250");
				config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
				config.addDataSourceProperty("useServerPrepStmts", "true");
				config.addDataSourceProperty("useLocalSessionState", "true");
				config.addDataSourceProperty("rewriteBatchedStatements", "true");
				config.addDataSourceProperty("cacheResultSetMetadata", "true");
				config.addDataSourceProperty("cacheServerConfiguration", "true");
				config.addDataSourceProperty("elideSetAutoCommits", "true");
				config.addDataSourceProperty("maintainTimeStats", "false");

				LOGGER.info("Initializing MySQL database connection to {}", credentials.toURI());
			}
			default -> throw new IllegalArgumentException("Base de données non supportée: " + dbType);
		}

		this.dataSource = new HikariDataSource(config);
		this.jdbi = Jdbi.create(dataSource);

		jdbi.registerRowMapper(new RecordRowMapper());

		if (testConnection()) {
			LOGGER.info("Database connection established successfully");
			LOGGER.info("Running database migrations...");
			MigrationManager migrationManager = new MigrationManager(jdbi, LOGGER);
			migrationManager.initialize();
			LOGGER.info("Database migrations completed");
		} else {
			throw new IllegalStateException("Database connection test failed");
		}
	}

	public void closePool() {
		if (dataSource != null && !dataSource.isClosed()) {
			LOGGER.info("Closing database connection pool");
			dataSource.close();
		}

		if (singleThreadExecutor != null) {
			singleThreadExecutor.shutdown();
		}
	}

	public DataSource getDataSource() {
		if (dataSource == null || dataSource.isClosed()) {
			throw new IllegalStateException("Database connection pool is not initialized");
		}
		return dataSource;
	}

	public Jdbi getJdbi() {
		if (jdbi == null) {
			throw new IllegalStateException("JDBI is not initialized");
		}
		return jdbi;
	}

	private boolean testConnection() {
		try (Connection conn = dataSource.getConnection()) {
			return conn.isValid(5);
		} catch (SQLException e) {
			LOGGER.error("Database connection test failed", e);
			return false;
		}
	}

	public ExecutorService getSingleThreadExecutor() {
		return singleThreadExecutor;
	}
}