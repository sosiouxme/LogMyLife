package net.sosiouxme.WhenDidI.activity;

import net.sosiouxme.WhenDidI.R;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Prefs extends PreferenceActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}

	public static final String TRACKER_CLICK_BEHAVIOR = "trackerClickBehavior";
	public static final String TRACKER_CLICK_BEHAVIOR_LOG = "log";
	public static final String TRACKER_CLICK_BEHAVIOR_LOG_DETAIL = "log_detail";
	public static final String TRACKER_CLICK_BEHAVIOR_VIEW = "view";
	public static String getTrackerClickBehavior(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString(TRACKER_CLICK_BEHAVIOR, TRACKER_CLICK_BEHAVIOR_VIEW);
	}
}
