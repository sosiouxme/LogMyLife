package net.sosiouxme.logmylife.activity;

import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import net.sosiouxme.logmylife.C;
import net.sosiouxme.logmylife.LogMyLife;
import net.sosiouxme.logmylife.R;
import net.sosiouxme.logmylife.Util;
import net.sosiouxme.logmylife.custom.NumberPicker;
import net.sosiouxme.logmylife.custom.NumberPicker.OnChangedListener;
import net.sosiouxme.logmylife.dialog.LogDeleteDialog;
import net.sosiouxme.logmylife.domain.DbAdapter;
import net.sosiouxme.logmylife.domain.dto.LogEntry;
import net.sosiouxme.logmylife.domain.dto.Tracker;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

/**
Activity that presents the UI for editing a single LogEntry 

@author Luke Meyer, Copyright 2010
See LICENSE file for this file's GPLv3 distribution license.
*/

public class LogEdit extends Activity implements android.view.View.OnClickListener, OnDateSetListener {

	private static final String TAG = "LML.LogEdit";
	private static final int DIALOG_DATE = 0;
	private static final int DIALOG_TIME = 1;
	private DateFormat mDateFormat;
	private DateFormat mTimeFormat;
	private static final int DIALOG_HELP = 100;
	private LogEntry mLogEntry = null;
	private Tracker mTracker = null;
	private DbAdapter mDba = null;
	private EditText metBody;
	private Button mDateEditButton;
	private boolean saveOnFinish = true; // by default, back button will save
	private Button mTimeEditButton;
	private EditText metValue;

	/* 
	 * (non-Javadoc)
	 * expects an extras bundle on the intent, specifying either:
	 * C.db_ID = rowId of log entry to edit
	 * C.db_LOG_TRACKER = rowId of tracker for which to create new log entry
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// get a DB handle
		mDba = new DbAdapter(this);

		// find existing log entry / tracker to work on
		long logId = 0;
		long trackerId = 0;
		Bundle e = getIntent().getExtras();
		if (e == null)
			throw new RuntimeException("required intent extras not supplied");
		logId = e.getLong(C.db_ID); //to edit existing log entry
		trackerId = e.getLong(C.db_LOG_TRACKER); //for new log entry
		// existing logId means we're going to edit it
		if (logId > 0) {
			mLogEntry = mDba.fetchLog(logId);
			if (mLogEntry == null)
				throw new RuntimeException("couldn't find log id " + logId);
			trackerId = mLogEntry.getTrackerId();
		} else {
			mLogEntry = new LogEntry(-1);
			mLogEntry.setTrackerId(trackerId);
			mLogEntry.setLogDate(new GregorianCalendar().getTime());
		}
		// get the tracker to log against (whether new or existing log) 
		mTracker = mDba.fetchTracker(trackerId);
		if (mTracker == null)
			throw new RuntimeException("couldn't find tracker id " + trackerId);

		// set up the date/time formatters
		mDateFormat = Settings.getDateFormat(this);
		mTimeFormat = Settings.getTimeFormat(this);

		// Set the layout for this activity.
		setContentView(R.layout.a_log_edit);
		
		/* locate and fill the necessary elements of the layout */

		// tracker information
		TextView tvName = (TextView) findViewById(R.id.tracker_name);
		TextView tvBody = (TextView) findViewById(R.id.tracker_details);
		tvName.setText(mTracker.name);
		tvBody.setText(mTracker.body);

		// text and value for log
		metBody = (EditText) findViewById(R.id.body);
		metBody.setText(mLogEntry.body);
		metValue = (EditText) findViewById(R.id.logValue);
		metValue.setText(Util.toString(mLogEntry.value));
		
		// log date/time
		mDateEditButton = (Button) findViewById(R.id.editDate);
		mDateEditButton.setOnClickListener(this);
		mTimeEditButton = (Button) findViewById(R.id.editTime);
		mTimeEditButton.setOnClickListener(this);
		setDateTimeDisplay();
		
