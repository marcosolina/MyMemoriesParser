package com.marco.mymemoriesparser.domain.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.marco.dao.MarcoDao;

public class PicturesDao extends MarcoDao {

	private static final long serialVersionUID = 1L;
	private PicturesDto dto = null;

	public PicturesDao() {
		this.setSqlViewName("pictures");
		this.setSqlKeys(new String[] { "full_path" });
		this.setSqlFields(new String[] { "thumbnail", "lng", "file_name", "type", "folder_date", "date_time_original", "taken", "file_modified", "full_path", "create_date", "ignore_pic", "modify_date", "lat", "date_time_to_set" });
		this.dto = new PicturesDto();
	}

	public String getThumbnail() {
		return dto.getThumbnail();
	}

	public void setThumbnail(String thumbnail) {
		checkChanged(this.dto.getThumbnail(), thumbnail);
		this.dto.setThumbnail(thumbnail);
	}

	public String getLng() {
		return dto.getLng();
	}

	public void setLng(String lng) {
		checkChanged(this.dto.getLng(), lng);
		this.dto.setLng(lng);
	}

	public String getFile_name() {
		return dto.getFile_name();
	}

	public void setFile_name(String file_name) {
		checkChanged(this.dto.getFile_name(), file_name);
		this.dto.setFile_name(file_name);
	}

	public String getType() {
		return dto.getType();
	}

	public void setType(String type) {
		checkChanged(this.dto.getType(), type);
		this.dto.setType(type);
	}

	public LocalDate getFolder_date() {
		return dto.getFolder_date();
	}

	public void setFolder_date(LocalDate folder_date) {
		checkChanged(this.dto.getFolder_date(), folder_date);
		this.dto.setFolder_date(folder_date);
	}

	public LocalDateTime getDate_time_original() {
		return dto.getDate_time_original();
	}

	public void setDate_time_original(LocalDateTime date_time_original) {
		checkChanged(this.dto.getDate_time_original(), date_time_original);
		this.dto.setDate_time_original(date_time_original);
	}

	public LocalDate getTaken() {
		return dto.getTaken();
	}

	public void setTaken(LocalDate taken) {
		checkChanged(this.dto.getTaken(), taken);
		this.dto.setTaken(taken);
	}

	public LocalDateTime getFile_modified() {
		return dto.getFile_modified();
	}

	public void setFile_modified(LocalDateTime file_modified) {
		checkChanged(this.dto.getFile_modified(), file_modified);
		this.dto.setFile_modified(file_modified);
	}

	public String getFull_path() {
		return dto.getFull_path();
	}

	public void setFull_path(String full_path) {
		checkChanged(this.dto.getFull_path(), full_path);
		this.dto.setFull_path(full_path);
	}

	public LocalDateTime getCreate_date() {
		return dto.getCreate_date();
	}

	public void setCreate_date(LocalDateTime create_date) {
		checkChanged(this.dto.getCreate_date(), create_date);
		this.dto.setCreate_date(create_date);
	}

	public String getIgnore_pic() {
		return dto.getIgnore_pic();
	}

	public void setIgnore_pic(String ignore_pic) {
		checkChanged(this.dto.getIgnore_pic(), ignore_pic);
		this.dto.setIgnore_pic(ignore_pic);
	}

	public LocalDateTime getModify_date() {
		return dto.getModify_date();
	}

	public void setModify_date(LocalDateTime modify_date) {
		checkChanged(this.dto.getModify_date(), modify_date);
		this.dto.setModify_date(modify_date);
	}

	public String getLat() {
		return dto.getLat();
	}

	public void setLat(String lat) {
		checkChanged(this.dto.getLat(), lat);
		this.dto.setLat(lat);
	}

	public LocalDateTime getDate_time_to_set() {
		return dto.getDate_time_to_set();
	}

	public void setDate_time_to_set(LocalDateTime date_time_to_set) {
		checkChanged(this.dto.getDate_time_to_set(), date_time_to_set);
		this.dto.setDate_time_to_set(date_time_to_set);
	}

	public PicturesDto getDto() {
		return this.dto;
	}

	public void setDto(PicturesDto dto) {
		this.dto = dto;
	}

}
