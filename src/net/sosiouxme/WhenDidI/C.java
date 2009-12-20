package net.sosiouxme.WhenDidI;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public interface C {

	public static final String db_ID = "_id";
	public static final String db_LIST_TABLE = "List";
	public static final String db_LIST_TITLE = "title";
	public static final String db_ITEM_TABLE = "Item";
	public static final String db_ITEM_LIST = "list_id";
	public static final String db_ITEM_TITLE = "title";
	public static final String db_ITEM_BODY = "body";
	public static final String db_ITEM_LAST_LOG = "last_log_time";
	public static final String db_LOG_TABLE = "ItemLog";
	public static final String db_LOG_ITEM = "item_id";
	public static final String db_LOG_TIME = "log_time";
	public static final String db_LOG_BODY = "body";
	
	public static final String db_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final DateFormat dbDateFormat = new SimpleDateFormat(db_DATE_FORMAT);

}