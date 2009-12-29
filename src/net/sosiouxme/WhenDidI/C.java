package net.sosiouxme.WhenDidI;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public interface C {

	public static final String db_ID = "_id";
	public static final String db_GROUP_TABLE = "Groups";
	public static final String db_GROUP_NAME = "name";
	public static final String db_TRACKER_TABLE = "Trackers";
	public static final String db_TRACKER_GROUP = "group_id";
	public static final String db_TRACKER_NAME = "name";
	public static final String db_TRACKER_BODY = "body";
	public static final String db_TRACKER_LAST_LOG = "last_log_time";
	public static final String db_LOG_TABLE = "TrackerLogs";
	public static final String db_LOG_TRACKER = "tracker_id";
	public static final String db_LOG_TIME = "log_time";
	public static final String db_LOG_BODY = "body";

	public static final String db_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final DateFormat dbDateFormat = new SimpleDateFormat(db_DATE_FORMAT);

}