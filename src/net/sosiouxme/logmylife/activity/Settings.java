package net.sosiouxme.logmylife.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import net.sosiouxme.logmylife.LogMyLife;
import net.sosiouxme.logmylife.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
Activity that lets the user set app-wide preferences.

@author Luke Meyer, Copyright 2010
See LICENSE file for this file's GPLv3 distribution license.
*/

public class Settings extends PreferenceActivity {


	public static final int TRACKER_BEHAVIOR_VIEW = 0;
	public static final int TRACKER_BEHAVIOR_LOG = 1;
	public static final int TRACKER_BEHAVIOR_LOG_DETAIL = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		((LogMyLife) getApplication()).refreshPrefs();
	}
	
	private static SharedPreferences getPrefs(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static String getDefaultRingtone(Context c) {
		return getPrefs(c).getString("defaultReminderRingtone", null);
	}
	
	public static int getTrackerClickBehavior(Context c) {
		String tb = getPrefs(c).getString("trackerClickBehavior", "");
		if(tb.equals("log_detail"))
			return TRACKER_BEHAVIOR_LOG_DETAIL;
		else if(tb.equals("log"))
			return TRACKER_BEHAVIOR_LOG;
		else
			return TRACKER_BEHAVIOR_VIEW;
	}

	public static DateFormat getDateFormat(Context c) {
		try {
			return new SimpleDateFormat(getPrefs(c).getString("dateFormat", "yyyy-MM-dd"));
		} catch(Exception e) {
			return new SimpleDateFormat( "yyyy-MM-dd");
		}
	}

	public static DateFormat getTimeFormat(Context c) {
		try {
			return new SimpleDateFormat(getPrefs(c).getString("timeFormat", "HH:mm:ss"));
		} catch(Exception e) {
			return new SimpleDateFormat( "HH:mm:ss");
		}
	}
	
	public static DateFormat getDateTimeFormat(Context c) {
		
		SharedPreferences prefs = getPrefs(c);
		try {
			return new SimpleDateFormat( //"yyyy-MM-dd HH:mm:ss");
				prefs.getString("dateFormat", "yyyy-MM-dd") + " " +
				prefs.getString("timeFormat", "HH:mm:ss"));
		} catch(Exception e) {
			return new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
		}
	}

	public static QuietHours getQuietHours(Context c) {
		SharedPreferences prefs = getPrefs(c);
		if(prefs.getBoolean("enableQuietHours", false)) {
			return new QuietHours(
					prefs.getString("beginQuietHours", "0000"),
					prefs.getString("endQuietHours", "0000")
					);
		} else {
			return new QuietHours();
		}
	}
	
	public static class QuietHours {
			
		private boolean enabled = false;
		private GregorianCalendar begin = null;
		private GregorianCalendar end = null;
		private GregorianCalendar now = new GregorianCalendar();
		
		public QuietHours() {		} // quiet hours disabled

		public QuietHours(String beginStr, String endStr) {
			enabled = true;
			begin = extractDateLimit(beginStr);
			end = extractDateLimit(endStr);
			if(endStr.compareTo(beginStr) <= 0) {
				// end equal or before beginning = overnight quiet hours
				if(begin.after(now)) // begin > now
					begin.roll(Calendar.DATE, -1); // use yesterday's period begin
				else if(end.before(now)) // end < now
					end.roll(Calendar.DATE, 1); // use tomorrow's period end
			}
		}

		private GregorianCalendar extractDateLimit(String hhmmTime) {
			GregorianCalendar limit = new GregorianCalendar();
			limit.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hhmmTime.substring(0, 2)));
			limit.set(Calendar.MINUTE, Integer.parseInt(hhmmTime.substring(2, 4)));
			limit.set(Calendar.SECOND, 0);
			return limit;
		}
		
		public boolean isQuietTime() {
			if(enabled)
				return begin.before(now) && now.before(end);
			return false;
		}
	}
}
