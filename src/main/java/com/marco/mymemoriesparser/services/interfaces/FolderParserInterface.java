package com.marco.mymemoriesparser.services.interfaces;

import com.marco.mymemoriesparser.domain.dao.PicturesDao;
import com.marco.utils.MarcoException;

public interface FolderParserInterface {

	public void insertIntoDb() throws MarcoException;
	public void testDate() throws MarcoException;
	public void setDateFromFileName() throws MarcoException;
	/**
	 * It returns the string thumbnail representation of
	 * the image.
	 * 
	 * @param file
	 * @return
	 * @throws MarcoException
	 */
	public String generateBase64Thumbnail(PicturesDao picture) throws MarcoException;
}
