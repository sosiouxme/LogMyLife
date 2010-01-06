package net.sosiouxme.WhenDidI.activity;

import net.sosiouxme.WhenDidI.C;
import net.sosiouxme.WhenDidI.DbAdapter;
import net.sosiouxme.WhenDidI.R;
import net.sosiouxme.WhenDidI.custom.EventCursorAdapter;
import net.sosiouxme.WhenDidI.dialog.TrackerDeleteDialog;
import net.sosiouxme.WhenDidI.model.Tracker;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class TrackerDetail extends ListActivity implements OnItemClickListener {
	// Logger tag
	private static final String TAG = "WDI.TrackerDetail";

	// the Item being handled
	private Tracker mTracker = null;

	private DbAdapter mDba;
	TextView mName;
	TextView mBody;

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
			fillLogList();
		}

		super.onResume();		
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
		
		// set up self as listener for when user clicks item
		this.getListView().setOnItemClickListener(this);
		// enable context menu for list items
		registerForContextMenu(getListView());
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
			//TODO
			break;
		case R.id.done:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void deleteTracker() {
		final long id = mTracker.id;
		Log.d(TAG, "deleteItem " + id);
		TrackerDeleteDialog.create(this, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "DeleteDialog onClick " + id);
				mDba.deleteTracker(id);
				finish();
			}
		}).show();
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onDestroy() {
		mDba.close();
		super.onDestroy();
	}
}
