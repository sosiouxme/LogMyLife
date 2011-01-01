/*
    This file is part of LogMyLife, an application for logging events.
    Copyright (C) 2011 Luke Meyer

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program (see LICENSE file).
    If not, see http://www.gnu.org/licenses/
*/

package net.sosiouxme.logmylife;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
   Application-wide constants in one handy location.
 
   @author Luke Meyer, Copyright 2011
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
	public static final String db_TRACKER_USE_VALUE = "use_value";
	public static final String db_TRACKER_VALUE_TYPE = "value_type";
	public static final String db_TRACKER_VALUE_LABEL = "value_label";
	public static final String db_TRACKER_VALUE_LABEL_POS = "value_label_position";
	public static final String db_TRACKER_SKIP_NEXT_ALERT = "flag_skip_next_alert";
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
	public static final String db_ALERT_TABLE = "Alerts";
	public static final String db_ALERT_TRACKER = "tracker_id";
	public static final String db_ALERT_INTERVAL_MONTHS = "interval_months";
	public static final String db_ALERT_INTERVAL_WEEKS = "interval_weeks";
	public static final String db_ALERT_INTERVAL_DAYS = "interval_days";
	public static final String db_ALERT_INTERVAL_HOURS = "interval_hours";
	public static final String db_ALERT_INTERVAL_MINUTES = "interval_minutes";
	public static final String db_ALERT_INTERVAL_SECONDS = "interval_seconds";
	public static final String db_ALERT_NEXT_TIME = "next_time";
	public static final String db_ALERT_RINGTONE = "ringtone";
	public static final String db_ALERT_ENABLED = "is_enabled";
	public static final String db_ALERT_SKIP_NEXT = "skip_next";

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
