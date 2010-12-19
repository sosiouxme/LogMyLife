package net.sosiouxme.logmylife;

import java.util.HashMap;
import java.util.Map;

import net.sosiouxme.logmylife.domain.DbAdapter;
import net.sosiouxme.logmylife.receiver.AlertReceiver;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
The Application object that can be retrieved from any LML Activity with 
getApplication(). Useful for referencing app-wide preferences and such.
 
@author Luke Meyer, Copyright 2010
See LICENSE file for this file's GPLv3 distribution license.
*/
public class LogMyLife extends Application {
	
	private SharedPreferences mPrefs = null;

	@Override
	public void onCreate() {
		super.onCreate();
		this.mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		// as backup to the broadcast receivers, make sure the next
		// reminder alert is set when starting the app.
		DbAdapter db = new DbAdapter(this);
		AlertReceiver.setNextAlert(this, db);
		db.close();
	}
	
	// repository for created toasts
	private Map<Long,Toast> mToasts = new HashMap<Long,Toast>();
	public void showToast(Long id) {
		Toast t = mToasts.get(id);
		if(t==null)
			mToasts.put(id, t = Toast.makeText(this, id.intValue(), Toast.LENGTH_SHORT));
		t.show();
	}

	/*
	 * represents the group currently selected, which persists between invocations. 
	 * this is not displayed with the Prefs activity.
	 */
	private static final String GROUP_SELECTED = "groupSelected";
	public long getSelectedGroup() {
		return mPrefs.getLong(GROUP_SELECTED, 0);
	}
	
	public void setSelectedGroup(long groupId) {
		mPrefs.edit().putLong(GROUP_SELECTED, groupId).commit();
	}

	private static final String FIRST_TIME = "firstTime";
	/**
	 * Returns whether this is the first time the user has ever used
	 * this application.
	 *
	 * @return True if this is the first time opening this app. False forever after.
	 */
	public boolean getFirstTime() {
		boolean first = mPrefs.getBoolean(FIRST_TIME, true);
		if(first)mPrefs.edit().putBoolean(FIRST_TIME, false).commit();	
		return first;
	}
	
	private static final String STORED_CHANGE_VERSION = "storedChangeVersion";
	private static final int CHANGE_VERSION = 0;
	/**
	 * Has something been updated that we should tell users about?
	 * 
	 * @return True if the version number here changes; false otherwise
	 */
	public boolean getShowChangedDialog() {
		int stored = mPrefs.getInt(STORED_CHANGE_VERSION, CHANGE_VERSION);
		if(stored < CHANGE_VERSION) {
			// previously stored version has changed
			mPrefs.edit().putInt(STORED_CHANGE_VERSION, CHANGE_VERSION).commit();	
			return true;
		} else if (mPrefs.getInt(STORED_CHANGE_VERSION, -1) == -1) 
			// first time we ever checked - initialize
			mPrefs.edit().putInt(STORED_CHANGE_VERSION, CHANGE_VERSION).commit();	
		return false;
	}
	
}
