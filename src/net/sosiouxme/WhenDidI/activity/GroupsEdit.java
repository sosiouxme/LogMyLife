package net.sosiouxme.WhenDidI.activity;

import net.sosiouxme.WhenDidI.C;
import net.sosiouxme.WhenDidI.DbAdapter;
import net.sosiouxme.WhenDidI.R;
import net.sosiouxme.WhenDidI.custom.RequiredFieldDialog;
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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class GroupsEdit extends ListActivity {
	private DbAdapter mDba;
	private static final String TAG = "WDI.GroupsEdit";
	private static final int DIALOG_NEW_LIST = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_groups_edit);
		mDba = new DbAdapter(this).open();
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
				new int[] { R.id.ler_list });
		
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
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.d(TAG, "onMenuItemSelected");
    	switch(item.getItemId()) {
    		case R.id.new_group:
    			showDialog(DIALOG_NEW_LIST);
    			return true;
    		case R.id.done:
    			finish();
    			return true;
    	}
    	return super.onMenuItemSelected(featureId, item);
    }

    
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Dialog d = new GroupDialog(id);
		d.setOwnerActivity(this);
		d.show();
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
		long rowId = info.id;
		switch(item.getItemId()) {
		case R.id.edit:
			Dialog d = new GroupDialog(rowId);
			d.setOwnerActivity(this);
			d.show();
			return true;
		case R.id.delete:
			showDeleteDialog(rowId);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private void showDeleteDialog(final long listId) {
		Log.d(TAG, "showDeleteDialog");
		Dialog d = new AlertDialog.Builder(this)
		// TODO: customize message with list title
		.setTitle(R.string.lec_dialog_delete_title)
		.setMessage(R.string.lec_dialog_delete_msg)
		.setNegativeButton(R.string.lec_dialog_cancel_button, null)
		.setPositiveButton(R.string.lec_dialog_delete_button,
			new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d(TAG, "DeleteDialog onClick " + listId);
					mDba.deleteGroup(listId);
					fillGroupList();
				}
			})
		.create();
		d.setOwnerActivity(this); // why can't the builder do this?
		d.show();

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_NEW_LIST:
			return new GroupDialog();
		}
		return null;
	}


	private class GroupDialog extends RequiredFieldDialog {

		private long mGroupId = 0;
		
		public GroupDialog() {
			super(GroupsEdit.this, android.R.style.Theme_Dialog);
		}
		public GroupDialog(long groupId) {
			this();
			mGroupId = groupId;
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.d_group_edit);
			this.setOwnerActivity(GroupsEdit.this);
			if(mGroupId > 0) {
				Cursor c = mDba.fetchGroup(mGroupId);
				c.moveToFirst();
				mEditor.setText(c.getString(c.getColumnIndex(C.db_GROUP_NAME)));
				c.close();
				this.setTitle(R.string.ged_edit_title);
			} else {
				this.setTitle(R.string.ged_new_title);
			}
		}

		@Override
		protected void onClickOk() {
			String title = mEditor.getText().toString();
			if(mGroupId == 0) {
				Log.d(TAG,"creating new item");
				mDba.createGroup(title);				
			} else {
				Log.d(TAG,"editing item " + mGroupId);
				mDba.updateGroup(mGroupId, title);
			}
			// udpate parent's view
			GroupsEdit.this.requery();
		}
	}
}
