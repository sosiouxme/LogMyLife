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

/**
 * 
 */
package net.sosiouxme.logmylife.domain;

//import java.sql.Date;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sosiouxme.logmylife.C;
import net.sosiouxme.logmylife.R;
import net.sosiouxme.logmylife.domain.dto.Alert;
import net.sosiouxme.logmylife.domain.dto.LogEntry;
import net.sosiouxme.logmylife.domain.dto.Tracker;
import net.sosiouxme.logmylife.receiver.AlertReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
  
  DB Adapter for the application; where all the low-level interaction with the DB
  occurs, including creation/upgrade of the DB and storage of domain objects.

  @author Luke Meyer, Copyright 2010
  See LICENSE file for this file's GPLv3 distribution license.
  
 */

public class DbAdapter implements C {

	/**	 name of the file used for the sqlite DB */
	private static final String DATABASE_NAME = "LogMyLife.db";
	/** version of the DB (to determine if migration is needed) */
	private static final int DATABASE_VERSION = 3;
	/** Logging tag for this class */
    private static final String TAG = "LML.DBAdapter";

    /** Helper that handles creating/migrating/opening the DB */
    private DbAdmin mDbHelper;
    /** Actual database handle */
    private SQLiteDatabase mDb;
    /** Context object from the application **/
    private final Context mContext;
    /** String resources from the application context */ 
	private final Resources resources;

	/**
	 * Creates the database adapter that mediates all DB access
	 * 
	 * @param ctx The application context in which this adapter is created
	 */
    public DbAdapter(Context ctx) {
        mContext = ctx;
        resources = ctx.getResources();
        open(ctx);
	}
    
    /**
     * Opens a database handle, creating if needed. If it cannot be created, throw exception.
     * @param ctx 
     * 
     * @return this
     * @throws SQLException if the database could be neither opened or created
     */
    
