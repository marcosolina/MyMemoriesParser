package com.marco.mymemoriesparser.enums;

public enum ExecMode {
	INSERT, NEW_DB, TEST_DATE, SET_DATE_FROM_FILE_NAME, NOT_MANAGED;

	public static ExecMode getValueFromString(String value) {
		for (ExecMode mode : ExecMode.values()) {
			if (mode.toString().equals(value)) {
				return mode;
			}
		}
		return NOT_MANAGED;
	}
}
