package com.marco.mymemoriesparser.services.implementations;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.marco.exifdatamanager.ExifDataManager;
import com.marco.exifdatamanager.ExifDataManagerFactory;
import com.marco.exifdatamanager.enums.ExifTags;
import com.marco.mymemoriesparser.domain.dao.PicturesDao;
import com.marco.mymemoriesparser.services.interfaces.FolderParserInterface;
import com.marco.mymemoriesparser.utils.MyMemoriesUtils;
import com.marco.utils.DatabaseUtils;
import com.marco.utils.DateUtils;
import com.marco.utils.MarcoException;
import com.marco.utils.enums.DateFormats;

import net.coobird.thumbnailator.Thumbnails;

public class FolderParserImpl implements FolderParserInterface {
    private Logger logger = Logger.getLogger(FolderParserImpl.class);
    private int thumbnailHeight = Integer.parseInt(MyMemoriesUtils.getProperty("com.marco.mymemoriesparser.thumbnail.px.height"));
    private int thumbnailWidth = Integer.parseInt(MyMemoriesUtils.getProperty("com.marco.mymemoriesparser.thumbnail.px.width"));
    private static final String TRACE_ERROR_DATE_FORMATE = "Tried to parse date time: %s into %s";

    private ExifDataManager edm;

    public FolderParserImpl() throws MarcoException {

        String exifFullPath = MyMemoriesUtils.getProperty("com.marco.exiftool.full.path");
        if (exifFullPath == null) {
            logger.error("Property com.marco.exiftool.full.path not defined");
            System.exit(2);
            return;
        }
        
        edm = new ExifDataManagerFactory().setExifToolPath(Paths.get(exifFullPath)).getExifDataManager();
    }

    private String getFolderToScann() throws MarcoException {
        String folderToScan = MyMemoriesUtils.getProperty("com.marco.mymemoriesparser.folder.to.scan");
        if (folderToScan == null) {
            logger.error("Property com.marco.mymemoriesparser.folder.to.scan not defined");
            throw new MarcoException("Check the folder to scan definition");
        }
        return folderToScan;
    }

