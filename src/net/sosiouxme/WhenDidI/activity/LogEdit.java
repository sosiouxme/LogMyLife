package net.sosiouxme.WhenDidI.activity;

import java.util.Calendar;
import java.util.Date;

import net.sosiouxme.WhenDidI.C;
import net.sosiouxme.WhenDidI.R;
import net.sosiouxme.WhenDidI.WhenDidI;
import net.sosiouxme.WhenDidI.custom.NumberPicker;
import net.sosiouxme.WhenDidI.dialog.LogDeleteDialog;
import net.sosiouxme.WhenDidI.domain.DbAdapter;
import net.sosiouxme.WhenDidI.domain.dto.LogEntry;
import net.sosiouxme.WhenDidI.domain.dto.Tracker;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

/**
Activity that presents the UI for editing a single LogEntry 

@author Luke Meyer, Copyright 2010
See LICENSE file for this file's GPLv3 distribution license.
*/

public class LogEdit extends Activity implements android.view.View.OnClickListener {

	private static final String TAG = "WDI.LogEdit";
	private LogEntry mLogEntry = null;
	private Tracker mTracker = null;
	private DbAdapter mDba = null;
	private EditText metBody;
	private DatePicker mdpDate;
	private TimePicker mtpTime;
	private boolean saveOnFinish = true; // by default, back button will save
	private NumberPicker mSecondPicker;

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
		}
		// get the tracker to log against (whether new or existing log) 
		mTracker = mDba.fetchTracker(trackerId);
		if (mTracker == null)
			throw new RuntimeException("couldn't find tracker id " + trackerId);

		// Set the layout for this activity.
		setContentView(R.layout.a_log_edit);
		
		// locate and fill the necessary elements of the layout
		TextView tvName = (TextView) findViewById(R.id.tracker_name);
		TextView tvBody = (TextView) findViewById(R.id.tracker_details);
		metBody = (EditText) findViewById(R.id.body);
		mdpDate = (DatePicker) findViewById(R.id.log_date);
		mtpTime = (TimePicker) findViewById(R.id.log_time);
        mSecondPicker = (NumberPicker) findViewById(R.id.log_time_seconds);
		
		tvName.setText(mTracker.name);
		tvBody.setText(mTracker.body);
        // digits of timepicker seconds (custom addition)
        mSecondPicker.setRange(0, 59);
        mSecondPicker.setSpeed(100);
        mSecondPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
		
		if(mLogEntry != null) {			
			// set the data in the views
			metBody.setText(mLogEntry.body);
			Date d = mLogEntry.logDate;
			mdpDate.updateDate(d.getYear() + 1900, d.getMonth(), d.getDate());
			mtpTime.setCurrentHour(d.getHours());
			mtpTime.setCurrentMinute(d.getMinutes());
	        mSecondPicker.setCurrent(d.getSeconds());
		}
		else {
			final Calendar c = Calendar.getInstance();
			mdpDate.updateDate(
	         c.get(Calendar.YEAR),
	         c.get(Calendar.MONTH),
	         c.get(Calendar.DAY_OF_MONTH)
	         );
			mtpTime.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
			mtpTime.setCurrentMinute(c.get(Calendar.MINUTE));
	        mSecondPicker.setCurrent(c.get(Calendar.SECOND));
		}
		mtpTime.setIs24HourView(true);
		
		// wire up the buttons
		Button cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
		Button saveButton = (Button) findViewById(R.id.save);
		saveButton.setOnClickListener(this);
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
		}
		return super.onOptionsItemSelected(item);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.save:
			if (saveLogEntry()) {
				saveOnFinish = false;
				finish();
			}
			break;
		case R.id.cancel:
			saveOnFinish = false;
			finish();
			break;
		}
	}
	
	

	@Override
	protected void onPause() {
		if(isFinishing() && saveOnFinish ) {
			// if we are finishing and need to save (e.g. back button), do so
			saveLogEntry();
		}
		super.onPause();
	}

	private void deleteLog() {
		final long id = mLogEntry.id;
		Log.d(TAG, "deleteLog " + id);
		LogDeleteDialog.create(this, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "DeleteDialog onClick " + id);
				mDba.deleteLog(id, mTracker.id);
				saveOnFinish = false;
				((WhenDidI) getApplication()).showToast(C.TOAST_LOG_DELETED);
				finish();
			}
		}).show();
	}

	// creates/updates the log entry
	private boolean saveLogEntry() {
		if (mLogEntry == null) {
			mDba.createLog(mTracker.id, getDateTime(), metBody.getText().toString());
			((WhenDidI) getApplication()).showToast(C.TOAST_LOG_CREATED);
		} else {
			mLogEntry.setBody(metBody.getText().toString());
			mLogEntry.setTrackerId(mTracker.id);
			mLogEntry.setLogDate(getDateTime());
			mDba.updateLog(mLogEntry);
			((WhenDidI) getApplication()).showToast(C.TOAST_LOG_UPDATED);
		}
		return true;
	}
	
	private Date getDateTime() {
		return new Date(
				mdpDate.getYear() - 1900,
				mdpDate.getMonth(),
				mdpDate.getDayOfMonth(),
				mtpTime.getCurrentHour(),
				mtpTime.getCurrentMinute(),
				mSecondPicker.getCurrent()
				);
	}

	@Override
	protected void onDestroy() {
		mDba.close();
		super.onDestroy();
	}


}
