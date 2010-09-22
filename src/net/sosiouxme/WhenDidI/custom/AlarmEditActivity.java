package net.sosiouxme.WhenDidI.custom;

import java.util.ArrayList;
import java.util.List;

import net.sosiouxme.WhenDidI.R;
import net.sosiouxme.WhenDidI.domain.DbAdapter;
import net.sosiouxme.WhenDidI.domain.dto.Alarm;
import net.sosiouxme.WhenDidI.domain.dto.Tracker;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/*
 * This class exists to provide alarm-UI functionality that both TrackerEdit
 * and TrackerDetail need; also to keep from cluttering those classes.
 * Might be a bit of a hack being a sort of "abstract" activity, or maybe
 * that's what inheritance is for.
 * 
 * Essentially the children of this activity just have to provide a
 * LinearLayout alarmContainer and allow this parent to handle its events.
 */
public class AlarmEditActivity extends ListActivity implements OnClickListener, OnCheckedChangeListener {

	private static final String TAG = "WDI.AlarmEditActivity";
	protected static final int ALARM_DIALOG = 0;
	private static final String STATE_KEY_ALARMS = "mAlarmList";
	private static final String STATE_KEY_DELETING = "mAlarmsToDeleteList";

	
	/** database handle */
	protected DbAdapter mDba = null;

	private LinearLayout mAlarmContainer = null;
	/** The tracker the activity is currently editing */
	protected Tracker mTracker = null;

	/** Ordered list of alarms attached to this Tracker */
	private List<Alarm> mAlarmList = null;
	private List<Alarm> mAlarmsToDeleteList = new ArrayList<Alarm>();

	/** for use in context-aware dialogs **/
	private Alarm mAlarmToEdit = null;
	private View mViewOfAlarmToEdit = null;
	private CharSequence[] mIntervalTextArray = null;
	
	protected boolean saveAlarmChangesImmediately = false;

	/* ***************************** lifecycle *************************** */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// get a DB handle
		mDba = new DbAdapter(this);

		mIntervalTextArray  = getResources().getTextArray(R.array.alarmIntervals);

		// get the list of Alarms if we stopped in the middle of editing
		restoreState(savedInstanceState);

