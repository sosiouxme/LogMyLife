package net.sosiouxme.logmylife.activity;

import net.sosiouxme.logmylife.C;
import net.sosiouxme.logmylife.LogMyLife;
import net.sosiouxme.logmylife.R;
import net.sosiouxme.logmylife.custom.AlarmEditActivity;
import net.sosiouxme.logmylife.custom.GroupSpinner;
import net.sosiouxme.logmylife.custom.RequireTextFor;
import net.sosiouxme.logmylife.custom.GroupSpinner.OnGroupSelectedListener;
import net.sosiouxme.logmylife.dialog.TrackerDeleteDialog;
import net.sosiouxme.logmylife.domain.dto.Tracker;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

/**
Activity that presents the UI to edit a single tracker.

@author Luke Meyer, Copyright 2010
See LICENSE file for this file's GPLv3 distribution license.
*/

public class TrackerEdit extends AlarmEditActivity implements android.view.View.OnClickListener, OnGroupSelectedListener {

	private static final String TAG = "LML.TrackerEdit";
	/** reference to the text edit field holding the tracker name */
	private EditText metName;
	/** reference to the text edit field holding the tracker body */
	private EditText metBody;
	/** reference to the group spinner so group can be changed for this tracker */
	private GroupSpinner mSpinner;
	/** current group for this tracker (shown in spinner) */
	private long mCurrentGroupId = 0;
	/** set if changes made; back button will save any changes by default */
	private boolean mSaveOnFinish = true;

/* *********************** lifecycle methods ************************ */	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		long trackerId = 0;
		Bundle e = getIntent().getExtras();
		if(e!= null) trackerId = e.getLong(C.db_ID);
		
		if(trackerId > 0) {
			// get item info
			mTracker = mDba.fetchTracker(trackerId);
			if(mTracker==null) {
				finish(); // nothing to do...
				return;
			}
		} else {
			mTracker = new Tracker(-1);
		}
		
		// Set the layout for this activity.
		setContentView(R.layout.a_tracker_edit);
		
		// locate and fill the necessary elements of the layout
		metName = (EditText) findViewById(R.id.name);
		metBody = (EditText) findViewById(R.id.body);

		if(!mTracker.isNew()) {			
			// set the data in the views
			metName.setText(mTracker.name);
			metBody.setText(mTracker.body);
		}
		
		// wire up the buttons
		Button cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
		Button okButton = (Button) findViewById(R.id.ok);
		metName.addTextChangedListener(new RequireTextFor(okButton, metName));
		okButton.setOnClickListener(this);
		
		initAlarmContainer();
		populateAlarms();
		fillGroupSpinner();
	}
	
	/* get the cursor with all lists and attach to the spinner */
	private void fillGroupSpinner() {
		Log.d(TAG, "fillGroupSpinner");
		mSpinner = new GroupSpinner(this,
				(Spinner) findViewById(R.id.group_spinner), mDba);
		mSpinner.setOnGroupSelectedListener(this);
		mCurrentGroupId = mSpinner.getSelectedItemId();
	}
	
	@Override
	public void onGroupSelected(long groupId) {
		mCurrentGroupId = groupId;		
	}

	@Override
	protected void onPause() {
		if(isFinishing() && mSaveOnFinish ) {
			// if we are finishing and need to save (e.g. back button), do so
			saveTracker();
		}
		super.onPause();
	}

	
/* ************************ event handling ******************************* */	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.tracker_edit, menu);
		if(!mTracker.isNew()) {
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
			if(saveTracker()) {
				mSaveOnFinish = false;
				finish();
			}
			break;
		case R.id.cancel_new:
		case R.id.cancel_existing:
			mSaveOnFinish = false;
			finish();
			break;
		case R.id.delete:
			deleteTracker();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ok:
			if (saveTracker()) {
				mSaveOnFinish = false;
				finish();
			}
			break;
		case R.id.cancel:
			mSaveOnFinish = false;
			finish();
			break;
		default:
			super.onClick(v);
		}
	}
	
/* **************************** worker methods ************************** */	
	
	private void deleteTracker() {
		final long id = mTracker.id;
		TrackerDeleteDialog.create(this, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "DeleteDialog onClick " + id);
				mDba.deleteTracker(id);
				mSaveOnFinish = false;
				((LogMyLife) getApplication()).showToast(C.TOAST_TRACKER_DELETED);
				finish();
			}
		}).show();
	}

	// creates/updates the tracker UNLESS the name is empty
	private boolean saveTracker() {
		String name = metName.getText().toString();
		long trackerId = -1;
		if (name.length() > 0) {
			mTracker.setName(name);
			mTracker.setBody(metBody.getText().toString());
			mTracker.setGroupId(mCurrentGroupId);
			mTracker.setSkipNextAlarm(false);
			if(mTracker.isNew()) {
				trackerId = mDba.createTracker(mTracker);
				((LogMyLife) getApplication()).showToast(C.TOAST_TRACKER_CREATED);
			} else {
				trackerId = mTracker.getId();
				mDba.updateTracker(mTracker);
				((LogMyLife) getApplication()).showToast(C.TOAST_TRACKER_UPDATED);
			}
			storeAlarms(trackerId);
		}
		return trackerId > -1;
	}
	
	@Override
	protected void onDestroy() {
		mDba.close();
		super.onDestroy();
	}
}