    public DbAdapter open(Context ctx) throws SQLException {
    	if(mDbHelper != null) return this;
        mDbHelper = new DbAdmin(ctx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    /**
     * Close any open database handles.
     */
    public void close() {
    	if(mDbHelper == null) return;
        mDbHelper.close();
        mDb = null;
        mDbHelper = null;
    }
    
    public String getDbPath() {
    	return mDb.getPath();
    }
    
    /**
     *  Return a string defined in resource file
     * @param id The R.id for the string
     * @return The corresponding string requested
     */
	private String str(int id) {
		return (String) resources.getText(id);
	}
    
    /*
     * *********************************************************************
     * 
     * Handle group table
     * 
     */
    
    public long createGroup(String title) {
        // @return listId or -1 if failed
    	Log.d(TAG, "creating group " + title);
        ContentValues initialValues = new ContentValues();
        initialValues.put(db_GROUP_NAME, title);

        return mDb.insert(db_GROUP_TABLE, null, initialValues);
    }

    /**
     * @param groupId rowId of group to return
     * @return Cursor positioned at single group matching id if any
     */
    public Cursor fetchGroup(long groupId) {
    	Log.d(TAG, "fetching group " + groupId);
		Cursor mCursor = mDb.query(true, 
				db_GROUP_TABLE, 
				new String[] { db_ID, db_GROUP_NAME }, 
				db_ID + "=" + groupId, 
				null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor fetchGroups() {
        // @return Cursor over all groups
    	Log.d(TAG, "fetching all groups");
    	return mDb.query(
    			db_GROUP_TABLE, 
    			new String[] {db_ID, db_GROUP_NAME}, 
    			null, null, null, null, null);
    }
    
    public boolean updateGroup(long groupId, String title) {
        // @return true if the note was successfully updated, false otherwise
    	Log.d(TAG, "updating group " + groupId);
        ContentValues args = new ContentValues();
        args.put(db_GROUP_NAME, title);
        return mDb.update(db_GROUP_TABLE, args, db_ID + "=" + groupId, null) > 0;
    }
    
    public boolean deleteGroup(long groupId) {
        // @return true if deleted, false otherwise
    	Log.d(TAG, "deleting group " + groupId);
       	String[] whereArgs = new String[] { Long.toString(groupId)};
		String whereClause = "tracker_id in (select _id from Trackers where group_id = ?)";
		mDb.delete(db_LOG_TABLE, whereClause, whereArgs);
        mDb.delete(db_ALERT_TABLE, whereClause, whereArgs);
        mDb.delete(db_TRACKER_TABLE, db_TRACKER_GROUP + "=" + groupId, null);
        updateAlertSchedule();
        return mDb.delete(db_GROUP_TABLE, db_ID + "=" + groupId, null) > 0;
    }

    
    /*
     * ******************************************************************************8
     * Handle Trackers table
     */

	private static final String[] TRACKER_COLUMNS = new String[] { db_ID,
			db_TRACKER_NAME, db_TRACKER_BODY, db_TRACKER_SKIP_NEXT_ALERT,
			db_TRACKER_GROUP, db_TRACKER_LAST_LOG_ID, db_TRACKER_USE_VALUE,
			db_TRACKER_VALUE_TYPE, db_TRACKER_VALUE_LABEL,
			db_TRACKER_VALUE_LABEL_POS };

	public long createTracker(Tracker tr) {
        // @return rowId or -1 if failed
		Log.d(TAG, "creating item " + tr.groupId + ":" + tr.name);
		return mDb.insert(db_TRACKER_TABLE, null, tr.getChanged());
    }

    public Cursor fetchTrackerCursor(long trackerId) {
        // @return Cursor positioned to matching note, if found
    	Log.d(TAG, "fetching tracker " + trackerId);
				
		Cursor mCursor = mDb.query(
				db_TRACKER_TABLE, 
				TRACKER_COLUMNS,
				db_ID + "=" + trackerId,
				null, null,	null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    

	public Tracker fetchTracker(long trackerId) {
		Cursor c = null;
		Tracker t = null;
		try {
			c = fetchTrackerCursor(trackerId);
			if (c.getCount() != 1)
				return null;

			c.moveToFirst();
			t = new Tracker(trackerId);
			t.name = c.getString(c.getColumnIndex(C.db_TRACKER_NAME));
			t.body = c.getString(c.getColumnIndex(C.db_TRACKER_BODY));
			t.groupId = c.getLong(c.getColumnIndex(C.db_TRACKER_GROUP));
			t.lastLogId = c.getLong(c.getColumnIndex(C.db_TRACKER_LAST_LOG_ID));
			t.skipNextAlert = c.getInt(c.getColumnIndex(C.db_TRACKER_SKIP_NEXT_ALERT)) > 0;
			t.logUseValue = c.getInt(c.getColumnIndex(C.db_TRACKER_USE_VALUE)) > 0;
			t.logValueType = c.getInt(c.getColumnIndex(C.db_TRACKER_VALUE_TYPE));
			t.logValueLabel = c.getString(c.getColumnIndex(C.db_TRACKER_VALUE_LABEL));
			t.logValueLabelPos = c.getInt(c.getColumnIndex(C.db_TRACKER_VALUE_LABEL_POS));
			if(t.lastLogId > 0)
				t.lastLog = fetchLog(t.lastLogId);
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return t;
	}

	public Cursor fetchTrackers(long groupId, String filter) {
        // @return Cursor over all trackers in a group
		Log.d(TAG, "fetching trackers for group " + groupId);
		if(filter == null)
			return mDb.rawQuery(str(R.string.db_select_trackers),
					new String[] {"" + groupId});
		else
			return mDb.rawQuery(str(R.string.db_select_trackers_filter), 
					new String[] {"" + groupId, "%" + filter + "%"});
	}

	public void updateTracker(Tracker tracker) {
		ContentValues args = tracker.getChanged();
		if(args.size() > 0) {
			mDb.update(db_TRACKER_TABLE, args, db_ID + "=" + tracker.id, null);
			tracker.clearChanged();
		}
	}
	
    public void requeryTracker(Tracker t) {
    	Tracker newTracker = fetchTracker(t.id);
    	if(newTracker != null) {
    		t.copyFrom(newTracker);
        	t.clearChanged();
    	} else {
    		throw new EntryMissingException();
    	}
    }

    private void updateTrackerLastLog(long trackerId) {
    	mDb.execSQL(str(R.string.db_update_tracker_last_log), new Long[] {trackerId});
    	Tracker t = fetchTracker(trackerId);
		List<Alert> alerts = fetchAlertList(trackerId);
		for(Alert a : alerts) {
			a.setNextTimeFromLast(t.getLastLog());
			updateAlert(a);
		}
    }
    
    public boolean deleteTracker(long trackerId) {
        // @return true if deleted, false otherwise
    	Log.d(TAG, "deleting tracker " + trackerId);
    	mDb.delete(db_LOG_TABLE, db_LOG_TRACKER + " = " + trackerId, null);
    	deleteAlerts(trackerId);
        return mDb.delete(db_TRACKER_TABLE, db_ID + "=" + trackerId, null) > 0;
    }

    
    /*
     * ******************************************************************************
     * Handle TrackerLogs table
     */
    
	private static final String[] LOG_COLUMNS = { db_ID, db_LOG_TRACKER,
			db_LOG_TIME, db_LOG_BODY, db_LOG_VALUE_TYPE, db_LOG_VALUE,
			db_LOG_IS_BREAK };

	public long createLog(Tracker tracker) {
		return createLog(LogEntry.newLogFor(tracker));
	}
    
    public long createLog(LogEntry log) {
    	Log.d(TAG, "creating log for tracker " + log.trackerId);
    	if(log.logDate == null) 
    		log.setLogDate(new Date());
    	long logId = mDb.insert(db_LOG_TABLE, null, log.getChanged());
    	updateTrackerLastLog(log.trackerId);
    	return logId;
    }

	public LogEntry fetchLog(long logId) {
		Cursor c = null;
		LogEntry le = null;
		try {
			c = mDb.query(true, db_LOG_TABLE,
            		LOG_COLUMNS,
					db_ID + "=" + logId, null,
					null, null, null, null);
			if (c.getCount() != 1)
				return null;
            c.moveToFirst();
			le = new LogEntry(logId);
			le.body = c.getString(c.getColumnIndex(C.db_LOG_BODY));
			le.trackerId = c.getLong(c.getColumnIndex(C.db_LOG_TRACKER));
			String date = c.getString(c.getColumnIndex(C.db_LOG_TIME));
			if (date != null)
				le.logDate = dbDateFormat.parse(date);
			le.isBreak = c.getInt(c.getColumnIndex(C.db_LOG_IS_BREAK)) > 0;
			if (c.isNull(c.getColumnIndex(C.db_LOG_VALUE)))
				le.value = null;
			else
				le.value = c.getString(c.getColumnIndex(C.db_LOG_VALUE));
			le.valueType = c.getLong(c.getColumnIndex(C.db_LOG_VALUE_TYPE));
		} catch (ParseException e) {
			throw new RuntimeException("fetchLog couldn't parse date for " + logId, e);
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return le;
	}

	private static final String[] LOG_CURSOR_COLUMNS = { db_ID, db_LOG_TIME,
			db_LOG_BODY, db_LOG_VALUE, db_LOG_VALUE_TYPE };

	public Cursor fetchLogs(long trackerId) {
        // @return Cursor positioned to matching log, if found
    	Log.d(TAG, "fetching logs for item " + trackerId);
        Cursor c = mDb.query(true, db_LOG_TABLE,
				LOG_CURSOR_COLUMNS,
				db_LOG_TRACKER + "=" + trackerId, null,
		        null, null, db_LOG_TIME + " DESC", null);
        if (c != null)
            c.moveToFirst();
        return c;

    }
    
	public void updateLog(LogEntry log) {
		ContentValues args = log.getChanged();
		if(args.size() > 0) {
	        mDb.update(db_LOG_TABLE, args, db_ID + "=" + log.id, null);
	    	updateTrackerLastLog(log.trackerId);
	        log.clearChanged();
		}
	}
    
    public boolean deleteLog(long logId, long trackerId) {
        // @return true if deleted, false otherwise
    	Log.d(TAG, "deleting log " + logId);
        boolean status = mDb.delete(db_LOG_TABLE, db_ID + "=" + logId, null) > 0;
    	updateTrackerLastLog(trackerId);
    	return status;
    }

    /*
     * ******************************************************************************
     * Handle Alerts table
     */
    
	    /**
	     * Create a DB alert entry from an Alert DTO
	     * 
	     * @param newAlert the DTO to create in the DB
	     * @return rowId of the alert created
	     */
    public long createAlert(Alert newAlert) {
    	Log.d(TAG, "creating Alert for tracker " + newAlert.trackerId);
       	long alertId = mDb.insert(db_ALERT_TABLE, null, newAlert.getChanged());
       	updateAlertSchedule();
       	return alertId;
    }

	    /** All columns from the Alerts table (for queries) */
	private static final String[] ALERT_COLUMNS = new String[] { 
			db_ID, db_ALERT_TRACKER,
			db_ALERT_INTERVAL_MONTHS, db_ALERT_INTERVAL_WEEKS, 
			db_ALERT_INTERVAL_DAYS, db_ALERT_INTERVAL_HOURS, 
			db_ALERT_INTERVAL_MINUTES, db_ALERT_INTERVAL_SECONDS, 
			db_ALERT_NEXT_TIME, db_ALERT_ENABLED, db_ALERT_SKIP_NEXT,
			db_ALERT_RINGTONE };
		
		/**
		 * Fetches an Alert DTO from the DB with the given rowId
		 * 
		 * @param alertId The rowId of the alert
		 * @return The corresponding Alert object, or null if not found
		 */
	public Alert fetchAlert(long alertId) {
		return fetchAlert(db_ID + "=" + alertId, null, null, null, null, null);
	}
	
	public Alert fetchAlert(String where, String[] where_params, String group_by, String having, String order_by, String limit) {
		Cursor c = null;
		Alert alert = null;
		try {
			c = mDb.query(true, db_ALERT_TABLE,
            		ALERT_COLUMNS,
					where, where_params,
					group_by, having, order_by, limit);
			if (c.getCount() != 1)
				return null;
            c.moveToFirst();
            alert = getAlertFromCursor(c);
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return alert;
	}
	
    public List<Alert> fetchAlertList(long trackerId) {
        // @return ArrayList of matching alerts, if found
    	// WARN: this has never been tested
    	Log.d(TAG, "fetching alerts for tracker " + trackerId);
        List<Alert> alerts = new ArrayList<Alert>();
        Cursor c = fetchAlertsCursor(trackerId);
        if (c != null) {
	        c.moveToFirst();
	        while(!c.isAfterLast()) {
	        	alerts.add(getAlertFromCursor(c));
	        	c.moveToNext();
	        }
	        c.close();
        }
        return alerts;
    }
	
		/**
		 * Turns a single cursor row from the DB into an Alert object
		 * @param c The cursor
		 * @return The filled out Alert oject
		 */
	private Alert getAlertFromCursor(Cursor c) {
		Alert a = new Alert(c.getLong(0));
		a.trackerId = c.getLong(c.getColumnIndex(C.db_ALERT_TRACKER));
		a.isEnabled = c.getInt(c.getColumnIndex(C.db_ALERT_ENABLED)) > 0;
		a.skipNext = c.getInt(c.getColumnIndex(C.db_ALERT_SKIP_NEXT)) > 0;
		a.ivalMonths = c.getInt(c.getColumnIndex(C.db_ALERT_INTERVAL_MONTHS));
		a.ivalWeeks = c.getInt(c.getColumnIndex(C.db_ALERT_INTERVAL_WEEKS));
		a.ivalDays = c.getInt(c.getColumnIndex(C.db_ALERT_INTERVAL_DAYS));
		a.ivalHours = c.getInt(c.getColumnIndex(C.db_ALERT_INTERVAL_HOURS));
		a.ivalMinutes = c.getInt(c.getColumnIndex(C.db_ALERT_INTERVAL_MINUTES));
		a.ivalSeconds = c.getInt(c.getColumnIndex(C.db_ALERT_INTERVAL_SECONDS));
		a.ringtone = c.getString(c.getColumnIndex(C.db_ALERT_RINGTONE));
		String date = c.getString(c.getColumnIndex(C.db_ALERT_NEXT_TIME));
		try {
			if (date != null)
				a.nextTime = dbDateFormat.parse(date);
		} catch (ParseException e) {
			throw new RuntimeException("fetchAlert couldn't parse nextTime for " + a.id, e);
		}
		return a;
	}
	
		/**
		 * Fetches an Alert cursor of all alerts for a tracker 
		 * @param trackerId The rowId of the tracker
		 * @return Cursor of all the tracker's alerts
		 */
    public Cursor fetchAlertsCursor(long trackerId) {
    	Log.d(TAG, "fetching alerts for tracker " + trackerId);
        Cursor c = mDb.query(true, db_ALERT_TABLE,
				ALERT_COLUMNS,
				db_ALERT_TRACKER + "=" + trackerId, null,
		        null, null, db_ID, null);
        if (c != null)
            c.moveToFirst();
        return c;

    }
    
    	/**
    	 * Updates an alert in the DB according to changes in the DTO (if any)
    	 * 
    	 * @param alert The Alert DTO of the alert to be updated
    	 */
	public void updateAlert(Alert alert) {
		ContentValues args = alert.getChanged();
		if(args.size() > 0) {
	        mDb.update(db_ALERT_TABLE, args, db_ID + "=" + alert.id, null);
	        alert.clearChanged();
	        updateAlertSchedule();
		}
	}
    
		/**
		 * Deletes an alert from the DB
		 * 
		 * @param alertId The rowId of the alert to delete
		 */
	public void deleteAlert(long alertId) {
    	Log.d(TAG, "deleting alert " + alertId);
        mDb.delete(db_ALERT_TABLE, db_ID + "=" + alertId, null);
        updateAlertSchedule();
    }

	/**
	 * Deletes an alert from the DB
	 * 
	 * @param trackerId The rowId of the tracker to delete alerts for
	 */
	public void deleteAlerts(long trackerId) {
		Log.d(TAG, "deleting alerts for tracker " + trackerId);
	    mDb.delete(db_ALERT_TABLE, db_ALERT_TRACKER + "=" + trackerId, null);
        updateAlertSchedule();
	}

		/**
		 * Fetches the Alert from the DB that is enabled and has the soonest
		 * scheduled time to go off (even if it may be skipped once reached).
		 *   
		 * @return The Alert DTO, or none if none are scheduled
		 */
    public Alert fetchNextAlert() {
		Log.d(TAG, "fetching next alert");
		return fetchAlert(
			db_ALERT_ENABLED + " = 1 AND " + db_ALERT_SKIP_NEXT + " != 1 AND " +
			db_ALERT_NEXT_TIME + " > datetime('now', 'localtime')",
			null, null, null, db_ALERT_NEXT_TIME + " ASC", "1"
			);
	}
    
    public void updateAlertSchedule() {
		Alert next = fetchNextAlert();
		if(next == null) {
			Log.d(TAG, "Clearing alerts");			
			AlertReceiver.clearAlert(mContext);
		} else {
			Log.d(TAG, "Next alert will be at " + next.getNextTime().toLocaleString());
			AlertReceiver.setAlert(mContext, next.getTrackerId(), next.getNextTime().getTime());
		}
	}
	
	/*
	 * **************************************************************
     * 
	 * Helper classes
     */
    
    public class EntryMissingException extends RuntimeException {
    	// This "should not happen"
		private static final long serialVersionUID = 1L;
    }
    
    // inner class for handling basic DB maintenance
	private class DbAdmin extends SQLiteOpenHelper {
		public DbAdmin(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override /* create DB tables */
		public void onCreate(SQLiteDatabase db) {
			onUpgrade(db, 0, DATABASE_VERSION);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (oldVersion == 0)
				Log.i(TAG, "Creating new database");
			else
				Log.i(TAG, "Upgrading database from version " + oldVersion
						+ " to " + newVersion);

			try {
				switch(oldVersion) {
				case 0:
					db.execSQL(str(R.string.db_1_create_groups));
					Log.i(TAG, "Created table " + db_GROUP_TABLE);

					ContentValues firstRow = new ContentValues();
					firstRow.put(db_GROUP_NAME, str(R.string.db_first_list));
					db.insertOrThrow(db_GROUP_TABLE, db_GROUP_NAME, firstRow);

					db.execSQL(str(R.string.db_1_create_trackers));
					Log.i(TAG, "Created table " + db_TRACKER_TABLE);

					db.execSQL(str(R.string.db_1_create_logs));
					Log.i(TAG, "Created table " + db_LOG_TABLE);
			
					db.execSQL(str(R.string.db_1_create_alerts));
					Log.i(TAG, "Created table " + db_ALERT_TABLE);
					
					db.execSQL(str(R.string.db_1_create_tracker_group_id_idx));
					db.execSQL(str(R.string.db_1_create_log_tracker_time_idx));
					db.execSQL(str(R.string.db_1_create_alert_tracker_id_idx));
					Log.i(TAG, "Created indexes");
				case 1:
					db.execSQL(str(R.string.db_2_alter_tracker_add_value_label));
					db.execSQL(str(R.string.db_2_alter_tracker_add_value_label_pos));
					Log.i(TAG, "Added tracker value labels");
				case 2:
					db.execSQL(str(R.string.db_3_alter_tracker_add_use_value));
					db.execSQL(str(R.string.db_3_alter_tracker_add_value_type));
					db.execSQL(str(R.string.db_3_alter_logs_add_value_type));
					db.execSQL(str(R.string.db_3_update_trackers_set_use_value));
					Log.i(TAG, "Added tracker value types");
					
/*					db.execSQL(str(R.string.db_create_valuetypes));
					ContentValues firstRow = new ContentValues();
					firstRow.put(db_VALUE_NAME, str(R.string.db_first_value_name));
					firstRow.put(db_VALUE_TYPE, str(R.string.db_first_value_type));
					db.insertOrThrow(db_VALUE_TABLE, null, firstRow);
					Log.i(TAG, "Created table " + db_VALUE_TABLE);
*/
				}

			} catch (SQLException e) {
				Log.e(TAG, e.getMessage(), e);
				throw (e);
			}
		}
	}
}
