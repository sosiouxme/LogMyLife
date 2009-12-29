package net.sosiouxme.WhenDidI;

import android.app.Activity;
import android.database.Cursor;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.AdapterView.OnItemSelectedListener;

public class GroupSpinner {
	private static final String TAG = "WDI.GroupSpinner";
	private final Spinner mSpinner;
	private final DbAdapter mDba;

	public GroupSpinner(Activity caller, Spinner spinner, DbAdapter dba) {
		Log.d(TAG, "new GroupSpinner");

		this.mSpinner = spinner;
		this.mDba = dba;

		Cursor cur = mDba.fetchGroups();
		caller.startManagingCursor(cur);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(caller,
				R.layout.w_group_spinner_row, 	// specify layout id with a TextView
				//android.R.layout.simple_spinner_item,
				cur, // Give the cursor to the list adapter
				new String[] { C.db_GROUP_NAME },	// Map column in the database to...
				new int[] { R.id.groupName });// The TextView defined in the layout
		adapter.setDropDownViewResource(R.layout.w_group_spinner_row);
		//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner.setAdapter(adapter);
	}

	public boolean setGroupId(long groupId) {
		SpinnerAdapter adapter = mSpinner.getAdapter();
		for (int pos = 0; pos < adapter.getCount(); pos++) {
			if (adapter.getItemId(pos) == groupId) {
				mSpinner.setSelection(pos);
				return true;
			}
		}
		return false;
	}
	
	public void notifyDataSetChanged() {
		Log.d(TAG, "notifyDataSetChanged");
		((SimpleCursorAdapter) mSpinner.getAdapter()).notifyDataSetChanged();
	}
	
	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		mSpinner.setOnItemSelectedListener(listener);
	}
	
	public long getSelectedItemId() {
		return mSpinner.getSelectedItemId();
	}
}
