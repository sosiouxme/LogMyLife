package net.sosiouxme.logmylife.activity;

import net.sosiouxme.logmylife.C;
import net.sosiouxme.logmylife.LogMyLife;
import net.sosiouxme.logmylife.R;
import net.sosiouxme.logmylife.Util;
import net.sosiouxme.logmylife.custom.AlertEditActivity;
import net.sosiouxme.logmylife.custom.EventCursorAdapter;
import net.sosiouxme.logmylife.dialog.LogDeleteDialog;
import net.sosiouxme.logmylife.dialog.TrackerDeleteDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
Activity for displaying the details of one tracker, including log entries

@author Luke Meyer, Copyright 2010
See LICENSE file for this file's GPLv3 distribution license.
*/

public class TrackerDetail extends AlertEditActivity implements  android.view.View.OnClickListener {
	// Logger tag
	private static final String TAG = "LML.TrackerDetail";

	private static final int DIALOG_INFO_LOGS = 100;

	private static final int DIALOG_HELP = 101;

	private TextView mName;
	private TextView mBody;
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the layout for this activity.
		setContentView(R.layout.a_tracker_detail);
		View header = getLayoutInflater().inflate(R.layout.a_tracker_detail_head, null);
		header.setFocusable(false);
		header.setClickable(false);
		getListView().addHeaderView(header, null, true);
		getListView().setItemsCanFocus(true);

		// locate the necessary elements of the layout
		mName = (TextView) findViewById(R.id.name);
		mBody = (TextView) findViewById(R.id.body);
		
		// set up the log buttons to do what i want
		findViewById(R.id.quick_log).setOnClickListener(this);
		findViewById(R.id.detailed_log).setOnClickListener(this);

		// as well as the info buttons
		findViewById(R.id.info_logs).setOnClickListener(this);
		findViewById(R.id.info_alert).setOnClickListener(this);

		saveAlertChangesImmediately = true;
		initAlertContainer();
	}
	
	@Override
	protected void onStart() {
		// This is in onStart rather than onResume because
		// it needs to proceed onPrepareDialog(reminder alert dialog) -
		// which requires mTracker.
		//
		// If there's ever a case where the activity's not stopped but only
		// paused, will need to duplicate this in onResume().

		Log.d(TAG, "Activity.onStart");
		refreshTracker();
		super.onStart();		
	}


	private void refreshTracker() {
		mTracker = mDba.fetchTracker(getIntent().getExtras().getLong(C.db_ID));
		if(mTracker == null){
			finish(); // nothing to show!
		} else {

			// set the data in the views
			mName.setText(mTracker.name);
			mBody.setText(mTracker.body);
			if(getListAdapter() == null)
				fillLogList();
			else
				requeryList();
			populateAlerts();
		}
	}

	@Override
	protected void onDestroy() {
		mDba.close();
		super.onDestroy();
	}

	private void fillLogList(){
		Log.d(TAG, "fillLogList");
		Cursor cur = mDba.fetchLogs(mTracker.id);
		startManagingCursor(cur);
		EventCursorAdapter adapter = new EventCursorAdapter(this,
				mTracker,
				R.layout.a_tracker_detail_row,
				cur, // Give the cursor to the list adapter
				new String[] { C.db_LOG_TIME, C.db_LOG_BODY, C.db_LOG_VALUE },
				new int[] { R.id.logTime, R.id.logBody, R.id.logValue });
		this.setListAdapter(adapter);
		
		// enable context menu for list items
		registerForContextMenu(getListView());
	}

	private void requeryList() {
		((EventCursorAdapter) getListAdapter()).requery();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.tracker_detail, menu);

		return true;
	}
	


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.edit:
			startActivity(new Intent(this, TrackerEdit.class).putExtra(C.db_ID, mTracker.id));
			break;
		case R.id.delete:
			deleteTracker();
			break;
		case R.id.new_log:
			newLogEntry();
			break;
		case R.id.done:
			finish();
			break;
		case R.id.settings:
			startActivity(new Intent(this, Settings.class));
			return true;
		case R.id.help:
			showDialog(DIALOG_HELP);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long rowId) {
		editLogEntry(rowId);
		super.onListItemClick(l, v, position, rowId);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if(menu.size() == 0) {
			getMenuInflater().inflate(R.menu.tracker_detail_context, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if(!super.onContextItemSelected(item)) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			long rowId = info.id;
			switch(item.getItemId()) {
			case R.id.delete:
				deleteLog(rowId);
				break;
			case R.id.edit:
				editLogEntry(rowId);
				break;
			}
		}
		return false;
	}

	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.quick_log:
			createQuickLog();
			break;
		case R.id.detailed_log:
			newLogEntry();
			break;
		case R.id.info_alert:
			showDialog(DIALOG_INFO_ALERT);
			break;
		case R.id.info_logs:
			showDialog(DIALOG_INFO_LOGS);
			break;
		default:
			super.onClick(v);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_INFO_LOGS:
			return Util.getHtmlDialogBuilder(this, R.string.td_dialog_logs_html)
			.setTitle(R.string.td_dialog_logs_title)
			.create();
		case DIALOG_HELP:
			return Util.getHtmlDialogBuilder(this, R.string.td_dialog_help_html)
				.setTitle(R.string.td_dialog_help_title)
				.create();
		default:
			return super.onCreateDialog(id);
		}
	}
	
	/*
	// repository for created toasts
	private Map<Long,Toast> mToasts = new HashMap<Long,Toast>();
	private static final Long TOAST_LOG_DELETED = new Long(R.string.log_entry_deleted);
	private static final Long TOAST_LOG_CREATED = new Long(R.string.new_log_entry);
	protected void showToast(Long id) {
		Toast t = mToasts.get(id);
		if(t==null)
			mToasts.put(id, (t = Toast.makeText(this, id.intValue(), Toast.LENGTH_SHORT)));
		t.show();
	}
*/
	private void deleteTracker() {
		final long id = mTracker.id;
		Log.d(TAG, "deleteTracker " + id);
		TrackerDeleteDialog.create(this, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "TDeleteDialog onClick " + id);
				mDba.deleteTracker(id);
				((LogMyLife) getApplication()).showToast(C.TOAST_TRACKER_DELETED);
				finish();
			}
		}).show();
	}
	
	private void createQuickLog() {
		mDba.createLog(mTracker.getId());
		mDba.requeryTracker(mTracker);
		((LogMyLife) getApplication()).showToast(C.TOAST_LOG_CREATED);
		requeryList();
		populateAlerts();
	}

	private void newLogEntry() {
		Intent i = new Intent(this, LogEdit.class)
			.putExtra(C.db_LOG_TRACKER, mTracker.id);
		startActivity(i);
	}

	private void editLogEntry(long rowId) {
		Intent i = new Intent(this, LogEdit.class)
			.putExtra(C.db_ID, rowId);
		startActivity(i);
	}

	private void deleteLog(final long logId) {
		final long trackerId = mTracker.id;
		Log.d(TAG, "deleteLog " + logId);
		LogDeleteDialog.create(this, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mDba.deleteLog(logId, trackerId);
				((LogMyLife) getApplication()).showToast(C.TOAST_LOG_DELETED);
				requeryList();
			}
		}).show();
	}
}
