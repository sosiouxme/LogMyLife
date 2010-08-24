package net.sosiouxme.WhenDidI.custom;

import java.util.ArrayList;
import java.util.List;

import net.sosiouxme.WhenDidI.R;
import net.sosiouxme.WhenDidI.domain.DbAdapter;
import net.sosiouxme.WhenDidI.domain.dto.Alarm;
import net.sosiouxme.WhenDidI.domain.dto.Tracker;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
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
public class AlarmEditActivity extends Activity implements OnClickListener, OnCheckedChangeListener {

	private static final String STATE_KEY = "alarmList";
	private static final String TAG = "WDI.AlarmEditActivity";
	private static final int ALARM_DIALOG = 0;

	
	/** database handle */
	protected DbAdapter mDba = null;

	private LinearLayout mAlarmContainer = null;
	/** The tracker the activity is currently editing */
	protected Tracker mTracker = new Tracker(-1);

	/** Ordered list of alarms attached to this Tracker */
	private List<Alarm> mAlarmList = null;

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

		//TODO: this ain't workin'
		mIntervalTextArray  = getResources().getTextArray(R.array.alarmIntervals);

		// get the list of Alarms if we stopped in the middle of editing
		restoreState(savedInstanceState);

		// now child must set layout and call initAlarmContainer
	}


	/* 
	 * Need to call this when state is to be saved for e.g. orientation change
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if(!isFinishing()) 
			outState.putParcelableArrayList(STATE_KEY, (ArrayList<Alarm>) mAlarmList);
	}

	/*
	 * Get state of alarms during reconstitution
	 */
	public void restoreState(Bundle savedInstanceState) {
		//mAlarmList = (ArrayList<Alarm>) savedInstanceState.getParcelableArrayList(STATE_KEY);
		//having to do this instead is really asinine:
		if(savedInstanceState == null) return;
		ArrayList<Parcelable> dummy = savedInstanceState.getParcelableArrayList(STATE_KEY);
		if(dummy != null) {
			mAlarmList = new ArrayList<Alarm>();
			for( Parcelable alarm : dummy){
				mAlarmList.add((Alarm) alarm);
			}
		}
	}

	protected void initAlarmContainer() {
		mAlarmContainer = (LinearLayout) findViewById(R.id.alarmContainer);
		initAlarms();
	}
	
	/*
	 * If not reconstituted, get the list of alarms for this tracker.
	 * Either way, redraw the widget.
	 */
	protected void initAlarms() {
		if(mAlarmList == null)
			mAlarmList = mDba.fetchAlarmList(mTracker.getId());
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
		View av = getLayoutInflater().inflate(R.layout.w_alarm, null);
		av.setTag(alarm);
		av.setClickable(true);
		av.setOnClickListener(this);
		registerForContextMenu(av);
		
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
		value.setText(Integer.toString(alarm.getSingleIval(alarm.getFirstIntervalSet())));
		units.setText(mIntervalTextArray[alarm.getFirstIntervalSet().ordinal()]);
	}

	/* 
	 * Add a view representing a newly-created alarm 
	 */
	private void addNewAlarmView(Alarm alarm) {
		mAlarmContainer.addView(createAlarmElement(alarm));
	}

	/*
	 * Open dialog for user to create a new alarm
	 */
	public void openNewAlarmDialog() {
		Alarm alarm = new Alarm(mTracker.getId());
		alarm.setIvalWeeks(1);
		mAlarmToEdit = alarm;
		mViewOfAlarmToEdit = null;
		showDialog(ALARM_DIALOG);
	}
	
	/* ***************************** event handling *************************** */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
		switch (view.getId()) {
		case R.id.alarmWidget:
			Alarm alarm = (Alarm) view.getTag();
			if (alarm == null)
				return;
			mAlarmToEdit = alarm;
			mViewOfAlarmToEdit = view;
			openContextMenu(view);
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
		if(v.getId() == R.id.alarmWidget) {
			getMenuInflater().inflate(R.menu.alarm_widget_context, menu);
		}
	}

	/* 
	 * Context menu from an alarm
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		View alarmView = ((AdapterContextMenuInfo) item.getMenuInfo()).targetView;
		if(alarmView.getId() == R.id.alarmWidget) {
			mViewOfAlarmToEdit = alarmView;
			mAlarmToEdit = (Alarm) alarmView.getTag();
			switch(item.getItemId()) {
			case R.id.edit:
				showDialog(ALARM_DIALOG);
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
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch(id) {
		case ALARM_DIALOG:
			dialog = new AlarmEditDialog(getApplicationContext());
			dialog.setContentView(R.layout.d_edit_alarm);
			break;
		}
		return dialog;
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch(id) {
		case ALARM_DIALOG:
			//CheckBox enabled = (CheckBox) dialog.findViewById(R.id.enabled);
			EditText value = (EditText) dialog.findViewById(R.id.editor);
			Spinner units = (Spinner) dialog.findViewById(R.id.units);
			
			Alarm alarm = mAlarmToEdit;
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
		if(mViewOfAlarmToEdit == null) {
			//completely new alarm, create view
			addNewAlarmView(a);
		} else {
			updateAlarmView(mViewOfAlarmToEdit, a);
		}
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