		// now child must set layout and call initAlarmContainer and populateAlarms
	}


	/* 
	 * Need to call this when state is to be saved for e.g. orientation change
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState");
		if(!isFinishing()) {
			outState.putSerializable(STATE_KEY_ALARMS, (ArrayList<Alarm>) mAlarmList);
			outState.putSerializable(STATE_KEY_DELETING, (ArrayList<Alarm>) mAlarmsToDeleteList);
		}
	}

	/*
	 * Get state of alarms during reconstitution
	 */
	@SuppressWarnings("unchecked")
	public void restoreState(Bundle savedInstanceState) {
		Log.d(TAG, "restoreState");
		if(savedInstanceState == null) return;
		mAlarmList = (ArrayList<Alarm>) savedInstanceState.getSerializable(STATE_KEY_ALARMS);
		mAlarmsToDeleteList = (ArrayList<Alarm>) savedInstanceState.getSerializable(STATE_KEY_DELETING);
	}

	protected void initAlarmContainer() {
		Log.d(TAG, "initAlarmContainer");
		mAlarmContainer = (LinearLayout) findViewById(R.id.alarmContainer);
		mAlarmList = new ArrayList<Alarm>();
		Button addNew = (Button) findViewById(R.id.add_new_alarm);
		if(addNew != null)
			addNew.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					openNewAlarmDialog();
				}
			});
	}
	
	/*
	 * Get the list of alarms for the tracker if known.
	 * Either way, redraw the widget.
	 */
	protected void populateAlarms() {
		Log.d(TAG, "populateAlarms");
		if(mTracker != null)
				mAlarmList = mDba.fetchAlarmList(mTracker.getId());
		populateAlarmViews();
	}
	
	/*
	 * Set up views showing all the alarms we have in the beginning
	 */
	private void populateAlarmViews() {
		Log.d(TAG, "populateAlarmViews");
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
		Log.d(TAG, "createAlarmElement " + alarm.id);
		View av = getLayoutInflater().inflate(R.layout.w_alarm, null);
		av.setTag(alarm);
		
		// each alarm has a checkbox for enable/disable -- attach listener
		CompoundButton enabled = (CompoundButton) av.findViewById(R.id.enabled);
		enabled.setOnClickListener(this);
		enabled.setTag(alarm);

		// each alarm also has a body that can be clicked for interaction
		ViewGroup alarmBody = (ViewGroup) av.findViewById(R.id.alarmBody);
		alarmBody.setClickable(true);
		alarmBody.setOnClickListener(this);
		alarmBody.setTag(alarm);
		registerForContextMenu(alarmBody);
		
		updateAlarmView(av, alarm);
		return av;
	}

	/*
	 * Update the alarmView according to the alarm's values
	 */
	private void updateAlarmView(View av, Alarm alarm) {
		Log.d(TAG, "updateAlarmView " + alarm.id);
		ToggleButton enabled = (ToggleButton) av.findViewById(R.id.enabled);
		TextView value = (TextView) av.findViewById(R.id.value);
		TextView units = (TextView) av.findViewById(R.id.units);
		
		enabled.setChecked(alarm.getIsEnabled());
		enabled.setOnCheckedChangeListener(this);
		enabled.setTag(alarm);
		value.setText(Integer.toString(alarm.getSingleIval(alarm.getFirstIntervalSet())));
		units.setText(mIntervalTextArray[alarm.getFirstIntervalSet().ordinal()]);
	}

	/* 
	 * Add a view representing a newly-created alarm 
	 */
	private void addNewAlarmView(Alarm alarm) {
		Log.d(TAG, "addNewAlarmView");
		mAlarmContainer.addView(createAlarmElement(alarm));
	}

	
	
	/* ***************************** event handling *************************** */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected");
		switch (item.getItemId()) {
		case R.id.new_alarm:
			openNewAlarmDialog();
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	/*
	 * User clicked on an alarm - open context menu to perform contextual action
	 * Or on dialog button - do the right thing.
	 */
	public void onClick(View view) {
		Alarm alarm;
		switch (view.getId()) {
		case R.id.alarmBody:
			alarm = (Alarm) view.getTag();
			if (alarm == null)
				return;
			mAlarmToEdit = alarm;
			mViewOfAlarmToEdit = (ViewGroup) view.getParent();
			showDialog(ALARM_DIALOG);
			break;
		case R.id.enabled:
			alarm = (Alarm) view.getTag();
			if (alarm == null)
				return;
			CompoundButton enabled = (CompoundButton) view;
			alarm.setIsEnabled(enabled.isChecked());
			updateAlarm(alarm);
			break;
		}
	}
	

	/* 
	 * Context menu from an alarmWidget
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Log.d(TAG, "onCreateContextMenu");
		super.onCreateContextMenu(menu, v, menuInfo);
		if(v.getId() == R.id.alarmBody) {
			getMenuInflater().inflate(R.menu.alarm_widget_context, menu);
			// evidently this is the only way to record which alarm the
			// context menu is for: (!)
			mViewOfAlarmToEdit = (View) v.getParent();
			mAlarmToEdit = (Alarm) v.getTag();
		}
	}

	/* 
	 * Context menu from an alarm
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		switch (item.getItemId()) {
		case R.id.edit_alarm:
			showDialog(ALARM_DIALOG);
			return true;
		case R.id.skip_alarm:
			// TODO: change skip, not enablement
			mAlarmToEdit.setIsEnabled(!mAlarmToEdit.isEnabled);
			updateAlarmView(mViewOfAlarmToEdit, mAlarmToEdit);
			updateAlarm(mAlarmToEdit);
			return true;
		case R.id.delete_alarm:
			removeAlarm(mViewOfAlarmToEdit);
			return true;
		}
		return false;
	}

	private void removeAlarm(View alarmWidget) {
		Alarm a = (Alarm) alarmWidget.getTag();
		mAlarmList.remove(a);
		mAlarmContainer.removeView(alarmWidget);
		mAlarmsToDeleteList.add(a);
		if (saveAlarmChangesImmediately && !a.isNew()) {
			mDba.deleteAlarm(a.getId());
			updateAlarmSchedule();
		} else
			mAlarmsToDeleteList.add(a);
	}
	
	/*
	 * Open dialog for user to create a new alarm
	 */

	protected void openNewAlarmDialog() {
		Log.d(TAG, "openNewAlarmDialog");

		Alarm alarm = new Alarm(-1);
		if(mTracker != null) 
			alarm.setTrackerId(mTracker.getId());
		alarm.setIvalWeeks(1);
		mAlarmToEdit = alarm;
		mViewOfAlarmToEdit = null;
		showDialog(ALARM_DIALOG);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		Dialog dialog = null;
		switch(id) {
		case ALARM_DIALOG:
			dialog = new AlarmEditDialog(this);
			dialog.setContentView(R.layout.d_edit_alarm);
			break;
		}
		return dialog;
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		Log.d(TAG, "onPrepareDialog");
		super.onPrepareDialog(id, dialog);
		switch(id) {
		case ALARM_DIALOG:
			//CheckBox enabled = (CheckBox) dialog.findViewById(R.id.enabled);
			EditText value = (EditText) dialog.findViewById(R.id.editor);
			Spinner units = (Spinner) dialog.findViewById(R.id.units);
			
			Alarm alarm = mAlarmToEdit;
			Log.d(TAG, "editing alarm: " + alarm.id);
			//enabled.setChecked(alarm.getIsEnabled());
			//enabled.setOnCheckedChangeListener(this);
			//enabled.setTag(alarm);
			value.setText(Integer.toString(alarm.getSingleIval(alarm.getFirstIntervalSet())));
			units.setSelection(alarm.getFirstIntervalSet().ordinal());
			break;
		}
	}


	/* 
	 * User changed enablement of the alarm
	 */
	public void onCheckedChanged(CompoundButton enabled, boolean checked) {
		Log.d(TAG, "onCheckedChanged");
		Alarm alarm = (Alarm) enabled.getTag();
		if(alarm == null) return;
		Log.d(TAG, "alarm enablement set: " + alarm.id + " " + checked);
		alarm.setIsEnabled(checked);
	}

	/* ***************************** worker methods *************************** */
	

	/*
	 * Persist to the DB the current set of alarms associated
	 * with the tracker that's been stored
	 */
	public void storeAlarms(long trackerId){
		Log.d(TAG, "storeAlarms for tracker " + trackerId);
		// when building a tracker there won't initially be an ID;
		// that's why it must be passed in to save the alarms with.

		for(Alarm alarm : mAlarmsToDeleteList) {
			if(!alarm.isNew()) // no need to delete those that weren't created
				mDba.deleteAlarm(alarm.getId());
		}
		for(Alarm alarm : mAlarmList) {
			if(alarm.isNew()) {
				alarm.setTrackerId(trackerId);
				mDba.createAlarm(alarm);				
			} else {
				mDba.updateAlarm(alarm);
			}
			updateNextAlarm(alarm);
		}
		mAlarmList = null; // just in case; because this object should never be reused
		updateAlarmSchedule();
	}

	private void processAlarmEdit() {
		Alarm a = mAlarmToEdit;
		Log.d(TAG, "processAlarmEdit " + a);
		if(mViewOfAlarmToEdit == null) {
			//completely new alarm, add and create view
			mAlarmList.add(a);
			addNewAlarmView(a);
		} else {
			updateAlarmView(mViewOfAlarmToEdit, a);
		}
		updateAlarm(a);
	}


	private void updateAlarm(Alarm a) {
		if(saveAlarmChangesImmediately) {
			if(a.isNew()) {
				// new alarm for this tracker
				mDba.createAlarm(a);
			} else {
				mDba.updateAlarm(a);
			}
			updateNextAlarm(a);
			updateAlarmSchedule();
		}
	}

	private void updateNextAlarm(Alarm a) {
		//TODO: update the alarm's next scheduled time to go off, if any
	}

	private void updateAlarmSchedule() {
		//TODO: in light of changes made here,
		//update the schedule of the next alarm to go off
	}
	
	/********************** inner class for a dialog *********************/
	
	class AlarmEditDialog extends RequiredFieldDialog {

		protected Spinner mUnits;
		
		public AlarmEditDialog(Context owner) {
			super(owner);
		}

		public void setContentView(int viewId) {
			super.setContentView(viewId);
			setTitle(R.string.ae_dialog_title);
			mUnits = (Spinner) findViewById(R.id.units);
		}
		
		@Override
		protected void onClickOk() {
			// TODO Auto-generated method stub
			mAlarmToEdit.setTotalAlarmValue(
					Alarm.Interval.getIntervalForPos(mUnits.getSelectedItemPosition()), 
					Integer.parseInt(mEditor.getText().toString()));
			processAlarmEdit();
		}
		
	}
}
