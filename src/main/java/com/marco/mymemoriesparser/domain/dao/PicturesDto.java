package com.marco.mymemoriesparser.domain.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.marco.dao.MarcoDto;

public class PicturesDto implements MarcoDto {

	private static final long serialVersionUID = 1L;
	private String thumbnail = "";
	private String lng = "";
	private String file_name = "";
	private String type = "";
	private LocalDate folder_date = null;
	private LocalDateTime date_time_original = null;
	private LocalDate taken = null;
	private LocalDateTime file_modified = null;
	private String full_path = "";
	private LocalDateTime create_date = null;
	private String ignore_pic = "";
	private LocalDateTime modify_date = null;
	private String lat = "";
	private LocalDateTime date_time_to_set = null;

	public String getThumbnail() {
		return this.thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getLng() {
		return this.lng;
	}

	public void setLng(String lng) {
		this.lng = lng;
	}

	public String getFile_name() {
		return this.file_name;
	}

	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public LocalDate getFolder_date() {
		return this.folder_date;
	}

	public void setFolder_date(LocalDate folder_date) {
		this.folder_date = folder_date;
	}

	public LocalDateTime getDate_time_original() {
		return this.date_time_original;
	}

	public void setDate_time_original(LocalDateTime date_time_original) {
		this.date_time_original = date_time_original;
	}

	public LocalDate getTaken() {
		return this.taken;
	}

	public void setTaken(LocalDate taken) {
		this.taken = taken;
	}

	public LocalDateTime getFile_modified() {
		return this.file_modified;
	}

	public void setFile_modified(LocalDateTime file_modified) {
		this.file_modified = file_modified;
	}

	public String getFull_path() {
		return this.full_path;
	}

	public void setFull_path(String full_path) {
		this.full_path = full_path;
	}

	public LocalDateTime getCreate_date() {
		return this.create_date;
	}

	public void setCreate_date(LocalDateTime create_date) {
		this.create_date = create_date;
	}

	public String getIgnore_pic() {
		return this.ignore_pic;
	}

	public void setIgnore_pic(String ignore_pic) {
		this.ignore_pic = ignore_pic;
	}

	public LocalDateTime getModify_date() {
		return this.modify_date;
	}

	public void setModify_date(LocalDateTime modify_date) {
		this.modify_date = modify_date;
	}

	public String getLat() {
		return this.lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public LocalDateTime getDate_time_to_set() {
		return this.date_time_to_set;
	}

	public void setDate_time_to_set(LocalDateTime date_time_to_set) {
		this.date_time_to_set = date_time_to_set;
	}

}
