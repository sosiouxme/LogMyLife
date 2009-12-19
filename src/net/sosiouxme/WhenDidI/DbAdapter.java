/**
 * 
 */
package net.sosiouxme.WhenDidI;

import java.sql.Date;

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

public class DbAdapter {
	private static final String DATABASE_NAME = "whendidi.db";
	private static final int DATABASE_VERSION = 2;
    private static final String TAG = "WDI.DBAdapter";

	public static final String _ID = "_id";	
	private static final String LIST_TABLE = "List";
	public static final String LIST_TITLE = "title";
	private static final String ITEM_TABLE = "Item";
	public static final String ITEM_LIST = "list_id";
	public static final String ITEM_TITLE = "title";
	public static final String ITEM_BODY = "body";
	private static final String LOG_TABLE = "ItemLog";
	public static final String LOG_ITEM = "item_id";
	public static final String LOG_TIME = "log_time";
	public static final String LOG_BODY = "body";
		
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
        initialValues.put(LIST_TITLE, title);

        return mDb.insert(LIST_TABLE, null, initialValues);
    }

    public Cursor fetchList(long listId) throws SQLException {
        // @return Cursor positioned to matching note, if found
        // @throws SQLException if note could not be found/retrieved
    	Log.d(TAG, "fetching list " + listId);
        Cursor mCursor =
                mDb.query(true, LIST_TABLE, new String[] {_ID,
                        LIST_TITLE}, _ID + "=" + listId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public Cursor fetchLists() {
        // @return Cursor over all lists
    	Log.d(TAG, "fetching all lists");
    	return mDb.query(LIST_TABLE, new String[] {_ID, LIST_TITLE}, null, null, null, null, null);
    }
    
    public boolean updateList(long listId, String title) {
        // @return true if the note was successfully updated, false otherwise
    	Log.d(TAG, "updating list " + listId);
        ContentValues args = new ContentValues();
        args.put(LIST_TITLE, title);
        return mDb.update(LIST_TABLE, args, _ID + "=" + listId, null) > 0;
    }
    
    public boolean deleteList(long listId) {
        // @return true if deleted, false otherwise
    	Log.d(TAG, "deleting list " + listId);
        deleteListLogs(listId);
        mDb.delete(ITEM_TABLE, ITEM_LIST + "=" + listId, null);
        return mDb.delete(LIST_TABLE, _ID + "=" + listId, null) > 0;
    }

    
    /**
     * Handle item table
     */

	public long createItem(long listId, String title, String body) {
        // @return rowId or -1 if failed
		Log.d(TAG, "creating item " + listId + ":" + title);
        ContentValues initialValues = new ContentValues();
        initialValues.put(ITEM_LIST, listId);
        initialValues.put(ITEM_TITLE, title);
        initialValues.put(ITEM_BODY, body);
        return mDb.insert(ITEM_TABLE, null, initialValues);
    }

    public Cursor fetchItem(long itemId) throws SQLException {
        // @return Cursor positioned to matching note, if found
        // @throws SQLException if note could not be found/retrieved
    	Log.d(TAG, "fetching item " + itemId);
        Cursor mCursor =
                mDb.query(true, ITEM_TABLE, 
                		new String[] {_ID, ITEM_TITLE, ITEM_BODY, ITEM_LIST},
                		_ID + "=" + itemId, 
                		null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

	public Cursor fetchItems(long listId) {
        // @return Cursor over all items in a list
		Log.d(TAG, "fetching items for list " + listId);
        return mDb.query(ITEM_TABLE,
        		new String[] {_ID, ITEM_TITLE, ITEM_BODY},
        		ITEM_LIST + " = " + listId,
        		null, null, null, _ID);
	}
	
    public boolean updateItem(long itemId, String title, String body) {
        // @return true if the note was successfully updated, false otherwise
    	Log.d(TAG, "updating item " + itemId);
        ContentValues args = new ContentValues();
        args.put(ITEM_TITLE, title);
        args.put(ITEM_BODY, body);
        return mDb.update(ITEM_TABLE, args, _ID + "=" + itemId, null) > 0;
    }
    
    public boolean deleteItem(long itemId) {
        // @return true if deleted, false otherwise
    	Log.d(TAG, "deleting item " + itemId);
        deleteItemLogs(itemId);
        return mDb.delete(ITEM_TABLE, _ID + "=" + itemId, null) > 0;
    }

    
    /**
     * Handle log table
     */
    public long createLog(long item, Date time, String body) {
        // @return rowId or -1 if failed
    	Log.d(TAG, "creating log for item " + item);
        ContentValues initialValues = new ContentValues();
        initialValues.put(LOG_ITEM, item);
        initialValues.put(LOG_TIME, time.toGMTString());
        initialValues.put(LOG_BODY, body);
        return mDb.insert(LIST_TABLE, null, initialValues);
    }

    public Cursor fetchLog(long logId) throws SQLException {
        // @return Cursor positioned to matching note, if found
        // @throws SQLException if note could not be found/retrieved
    	Log.d(TAG, "fetching log " + logId);
        Cursor mCursor =
                mDb.query(true, LOG_TABLE,
                		new String[] {_ID, LOG_ITEM, LOG_TIME, LOG_BODY},
                		_ID + "=" + logId, null,
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
        Cursor mCursor =
                mDb.query(true, LOG_TABLE,
                		new String[] {_ID, LOG_TIME, LOG_BODY},
                		LOG_ITEM + "=" + itemId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public boolean updateLog(long logId, Date time, String body) {
        // @return true if the note was successfully updated, false otherwise
    	Log.d(TAG, "updating log " + logId);
        ContentValues args = new ContentValues();
        args.put(LOG_TIME, time.toGMTString());
        args.put(LOG_BODY, body);
        return mDb.update(LOG_TABLE, args, _ID + "=" + logId, null) > 0;
    }
    
    public boolean deleteLog(long logId) {
        // @return true if deleted, false otherwise
    	Log.d(TAG, "deleting log " + logId);
        return mDb.delete(LOG_TABLE, _ID + "=" + logId, null) > 0;
    }
    
    public boolean deleteItemLogs(long itemId) {
    	Log.d(TAG, "deleting logs for item " + itemId);
    	return mDb.delete(LOG_TABLE, LOG_ITEM + " = " + itemId, null) > 0;
    }
    
    private static final String WHERE_LIST_LOGS =
    	LOG_ITEM + " in (select " + _ID + " from " + ITEM_TABLE 
    	+ " where " + ITEM_LIST + " = ?)";
    public boolean deleteListLogs(long listId) {
    	Log.d(TAG, "deleting logs for list " + listId);
    	return mDb.delete(LOG_TABLE, WHERE_LIST_LOGS,
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
				db.execSQL("CREATE TABLE " + LIST_TABLE + " ("
	                    + _ID + " INTEGER PRIMARY KEY,"
	                    + LIST_TITLE + " TEXT"
	                    + ");");
				Log.i(TAG, "Created table " + LIST_TABLE);
				
	            ContentValues firstRow = new ContentValues();
	            firstRow.put(LIST_TITLE, (String) resources.getText(R.string.first_list));
	            db.insertOrThrow(LIST_TABLE, LIST_TITLE, firstRow);
	
	            // TODO: foreign keys
	            db.execSQL("CREATE TABLE " + ITEM_TABLE + " ("
	                    + _ID + " INTEGER PRIMARY KEY,"
	                    + ITEM_LIST + " INTEGER," 
	                    + ITEM_TITLE + " TEXT,"
	                    + ITEM_BODY + " TEXT"
	                    + ");");
				Log.i(TAG, "Created table " + ITEM_TABLE);
			
				db.execSQL("CREATE TABLE " + LOG_TABLE + " ("
	                    + _ID + " INTEGER PRIMARY KEY,"
	                    + LOG_ITEM + " INTEGER," 
	                    + LOG_TIME + " DATETIME,"
	                    + LOG_BODY + " TEXT"
	                    + ");");
				Log.i(TAG, "Created table " + LOG_TABLE);
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