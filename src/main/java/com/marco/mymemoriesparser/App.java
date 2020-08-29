package com.marco.mymemoriesparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.marco.mymemoriesparser.enums.ExecMode;
import com.marco.mymemoriesparser.services.implementations.DatabaseSetupImpl;
import com.marco.mymemoriesparser.services.implementations.FolderParserImpl;
import com.marco.mymemoriesparser.services.interfaces.DatabaseSetupInt;
import com.marco.mymemoriesparser.services.interfaces.FolderParserInterface;
import com.marco.mymemoriesparser.utils.MyMemoriesUtils;
import com.marco.utils.MarcoException;

/**
 * This App parses my picture folder and extracts a different set of info and
 * stores them into the DB for later use
 * 
 * @author Marco
 *
 */
public class App {

	public static void main(String[] args) {
		Logger logger = configureAndGetLogger();
		retrieveAppProperties();

		LocalDateTime start = LocalDateTime.now();
		logger.info("START");

		try {
			/*
			 * Get the execution type
			 */
			String execModality = MyMemoriesUtils.getProperty("com.marco.mymemoriesparser.exec.modality");
			if (execModality == null) {
				logger.error("com.marco.mymemoriesparser.exec.modality not defined");
			}

			ExecMode executionMode = ExecMode.getValueFromString(execModality);
			/*
			 * Get an instance of the services
			 */
			FolderParserInterface service = new FolderParserImpl();
			DatabaseSetupInt dbService = null;

			switch (executionMode) {
			case INSERT:
			case NEW_DB:
				MyMemoriesUtils.initialiseDbConnection();
				dbService = new DatabaseSetupImpl();
				break;
			default:
				break;

			}

			/*
			 * Do the job
			 */
			switch (executionMode) {
			case INSERT:
				dbService.dropDatabase();
				dbService.createDatabase();
				dbService.createTable();
				service.insertIntoDb();
				break;
			case NEW_DB:
				dbService.dropDatabase();
				dbService.createDatabase();
				dbService.createTable();
				break;
			case TEST_DATE:
				service.testDate();
				break;
			case SET_DATE_FROM_FILE_NAME:
				service.setDateFromFileName();
				break;
			default:
				logger.error(String.format("Execution modality not supported: %s", execModality));
				break;
			}

		} catch (MarcoException e) {
			logger.error(e.getMessage());
		}
		LocalDateTime end = LocalDateTime.now();
		logger.info("END");
		logger.info(Duration.between(start, end));

	}

	/**
	 * It load the default application.properties and then it looks for an external
	 * file in the same folder of the Jar
	 * 
	 * @return
	 */
	private static Properties retrieveAppProperties() {
		InputStream isAppProps = null;
		Properties appProps = new Properties();

		Logger logger = Logger.getLogger(App.class);

		try {
			/*
			 * Get the folder where this Jar is saved
			 */
			String jarFolder = new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI())
					.getParent();

			File propertiesFile = new File(jarFolder + "/application.properties");

			/*
			 * Loading default application.properties
			 */
			isAppProps = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFile.getName());
			appProps.load(isAppProps);
			isAppProps.close();

			/*
			 * Try to override application.properties
			 */
			if (propertiesFile.exists()) {
				isAppProps = new FileInputStream(propertiesFile);
				appProps.load(isAppProps);
				logger.info("Using custom application.properties");
			} else {
				logger.info("Using default application.properties");
			}
			/*
			 * Load the new configuration into the system
			 */
			MyMemoriesUtils.setAppProperties(appProps);

		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		} finally {
			if (isAppProps != null) {
				try {
					isAppProps.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return appProps;
	}

	/**
	 * It configures log4j. It loads first the default log4j.properties file
	 * provided within this project, and then it looks for an external
	 * log4j.properties file to to overwrite the default properties
	 * 
	 * @return
	 */
	private static Logger configureAndGetLogger() {

		InputStream isLog4jProps = null;

		Logger logger = Logger.getLogger(App.class);
		try {
			/*
			 * Get the folder where this Jar is saved
			 */
			String jarFolder = new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI())
					.getParent();

			File log4JFile = new File(jarFolder + "/log4j.properties");

			Properties log4JProps = new Properties();

			/*
			 * Loading default log4j.properties
			 */
			isLog4jProps = Thread.currentThread().getContextClassLoader().getResourceAsStream(log4JFile.getName());
			log4JProps.load(isLog4jProps);
			isLog4jProps.close();

			/*
			 * Try to override log4j.properties
			 */
			if (log4JFile.exists()) {
				isLog4jProps = new FileInputStream(log4JFile);
				log4JProps.load(isLog4jProps);
				logger.info("Using custom log4j.properties");
			} else {
				logger.info("Using default log4j.properties");
			}

			/*
			 * Load the configuration into the system
			 */
			PropertyConfigurator.configure(log4JProps);

		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		} finally {
			if (isLog4jProps != null) {
				try {
					isLog4jProps.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return logger;
	}
}
