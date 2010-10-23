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
import net.sosiouxme.logmylife.domain.dto.Alarm;
import net.sosiouxme.logmylife.domain.dto.LogEntry;
import net.sosiouxme.logmylife.domain.dto.Tracker;
import net.sosiouxme.logmylife.receiver.AlarmReceiver;
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
	private static final int DATABASE_VERSION = 1;
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
        mDb.delete(db_ALARM_TABLE, whereClause, whereArgs);
        mDb.delete(db_TRACKER_TABLE, db_TRACKER_GROUP + "=" + groupId, null);
        updateAlarmSchedule();
        return mDb.delete(db_GROUP_TABLE, db_ID + "=" + groupId, null) > 0;
    }

    
    /*
     * ******************************************************************************8
     * Handle Trackers table
     */

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
				new String[] { db_ID, db_TRACKER_NAME, db_TRACKER_BODY,
						db_TRACKER_SKIP_NEXT_ALARM, db_TRACKER_GROUP,
						db_TRACKER_LAST_LOG_ID },
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
			t.skipNextAlarm = c.getInt(c.getColumnIndex(C.db_TRACKER_SKIP_NEXT_ALARM)) > 0;

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
		List<Alarm> alarms = fetchAlarmList(trackerId);
		for(Alarm a : alarms) {
			a.setNextTimeFromLast(t.getLastLog());
			updateAlarm(a);
		}
    }
    
    public boolean deleteTracker(long trackerId) {
        // @return true if deleted, false otherwise
    	Log.d(TAG, "deleting tracker " + trackerId);
    	mDb.delete(db_LOG_TABLE, db_LOG_TRACKER + " = " + trackerId, null);
    	deleteAlarms(trackerId);
        return mDb.delete(db_TRACKER_TABLE, db_ID + "=" + trackerId, null) > 0;
    }

    
    /*
     * ******************************************************************************
     * Handle TrackerLogs table
     */
    
	public long createLog(long trackerId) {
		LogEntry le = new LogEntry(-1);
		le.setTrackerId(trackerId);
		return createLog(le);
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
            		new String[] {db_ID, db_LOG_TRACKER, db_LOG_TIME, db_LOG_BODY,
					  db_LOG_VALUE, /*db_LOG_VALUE_TYPE,*/ db_LOG_IS_BREAK},
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
			//le.valueType = c.getLong(c.getColumnIndex(C.db_LOG_VALUE_TYPE));
			if (c.isNull(c.getColumnIndex(C.db_LOG_VALUE)))
				le.value = null;
			else
				le.value = c.getInt(c.getColumnIndex(C.db_LOG_VALUE));
		} catch (ParseException e) {
			throw new RuntimeException("fetchLog couldn't parse date for " + logId, e);
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return le;
	}

    public Cursor fetchLogs(long trackerId) {
        // @return Cursor positioned to matching log, if found
    	Log.d(TAG, "fetching logs for item " + trackerId);
        Cursor c = mDb.query(true, db_LOG_TABLE,
				new String[] {db_ID, db_LOG_TIME, db_LOG_BODY},
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
     * Handle Alarms table
     */
    
	    /**
	     * Create a DB alarm entry from an Alarm DTO
	     * 
	     * @param newAlarm the DTO to create in the DB
	     * @return rowId of the alarm created
	     */
    public long createAlarm(Alarm newAlarm) {
    	Log.d(TAG, "creating Alarm for tracker " + newAlarm.trackerId);
       	long alarmId = mDb.insert(db_ALARM_TABLE, null, newAlarm.getChanged());
       	updateAlarmSchedule();
       	return alarmId;
    }

	    /** All columns from the Alarms table (for queries) */
	private static final String[] ALARM_COLUMNS = new String[] { 
			db_ID, db_ALARM_TRACKER,
			db_ALARM_INTERVAL_MONTHS, db_ALARM_INTERVAL_WEEKS, 
			db_ALARM_INTERVAL_DAYS, db_ALARM_INTERVAL_HOURS, 
			db_ALARM_INTERVAL_MINUTES, db_ALARM_INTERVAL_SECONDS, 
			db_ALARM_NEXT_TIME, db_ALARM_ENABLED, db_ALARM_SKIP_NEXT,
			db_ALARM_RINGTONE };
		
		/**
		 * Fetches an Alarm DTO from the DB with the given rowId
		 * 
		 * @param alarmId The rowId of the alarm
		 * @return The corresponding Alarm object, or null if not found
		 */
	public Alarm fetchAlarm(long alarmId) {
		return fetchAlarm(db_ID + "=" + alarmId, null, null, null, null, null);
	}
	
	public Alarm fetchAlarm(String where, String[] where_params, String group_by, String having, String order_by, String limit) {
		Cursor c = null;
		Alarm alarm = null;
		try {
			c = mDb.query(true, db_ALARM_TABLE,
            		ALARM_COLUMNS,
					where, where_params,
					group_by, having, order_by, limit);
			if (c.getCount() != 1)
				return null;
            c.moveToFirst();
            alarm = getAlarmFromCursor(c);
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return alarm;
	}
	
    public List<Alarm> fetchAlarmList(long trackerId) {
        // @return ArrayList of matching alarms, if found
    	// WARN: this has never been tested
    	Log.d(TAG, "fetching alarms for tracker " + trackerId);
        List<Alarm> alarms = new ArrayList<Alarm>();
        Cursor c = fetchAlarmsCursor(trackerId);
        if (c != null) {
	        c.moveToFirst();
	        while(!c.isAfterLast()) {
	        	alarms.add(getAlarmFromCursor(c));
	        	c.moveToNext();
	        }
	        c.close();
        }
        return alarms;
    }
	
		/**
		 * Turns a single cursor row from the DB into an Alarm object
		 * @param c The cursor
		 * @return The filled out Alarm oject
		 */
	private Alarm getAlarmFromCursor(Cursor c) {
		Alarm a = new Alarm(c.getLong(0));
		a.trackerId = c.getLong(c.getColumnIndex(C.db_ALARM_TRACKER));
		a.isEnabled = c.getInt(c.getColumnIndex(C.db_ALARM_ENABLED)) > 0;
		a.skipNext = c.getInt(c.getColumnIndex(C.db_ALARM_SKIP_NEXT)) > 0;
		a.ivalMonths = c.getInt(c.getColumnIndex(C.db_ALARM_INTERVAL_MONTHS));
		a.ivalWeeks = c.getInt(c.getColumnIndex(C.db_ALARM_INTERVAL_WEEKS));
		a.ivalDays = c.getInt(c.getColumnIndex(C.db_ALARM_INTERVAL_DAYS));
		a.ivalHours = c.getInt(c.getColumnIndex(C.db_ALARM_INTERVAL_HOURS));
		a.ivalMinutes = c.getInt(c.getColumnIndex(C.db_ALARM_INTERVAL_MINUTES));
		a.ivalSeconds = c.getInt(c.getColumnIndex(C.db_ALARM_INTERVAL_SECONDS));
		a.ringtone = c.getString(c.getColumnIndex(C.db_ALARM_RINGTONE));
		String date = c.getString(c.getColumnIndex(C.db_ALARM_NEXT_TIME));
		try {
			if (date != null)
				a.nextTime = dbDateFormat.parse(date);
		} catch (ParseException e) {
			throw new RuntimeException("fetchAlarm couldn't parse nextTime for " + a.id, e);
		}
		return a;
	}
	
		/**
		 * Fetches an Alarm cursor of all alarms for a tracker 
		 * @param trackerId The rowId of the tracker
		 * @return Cursor of all the tracker's alarms
		 */
    public Cursor fetchAlarmsCursor(long trackerId) {
    	Log.d(TAG, "fetching alarms for tracker " + trackerId);
        Cursor c = mDb.query(true, db_ALARM_TABLE,
				ALARM_COLUMNS,
				db_ALARM_TRACKER + "=" + trackerId, null,
		        null, null, db_ID, null);
        if (c != null)
            c.moveToFirst();
        return c;

    }
    
    	/**
    	 * Updates an alarm in the DB according to changes in the DTO (if any)
    	 * 
    	 * @param alarm The Alarm DTO of the alarm to be updated
    	 */
	public void updateAlarm(Alarm alarm) {
		ContentValues args = alarm.getChanged();
		if(args.size() > 0) {
	        mDb.update(db_ALARM_TABLE, args, db_ID + "=" + alarm.id, null);
	        alarm.clearChanged();
	        updateAlarmSchedule();
		}
	}
    
		/**
		 * Deletes an alarm from the DB
		 * 
		 * @param alarmId The rowId of the alarm to delete
		 */
	public void deleteAlarm(long alarmId) {
    	Log.d(TAG, "deleting alarm " + alarmId);
        mDb.delete(db_ALARM_TABLE, db_ID + "=" + alarmId, null);
        updateAlarmSchedule();
    }

	/**
	 * Deletes an alarm from the DB
	 * 
	 * @param trackerId The rowId of the tracker to delete alarms for
	 */
	public void deleteAlarms(long trackerId) {
		Log.d(TAG, "deleting alarms for tracker " + trackerId);
	    mDb.delete(db_ALARM_TABLE, db_ALARM_TRACKER + "=" + trackerId, null);
        updateAlarmSchedule();
	}

		/**
		 * Fetches the Alarm from the DB that is enabled and has the soonest
		 * scheduled time to go off (even if it may be skipped once reached).
		 *   
		 * @return The Alarm DTO, or none if none are scheduled
		 */
    public Alarm fetchNextAlarm() {
		Log.d(TAG, "fetching next alarm");
		return fetchAlarm(
			db_ALARM_ENABLED + " = 1 AND " + db_ALARM_SKIP_NEXT + " != 1 AND " +
			db_ALARM_NEXT_TIME + " > datetime('now', 'localtime')",
			null, null, null, db_ALARM_NEXT_TIME + " ASC", "1"
			);
	}
    
    public void updateAlarmSchedule() {
		Alarm next = fetchNextAlarm();
		if(next == null) {
			Log.d(TAG, "Clearing alarms");			
			AlarmReceiver.clearAlarm(mContext);
		} else {
			Log.d(TAG, "Next alarm will be at " + next.getNextTime().toLocaleString());
			AlarmReceiver.setAlarm(mContext, next.getTrackerId(), next.getNextTime().getTime());
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
				if (oldVersion < 1) {
					db.execSQL(str(R.string.db_create_groups));
					Log.i(TAG, "Created table " + db_GROUP_TABLE);

					ContentValues firstRow = new ContentValues();
					firstRow.put(db_GROUP_NAME, str(R.string.db_first_list));
					db.insertOrThrow(db_GROUP_TABLE, db_GROUP_NAME, firstRow);

					db.execSQL(str(R.string.db_create_trackers));
					Log.i(TAG, "Created table " + db_TRACKER_TABLE);

					db.execSQL(str(R.string.db_create_logs));
					Log.i(TAG, "Created table " + db_LOG_TABLE);
			
					db.execSQL(str(R.string.db_create_alarms));
					Log.i(TAG, "Created table " + db_ALARM_TABLE);
					
					db.execSQL(str(R.string.db_create_tracker_group_id_idx));
					db.execSQL(str(R.string.db_create_log_tracker_time_idx));
					db.execSQL(str(R.string.db_create_alarm_tracker_id_idx));
					Log.i(TAG, "Created indexes");

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
