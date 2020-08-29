package com.marco.mymemoriesparser.services.interfaces;

import com.marco.utils.MarcoException;

public interface DatabaseSetupInt {

	public void createDatabase() throws MarcoException;

	public void createTable() throws MarcoException;

	public void dropTable() throws MarcoException;

	public void dropDatabase() throws MarcoException;
}
