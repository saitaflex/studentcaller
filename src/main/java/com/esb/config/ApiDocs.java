package com.esb.config;

/** Example error bodies reused in the Swagger documentation of the controllers. */
public final class ApiDocs {

	private ApiDocs() {
	}

	public static final String JSON = "application/json";

	public static final String CONFLICT = "{\"error\": \"Operation not allowed: this record is linked to other data\"}";
	public static final String BAD_PARAM = "{\"error\": \"Invalid value 'abc' for parameter 'id'\"}";

	public static final String DEPARTMENT_NOT_FOUND = "{\"error\": \"Department not found\"}";
	public static final String TEACHER_NOT_FOUND = "{\"error\": \"Teacher not found\"}";
	public static final String BUILDING_NOT_FOUND = "{\"error\": \"Building not found\"}";
	public static final String ROOM_NOT_FOUND = "{\"error\": \"Room not found\"}";
	public static final String RESERVATION_NOT_FOUND = "{\"error\": \"Reservation not found\"}";
	public static final String PAYMENT_NOT_FOUND = "{\"error\": \"Payment not found\"}";
}
