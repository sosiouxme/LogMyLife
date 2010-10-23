package net.sosiouxme.logmylife;

import java.util.HashMap;
import java.util.Map;

import android.app.Application;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
The Application object that can be retrieved from any LML Activity with 
getApplication(). Useful for referencing app-wide preferences and such.
 
@author Luke Meyer, Copyright 2010
See LICENSE file for this file's GPLv3 distribution license.
*/
public class LogMyLife extends Application {
	
	private SharedPreferences mPrefs = null;
	private static final String PREFS_FILE = "prefs";
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.mPrefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
	}

	public static final String TRACKER_CLICK_BEHAVIOR = "trackerClickBehavior";
	public static final String TRACKER_CLICK_BEHAVIOR_LOG = "log";
	public static final String TRACKER_CLICK_BEHAVIOR_LOG_DETAIL = "log_detail";
	public static final String TRACKER_CLICK_BEHAVIOR_VIEW = "view";
	public String getTrackerClickBehavior() {
		return mPrefs.getString(TRACKER_CLICK_BEHAVIOR, TRACKER_CLICK_BEHAVIOR_VIEW);
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
}
