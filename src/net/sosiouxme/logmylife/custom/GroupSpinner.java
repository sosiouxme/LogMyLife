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

package net.sosiouxme.logmylife.custom;

import net.sosiouxme.logmylife.C;
import net.sosiouxme.logmylife.LogMyLife;
import net.sosiouxme.logmylife.R;
import net.sosiouxme.logmylife.domain.DbAdapter;
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
	private static final String TAG = "LML.GroupSpinner";
	private final Spinner mSpinner;
	private final DbAdapter mDba;
	private final LogMyLife mApp;
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
		mApp = (LogMyLife) caller.getApplication();
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


