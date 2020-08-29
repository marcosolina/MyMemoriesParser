package com.marco.mymemoriesparser.utils;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.marco.utils.DatabaseUtils;
import com.marco.utils.MarcoException;
import com.marco.utils.enums.DbType;

/**
 * This class provides a set of Utils functions
 * 
 * @author marco
 *
 */
public class MyMemoriesUtils {
	private static Logger logger = Logger.getLogger(MyMemoriesUtils.class);
	private static Properties appProperties = null;

	public static void setAppProperties(Properties appProperties) {
		MyMemoriesUtils.appProperties = appProperties;
	}

	/**
	 * It returns the value of the property
	 * 
	 * @param property
	 * @return
	 * @throws MarcoException
	 */
	public static String getProperty(String property) throws MarcoException {
		if (appProperties == null) {
			throw new MarcoException("Properties object not setted");
		}

		String prp = appProperties.getProperty(property);
		logger.trace(String.format("Loaded Property: %s Value: %s", property, prp));
		return prp;
	}

	public static void initialiseDbConnection() throws MarcoException {
		String dbhost = getProperty("com.marco.mymemroriesparser.db.host");
		int port = Integer.parseInt(getProperty("com.marco.mymemroriesparser.db.port"));
		String database = getProperty("com.marco.mymemroriesparser.db.schema");
		String user = getProperty("com.marco.mymemroriesparser.db.user");
		String password = getProperty("com.marco.mymemroriesparser.db.password");
		String dbType = getProperty("com.marco.mymemroriesparser.db.type");
		if("postgres".equals(dbType)) {
			DatabaseUtils.initialize(dbhost, port, database, user, password, DbType.POSTGRES);
		}else {
			DatabaseUtils.initialize(dbhost, port, database, user, password, DbType.MYSQL);			
		}
	}

}
