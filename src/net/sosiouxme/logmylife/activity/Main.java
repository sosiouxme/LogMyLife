package net.sosiouxme.logmylife.activity;

import java.util.Date;

import net.sosiouxme.logmylife.C;
import net.sosiouxme.logmylife.LogMyLife;
import net.sosiouxme.logmylife.R;
import net.sosiouxme.logmylife.Util;
import net.sosiouxme.logmylife.custom.EventCursorAdapter;
import net.sosiouxme.logmylife.custom.GroupSpinner;
import net.sosiouxme.logmylife.custom.GroupSpinner.OnGroupSelectedListener;
import net.sosiouxme.logmylife.dialog.TrackerDeleteDialog;
import net.sosiouxme.logmylife.domain.DbAdapter;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

/**
Activity that presents the UI for the application's main screen, which 
displays all trackers for the currently selected group.

@author Luke Meyer, Copyright 2010
See LICENSE file for this file's GPLv3 distribution license.
*/

public class Main extends ListActivity implements OnItemClickListener, OnGroupSelectedListener, FilterQueryProvider, android.view.View.OnClickListener {
	private static final String TAG = "LML.Main";
	private static final int DIALOG_ABOUT = 0;
	private static final int DIALOG_CHANGELOG = 1;
	private static final int DIALOG_GROUP_INFO = 2;
	private DbAdapter mDba;
	private GroupSpinner mSpinner = null;
	private long mCurrentGroupId = 0;
	private EventCursorAdapter mAdapter;
	// TODO: writing my own multithreading code was all well and good,
	// but there's a better way: http://developer.android.com/resources/articles/timed-ui-updates.html
	private final Handler mUiHandler = new Handler();
	private Thread mUpdateThread;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_main);
		getListView().addHeaderView(getLayoutInflater().inflate(R.layout.a_main_new, null));

		findViewById(R.id.group_info).setOnClickListener(this);
		
		mDba = new DbAdapter(this);
		fillGroupSpinner();
		fillTrackerList();
		
		
		if(((LogMyLife) getApplication()).getFirstTime())
			showDialog(DIALOG_ABOUT);
		if(((LogMyLife) getApplication()).getShowChangedDialog())
			showDialog(DIALOG_CHANGELOG);
	}

	/* get the cursor with all lists and attach to the spinner */
	private void fillGroupSpinner() {
		Log.d(TAG, "fillGroupSpinner");
		GroupSpinner spinner = new GroupSpinner(this,
				(Spinner) findViewById(R.id.group_list_spinner), mDba);
		mCurrentGroupId = spinner.getSelectedItemId();
		spinner.setOnGroupSelectedListener(this);
		mSpinner = spinner;
	}

	/* attach cursor for the items in the current list to the listview */
	private void fillTrackerList() {
		Log.d(TAG, "fillTrackerList");
		// TODO: deal with situation where all groups were deleted

		Cursor cur = mDba.fetchTrackers(mCurrentGroupId, null);
		startManagingCursor(cur);
		mAdapter = new EventCursorAdapter(this,
				R.layout.a_main_row,
				cur, // Give the cursor to the list adapter
				new String[] { C.db_TRACKER_NAME, C.db_LOG_TIME, C.db_LOG_BODY },
				new int[] { R.id.name, R.id.lastLog, R.id.logBody });
		mAdapter.setFilterQueryProvider(this);
		this.setListAdapter(mAdapter);

		// set up self as listener for when user clicks item
		this.getListView().setOnItemClickListener(this);
		// enable context menu for list items
		registerForContextMenu(getListView());
	}

	/* FilterQueryProvider interface - provide a cursor for filtered results */
	public Cursor runQuery(CharSequence constraint) {
		Cursor cur = mDba.fetchTrackers(mCurrentGroupId, constraint.toString());
		startManagingCursor(cur);
		return cur;
	}

	@Override
	public void onGroupSelected(long groupId) {
		if(mCurrentGroupId != groupId) {
			mCurrentGroupId = groupId;
			fillTrackerList();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onMenuItemSelected");
		switch (item.getItemId()) {
		case R.id.new_tracker:
			startActivity(new Intent(this, TrackerEdit.class));
			return true;
		case R.id.about:
			showDialog(DIALOG_ABOUT);
			return true;
		case R.id.manage_groups:
			startActivity(new Intent(this, GroupsEdit.class));
			return true;
		case R.id.settings:
			startActivity(new Intent(this, Prefs.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Log.d(TAG, "onCreateDialog");
		switch (id) {
		case DIALOG_ABOUT:
		case DIALOG_CHANGELOG:
			return Util.getHtmlDialogBuilder(this, R.string.main_dialog_info_asset)
				.setTitle(R.string.main_dialog_info_title)
				.setPositiveButton(R.string.info_dialog_dismiss_button, null)
				.create();
		case DIALOG_GROUP_INFO:
			return Util.getHtmlDialogBuilder(this, R.string.main_dialog_group_asset)
				.setTitle(R.string.main_dialog_group_title)
				.setPositiveButton(R.string.info_dialog_dismiss_button, null)
				.create();
		}
		return null;
	}



	public void onItemClick(AdapterView<?> parent, View v, int position,
			long itemId) {
		Log.d(TAG, "onItemClick " + itemId);
		if (v.getId() == R.id.add_new_tracker) {
			startActivity(new Intent(this, TrackerEdit.class));
		} else {
			Intent intent = new Intent(this, TrackerDetail.class);
			intent.setAction(Intent.ACTION_EDIT);
			intent.putExtra(C.db_TRACKER_GROUP, mCurrentGroupId);
			intent.putExtra(C.db_ID, itemId);
			startActivity(intent);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Log.d(TAG, "onCreateContextMenu");
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.main_context, menu);
	}

	public boolean onContextItemSelected(MenuItem item) {
		Log.d(TAG, "onContextItemSelected");
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		long rowId = info.id;
		switch (item.getItemId()) {
		case R.id.quicklog:
			quickLog(rowId);
			return true;
		case R.id.detailed_log:
			startActivity(new Intent(this, LogEdit.class).putExtra(C.db_LOG_TRACKER, rowId));
			return true;
		case R.id.delete:
			showDeleteDialog(rowId);
			return true;
		case R.id.view:
			startActivity(new Intent(this, TrackerDetail.class).putExtra(C.db_ID, rowId));
			return true;
		case R.id.edit:
			startActivity(new Intent(this, TrackerEdit.class).putExtra(C.db_ID, rowId));
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.group_info:
			showDialog(DIALOG_GROUP_INFO);
			break;
		}
	}

	private void quickLog(long rowId) {
		mDba.createLog(rowId);
		((LogMyLife) getApplication()).showToast(C.TOAST_LOG_CREATED);
		mAdapter.requery();


	}

	private void showDeleteDialog(final long itemId) {
		Log.d(TAG, "showDeleteDialog");
		TrackerDeleteDialog.create(this, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "DeleteDialog onClick " + itemId);
				mDba.deleteTracker(itemId);
				((LogMyLife) getApplication()).showToast(C.TOAST_TRACKER_DELETED);
				mAdapter.requery();
			}
		}).show();
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		mSpinner.notifyDataSetChanged();
		startUpdateThread();
	}
	
	@Override
	protected void onPause() {
		stopUpdateThread();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		mDba.close();
		super.onDestroy();
	}
	
	public void updateLastLogTimes() {
//		Log.d(TAG, "updateLastLogTimes");
		ListView list = getListView();
		int children = list.getChildCount();
		Date now = new Date();
		for (int i = 0; i < children; i++) {
			View v = list.getChildAt(i);
			if (v == null)
				continue;
			TextView tv = (TextView) v.findViewById(R.id.lastLog);
			if (tv == null)
				continue;
			Date date = (Date) tv.getTag();
			if (date == null)
				continue;
			String newText = Util.getTimeSince(date, now);
			if(newText != tv.getText())
				tv.setText(newText);
		}
	}

	public void startUpdateThread() {
		if (mUpdateThread != null)
			stopUpdateThread();
		mUpdateThread = new Thread() {
			// This is the bit that is passed to the UI thread to run
			private final Runnable update = new Runnable() {
				public void run() {
					updateLastLogTimes();
				}
			};

			// This is the timekeeper for this background thread
			public void run() {
				try {
					while (true) {
						Thread.sleep(1000); // 1 sec
						mUiHandler.post(update);
					}
				} catch (InterruptedException e) {
//					Log.d(TAG, "interrupting update thread");
				}
			}

		};

		mUpdateThread.start();
	}
	
	public void stopUpdateThread() {
//		Log.d(TAG, "stopUpdateThread");
		if(mUpdateThread != null) {
//			Log.d(TAG, "trying to interrupt update thread");
			mUpdateThread.interrupt();
			mUpdateThread = null;
		}
	}
	
}
