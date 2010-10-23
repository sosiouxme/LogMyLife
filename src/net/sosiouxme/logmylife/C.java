package net.sosiouxme.logmylife;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
   Application-wide constants in one handy location.
 
   @author Luke Meyer, Copyright 2010
   See LICENSE file for this file's GPLv3 distribution license.
 */
public interface C {

	// Mostly constants for referring to table names and columns
	public static final String db_ID = "_id";
	//
	public static final String db_GROUP_TABLE = "Groups";
	public static final String db_GROUP_NAME = "name";
	//
	public static final String db_TRACKER_TABLE = "Trackers";
	public static final String db_TRACKER_GROUP = "group_id";
	public static final String db_TRACKER_NAME = "name";
	public static final String db_TRACKER_BODY = "body";
	public static final String db_TRACKER_LAST_LOG_ID = "last_log_id";
	public static final String db_TRACKER_SKIP_NEXT_ALARM = "flag_skip_next_alarm";
	//
	public static final String db_LOG_TABLE = "TrackerLogs";
	public static final String db_LOG_TRACKER = "tracker_id";
	public static final String db_LOG_TIME = "log_time";
	public static final String db_LOG_BODY = "body";
	public static final String db_LOG_VALUE_TYPE = "value_type";
	public static final String db_LOG_VALUE = "value";
	public static final String db_LOG_IS_BREAK = "is_break";
	//
	public static final String db_VALUE_TABLE = "ValueTypes";
	public static final String db_VALUE_NAME = "name";
	public static final String db_VALUE_TYPE = "type";
	//
	public static final String db_ALARM_TABLE = "Alarms";
	public static final String db_ALARM_TRACKER = "tracker_id";
	public static final String db_ALARM_INTERVAL_MONTHS = "interval_months";
	public static final String db_ALARM_INTERVAL_WEEKS = "interval_weeks";
	public static final String db_ALARM_INTERVAL_DAYS = "interval_days";
	public static final String db_ALARM_INTERVAL_HOURS = "interval_hours";
	public static final String db_ALARM_INTERVAL_MINUTES = "interval_minutes";
	public static final String db_ALARM_INTERVAL_SECONDS = "interval_seconds";
	public static final String db_ALARM_NEXT_TIME = "next_time";
	public static final String db_ALARM_RINGTONE = "ringtone";
	public static final String db_ALARM_ENABLED = "is_enabled";
	public static final String db_ALARM_SKIP_NEXT = "skip_next";

	public static final String db_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final DateFormat dbDateFormat = new SimpleDateFormat(db_DATE_FORMAT);

	// Only want to create a toast once - referred to  by the string that's shown.
	public static final Long TOAST_LOG_DELETED = new Long(R.string.log_entry_deleted);
	public static final Long TOAST_LOG_CREATED = new Long(R.string.new_log_entry);
	public static final Long TOAST_LOG_UPDATED = new Long(R.string.toast_log_entry_updated);
	public static final Long TOAST_TRACKER_DELETED = new Long(R.string.toast_tracker_deleted);
	public static final Long TOAST_TRACKER_CREATED = new Long(R.string.toast_tracker_created);
	public static final Long TOAST_TRACKER_UPDATED = new Long(R.string.toast_tracker_updated);
	public static final Long TOAST_GROUP_CREATED = new Long(R.string.toast_group_created);
	public static final Long TOAST_GROUP_UPDATED = new Long(R.string.toast_group_updated);
	public static final Long TOAST_GROUP_DELETED = new Long(R.string.toast_group_deleted);

}