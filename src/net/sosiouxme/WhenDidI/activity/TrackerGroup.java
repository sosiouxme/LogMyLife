package net.sosiouxme.WhenDidI.activity;

import net.sosiouxme.WhenDidI.C;
import net.sosiouxme.WhenDidI.DbAdapter;
import net.sosiouxme.WhenDidI.R;
import net.sosiouxme.WhenDidI.custom.EventCursorAdapter;
import net.sosiouxme.WhenDidI.custom.GroupSpinner;
import net.sosiouxme.WhenDidI.custom.RequiredFieldDialog;
import net.sosiouxme.WhenDidI.custom.GroupSpinner.OnGroupSelectedListener;
import net.sosiouxme.WhenDidI.dialog.TrackerDeleteDialog;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class TrackerGroup extends ListActivity implements OnItemClickListener, OnGroupSelectedListener {
	private static final String TAG = "WDI.TrackerGroup";
	private static final int DIALOG_ABOUT = 0;
	private static final int DIALOG_NEW = 1;
	private DbAdapter mDba;
	private GroupSpinner mSpinner = null;
	private long mCurrentGroupId = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_tracker_group);
		getListView().addHeaderView(getLayoutInflater().inflate(R.layout.a_tracker_group_new, null));

		mDba = new DbAdapter(this).open();
		fillGroupSpinner();
		fillTrackerList();
	}

	/* get the cursor with all lists and attach to the spinner */
	private void fillGroupSpinner() {
		Log.d(TAG, "fillGroupSpinner");
		mSpinner = new GroupSpinner(this,
				(Spinner) findViewById(R.id.il_list_spinner), mDba);
		mCurrentGroupId = mSpinner.getSelectedItemId();
		mSpinner.setOnGroupSelectedListener(this);
	}


	/* attach cursor for the items in the current list to the listview */
	private void fillTrackerList() {
		Log.d(TAG, "fillTrackerList");
		// TODO: deal with situation where all groups were deleted
		// TODO: does this leak the cursor last used when a new one is created?
		Cursor cur = mDba.fetchTrackers(mCurrentGroupId);
		startManagingCursor(cur);
		EventCursorAdapter adapter = new EventCursorAdapter(this,
				R.layout.a_tracker_group_row,
				cur, // Give the cursor to the list adapter
				new String[] { C.db_TRACKER_NAME, C.db_TRACKER_LAST_LOG },
				new int[] { R.id.ilr_itemTitle, R.id.logTime });
		this.setListAdapter(adapter);

		// set up self as listener for when user clicks item
		this.getListView().setOnItemClickListener(this);
		// enable context menu for list items
		registerForContextMenu(getListView());
	}

	@Override
	public void onGroupSelected(long groupId) {
		mCurrentGroupId = groupId;
		fillTrackerList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.tracker_group, menu);
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

	void newItem() {
		Log.d(TAG, "newItem with listId " + mCurrentGroupId);
		Intent intent = new Intent(this, TrackerDetail.class);
		intent.setAction(Intent.ACTION_INSERT);
		intent.putExtra(C.db_TRACKER_GROUP, mCurrentGroupId);
		startActivity(intent);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Log.d(TAG, "onCreateDialog");
		switch (id) {
		case DIALOG_NEW:
			return new NewTrackerDialog();
		case DIALOG_ABOUT:
			return new AlertDialog.Builder(this).setTitle(
					R.string.il_dialog_about_title).setMessage(
					R.string.il_dialog_about_text).setPositiveButton(
					R.string.il_dialog_about_button, null).create();
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
		getMenuInflater().inflate(R.menu.tracker_group_context, menu);
	}

	public boolean onContextItemSelected(MenuItem item) {
		Log.d(TAG, "onContextItemSelected");
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		long rowId = info.id;
		TextView tv = (TextView) info.targetView.findViewById(R.id.logTime);
		switch (item.getItemId()) {
		case R.id.quicklog:
			String time = mDba.createLog(rowId, null, null);
			if (time != null)
				tv.setText(time);
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

	private void showDeleteDialog(final long itemId) {
		Log.d(TAG, "showDeleteDialog");
		TrackerDeleteDialog.create(this, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "DeleteDialog onClick " + itemId);
				mDba.deleteTracker(itemId);
				((EventCursorAdapter) getListAdapter()).requery();
			}
		}).show();
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		mSpinner.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		mDba.close();
		super.onDestroy();
	}


	private class NewTrackerDialog extends RequiredFieldDialog {

		private EditText mBodyEditor = null;

		public NewTrackerDialog() {
			super(TrackerGroup.this, android.R.style.Theme);
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			Log.d(TAG, "onCreate NewTrackerDialog");
			super.onCreate(savedInstanceState);
			setContentView(R.layout.d_new_tracker);
			this.setOwnerActivity(TrackerGroup.this);
			this.setTitle(R.string.ilni_title);

			// and we'll need this later
			mBodyEditor = (EditText) findViewById(R.id.body);
		}

		@Override
		protected void onClickOk() {
			Log.d(TAG, "creating new item");
			mDba.createTracker(mCurrentGroupId, mEditor.getText()
					.toString(), mBodyEditor.getText().toString());
			// udpate parent's view
			((EventCursorAdapter) getListAdapter()).requery();
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			Log.d(TAG, "onDismiss NewTrackerDialog");
			super.onDismiss(dialog);
			// clear for next time dialog is called
			mBodyEditor.setText("");
		}
	}
}
