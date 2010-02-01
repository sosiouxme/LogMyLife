package net.sosiouxme.WhenDidI.activity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sosiouxme.WhenDidI.C;
import net.sosiouxme.WhenDidI.DbAdapter;
import net.sosiouxme.WhenDidI.R;
import net.sosiouxme.WhenDidI.custom.EventCursorAdapter;
import net.sosiouxme.WhenDidI.dialog.LogDeleteDialog;
import net.sosiouxme.WhenDidI.dialog.TrackerDeleteDialog;
import net.sosiouxme.WhenDidI.model.dto.Tracker;
import android.app.ListActivity;
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
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class TrackerDetail extends ListActivity implements  android.view.View.OnClickListener {
	// Logger tag
	private static final String TAG = "WDI.TrackerDetail";

	// the Item being handled
	private Tracker mTracker = null;

	private DbAdapter mDba;
	TextView mName;
	TextView mBody;
	
	// repository for created toasts
	private Map<Long,Toast> mToasts = new HashMap<Long,Toast>();
	private static final Long TOAST_LOG_DELETED = new Long(R.string.log_entry_deleted);
	private static final Long TOAST_LOG_CREATED = new Long(R.string.new_log_entry);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get a DB handle
		mDba = new DbAdapter(this).open();

		// Set the layout for this activity.
		setContentView(R.layout.a_tracker_detail);
		View header = getLayoutInflater().inflate(R.layout.a_tracker_detail_head, null);
		header.setFocusable(false);
		header.setClickable(false);
		getListView().addHeaderView(header, null, true);

		// locate the necessary elements of the layout
		mName = (TextView) findViewById(R.id.name);
		mBody = (TextView) findViewById(R.id.body);
		
		// set up the log buttons to do what i want
		findViewById(R.id.quick_log).setOnClickListener(this);
		findViewById(R.id.detailed_log).setOnClickListener(this);

	}
	
	@Override
	protected void onResume() {
		// get (or refresh) item info
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
		}

		super.onResume();		
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
				R.layout.a_tracker_detail_row,
				cur, // Give the cursor to the list adapter
				new String[] { C.db_LOG_TIME, C.db_LOG_BODY },
				new int[] { R.id.logTime, R.id.ivel_logBody });
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
		case R.id.new_log:
			newLogEntry();
			break;
		case R.id.done:
			finish();
			return true;
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
		getMenuInflater().inflate(R.menu.tracker_detail_context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
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
		return super.onContextItemSelected(item);
	}

	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.quick_log:
			createQuickLog();
			break;
		case R.id.detailed_log:
			newLogEntry();
			break;
		}
	}

	protected void showToast(Long id) {
		Toast t = mToasts.get(id);
		if(t==null)
			mToasts.put(id, (t = Toast.makeText(this, id.intValue(), Toast.LENGTH_SHORT)));
		t.show();
	}

	private void deleteTracker() {
		final long id = mTracker.id;
		Log.d(TAG, "deleteTracker " + id);
		TrackerDeleteDialog.create(this, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "TDeleteDialog onClick " + id);
				mDba.deleteTracker(id);
				finish();
			}
		}).show();
	}
	
	private void createQuickLog() {
		mDba.createLog(mTracker.id, new Date(), null);
		requeryList();
		showToast(TOAST_LOG_CREATED);
	}

	private void newLogEntry() {
		Intent i = new Intent(this, LogEdit.class)
			.putExtra(C.db_LOG_TRACKER, mTracker.id);
		startActivity(i);
	}

	private void editLogEntry(long rowId) {
		Intent i = new Intent(this, LogEdit.class)
			.putExtra(C.db_LOG_TRACKER, mTracker.id)
			.putExtra(C.db_ID, rowId);
		startActivity(i);
	}

	private void deleteLog(final long logId) {
		final long trackerId = mTracker.id;
		Log.d(TAG, "deleteLog " + logId);
		LogDeleteDialog.create(this, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mDba.deleteLog(logId, trackerId);
				requeryList();
				showToast(TOAST_LOG_DELETED);
			}
		}).show();
	}
}