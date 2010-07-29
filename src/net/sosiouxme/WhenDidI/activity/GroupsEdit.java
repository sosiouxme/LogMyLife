package net.sosiouxme.WhenDidI.activity;

import net.sosiouxme.WhenDidI.C;
import net.sosiouxme.WhenDidI.R;
import net.sosiouxme.WhenDidI.WhenDidI;
import net.sosiouxme.WhenDidI.custom.RequiredFieldDialog;
import net.sosiouxme.WhenDidI.domain.DbAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
Activity for presenting the UI to edit the groups that trackers can be in.

@author Luke Meyer, Copyright 2010
See LICENSE file for this file's GPLv3 distribution license.
*/

public class GroupsEdit extends ListActivity {
	private DbAdapter mDba;
	private static final String TAG = "WDI.GroupsEdit";

	private static final int DIALOG_GROUP_NAME = 0;
	private long mGroupEditId = 0;
	private static final int DIALOG_GROUP_DELETE = 1;
	private long mGroupDeleteId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_groups_edit);
		getListView().addHeaderView(
				getLayoutInflater().inflate(R.layout.a_groups_edit_new, null));
		mDba = new DbAdapter(this);
		fillGroupList();
	}
	
	@Override
	protected void onDestroy() {
		mDba.close();
		super.onDestroy();
	}
	
	private void fillGroupList() {
		Log.d(TAG, "fillGroupList");
		Cursor cur = mDba.fetchGroups();
		startManagingCursor(cur);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.a_groups_edit_row,
				cur, // Give the cursor to the list adapter
				new String[] { C.db_GROUP_NAME },
				new int[] { R.id.listName });
		
		this.setListAdapter(adapter);
		this.registerForContextMenu(getListView());
	}
	
	private void requery(){
		((SimpleCursorAdapter) getListAdapter()).getCursor().requery();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.groups_edit, menu);
        return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onMenuItemSelected");
    	switch(item.getItemId()) {
    		case R.id.new_group:
    			mGroupEditId = 0;
    			showDialog(DIALOG_GROUP_NAME);
    			return true;
    		case R.id.done:
    			finish();
    			return true;
    	}
    	return super.onOptionsItemSelected(item);
    }

    
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (v.getId() == R.id.add_new_group) {
			mGroupEditId = 0;
			showDialog(DIALOG_GROUP_NAME);
		} else {
			((WhenDidI) getApplication()).setSelectedGroup(id);
			finish();
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.groups_edit_context, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		long groupId = info.id;
		switch(item.getItemId()) {
		case R.id.view:
			((WhenDidI) getApplication()).setSelectedGroup(groupId);
			finish();
			break;
		case R.id.edit:
			mGroupEditId = groupId;
			showDialog(DIALOG_GROUP_NAME);
			break;
		case R.id.delete:
			mGroupDeleteId = groupId;
			showDialog(DIALOG_GROUP_DELETE);
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_GROUP_NAME:
			return new GroupDialog();
		case DIALOG_GROUP_DELETE:
			return new AlertDialog.Builder(this)
			// TODO: customize message with list title
			.setTitle(R.string.ge_dialog_delete_title)
			.setMessage(R.string.ge_dialog_delete_msg)
			.setNegativeButton(R.string.ge_dialog_cancel_button, null)
			.setPositiveButton(R.string.ge_dialog_delete_button,
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.d(TAG, "DeleteDialog onClick " + mGroupDeleteId);
						mDba.deleteGroup(mGroupDeleteId);
						((WhenDidI) getApplication()).showToast(C.TOAST_GROUP_DELETED);
						GroupsEdit.this.requery();
					}
				})
			.create();
		}
		return null;
	}


	private class GroupDialog extends RequiredFieldDialog {

		
		public GroupDialog() {
			super(GroupsEdit.this, android.R.style.Theme_Dialog);
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.d_group_edit);
			this.setOwnerActivity(GroupsEdit.this);
		}
		
		@Override
		protected void onStart() {
			if(mGroupEditId > 0) {
				Cursor c = mDba.fetchGroup(mGroupEditId);
				c.moveToFirst();
				mEditor.setText(c.getString(c.getColumnIndex(C.db_GROUP_NAME)));
				c.close();
				setTitle(R.string.ged_edit_title);
				setOkText(R.string.ged_ok_update);
			} else {
				setTitle(R.string.ged_new_title);
				setOkText(R.string.ged_ok_create);
			}
			super.onStart();
		}
		
		private void setOkText(int textResId) {
			Button ok = (Button) findViewById(R.id.ok);
			ok.setText(textResId);
		}

		@Override
		protected void onClickOk() {
			String title = mEditor.getText().toString();
			if(mGroupEditId <= 0) {
				Log.d(TAG,"creating new item");
				mDba.createGroup(title);
				((WhenDidI) getApplication()).showToast(C.TOAST_GROUP_CREATED);
			} else {
				Log.d(TAG,"editing item " + mGroupEditId);
				mDba.updateGroup(mGroupEditId, title);
				((WhenDidI) getApplication()).showToast(C.TOAST_GROUP_UPDATED);
			}
			// update parent's view
			GroupsEdit.this.requery();
		}
	}
}
