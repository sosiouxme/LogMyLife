package net.sosiouxme.WhenDidI.custom;

import net.sosiouxme.WhenDidI.C;
import net.sosiouxme.WhenDidI.R;
import net.sosiouxme.WhenDidI.WhenDidI;
import net.sosiouxme.WhenDidI.domain.DbAdapter;
import android.app.Activity;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.AdapterView.OnItemSelectedListener;

public class GroupSpinner implements OnItemSelectedListener {
	private static final String TAG = "WDI.GroupSpinner";
	private final Spinner mSpinner;
	private final DbAdapter mDba;
	private final WhenDidI mApp;
	private OnGroupSelectedListener onGroupSelectedListener = null;

	public GroupSpinner(Activity caller, Spinner spinner, DbAdapter dba) {
		Log.d(TAG, "new GroupSpinner");

		this.mSpinner = spinner;
		this.mDba = dba;

		Cursor cur = mDba.fetchGroups();
		caller.startManagingCursor(cur);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(caller,
				R.layout.w_group_spinner_display, 	// specify layout id with a TextView
				cur, // Give the cursor to the list adapter
				new String[] { C.db_GROUP_NAME },	// Map column in the database to...
				new int[] { R.id.groupName });// The TextView defined in the layout
		adapter.setDropDownViewResource(R.layout.w_group_spinner_row);
		mSpinner.setAdapter(adapter);
		mSpinner.setOnItemSelectedListener(this);

		//TODO: add a "Create group" option at beginning of list
		mApp = (WhenDidI) caller.getApplication();
		setGroupId(mApp.getSelectedGroup());
	}
	

	@Override
	public void onItemSelected(AdapterView<?> arg0av, View v, int i, long groupId) {
		mApp.setSelectedGroup(groupId);
		if(onGroupSelectedListener != null)
			onGroupSelectedListener.onGroupSelected(groupId);
	}

	@Override
	public void onNothingSelected(AdapterView<?> av) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onNothingSelected");
	}

	private boolean setGroupId(long groupId) {
		SpinnerAdapter adapter = mSpinner.getAdapter();
		for (int pos = 0; pos < adapter.getCount(); pos++) {
			if (adapter.getItemId(pos) == groupId) {
				mSpinner.setSelection(pos);
				return true;
			}
		}
		// nothing in the list matched. set groupId to first item in list
		mSpinner.setSelection(0);
		if(onGroupSelectedListener != null)
			onGroupSelectedListener.onGroupSelected(mSpinner.getItemIdAtPosition(0));		
		return false;
	}
	
	public void notifyDataSetChanged() {
		Log.d(TAG, "notifyDataSetChanged");
		((SimpleCursorAdapter) mSpinner.getAdapter()).notifyDataSetChanged();
		setGroupId(mApp.getSelectedGroup());
	}
	
	public long getSelectedItemId() {
		return mSpinner.getSelectedItemId();
	}
	
	public void setOnGroupSelectedListener(OnGroupSelectedListener groupSelectedListener) {
		this.onGroupSelectedListener = groupSelectedListener;
	}

	public OnGroupSelectedListener getOnGroupSelectedListener() {
		return onGroupSelectedListener;
	}

	public interface OnGroupSelectedListener {
		void onGroupSelected(long groupId);
	}

}


