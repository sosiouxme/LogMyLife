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
	private static final int DATABASE_VERSION = 2;
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
    
    public long createList(String title) {
        // @return listId or -1 if failed
    	Log.d(TAG, "creating list " + title);
        ContentValues initialValues = new ContentValues();
        initialValues.put(db_LIST_TITLE, title);

        return mDb.insert(db_LIST_TABLE, null, initialValues);
    }

    public Cursor fetchList(long listId) throws SQLException {
        // @return Cursor positioned to matching note, if found
        // @throws SQLException if note could not be found/retrieved
    	Log.d(TAG, "fetching list " + listId);
        Cursor mCursor =
                mDb.query(true, db_LIST_TABLE, new String[] {db_ID,
                        db_LIST_TITLE}, db_ID + "=" + listId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public Cursor fetchLists() {
        // @return Cursor over all lists
    	Log.d(TAG, "fetching all lists");
    	return mDb.query(db_LIST_TABLE, new String[] {db_ID, db_LIST_TITLE}, null, null, null, null, null);
    }
    
    public boolean updateList(long listId, String title) {
        // @return true if the note was successfully updated, false otherwise
    	Log.d(TAG, "updating list " + listId);
        ContentValues args = new ContentValues();
        args.put(db_LIST_TITLE, title);
        return mDb.update(db_LIST_TABLE, args, db_ID + "=" + listId, null) > 0;
    }
    
    public boolean deleteList(long listId) {
        // @return true if deleted, false otherwise
    	Log.d(TAG, "deleting list " + listId);
        deleteListLogs(listId);
        mDb.delete(db_ITEM_TABLE, db_ITEM_LIST + "=" + listId, null);
        return mDb.delete(db_LIST_TABLE, db_ID + "=" + listId, null) > 0;
    }

    
    /**
     * Handle item table
     */

	public long createItem(long listId, String title, String body) {
        // @return rowId or -1 if failed
		Log.d(TAG, "creating item " + listId + ":" + title);
        ContentValues initialValues = new ContentValues();
        initialValues.put(db_ITEM_LIST, listId);
        initialValues.put(db_ITEM_TITLE, title);
        initialValues.put(db_ITEM_BODY, body);
        return mDb.insert(db_ITEM_TABLE, null, initialValues);
    }

    public Cursor fetchItem(long itemId) throws SQLException {
        // @return Cursor positioned to matching note, if found
        // @throws SQLException if note could not be found/retrieved
    	Log.d(TAG, "fetching item " + itemId);
        Cursor mCursor =
                mDb.query(true, db_ITEM_TABLE, 
                		new String[] {db_ID, db_ITEM_TITLE, db_ITEM_BODY, db_ITEM_LIST, db_ITEM_LAST_LOG},
                		db_ID + "=" + itemId, 
                		null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

	public Cursor fetchItems(long listId) {
        // @return Cursor over all items in a list
		Log.d(TAG, "fetching items for list " + listId);
        return mDb.query(db_ITEM_TABLE,
        		new String[] {db_ID, db_ITEM_TITLE, db_ITEM_BODY, db_ITEM_LAST_LOG},
        		db_ITEM_LIST + " = " + listId,
        		null, null, null, db_ID);
	}
	
    public boolean updateItem(long itemId, String title, String body) {
        // @return true if the note was successfully updated, false otherwise
    	Log.d(TAG, "updating item " + itemId);
        ContentValues args = new ContentValues();
        args.put(db_ITEM_TITLE, title);
        args.put(db_ITEM_BODY, body);
        return mDb.update(db_ITEM_TABLE, args, db_ID + "=" + itemId, null) > 0;
    }
    
	private static final String STMT_UPDATE_LAST_LOG = "update Item " +
	"set last_log_time = (" +
		"select max(log_time) " +
		"from ItemLog " +
		"where item_id = ?" +
		") " +
	"where _id = ?";
    private boolean updateItemLastLog(long itemId) {
    	mDb.execSQL(STMT_UPDATE_LAST_LOG, new Long[] {itemId, itemId});
    	return true;
    }
    
    public boolean deleteItem(long itemId) {
        // @return true if deleted, false otherwise
    	Log.d(TAG, "deleting item " + itemId);
        deleteItemLogs(itemId);
        return mDb.delete(db_ITEM_TABLE, db_ID + "=" + itemId, null) > 0;
    }

    
    /**
     * Handle log table
     */
    public String createLog(long item, Date time, String body) {
        // @return rowId or -1 if failed
    	Log.d(TAG, "creating log for item " + item);
    	time = (time == null) ? new Date() : time;
        ContentValues initialValues = new ContentValues();
        initialValues.put(db_LOG_ITEM, item);
        initialValues.put(db_LOG_TIME, dbDateFormat.format(time));
        initialValues.put(db_LOG_BODY, body);
        try {
        	mDb.insert(db_LOG_TABLE, null, initialValues);
        	updateItemLastLog(item);
        	return time.toLocaleString();
        } catch(Exception e) {
        	Log.e(TAG, "Could not insert log for item " + item);
        	return "";
        }
    }

    public Cursor fetchLog(long logId) throws SQLException {
        // @return Cursor positioned to matching note, if found
        // @throws SQLException if note could not be found/retrieved
    	Log.d(TAG, "fetching log " + logId);
        Cursor mCursor =
                mDb.query(true, db_LOG_TABLE,
                		new String[] {db_ID, db_LOG_ITEM, db_LOG_TIME, db_LOG_BODY},
                		db_ID + "=" + logId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor fetchLogs(long itemId) throws SQLException {
        // @return Cursor positioned to matching note, if found
        // @throws SQLException if note could not be found/retrieved
    	Log.d(TAG, "fetching logs for item " + itemId);
        Cursor c =
                mDb.query(true, db_LOG_TABLE,
                		new String[] {db_ID, db_LOG_TIME, db_LOG_BODY},
                		db_LOG_ITEM + "=" + itemId, null,
                        null, null, null, null);
        if (c != null)
            c.moveToFirst();
        return c;

    }

    public boolean updateLog(long logId, Date time, String body, long itemId) {
        // @return true if the note was successfully updated, false otherwise
    	Log.d(TAG, "updating log " + logId);
        ContentValues args = new ContentValues();
        if (time != null)
        	args.put(db_LOG_TIME, dbDateFormat.format(time));
        args.put(db_LOG_BODY, body);
        boolean status = mDb.update(db_LOG_TABLE, args, db_ID + "=" + logId, null) > 0;
    	updateItemLastLog(itemId);
    	return status;
    }
    
    public boolean deleteLog(long logId, long itemId) {
        // @return true if deleted, false otherwise
    	Log.d(TAG, "deleting log " + logId);
        boolean status = mDb.delete(db_LOG_TABLE, db_ID + "=" + logId, null) > 0;
    	updateItemLastLog(itemId);
    	return status;
    }
    
    public boolean deleteItemLogs(long itemId) {
    	Log.d(TAG, "deleting logs for item " + itemId);
    	boolean status = mDb.delete(db_LOG_TABLE, db_LOG_ITEM + " = " + itemId, null) > 0;
    	updateItemLastLog(itemId);
    	return status;
    }
    
    private static final String WHERE_LIST_LOGS =
    	db_LOG_ITEM + " in (select " + db_ID + " from " + db_ITEM_TABLE 
    	+ " where " + db_ITEM_LIST + " = ?)";
    public boolean deleteListLogs(long listId) {
    	Log.d(TAG, "deleting logs for list " + listId);
    	return mDb.delete(db_LOG_TABLE, WHERE_LIST_LOGS,
    			new String[] { Long.toString(listId)}) > 0;
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
				db.execSQL("CREATE TABLE " + db_LIST_TABLE + " ("
	                    + db_ID + " INTEGER PRIMARY KEY,"
	                    + db_LIST_TITLE + " TEXT NOT NULL"
	                    + ");");
				Log.i(TAG, "Created table " + db_LIST_TABLE);
				
	            ContentValues firstRow = new ContentValues();
	            firstRow.put(db_LIST_TITLE, (String) resources.getText(R.string.first_list));
	            db.insertOrThrow(db_LIST_TABLE, db_LIST_TITLE, firstRow);
	
	            // TODO: foreign keys
	            db.execSQL("CREATE TABLE " + db_ITEM_TABLE + " ("
	                    + db_ID + " INTEGER PRIMARY KEY,"
	                    + db_ITEM_LIST + " INTEGER NOT NULL," 
	                    + db_ITEM_TITLE + " TEXT NOT NULL,"
	                    + db_ITEM_BODY + " TEXT,"
	                    + db_ITEM_LAST_LOG + " DATETIME"
	                    + ");");
				Log.i(TAG, "Created table " + db_ITEM_TABLE);
			
				db.execSQL("CREATE TABLE " + db_LOG_TABLE + " ("
	                    + db_ID + " INTEGER PRIMARY KEY,"
	                    + db_LOG_ITEM + " INTEGER NOT NULL," 
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
             * Copied from Notepad demo, but seems like a bad idea
             * Will implement once I understand why this might happen (TODO) 
             *   
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + LIST_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + ITEM_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + LOG_TABLE);
            onCreate(db);
            */
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", but don't know what to do.");
		}

	}




}