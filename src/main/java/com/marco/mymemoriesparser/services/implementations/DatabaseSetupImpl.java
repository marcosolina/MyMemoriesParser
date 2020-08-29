package com.marco.mymemoriesparser.services.implementations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.marco.mymemoriesparser.services.interfaces.DatabaseSetupInt;
import com.marco.utils.DatabaseUtils;
import com.marco.utils.MarcoException;

public class DatabaseSetupImpl implements DatabaseSetupInt {
	private static Logger logger = Logger.getLogger(DatabaseSetupImpl.class);

	private String dbName = "mymemories";
	private String tableName = "pictures";
	private StringBuilder createStatement = new StringBuilder();
	{
		createStatement.append("CREATE TABLE ");
		createStatement.append(tableName);
		createStatement.append(" (");
		createStatement.append(" file_name            VARCHAR(100) NOT NULL, ");
		createStatement.append(" full_path            VARCHAR(250) NOT NULL, ");
		createStatement.append(" type                 TEXT, ");
		createStatement.append(" lat                  TEXT, ");
		createStatement.append(" lng                  TEXT, ");
		createStatement.append(" thumbnail            TEXT, ");
		createStatement.append(" taken                DATE NULL DEFAULT NULL, ");
		createStatement.append(" folder_date          DATE NULL DEFAULT NULL, ");
		createStatement.append(" create_date          TIMESTAMP NULL DEFAULT NULL, ");
		createStatement.append(" modify_date          TIMESTAMP NULL DEFAULT NULL, ");
		createStatement.append(" date_time_original   TIMESTAMP NULL DEFAULT NULL, ");
		createStatement.append(" date_time_to_set     TIMESTAMP NULL DEFAULT NULL, ");
		createStatement.append(" file_modified        TIMESTAMP NULL DEFAULT NULL, ");
		createStatement.append(" ignore_pic           CHARACTER(1) NOT NULL, ");
		createStatement.append(" PRIMARY KEY (full_path) ");
		createStatement.append(")");

	}

	@Override
	public void createDatabase() throws MarcoException {
		Connection cn = DatabaseUtils.getInstance().createDbConnectionNoDb();
		Statement st = null;
		try {
			st = cn.createStatement();
			st.execute("CREATE DATABASE " + dbName);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} finally {
			DatabaseUtils.closeSqlObjects(cn, st, null);
		}
	}

	@Override
	public void createTable() throws MarcoException {
		Connection cn = DatabaseUtils.getInstance().createDbConnection();
		Statement st = null;
		try {
			st = cn.createStatement();
			st.execute(createStatement.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} finally {
			DatabaseUtils.closeSqlObjects(cn, st, null);
		}
	}

	@Override
	public void dropTable() throws MarcoException {
		Connection cn = DatabaseUtils.getInstance().createDbConnection();
		Statement st = null;
		try {
			st = cn.createStatement();
			st.execute("DROP TABLE IF EXISTS " + tableName);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} finally {
			DatabaseUtils.closeSqlObjects(cn, st, null);
		}
	}

	@Override
	public void dropDatabase() throws MarcoException {
		Connection cn = DatabaseUtils.getInstance().createDbConnectionNoDb();
		Statement st = null;
		try {
			st = cn.createStatement();
			st.execute("DROP DATABASE IF EXISTS " + dbName);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} finally {
			DatabaseUtils.closeSqlObjects(cn, st, null);
		}

	}

}
