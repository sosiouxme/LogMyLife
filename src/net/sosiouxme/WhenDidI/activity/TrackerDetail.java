package net.sosiouxme.WhenDidI.activity;

import net.sosiouxme.WhenDidI.C;
import net.sosiouxme.WhenDidI.DbAdapter;
import net.sosiouxme.WhenDidI.R;
import net.sosiouxme.WhenDidI.custom.EventCursorAdapter;
import android.app.ListActivity;
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
	private static final String TAG = "WDI.TrackerViewEdit";

	// the Item being handled
	private long mTrackerId = 0;
	private long mGroupId = 0;
	private String mTitle = null;
	private String mBody = null;
	private TextView mtvTitle;
	private TextView mtvBody;
	//private EditText metTitle;
	//private EditText metBody;

	private DbAdapter mDba;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get item info
		mTrackerId = getIntent().getExtras().getLong(C.db_ID);

		// get a DB handle
		mDba = new DbAdapter(this).open();

		// Set the layout for this activity.
		setContentView(R.layout.a_tracker_view_edit);
		View header = getLayoutInflater().inflate(R.layout.a_tracker_view_edit_details, null);
		getListView().addHeaderView(header);

		// retrieve the data for the item
		Cursor c = null;
		try {
			c = mDba.fetchTracker(mTrackerId);
			c.moveToFirst();
			mTitle = c.getString(c.getColumnIndex(C.db_TRACKER_NAME));
			mBody = c.getString(c.getColumnIndex(C.db_TRACKER_BODY));
			mGroupId = c.getLong(c.getColumnIndex(C.db_TRACKER_GROUP));
		} finally {
			if (c != null) {
				c.close();
			}
		}

		// locate and fill the necessary elements of the layout
		mtvTitle = (TextView) findViewById(R.id.ive_item_title);
		mtvBody = (TextView) findViewById(R.id.ive_item_body);
		//metTitle = (EditText) findViewById(R.id.ive_item_title_edit);
		//metBody = (EditText) findViewById(R.id.ive_item_body_edit);
		// set the data in the views
		mtvTitle.setText(mTitle);
		//metTitle.setText(mTitle);
		mtvBody.setText(mBody);
		//metBody.setText(mBody);
		
		fillLogList();
	}

	private void fillLogList(){
		Log.d(TAG, "fillLogList");
		Cursor cur = mDba.fetchLogs(mTrackerId);
		startManagingCursor(cur);
		EventCursorAdapter adapter = new EventCursorAdapter(this,
				R.layout.a_tracker_view_edit_logrow,
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
		getMenuInflater().inflate(R.menu.tracker_edit, menu);
		// menu is inflated for MODE_INSERT; modify if MODE_EDIT
			menu.findItem(R.id.cancel_new).setVisible(false);
			menu.findItem(R.id.cancel_existing).setVisible(true);
			menu.findItem(R.id.delete).setVisible(true);
		return true;
	}
	


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.save:
			saveItem();
			return true;
		case R.id.cancel_new:
		case R.id.cancel_existing:
			finish();
			return true;
		case R.id.delete:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void saveItem() {
		//mTitle = metTitle.getText().toString();
		//mBody = metBody.getText().toString();
		mtvTitle.setText(mTitle);
		mtvBody.setText(mBody);

		mDba.updateTracker(mTrackerId, mTitle, mBody);
		finish();
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
