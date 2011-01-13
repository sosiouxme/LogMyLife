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

package net.sosiouxme.logmylife.activity;

import net.sosiouxme.logmylife.C;
import net.sosiouxme.logmylife.LogMyLife;
import net.sosiouxme.logmylife.R;
import net.sosiouxme.logmylife.Util;
import net.sosiouxme.logmylife.custom.AlertEditActivity;
import net.sosiouxme.logmylife.custom.GroupSpinner;
import net.sosiouxme.logmylife.custom.RequireTextFor;
import net.sosiouxme.logmylife.custom.GroupSpinner.OnGroupSelectedListener;
import net.sosiouxme.logmylife.dialog.TrackerDeleteDialog;
import net.sosiouxme.logmylife.domain.dto.Tracker;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;

/**
Activity that presents the UI to edit a single tracker.

@author Luke Meyer, Copyright 2010
See LICENSE file for this file's GPLv3 distribution license.
*/

public class TrackerEdit extends AlertEditActivity implements android.view.View.OnClickListener, OnGroupSelectedListener, OnItemSelectedListener {

	private static final String TAG = "LML.TrackerEdit";
	private static final int DIALOG_EDIT_HELP = 10;
	private static final int DIALOG_CREATE_HELP = 11;
	/** reference to the text edit field holding the tracker name */
	private EditText metName;
	/** reference to the text edit field holding the tracker body */
	private EditText metBody;
	/** reference to the text edit field holding the log value label */
	private EditText metValLabel;
	/** reference to the group spinner so group can be changed for this tracker */
	private GroupSpinner mSpinner;
	/** current group for this tracker (shown in spinner) */
	private long mCurrentGroupId = 0;
	/** set if changes made; back button will save any changes by default */
	private boolean mSaveOnFinish = true;
	private CheckBox metUseVal;
	private ViewGroup metValueContainer;
	private Spinner metValueType;
	private ToggleButton metValLabelPos;

/* *********************** lifecycle methods ************************ */	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		long trackerId = 0;
		Bundle x = getIntent().getExtras();
		if(x != null) trackerId = x.getLong(C.db_ID);
		
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
		metUseVal = (CheckBox) findViewById(R.id.useValue);
		metValueContainer = (ViewGroup) findViewById(R.id.valueContainer);
		metValueType = (Spinner) findViewById(R.id.valueType);
		metValLabel = (EditText) findViewById(R.id.logValueLabel);
		metValLabelPos = (ToggleButton) findViewById(R.id.logValLabelPos);
		
		if(!mTracker.isNew()) {			
			// set the data in the views
			metName.setText(mTracker.getName());
			metBody.setText(mTracker.getBody());
			metValLabel.setText(mTracker.getLogValueLabel());
			metValLabelPos.setChecked(mTracker.getLogValueLabelPos() == Tracker.LABEL_RIGHT);
		}
		metUseVal.setChecked(mTracker.getLogUseValue());
		metValueContainer.setVisibility(metUseVal.isChecked() ? View.VISIBLE : View.GONE);
		metValueType.setSelection((int) mTracker.getLogValueType());
		
		// wire up the buttons
		Button cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
		Button okButton = (Button) findViewById(R.id.ok);
		metName.addTextChangedListener(new RequireTextFor(okButton, metName));
		okButton.setOnClickListener(this);
		metUseVal.setOnCheckedChangeListener(this);
		metValueType.setOnItemSelectedListener(this);
		findViewById(R.id.info_group).setOnClickListener(this);
		findViewById(R.id.info_alert).setOnClickListener(this);
		findViewById(R.id.info_value).setOnClickListener(this);
		
		initAlertContainer();
		populateAlerts();
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
		case R.id.settings:
			startActivity(new Intent(this, Settings.class));
			return true;
		case R.id.help:
			showDialog(mTracker.isNew() ? DIALOG_CREATE_HELP : DIALOG_EDIT_HELP);
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onCheckedChanged(CompoundButton enabled, boolean checked) {
		Log.d(TAG, "onCheckedChanged");
		if(enabled == metUseVal) {
			metValueContainer.setVisibility(checked ? View.VISIBLE : View.GONE);
			mTracker.setLogUseValue(checked);
		} else
			super.onCheckedChanged(enabled, checked);
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
		case R.id.info_group:
			showDialog(DIALOG_INFO_GROUP);
			break;
		case R.id.info_value:
			showDialog(DIALOG_INFO_VALUE);
			break;
		case R.id.info_alert:
			showDialog(DIALOG_INFO_ALERT);
			break;
		default:
			super.onClick(v);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_CREATE_HELP:
			return Util.getHtmlDialogBuilder(this, R.string.te_dialog_create_html)
				.setTitle(R.string.te_dialog_create_title)
				.setPositiveButton(R.string.info_dialog_dismiss_button, null)
				.create();
		case DIALOG_EDIT_HELP:
			return Util.getHtmlDialogBuilder(this, R.string.te_dialog_edit_html)
			.setTitle(R.string.te_dialog_edit_title)
			.setPositiveButton(R.string.info_dialog_dismiss_button, null)
			.create();
		}
		return super.onCreateDialog(id);
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
			mTracker.setLogValueLabel(metValLabel.getText().toString());
			mTracker.setLogValueLabelPos(metValLabelPos.isChecked() ? Tracker.LABEL_RIGHT : Tracker.LABEL_LEFT);
			mTracker.setGroupId(mCurrentGroupId);
			mTracker.setSkipNextAlert(false);
			if(mTracker.isNew()) {
				trackerId = mDba.createTracker(mTracker);
				((LogMyLife) getApplication()).showToast(C.TOAST_TRACKER_CREATED);
			} else {
				trackerId = mTracker.getId();
				mDba.updateTracker(mTracker);
				((LogMyLife) getApplication()).showToast(C.TOAST_TRACKER_UPDATED);
			}
			storeAlerts(trackerId);
		}
		return trackerId > -1;
	}
	
	@Override
	protected void onDestroy() {
		mDba.close();
		super.onDestroy();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		mTracker.setLogValueType(position);		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
}
