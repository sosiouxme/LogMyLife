package net.sosiouxme.WhenDidI.custom;

import java.util.ArrayList;
import java.util.List;

import net.sosiouxme.WhenDidI.R;
import net.sosiouxme.WhenDidI.domain.DbAdapter;
import net.sosiouxme.WhenDidI.domain.dto.Alarm;
import net.sosiouxme.WhenDidI.domain.dto.Tracker;
import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

/*
 * This class exists to provide alarm-UI functionality that both TrackerEdit
 * and TrackerDetail need; also to keep from cluttering those classes.
 * 
 * Requires activity to propagate context menu events and state saving events.
 * 
 */
public class AlarmWidget implements OnClickListener, OnCheckedChangeListener {

	private static final String STATE_KEY = "alarmList";
	private static final String TAG = "WDI.AlarmWidget";
	private static final int ALARM_DIALOG = 0;
	private final Activity mParent;
	private final DbAdapter mDba;
	private final LinearLayout mAlarmContainer;
	private final long mTrackerId;
	/** Ordered list of alarms attached to this Tracker */
	private List<Alarm> mAlarmList;

	private Alarm mAlarmToEdit;
	private View mViewOfAlarmToEdit;

	/* ***************************** lifecycle *************************** */

	/* 
	 * Creates this widget that uses the container for the alarm create/edit UI
	 */
	public AlarmWidget(Activity parent, DbAdapter dba, LinearLayout alarmContainer, Tracker mTracker, Bundle savedInstanceState) {
		mParent = parent;
		mDba = dba;
		mAlarmContainer = alarmContainer;
		mTrackerId = (mTracker == null) ? -1 : mTracker.getId();
		restoreState(savedInstanceState);
		parent.registerForContextMenu(null);
		initAlarms();
	}


	/* 
	 * Need to call this when state is to be saved for e.g. orientation change
	 */
	public void saveState(Bundle saveInstanceState) {
		saveInstanceState.putParcelableArrayList(STATE_KEY, (ArrayList<Alarm>) mAlarmList);
	}

	/*
	 * Get state of alarms after reconstitution
	 */
	public void restoreState(Bundle savedInstanceState) {
		//mAlarmList = (ArrayList<Alarm>) savedInstanceState.getParcelableArrayList(STATE_KEY);
		// this is really asinine
		mAlarmList = new ArrayList<Alarm>();
		ArrayList<Parcelable> dummy = savedInstanceState.getParcelableArrayList(STATE_KEY);
		for( Parcelable alarm : dummy){
			mAlarmList.add((Alarm) alarm);
		}
	}


	/*
	 * If not reconstituted, get the list of alarms for this tracker
	 */
	private void initAlarms() {
		if(mAlarmList == null)
			mAlarmList = mDba.fetchAlarmList(mTrackerId);
		initAlarmViews();
	}
	
	/*
	 * Set up views showing all the alarms we have in the beginning
	 */
	private void initAlarmViews() {
		mAlarmContainer.removeAllViews();
		for(Alarm alarm : mAlarmList) {
			mAlarmContainer.addView(createAlarmElement(alarm));
		}
	}

	/* ***************************** UI bits *************************** */

	/* 
	 * Create a view representing a single alarm
	 */
	private View createAlarmElement(Alarm alarm) {
		View av = mParent.getLayoutInflater().inflate(R.layout.w_alarm, null);
		av.setTag(alarm);
		av.setClickable(true);
		av.setOnClickListener(this);
		
		updateAlarmView(av, alarm);
		return av;
	}

	/*
	 * Update the alarmView according to the alarm's values
	 */
	private void updateAlarmView(View av, Alarm alarm) {
		CheckBox enabled = (CheckBox) av.findViewById(R.id.enabled);
		TextView value = (TextView) av.findViewById(R.id.value);
		TextView units = (TextView) av.findViewById(R.id.units);
		
		enabled.setChecked(alarm.getIsEnabled());
		enabled.setOnCheckedChangeListener(this);
		enabled.setTag(alarm);
		value.setText("1"); //TODO
		units.setText("week(s)"); //TODO
	}

	/* 
	 * Add a view representing a newly-created alarm 
	 */
	private void addNewAlarm(Alarm alarm) {
		//TODO
		mAlarmContainer.addView(createAlarmElement(alarm));
	}

	/*
	 * Open dialog for user to create a new alarm
	 */
	public void openNewAlarmDialog() {
		//TODO
		Alarm alarm = new Alarm(mTrackerId);
		addNewAlarm(alarm);
	}
	
	/* ***************************** event handling *************************** */

	/*
	 * User clicked on an alarm - open context menu to perform contextual action
	 */
	public void onClick(View alarmView) {
		Alarm alarm = (Alarm) alarmView.getTag();
		if(alarm == null) return;
		mAlarmToEdit = alarm;
		mViewOfAlarmToEdit = alarmView;
		mParent.openContextMenu(alarmView);
	}

	/* 
	 * Should be delegated from parent activity method of same signature
	 */
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Log.d(TAG, "onCreateContextMenu");
		mParent.getMenuInflater().inflate(R.menu.alarm_widget_context, menu);
	}

	/* 
	 * Should be delegated from parent activity method of same signature
	 */
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.edit:
			mParent.showDialog(ALARM_DIALOG);
//TODO
			break;
		case R.id.enable:
			mAlarmToEdit.setIsEnabled(!mAlarmToEdit.isEnabled);
			updateAlarmView(mAlarmContainer, mAlarmToEdit);
			break;
		case R.id.skip:
			break;
		case R.id.delete:
			mAlarmList.remove(mAlarmToEdit);
			mAlarmContainer.removeView(mViewOfAlarmToEdit);
			break;
		}
		return false;
	}

	/* 
	 * User changed enablement of the alarm
	 */
	public void onCheckedChanged(CompoundButton enabled, boolean checked) {
		Alarm alarm = (Alarm) enabled.getTag();
		if(alarm == null) return;
		alarm.setIsEnabled(checked);
	}

	/* ***************************** worker methods *************************** */
	

	/*
	 * Persist to the DB the current set of alarms associated
	 * with the tracker that's been stored
	 */
	public void storeAlarms(long trackerId){
		// when building a tracker there won't initially be an ID;
		// that's why it must be passed in to save the alarms with.
		mDba.deleteAlarms(trackerId);
		for(Alarm alarm : mAlarmList) {
			if(alarm.getId() == -1) {
				alarm.setTrackerId(trackerId);
				mDba.createAlarm(alarm);				
			} else {
				mDba.updateAlarm(alarm);
			}
		}
		mAlarmList = null; // just in case; because this object should never be reused
		//TODO: fix up the next scheduled alarm
	}
	
}