    @Override
    public void insertIntoDb() throws MarcoException {
        String folderToScan = getFolderToScann();

        logger.info("Try to scan the folder: " + folderToScan);
        File foldToScan = new File(folderToScan);
        if (!foldToScan.exists() || !foldToScan.isDirectory()) {
            logger.error(String.format("Not able to find the folder: %s", folderToScan));
        }

        final Connection cn = DatabaseUtils.getInstance().createDbConnection();

        try (Stream<Path> walk = Files.walk(Paths.get(folderToScan))) {

            List<File> result = walk.parallel().filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());

            List<ExifTags> tagsToRead = new ArrayList<>();
            tagsToRead.add(ExifTags.DATE_DATE_TIME_ORIGINAL);
            tagsToRead.add(ExifTags.MIME_TYPE);
            tagsToRead.add(ExifTags.GPS_LATITUDE);
            tagsToRead.add(ExifTags.GPS_LONGITUTE);
            tagsToRead.add(ExifTags.DATE_FILE_MODIFIED);
            tagsToRead.add(ExifTags.DATE_MODIFY_DATE);
            tagsToRead.add(ExifTags.DATE_CREATE_DATE);

            AtomicInteger count = new AtomicInteger(result.size());

            logger.info(String.format("Files to process: %s", count));

            result.parallelStream().forEach(f -> {
                try {
                    PicturesDao dao = new PicturesDao();
                    dao.setFile_name("PROCESSING");
                    dao.setFull_path(f.getAbsolutePath());
                    dao.updateInsert(cn);

                    try {
                        fillTheData(dao, f, tagsToRead);
                        dao.updateInsert(cn);
                    } catch (Exception ee) {
                        logger.error("Error with the picture: " + f.getName());
                        dao.setFile_name("ERROR");
                        dao.setThumbnail(ee.getMessage());
                        dao.updateInsert(cn);
                    }
                } catch (MarcoException e) {
                    throw new RuntimeException(e);
                }

                if(logger.isTraceEnabled()) {
                    logger.trace(String.format("Files remaining: %s", count.decrementAndGet()));
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } finally {
            DatabaseUtils.closeSqlObjects(cn, null, null);
        }

    }

    private void fillTheData(PicturesDao dao, File file, List<ExifTags> tagsToRead) throws MarcoException {
        Map<ExifTags, String> tags = edm.readExifData(tagsToRead, file);

        boolean noException = false;
        LocalDateTime dateOriginal = tryToParseStringIntoDateTime(tags.get(ExifTags.DATE_DATE_TIME_ORIGINAL), noException);
        if (dateOriginal == null) {
            dateOriginal = calculateDateTimeFromFileName(file);
        }
        noException = dateOriginal != null;
        dao.setFolder_date(tryToCalculateFolderDate(file));
        dao.setFile_modified(tryToParseStringIntoDateTime(tags.get(ExifTags.DATE_FILE_MODIFIED), noException));
        dao.setCreate_date(tryToParseStringIntoDateTime(tags.get(ExifTags.DATE_CREATE_DATE), noException));
        dao.setModify_date(tryToParseStringIntoDateTime(tags.get(ExifTags.DATE_MODIFY_DATE), noException));

        dao.setFile_name(file.getName());
        dao.setLng(tags.get(ExifTags.GPS_LONGITUTE));
        dao.setLat(tags.get(ExifTags.GPS_LATITUDE));
        dao.setType(tags.get(ExifTags.MIME_TYPE));
        dao.setDate_time_original(dateOriginal);
        dao.setTaken(dateOriginal == null ? dao.getFolder_date() : dateOriginal.toLocalDate());
        dao.setIgnore_pic("");

        /*
         * Try to generate the image thumbnail
         */
        dao.setThumbnail(generateBase64Thumbnail(dao));
    }

    private LocalDate tryToCalculateFolderDate(File file) {
        String folderName = file.getParent();
        folderName = folderName.substring(folderName.lastIndexOf(File.separator) + 1);
        try {
            return DateUtils.fromStringToLocalDate(folderName, DateFormats.FOLDER_NAME);
        } catch (Exception e) {
            logger.warn("Not able to parse folder date: " + folderName);
        }
        return null;
    }

    private LocalDateTime calculateDateTimeFromFileName(File file) {
        String name = file.getName().substring(0, file.getName().lastIndexOf('.'));

        /*
         * Try to clean a little the name
         */
        name = name.replace("_PN", "");
        name = name.replace("_BF", "");
        name = name.replace("V_", "");
        name = name.replace("P_", "");
        name = name.replace("VID_", "");
        name = name.replace("PANO_", "");
        name = name.replace("_Richtone(HDR)", "");
        for (int i = 0; i < 1000; i++) {
            String formatted = String.format("_%05d", i);
            name = name.replaceAll(formatted, "");
        }

        /*
         * Formats to try to parse
         */
        List<DateFormats> formatsToAttemps = new ArrayList<>();
        formatsToAttemps.add(DateFormats.FILE_NAME);
        formatsToAttemps.add(DateFormats.FILE_NAME_SHORT);
        formatsToAttemps.add(DateFormats.FILE_NAME_WITH_SPACE);
        formatsToAttemps.add(DateFormats.FILE_NAME_COMPACT);
        formatsToAttemps.add(DateFormats.FILE_NAME_HHMM);
        formatsToAttemps.add(DateFormats.FILE_NAME_JUST_DATE);

        
        int dateLenght = 15;
        
        /*
         * Look this kind of formats yyyyMMddXhhmmss
         */
        if (name.length() > dateLenght) {
            name = name.substring(0, dateLenght);
        }

        for (DateFormats dateFormats : formatsToAttemps) {
            try {
                return DateUtils.fromStringToLocalDateTime(name, dateFormats);
            } catch (Exception e) {
            	logger.trace(String.format(TRACE_ERROR_DATE_FORMATE, name, dateFormats.getFormat()));
            }
        }

        /*
         * Look this kind of formats yyyyMMdd_hhmm or yyMMdd_hhmmss
         */
        dateLenght = 13;
        if (name.length() > dateLenght) {
            name = name.substring(0, dateLenght);
        }
        for (DateFormats dateFormats : formatsToAttemps) {
            try {
                return DateUtils.fromStringToLocalDateTime(name, dateFormats);
            } catch (Exception e) {
            	logger.error(String.format(TRACE_ERROR_DATE_FORMATE, name, dateFormats.getFormat()));
            }
        }

        /*
         * Look this kind of formats yyyyMMddHHmm
         */
        dateLenght = 12;
        if (name.length() > dateLenght) {
            name = name.substring(0, dateLenght);
        }
        for (DateFormats dateFormats : formatsToAttemps) {
            try {
                return DateUtils.fromStringToLocalDateTime(name, dateFormats);
            } catch (Exception e) {
            	logger.trace(String.format(TRACE_ERROR_DATE_FORMATE, name, dateFormats.getFormat()));
            }
        }
        
        /*
         * Look this kind of formats: yyyyMMddxxxxx
         */
        dateLenght = 8;
        if (name.length() > dateLenght) {
            name = name.substring(0, dateLenght);
        }
        for (DateFormats dateFormats : formatsToAttemps) {
            try {
                return DateUtils.fromStringToLocalDateTime(name, dateFormats);
            } catch (Exception e) {
            	logger.trace(String.format(TRACE_ERROR_DATE_FORMATE, name, dateFormats.getFormat()));
            }
        }

        logger.warn("Not able to parse file name date: " + file.getName());
        return null;
    }

    private LocalDateTime tryToParseStringIntoDateTime(String value, boolean noException) throws MarcoException {
        if (value == null) {
            return null;
        }
        value = value.trim();

        if (value.equals("0000:00:00 00:00:00") || value.isEmpty()) {
            return null;
        }
        for (DateFormats format : DateFormats.values()) {
            try {
                switch (format) {
                case EXIF_DATE_TIME:
                case DB_TIME_STAMP:
                    return DateUtils.fromStringToLocalDateTime(value, format);
                case EXIF_DATE_TIME_WITH_ZONE:
                    ZonedDateTime zdt = DateUtils.fromStringToZonedDateTime(value, format);
                    return zdt.toLocalDateTime();
                case EXIF_DATE_TIME_RUBBISH_01:
                case EXIF_DATE_TIME_RUBBISH_02:
                case EXIF_DATE_TIME_RUBBISH_03:
                case EXIF_DATE_TIME_RUBBISH_04:
                    if (value.length() > 19) {
                        value = value.substring(0, 19);
                    }
                    value = value.replaceAll("&|#|'", "*");
                    return DateUtils.fromStringToLocalDateTime(value, format);
                case EXIF_DATE_TIME_RUBBISH_05:
                    if (value.length() > 15) {
                        value = value.substring(0, 15);
                    }
                    value = value.replaceAll("&|#|'", "*");
                    return DateUtils.fromStringToLocalDateTime(value, format);
                case EXIF_DATE_TIME_RUBBISH_06:
                case EXIF_DATE_TIME_RUBBISH_07:
                case EXIF_DATE_TIME_RUBBISH_08:
                case EXIF_DATE_TIME_RUBBISH_09:
                case EXIF_DATE_TIME_RUBBISH_10:
                case EXIF_DATE_TIME_RUBBISH_11:
                case EXIF_DATE_TIME_RUBBISH_14:
                case EXIF_DATE_TIME_RUBBISH_15:
                    LocalDateTime ldt = DateUtils.fromStringToLocalDateTime(value, format);
                    int minutes = ldt.getMinute() * 10;
                    return ldt.minusMinutes(ldt.getMinute()).plusMinutes(minutes);
                case EXIF_DATE_TIME_RUBBISH_12:
                case EXIF_DATE_TIME_RUBBISH_13:
                    return DateUtils.fromStringToLocalDateTime(value, format);
                default:
                    break;
                }
            } catch (Exception e) {
                logger.trace(e.getMessage());
            }
        }
        if (noException) {
            return null;
        }
        throw new MarcoException("Not able to parse date time: " + value);
    }

    @Override
    public void testDate() throws MarcoException {
        String folderToScan = getFolderToScann();

        logger.info("Try to scan the folder: " + folderToScan);
        File foldToScan = new File(folderToScan);
        if (!foldToScan.exists() || !foldToScan.isDirectory()) {
            logger.error(String.format("Not able to find the folder: %s", folderToScan));
        }

        try (Stream<Path> walk = Files.walk(Paths.get(folderToScan))) {

            List<File> result = walk.filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());

            List<ExifTags> tagsToRead = new ArrayList<>();
            tagsToRead.add(ExifTags.DATE_DATE_TIME_ORIGINAL);
            tagsToRead.add(ExifTags.DATE_FILE_MODIFIED);
            tagsToRead.add(ExifTags.DATE_MODIFY_DATE);
            tagsToRead.add(ExifTags.DATE_CREATE_DATE);

            while (!result.isEmpty()) {
                File file = result.remove(0);
                Map<ExifTags, String> tags = edm.readExifData(tagsToRead, file);

                try {
                    tryToParseStringIntoDateTime(tags.get(ExifTags.DATE_DATE_TIME_ORIGINAL), false);
                    tryToParseStringIntoDateTime(tags.get(ExifTags.DATE_FILE_MODIFIED), false);
                    tryToParseStringIntoDateTime(tags.get(ExifTags.DATE_CREATE_DATE), false);
                    tryToParseStringIntoDateTime(tags.get(ExifTags.DATE_MODIFY_DATE), false);
                } catch (MarcoException e) {
                    logger.error(file.getName() + " " + e.getMessage());
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }

    }

    @Override
    public String generateBase64Thumbnail(PicturesDao pictureDao) throws MarcoException {
        
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            File file = new File(pictureDao.getFull_path());
            if (!file.exists()) {
                throw new MarcoException("The file does not exist enymore...");
            }

            if (pictureDao.getType().startsWith("image")) {
                Thumbnails.of(file).size(thumbnailWidth, thumbnailHeight).toOutputStream(bos);
                return Base64.getEncoder().encodeToString(bos.toByteArray());
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new MarcoException(e);
        } 
        return "";
    }

	@Override
	public void setDateFromFileName() throws MarcoException {
		try (Stream<Path> walk = Files.walk(Paths.get(getFolderToScann()))) {
			List<File> result = walk.filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());
			result.parallelStream().forEach(f -> {
				LocalDateTime ldt = calculateDateTimeFromFileName(f);
				if(ldt != null) {
					String date = DateUtils.fromLocalDateTimeToString(ldt, DateFormats.EXIF_DATE_TIME);
					
					EnumMap<ExifTags, String> mapOfTags = new EnumMap<>(ExifTags.class);
					mapOfTags.put(ExifTags.DATE_MODIFY_DATE, date);
					mapOfTags.put(ExifTags.DATE_FILE_MODIFIED, date);
					mapOfTags.put(ExifTags.DATE_DATE_TIME_ORIGINAL, date);
					mapOfTags.put(ExifTags.DATE_CREATE_DATE, date);
					try {
						edm.updateExifDataInFile(f, mapOfTags);
					} catch (MarcoException e) {
						e.printStackTrace();
					}
				}
				
			});
		} catch (IOException e) {
			throw new MarcoException(e);
		}
		
	}

}
