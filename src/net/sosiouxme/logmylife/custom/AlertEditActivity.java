/*
    This file is part of LogMyLife, an application for logging events.
    Copyright (C) 2011 Luke Meyer

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program (see LICENSE file).
    If not, see http://www.gnu.org/licenses/
*/

package net.sosiouxme.logmylife.custom;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sosiouxme.logmylife.R;
import net.sosiouxme.logmylife.Util;
import net.sosiouxme.logmylife.domain.DbAdapter;
import net.sosiouxme.logmylife.domain.dto.Alert;
import net.sosiouxme.logmylife.domain.dto.LogEntry;
import net.sosiouxme.logmylife.domain.dto.Tracker;
import net.sosiouxme.logmylife.domain.dto.Alert.Interval;
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
 * This class exists to provide alert-UI functionality that both TrackerEdit
 * and TrackerDetail need; also to keep from cluttering those classes.
 * Might be a bit of a hack being a sort of "abstract" activity, or maybe
 * that's what inheritance is for.
 * 
 * Essentially the children of this activity just have to provide a
 * LinearLayout alertContainer and allow this parent to handle its events.
 */
public class AlertEditActivity extends ListActivity implements OnClickListener, OnCheckedChangeListener {

	private static final String STATE_KEY_ALERT_EDIT = "mAlertToEdit";
	private static final String TAG = "LML.AlertEditActivity";
	protected static final int ALERT_DIALOG = 0;
	protected static final int DIALOG_INFO_GROUP = 1;
	protected static final int DIALOG_INFO_VALUE = 2;
	protected static final int DIALOG_INFO_ALERT = 3;
	private static final String STATE_KEY_ALERTS = "mAlertList";
	private static final String STATE_KEY_DELETING = "mAlertsToDeleteList";

	
	/** database handle */
	protected DbAdapter mDba = null;

	private LinearLayout mAlertContainer = null;
	/** The tracker the activity is currently editing */
	protected Tracker mTracker = null;

	/** Ordered list of alerts attached to this Tracker */
	private List<Alert> mAlertList = null;
	private List<Alert> mAlertsToDeleteList = new ArrayList<Alert>();

	/** for use in context-aware dialogs **/
	private Alert mAlertToEdit = null;
	private View mViewOfAlertToEdit = null;
	private CharSequence[] mIntervalTextArray = null;
	
	protected boolean saveAlertChangesImmediately = false;
	private AlertEditDialog mNotifDialog;

	/* ***************************** lifecycle *************************** */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// get a DB handle
		mDba = new DbAdapter(this);

		mIntervalTextArray  = getResources().getTextArray(R.array.alertIntervals);

		// get the list of Alerts if we stopped in the middle of editing
		restoreState(savedInstanceState);

