package net.sosiouxme.WhenDidI.custom;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sosiouxme.WhenDidI.R;
import net.sosiouxme.WhenDidI.Util;
import net.sosiouxme.WhenDidI.domain.DbAdapter;
import net.sosiouxme.WhenDidI.domain.dto.Alarm;
import net.sosiouxme.WhenDidI.domain.dto.LogEntry;
import net.sosiouxme.WhenDidI.domain.dto.Tracker;
import net.sosiouxme.WhenDidI.domain.dto.Alarm.Interval;
import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;
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
	private AlarmEditDialog mNotifDialog;

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
		if(!mTracker.isNew())
				mAlarmList = mDba.fetchAlarmList(mTracker.getId());
		populateAlarmViews();
	}
	
	/*
	 * Set up views showing all the alarms we have in the beginning
	 */
	protected void populateAlarmViews() {
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
		TextView nextTime = (TextView) av.findViewById(R.id.next_time);
	
		
		enabled.setChecked(alarm.getIsEnabled());
		enabled.setOnCheckedChangeListener(this);
		enabled.setTag(alarm);

		Interval i = alarm.getFirstIntervalSet();
		int ival = alarm.getSingleIval(i);
		value.setText(Integer.toString(ival));
		units.setText(mIntervalTextArray[i.ordinal()]);
		LogEntry lastLog = mTracker.getLastLog();
		nextTime.setText(createNextTimeText(lastLog == null ? null : lastLog
				.getLogDate(), ival, i));
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
		case R.id.toggle_alarm:
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
		} else
			mAlarmsToDeleteList.add(a);
	}
	
	/*
	 * Open dialog for user to create a new alarm
	 */

	protected void openNewAlarmDialog() {
		Log.d(TAG, "openNewAlarmDialog");

		Alarm alarm = new Alarm(-1);
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
			mNotifDialog = (AlarmEditDialog) dialog;
			mNotifDialog.onPrepare();
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

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent intent) {
		if (resultCode == Activity.RESULT_OK
				&& requestCode == AlarmEditDialog.ACTIVITY_RINGTONE_RETURN_CODE) {
			Uri uri = intent
					.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			((AlarmEditDialog) mNotifDialog).setRingtone(uri);
		}
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
			alarm.setNextTimeFromLast(mTracker.getLastLog());
			if(alarm.isNew()) {
				alarm.setTrackerId(trackerId);
				mDba.createAlarm(alarm);				
			} else {
				mDba.updateAlarm(alarm);
			}
		}
		mAlarmList = null; // just in case; because this object should never be reused
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
		a.setNextTimeFromLast(mTracker.getLastLog());
		if(saveAlarmChangesImmediately) {
			if(a.isNew()) {
				// new alarm for this tracker
				mDba.createAlarm(a);
			} else {
				mDba.updateAlarm(a);
			}
			mDba.updateAlarmSchedule();
		}
	}

	
	private String createNextTimeText(Date mLastTime, int value, Interval i) {
		String timeStr = getString(R.string.empty_time);
		if(mLastTime != null) {
			if(value > 0) {
				Date nextTime = Alarm.calculateNextTime(mLastTime, i, value);
				if(nextTime.after(new Date()))
					timeStr = nextTime.toLocaleString();
			}
		}
		return timeStr;
	}

	/********************** inner class for a dialog *********************/
	
	class AlarmEditDialog extends RequiredFieldDialog implements TextWatcher, OnItemSelectedListener {

		public static final int ACTIVITY_RINGTONE_RETURN_CODE = 1;

		// capture UI elements
		protected Spinner mUnits;
		protected Button mChooseRingTone;
		private CompoundButton mEnabled;
		private TextView mNextTime;

		// record state of alarm according to dialog
		private Uri mRingTone;
		private Date mLastLog;

		private Button mDeleteButton;

		public AlarmEditDialog(Context owner) {
			super(owner);
		}

		public void setContentView(int viewId) {
			super.setContentView(viewId);
			setTitle(R.string.ae_dialog_title);
			mEditor.addTextChangedListener(this);
			mEnabled = (CompoundButton) findViewById(R.id.enabled);
			mUnits = (Spinner) findViewById(R.id.units);
			mUnits.setOnItemSelectedListener(this);
			mNextTime = (TextView) findViewById(R.id.next_time);
			mChooseRingTone = (Button) findViewById(R.id.ringtone);
			mChooseRingTone.setOnClickListener(this);
			mDeleteButton = (Button) findViewById(R.id.delete);
			mDeleteButton.setOnClickListener(this);
		}

		public void onPrepare() {	
			Alarm a = mAlarmToEdit;
			// wire up the "enabled" button
			mEnabled.setChecked(a.getIsEnabled());
			
			// show or hide the "delete" button
			mDeleteButton.setVisibility(a.isNew() ? View.GONE : View.VISIBLE);
			
			// wire up the interval selection area
			mEditor.setText(Integer.toString(a.getSingleIval(a.getFirstIntervalSet())));
			mUnits.setSelection(mAlarmToEdit.getFirstIntervalSet().ordinal());
			
			// wire up the "next time" area
			LogEntry lastLogEntry = mTracker.getLastLog();
			mLastLog = (lastLogEntry == null) ? null : lastLogEntry.getLogDate();
			showNextTime();
			
			// wire up the ringtone picker
			setRingtone(mAlarmToEdit.getRingtoneUri());
		}

		private void showNextTime() {
			int value = getIntervalValue();
			Interval i = Alarm.Interval.getIntervalForPos(mUnits.getSelectedItemPosition());
			String timeStr = createNextTimeText(mLastLog, value, i);
			mNextTime.setText(timeStr);
		}

		private int getIntervalValue() {
			String text = mEditor.getText().toString();
			if(text == null || text.length() == 0)
				return 0;
			int value = Integer.parseInt(text);
			return (value > 0) ? value : 0;
		}

		public void setRingtone(Uri uri) {
			mRingTone = uri;
			String rtTitle = getContext().getString(R.string.empty_ringtone);
			if(uri != null) {
				Ringtone rt = RingtoneManager.getRingtone(getOwnerActivity(), uri);
				if(rt != null)
					rtTitle = rt.getTitle(getOwnerActivity());
			}
			mChooseRingTone.setText(rtTitle);
		}
		
		@Override
		protected void onClickOk() {
			mAlarmToEdit.setIsEnabled(mEnabled.isChecked());
			mAlarmToEdit.setTotalAlarmValue(
					Alarm.Interval.getIntervalForPos(mUnits.getSelectedItemPosition()), 
					Integer.parseInt(mEditor.getText().toString()));
			mAlarmToEdit.setRingtone(Util.toString(mRingTone));
			processAlarmEdit();
		}
		
		@Override
		public void onClick(View v) {
			if(v == mChooseRingTone) {
				Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getContext().getString(R.string.ringtone_dialog_title));
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, mRingTone);
				this.getOwnerActivity().startActivityForResult(intent, ACTIVITY_RINGTONE_RETURN_CODE);
			} else if(v == mDeleteButton) {
				removeAlarm(mViewOfAlarmToEdit);
				dismiss();
			} else
				super.onClick(v);
		}

		@Override
		public void afterTextChanged(Editable s) {
			// do nothing
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// do nothing
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			showNextTime();
		}

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			showNextTime();			
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// do nothing
		}
		
	}
}
