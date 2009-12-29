/**
 * 
 */
package net.sosiouxme.WhenDidI;

//import java.sql.Date;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 
 *         DB Adapter for the app
 * 
 */

public class DbAdapter implements C {

	private static final String DATABASE_NAME = "whendidi.db";
	private static final int DATABASE_VERSION = 1;
    private static final String TAG = "WDI.DBAdapter";

	private DbAdmin mDbHelper;
    private SQLiteDatabase mDb;
    private final Context mCtx;

    public DbAdapter(Context ctx) {
        mCtx = ctx;
	}
    
    /**
     * Open a database handle, creating if needed. If it cannot be created, throw exception.
     * 
     * @return this
     * @throws SQLException if the database could be neither opened or created
     */
    
    public DbAdapter open() throws SQLException {
        mDbHelper = new DbAdmin(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }
    
    /**
     * 
     * Handle list table
     * 
     */
    
    public long createGroup(String title) {
        // @return listId or -1 if failed
    	Log.d(TAG, "creating group " + title);
        ContentValues initialValues = new ContentValues();
        initialValues.put(db_GROUP_NAME, title);

        return mDb.insert(db_GROUP_TABLE, null, initialValues);
    }

    public Cursor fetchGroup(long groupId) throws SQLException {
        // @return Cursor positioned to matching note, if found
        // @throws SQLException if note could not be found/retrieved
    	Log.d(TAG, "fetching group " + groupId);
        Cursor mCursor =
                mDb.query(true, db_GROUP_TABLE, new String[] {db_ID,
                        db_GROUP_NAME}, db_ID + "=" + groupId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public Cursor fetchGroups() {
        // @return Cursor over all groups
    	Log.d(TAG, "fetching all groups");
    	return mDb.query(db_GROUP_TABLE, new String[] {db_ID, db_GROUP_NAME}, null, null, null, null, null);
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
        deleteGroupLogs(groupId);
        mDb.delete(db_TRACKER_TABLE, db_TRACKER_GROUP + "=" + groupId, null);
        return mDb.delete(db_GROUP_TABLE, db_ID + "=" + groupId, null) > 0;
    }

    
    /**
     * Handle item table
     */

	public long createTracker(long groupId, String name, String body) {
        // @return rowId or -1 if failed
		Log.d(TAG, "creating item " + groupId + ":" + name);
        ContentValues initialValues = new ContentValues();
        initialValues.put(db_TRACKER_GROUP, groupId);
        initialValues.put(db_TRACKER_NAME, name);
        initialValues.put(db_TRACKER_BODY, body);
        return mDb.insert(db_TRACKER_TABLE, null, initialValues);
    }

    public Cursor fetchTracker(long trackerId) throws SQLException {
        // @return Cursor positioned to matching note, if found
        // @throws SQLException if note could not be found/retrieved
    	Log.d(TAG, "fetching tracker " + trackerId);
        Cursor mCursor =
                mDb.query(true, db_TRACKER_TABLE, 
                		new String[] {db_ID, db_TRACKER_NAME, db_TRACKER_BODY, db_TRACKER_GROUP, db_TRACKER_LAST_LOG},
                		db_ID + "=" + trackerId, 
                		null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

	public Cursor fetchTrackers(long groupId) {
        // @return Cursor over all items in a list
		Log.d(TAG, "fetching trackers for group " + groupId);
        return mDb.query(db_TRACKER_TABLE,
        		new String[] {db_ID, db_TRACKER_NAME, db_TRACKER_BODY, db_TRACKER_LAST_LOG},
        		db_TRACKER_GROUP + " = " + groupId,
        		null, null, null, db_ID);
	}
	
    public boolean updateTracker(long trackerId, String name, String body) {
        // @return true if the tracker was successfully updated, false otherwise
    	Log.d(TAG, "updating tracker " + trackerId);
        ContentValues args = new ContentValues();
        args.put(db_TRACKER_NAME, name);
        args.put(db_TRACKER_BODY, body);
        return mDb.update(db_TRACKER_TABLE, args, db_ID + "=" + trackerId, null) > 0;
    }
    
	private static final String STMT_UPDATE_LAST_LOG = "update Trackers " +
	"set last_log_time = (" +
		"select max(log_time) " +
		"from TrackerLogs " +
		"where tracker_id = ?" +
		") " +
	"where _id = ?";
    private boolean updateTrackerLastLog(long trackerId) {
    	mDb.execSQL(STMT_UPDATE_LAST_LOG, new Long[] {trackerId, trackerId});
    	return true;
    }
    
    public boolean deleteTracker(long trackerId) {
        // @return true if deleted, false otherwise
    	Log.d(TAG, "deleting tracker " + trackerId);
        deleteTrackerLogs(trackerId);
        return mDb.delete(db_TRACKER_TABLE, db_ID + "=" + trackerId, null) > 0;
    }

    
    /**
     * Handle log table
     */
    public String createLog(long trackerId, Date time, String body) {
        // @return rowId or -1 if failed
    	Log.d(TAG, "creating log for tracker " + trackerId);
    	time = (time == null) ? new Date() : time;
        ContentValues initialValues = new ContentValues();
        initialValues.put(db_LOG_TRACKER, trackerId);
        initialValues.put(db_LOG_TIME, dbDateFormat.format(time));
        initialValues.put(db_LOG_BODY, body);
        try {
        	mDb.insert(db_LOG_TABLE, null, initialValues);
        	updateTrackerLastLog(trackerId);
        	return time.toLocaleString();
        } catch(Exception e) {
        	Log.e(TAG, "Could not insert log for tracker " + trackerId);
        	return "";
        }
    }

    public Cursor fetchLog(long logId) throws SQLException {
        // @return Cursor positioned to matching note, if found
        // @throws SQLException if note could not be found/retrieved
    	Log.d(TAG, "fetching log " + logId);
        Cursor mCursor =
                mDb.query(true, db_LOG_TABLE,
                		new String[] {db_ID, db_LOG_TRACKER, db_LOG_TIME, db_LOG_BODY},
                		db_ID + "=" + logId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor fetchLogs(long trackerId) throws SQLException {
        // @return Cursor positioned to matching log, if found
        // @throws SQLException if log could not be found/retrieved
    	Log.d(TAG, "fetching logs for item " + trackerId);
        Cursor c =
                mDb.query(true, db_LOG_TABLE,
                		new String[] {db_ID, db_LOG_TIME, db_LOG_BODY},
                		db_LOG_TRACKER + "=" + trackerId, null,
                        null, null, null, null);
        if (c != null)
            c.moveToFirst();
        return c;

    }

    public boolean updateLog(long logId, Date time, String body, long trackerId) {
        // @return true if the note was successfully updated, false otherwise
    	Log.d(TAG, "updating log " + logId);
        ContentValues args = new ContentValues();
        if (time != null)
        	args.put(db_LOG_TIME, dbDateFormat.format(time));
        args.put(db_LOG_BODY, body);
        boolean status = mDb.update(db_LOG_TABLE, args, db_ID + "=" + logId, null) > 0;
    	updateTrackerLastLog(trackerId);
    	return status;
    }
    
    public boolean deleteLog(long logId, long trackerId) {
        // @return true if deleted, false otherwise
    	Log.d(TAG, "deleting log " + logId);
        boolean status = mDb.delete(db_LOG_TABLE, db_ID + "=" + logId, null) > 0;
    	updateTrackerLastLog(trackerId);
    	return status;
    }
    
    public boolean deleteTrackerLogs(long trackerId) {
    	Log.d(TAG, "deleting logs for item " + trackerId);
    	boolean status = mDb.delete(db_LOG_TABLE, db_LOG_TRACKER + " = " + trackerId, null) > 0;
    	updateTrackerLastLog(trackerId);
    	return status;
    }
    
    private static final String WHERE_GROUP_LOGS =
    	db_LOG_TRACKER + " in (select " + db_ID + " from " + db_TRACKER_TABLE 
    	+ " where " + db_TRACKER_GROUP + " = ?)";
    public boolean deleteGroupLogs(long groupId) {
    	Log.d(TAG, "deleting logs for group " + groupId);
    	return mDb.delete(db_LOG_TABLE, WHERE_GROUP_LOGS,
    			new String[] { Long.toString(groupId)}) > 0;
    }


    
    // inner class for handling basic DB maintenance
	private class DbAdmin extends SQLiteOpenHelper {
		private Resources resources;
		public DbAdmin(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            resources = context.getResources();
		}

		@Override /* create DB tables */
		public void onCreate(SQLiteDatabase db) {
      
			try {
				db.execSQL("CREATE TABLE " + db_GROUP_TABLE + " ("
	                    + db_ID + " INTEGER PRIMARY KEY,"
	                    + db_GROUP_NAME + " TEXT NOT NULL"
	                    + ");");
				Log.i(TAG, "Created table " + db_GROUP_TABLE);
				
	            ContentValues firstRow = new ContentValues();
	            firstRow.put(db_GROUP_NAME, (String) resources.getText(R.string.first_list));
	            db.insertOrThrow(db_GROUP_TABLE, db_GROUP_NAME, firstRow);
	
	            // TODO: foreign keys
	            db.execSQL("CREATE TABLE " + db_TRACKER_TABLE + " ("
	                    + db_ID + " INTEGER PRIMARY KEY,"
	                    + db_TRACKER_GROUP + " INTEGER NOT NULL," 
	                    + db_TRACKER_NAME + " TEXT NOT NULL,"
	                    + db_TRACKER_BODY + " TEXT,"
	                    + db_TRACKER_LAST_LOG + " DATETIME"
	                    + ");");
				Log.i(TAG, "Created table " + db_TRACKER_TABLE);
			
				db.execSQL("CREATE TABLE " + db_LOG_TABLE + " ("
	                    + db_ID + " INTEGER PRIMARY KEY,"
	                    + db_LOG_TRACKER + " INTEGER NOT NULL," 
	                    + db_LOG_TIME + " DATETIME NOT NULL,"
	                    + db_LOG_BODY + " TEXT"
	                    + ");");
				Log.i(TAG, "Created table " + db_LOG_TABLE);
			} catch(SQLException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            /*
             * Will implement as necessary
             * 
             * switch(oldVersion) {
             * case 1: (no break)
             * case 2:
             * etc
             * }
            */
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", but don't know what to do.");
		}

	}




}