		// now child must set layout and call initAlertContainer and populateAlerts
	}


	/* 
	 * Need to call this when state is to be saved for e.g. orientation change
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState");
		if(!isFinishing()) {
			outState.putSerializable(STATE_KEY_ALERT_EDIT, mAlertToEdit);
			outState.putSerializable(STATE_KEY_ALERTS, (ArrayList<Alert>) mAlertList);
			outState.putSerializable(STATE_KEY_DELETING, (ArrayList<Alert>) mAlertsToDeleteList);
		}
	}

	/*
	 * Get state of alerts during reconstitution
	 */
	@SuppressWarnings("unchecked")
	protected void restoreState(Bundle savedInstanceState) {
		Log.d(TAG, "restoreState");
		if(savedInstanceState == null) return;
		mAlertToEdit = (Alert) savedInstanceState.getSerializable(STATE_KEY_ALERT_EDIT);
		mAlertList = (ArrayList<Alert>) savedInstanceState.getSerializable(STATE_KEY_ALERTS);
		mAlertsToDeleteList = (ArrayList<Alert>) savedInstanceState.getSerializable(STATE_KEY_DELETING);
	}

	protected void initAlertContainer() {
		Log.d(TAG, "initAlertContainer");
		mAlertContainer = (LinearLayout) findViewById(R.id.alertContainer);
		mAlertList = new ArrayList<Alert>();
		View addNew = findViewById(R.id.add_new_alert);
		if(addNew != null)
			addNew.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					openNewAlertDialog();
				}
			});
	}
	
	/*
	 * Get the list of alerts for the tracker if known.
	 * Either way, redraw the widget.
	 */
	protected void populateAlerts() {
		Log.d(TAG, "populateAlerts");
		if(!mTracker.isNew())
				mAlertList = mDba.fetchAlertList(mTracker.getId());
		populateAlertViews();
	}
	
	/*
	 * Set up views showing all the alerts we have in the beginning
	 */
	protected void populateAlertViews() {
		Log.d(TAG, "populateAlertViews");
		mAlertContainer.removeAllViews();
		for(Alert alert : mAlertList) {
			mAlertContainer.addView(createAlertElement(alert));
		}
	}

	/* ***************************** UI bits *************************** */

	/* 
	 * Create a view representing a single alert
	 */
	private View createAlertElement(Alert alert) {
		Log.d(TAG, "createAlertElement " + alert.id);
		View av = getLayoutInflater().inflate(R.layout.w_alert, null);
		av.setTag(alert);
		
		// each alert has a checkbox for enable/disable -- attach listener
		CompoundButton enabled = (CompoundButton) av.findViewById(R.id.enabled);
		enabled.setOnClickListener(this);
		enabled.setTag(alert);

		// each alert also has a body that can be clicked for interaction
		ViewGroup alertBody = (ViewGroup) av.findViewById(R.id.alertBody);
		alertBody.setClickable(true);
		alertBody.setOnClickListener(this);
		alertBody.setTag(alert);
		registerForContextMenu(alertBody);
		
		updateAlertView(av, alert);
		return av;
	}

	/*
	 * Update the alertView according to the alert's values
	 */
	private void updateAlertView(View av, Alert alert) {
		Log.d(TAG, "updateAlertView " + alert.id);
		ToggleButton enabled = (ToggleButton) av.findViewById(R.id.enabled);
		TextView value = (TextView) av.findViewById(R.id.value);
		TextView units = (TextView) av.findViewById(R.id.units);
		TextView nextTime = (TextView) av.findViewById(R.id.next_time);
	
		
		enabled.setChecked(alert.getIsEnabled());
		enabled.setOnCheckedChangeListener(this);
		enabled.setTag(alert);

		Interval i = alert.getFirstIntervalSet();
		int ival = alert.getSingleIval(i);
		value.setText(Integer.toString(ival));
		units.setText(mIntervalTextArray[i.ordinal()]);
		LogEntry lastLog = mTracker.getLastLog();
		nextTime.setText(createNextTimeText(lastLog == null ? null : lastLog
				.getLogDate(), ival, i));
	}

	/* 
	 * Add a view representing a newly-created alert 
	 */
	private void addNewAlertView(Alert alert) {
		Log.d(TAG, "addNewAlertView");
		mAlertContainer.addView(createAlertElement(alert));
	}

	
	
	/* ***************************** event handling *************************** */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected");
		switch (item.getItemId()) {
		case R.id.new_alert:
			openNewAlertDialog();
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	/*
	 * User clicked on an alert - open context menu to perform contextual action
	 * Or on dialog button - do the right thing.
	 */
	public void onClick(View view) {
		Alert alert;
		switch (view.getId()) {
		case R.id.alertBody:
			alert = (Alert) view.getTag();
			if (alert == null)
				return;
			mAlertToEdit = alert;
			mViewOfAlertToEdit = (ViewGroup) view.getParent();
			showDialog(ALERT_DIALOG);
			break;
		case R.id.enabled:
			alert = (Alert) view.getTag();
			if (alert == null)
				return;
			CompoundButton enabled = (CompoundButton) view;
			alert.setIsEnabled(enabled.isChecked());
			updateAlert(alert);
			break;
		}
	}
	

	/* 
	 * Context menu from an alertWidget
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Log.d(TAG, "onCreateContextMenu");
		super.onCreateContextMenu(menu, v, menuInfo);
		if(v.getId() == R.id.alertBody) {
			getMenuInflater().inflate(R.menu.alert_widget_context, menu);
			// evidently this is the only way to record which alert the
			// context menu is for: (!)
			mViewOfAlertToEdit = (View) v.getParent();
			mAlertToEdit = (Alert) v.getTag();
		}
	}

	/* 
	 * Context menu from an alert
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		switch (item.getItemId()) {
		case R.id.edit_alert:
			showDialog(ALERT_DIALOG);
			return true;
		case R.id.toggle_alert:
			mAlertToEdit.setIsEnabled(!mAlertToEdit.isEnabled);
			updateAlertView(mViewOfAlertToEdit, mAlertToEdit);
			updateAlert(mAlertToEdit);
			return true;
		case R.id.delete_alert:
			removeAlert(mViewOfAlertToEdit);
			return true;
		}
		return false;
	}

	private void removeAlert(View alertWidget) {
		Alert a = (Alert) alertWidget.getTag();
		mAlertList.remove(a);
		mAlertContainer.removeView(alertWidget);
		mAlertsToDeleteList.add(a);
		if (saveAlertChangesImmediately && !a.isNew()) {
			mDba.deleteAlert(a.getId());
		} else
			mAlertsToDeleteList.add(a);
	}
	
	/*
	 * Open dialog for user to create a new alert
	 */

	protected void openNewAlertDialog() {
		Log.d(TAG, "openNewAlertDialog");

		Alert alert = new Alert(-1);
		alert.setTrackerId(mTracker.getId());
		alert.setIvalWeeks(1);
		mAlertToEdit = alert;
		mViewOfAlertToEdit = null;
		showDialog(ALERT_DIALOG);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		Dialog dialog = null;
		switch(id) {
		case ALERT_DIALOG:
			dialog = new AlertEditDialog(this);
			dialog.setContentView(R.layout.d_edit_alert);
			break;
		case DIALOG_INFO_GROUP:
			return Util.getHtmlDialogBuilder(this, R.string.te_dialog_group_html)
			.setTitle(R.string.te_dialog_group_title)
			.setPositiveButton(R.string.info_dialog_dismiss_button, null)
			.create();
		case DIALOG_INFO_VALUE:
			return Util.getHtmlDialogBuilder(this, R.string.te_dialog_value_html)
			.setTitle(R.string.te_dialog_value_title)
			.setPositiveButton(R.string.info_dialog_dismiss_button, null)
			.create();
		case DIALOG_INFO_ALERT:
			return Util.getHtmlDialogBuilder(this, R.string.te_dialog_alert_html)
			.setTitle(R.string.te_dialog_alert_title)
			.setPositiveButton(R.string.info_dialog_dismiss_button, null)
			.create();

		}
		return dialog;
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		Log.d(TAG, "onPrepareDialog");
		super.onPrepareDialog(id, dialog);
		switch(id) {
		case ALERT_DIALOG:
			mNotifDialog = (AlertEditDialog) dialog;
			mNotifDialog.onPrepare();
			break;
		}
	}


	/* 
	 * User changed enablement of the alert
	 */
	public void onCheckedChanged(CompoundButton enabled, boolean checked) {
		Log.d(TAG, "onCheckedChanged");
		Alert alert = (Alert) enabled.getTag();
		if(alert == null) return;
		//Log.d(TAG, "alert enablement set: " + alert.id + " " + checked);
		alert.setIsEnabled(checked);
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent intent) {
		if (resultCode == Activity.RESULT_OK
				&& requestCode == AlertEditDialog.ACTIVITY_RINGTONE_RETURN_CODE) {
			Uri uri = intent
					.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			((AlertEditDialog) mNotifDialog).setRingtone(uri);
		}
	}

	/* ***************************** worker methods *************************** */
	

	/*
	 * Persist to the DB the current set of alerts associated
	 * with the tracker that's been stored
	 */
	public void storeAlerts(long trackerId){
		Log.d(TAG, "storeAlerts for tracker " + trackerId);
		// when building a tracker there won't initially be an ID;
		// that's why it must be passed in to save the alerts with.

		for(Alert alert : mAlertsToDeleteList) {
			if(!alert.isNew()) // no need to delete those that weren't created
				mDba.deleteAlert(alert.getId());
		}
		for(Alert alert : mAlertList) {
			alert.setNextTimeFromLast(mTracker.getLastLog());
			if(alert.isNew()) {
				alert.setTrackerId(trackerId);
				mDba.createAlert(alert);				
			} else {
				mDba.updateAlert(alert);
			}
		}
		mAlertList = null; // just in case; because this object should never be reused
	}

	private void processAlertEdit() {
		Alert a = mAlertToEdit;
		Log.d(TAG, "processAlertEdit " + a);
		if(mViewOfAlertToEdit == null) {
			//completely new alert, add and create view
			mAlertList.add(a);
			addNewAlertView(a);
		} else {
			updateAlertView(mViewOfAlertToEdit, a);
		}
		updateAlert(a);
	}


	private void updateAlert(Alert a) {
		a.setNextTimeFromLast(mTracker.getLastLog());
		if(saveAlertChangesImmediately) {
			if(a.isNew()) {
				// new alert for this tracker
				mDba.createAlert(a);
			} else {
				mDba.updateAlert(a);
			}
			mDba.updateAlertSchedule();
		}
	}

	
	private String createNextTimeText(Date mLastTime, int value, Interval i) {
		String timeStr = getString(R.string.empty_time);
		if(mLastTime != null) {
			if(value > 0) {
				Date nextTime = Alert.calculateNextTime(mLastTime, i, value);
				if(nextTime.after(new Date()))
					timeStr = nextTime.toLocaleString();
			}
		}
		return timeStr;
	}

	/********************** inner class for a dialog *********************/
	
	class AlertEditDialog extends RequiredFieldDialog implements TextWatcher, OnItemSelectedListener {

		public static final int ACTIVITY_RINGTONE_RETURN_CODE = 1;

		// capture UI elements
		protected Spinner mUnits;
		protected Button mChooseRingTone;
		private CompoundButton mEnabled;
		private TextView mNextTime;

		// record state of alert according to dialog
		private Uri mRingTone;
		private Date mLastLog;

		private Button mDeleteButton;

		public AlertEditDialog(Context owner) {
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
			Alert a = mAlertToEdit;
			// wire up the "enabled" button
			mEnabled.setChecked(a.getIsEnabled());
			
			// show or hide the "delete" button
			mDeleteButton.setVisibility(a.isNew() ? View.GONE : View.VISIBLE);
			
			// wire up the interval selection area
			mEditor.setText(Integer.toString(a.getSingleIval(a.getFirstIntervalSet())));
			mUnits.setSelection(mAlertToEdit.getFirstIntervalSet().ordinal());
			
			// wire up the "next time" area
			LogEntry lastLogEntry = mTracker.getLastLog();
			mLastLog = (lastLogEntry == null) ? null : lastLogEntry.getLogDate();
			showNextTime();
			
			// wire up the ringtone picker
			setRingtone(mAlertToEdit.getRingtoneUri());
		}

		private void showNextTime() {
			int value = getIntervalValue();
			Interval i = Alert.Interval.getIntervalForPos(mUnits.getSelectedItemPosition());
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
			mAlertToEdit.setIsEnabled(mEnabled.isChecked());
			mAlertToEdit.setTotalAlertValue(
					Alert.Interval.getIntervalForPos(mUnits.getSelectedItemPosition()), 
					Integer.parseInt(mEditor.getText().toString()));
			mAlertToEdit.setRingtone(Util.toString(mRingTone));
			processAlertEdit();
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
				removeAlert(mViewOfAlertToEdit);
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