		// wire up the save/cancel buttons
		Button cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
		Button saveButton = (Button) findViewById(R.id.save);
		saveButton.setOnClickListener(this);
	}

	private void setDateTimeDisplay() {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(mLogEntry.getLogDate());
		mDateEditButton.setText(mDateFormat.format(mLogEntry.getLogDate()));
		mTimeEditButton.setText(mTimeFormat.format(mLogEntry.getLogDate()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.log_edit, menu);
		if(mLogEntry != null) {
			// modify menus to show edit mode
			menu.findItem(R.id.cancel_new).setVisible(false);
			menu.findItem(R.id.cancel_existing).setVisible(true);
			menu.findItem(R.id.delete).setVisible(true);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.save:
			if(saveLogEntry()) {
				saveOnFinish = false;
				finish();
			}
			break;
		case R.id.cancel_new:
		case R.id.cancel_existing:
			saveOnFinish = false;
			finish();
			return true;
		case R.id.delete:
			deleteLog();
			return true;
		case R.id.settings:
			startActivity(new Intent(this, Settings.class));
			return true;
		case R.id.help:
			showDialog(DIALOG_HELP);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.save:
			if (saveLogEntry()) { // if save works, quit
				saveOnFinish = false;
				finish();
			}
			break;
		case R.id.cancel:
			saveOnFinish = false;
			finish();
			break;
		case R.id.editDate:
			showDialog(DIALOG_DATE);
			break;
		case R.id.editTime:
			showDialog(DIALOG_TIME);
			break;
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Date date = mLogEntry.getLogDate();
		switch(id) {
		case DIALOG_DATE:
			return new DatePickerDialog(this, this, date.getYear()+1900, date.getMonth(), date.getDate());
		case DIALOG_TIME:
			return new ExtendedTimePickerDialog(this, date.getHours(), date.getMinutes(), date.getSeconds());
		case DIALOG_HELP:
			return Util.getHtmlDialogBuilder(this, R.string.log_dialog_help_html)
				.setTitle(R.string.log_dialog_help_title)
				.create();
		default:
		return super.onCreateDialog(id);
		}
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id) {
		case DIALOG_TIME:
			break;
		}
		super.onPrepareDialog(id, dialog);
		
	}

	@Override
	protected void onPause() {
		if(isFinishing() && saveOnFinish ) {
			// if we are finishing and need to save (e.g. back button), do so
			saveLogEntry();
			// preserve any changes in mLogEntry
			
		}
		super.onPause();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// preserve any changes in mLogEntry
		outState.putSerializable(C.db_LOG_TABLE, mLogEntry);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mLogEntry = (LogEntry) savedInstanceState.getSerializable(C.db_LOG_TABLE);
	}

	private void deleteLog() {
		final long id = mLogEntry.id;
		Log.d(TAG, "deleteLog " + id);
		LogDeleteDialog.create(this, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "DeleteDialog onClick " + id);
				mDba.deleteLog(id, mTracker.id);
				saveOnFinish = false;
				((LogMyLife) getApplication()).showToast(C.TOAST_LOG_DELETED);
				finish();
			}
		}).show();
	}

	// creates/updates the log entry
	private boolean saveLogEntry() {
		mLogEntry.setBody(metBody.getText().toString());
		String value = metValue.getText().toString();
		mLogEntry.setValue(value.equals("") ? null : Integer.parseInt(value));
		if (mLogEntry.isNew()) {
			mDba.createLog(mLogEntry);
			((LogMyLife) getApplication()).showToast(C.TOAST_LOG_CREATED);
		} else {
			mDba.updateLog(mLogEntry);
			((LogMyLife) getApplication()).showToast(C.TOAST_LOG_UPDATED);
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		mDba.close();
		super.onDestroy();
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		Date d = mLogEntry.getLogDate();
		d.setYear(year - 1900);
		d.setMonth(monthOfYear);
		d.setDate(dayOfMonth);
		mLogEntry.setLogDate(d); // so that it's recorded as having changed
		setDateTimeDisplay();
	}

	class ExtendedTimePickerDialog extends AlertDialog implements
			OnChangedListener, OnClickListener {

		private static final String HOUR = "ETPDhour";
		private static final String MINUTE = "ETPDminute";
		private static final String SECOND = "ETPDsecond";
		private int mHour;
		private int mMinute;
		private int mSecond;
		private NumberPicker mHourPicker;
		private NumberPicker mMinutePicker;
		private NumberPicker mSecondPicker;

		public ExtendedTimePickerDialog(Context context, int hourOfDay,
				int minute, int second) {
			super(context);
			mHour = hourOfDay;
			mMinute = minute;
			mSecond = second;

			updateTitle();

			setButton(context.getText(android.R.string.ok), this);
			setButton2(context.getText(android.R.string.cancel),
					(OnClickListener) null);
			setIcon(android.R.drawable.ic_dialog_info);

			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.d_timepicker, null);
			setView(view);

			// digits of timepicker hours
			mHourPicker = (NumberPicker) view.findViewById(R.id.hours);
			mHourPicker.setRange(0, 23);
			mHourPicker.setSpeed(100);
			mHourPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
			mHourPicker.setCurrent(mHour);
			mHourPicker.setOnChangeListener(this);

			// digits of timepicker minutes
			mMinutePicker = (NumberPicker) view.findViewById(R.id.minutes);
			mMinutePicker.setRange(0, 59);
			mMinutePicker.setSpeed(100);
			mMinutePicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
			mMinutePicker.setCurrent(mMinute);
			mMinutePicker.setOnChangeListener(this);

			// digits of timepicker seconds
			mSecondPicker = (NumberPicker) view.findViewById(R.id.seconds);
			mSecondPicker.setRange(0, 59);
			mSecondPicker.setSpeed(100);
			mSecondPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
			mSecondPicker.setCurrent(mSecond);
			mSecondPicker.setOnChangeListener(this);
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			Date d = mLogEntry.getLogDate();
			d.setHours(mHour);
			d.setMinutes(mMinute);
			d.setSeconds(mSecond);
			mLogEntry.setLogDate(d);
			setDateTimeDisplay();
		}

		private void updateTitle() {
			Date d = new Date();
			d.setHours(mHour);
			d.setMinutes(mMinute);
			d.setSeconds(mSecond);
			setTitle(mTimeFormat.format(d));
		}

		@Override
		public Bundle onSaveInstanceState() {
			Bundle state = super.onSaveInstanceState();
			state.putInt(HOUR, mHour);
			state.putInt(MINUTE, mMinute);
			state.putInt(SECOND, mSecond);
			return state;
		}

		@Override
		public void onRestoreInstanceState(Bundle savedInstanceState) {
			super.onRestoreInstanceState(savedInstanceState);
			mHourPicker.setCurrent(mHour = savedInstanceState.getInt(HOUR));
			mMinutePicker.setCurrent(mMinute = savedInstanceState.getInt(MINUTE));
			mSecondPicker.setCurrent(mSecond = savedInstanceState.getInt(SECOND));
			updateTitle();
		}

		@Override
		public void onChanged(NumberPicker picker, int oldVal, int newVal) {
			switch (picker.getId()) {
			case R.id.hours:
				mHour = picker.getCurrent();
				break;
			case R.id.minutes:
				mMinute = picker.getCurrent();
				break;
			case R.id.seconds:
				mSecond = picker.getCurrent();
				break;
			}
			updateTitle();
		}
	}
}
