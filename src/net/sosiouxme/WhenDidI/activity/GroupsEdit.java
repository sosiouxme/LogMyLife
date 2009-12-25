package net.sosiouxme.WhenDidI.activity;

import net.sosiouxme.WhenDidI.C;
import net.sosiouxme.WhenDidI.DbAdapter;
import net.sosiouxme.WhenDidI.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
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
		setContentView(R.layout.list_edit);
		mDba = new DbAdapter(this).open();
		fillLists();
	}
	
	@Override
	protected void onDestroy() {
		mDba.close();
		super.onDestroy();
	}
	
	private void fillLists() {
		Log.d(TAG, "fillItemList");
		Cursor cur = mDba.fetchLists();
		startManagingCursor(cur);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.list_edit_row,
				cur, // Give the cursor to the list adapter
				new String[] { C.db_LIST_TITLE },
				new int[] { R.id.ler_list });
		
		this.setListAdapter(adapter);
		this.registerForContextMenu(getListView());
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.list_edit, menu);
        return true;
    }
    
    @Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.d(TAG, "onMenuItemSelected");
    	switch(item.getItemId()) {
    		case R.id.lemenu_new_item:
    			showDialog(DIALOG_NEW_LIST);
    			return true;
    		case R.id.lemenu_done:
    			finish();
    			return true;
    	}
    	return super.onMenuItemSelected(featureId, item);
    }

    
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		showDialog(DIALOG_NEW_LIST);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.list_edit_context, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		long rowId = info.id;
		switch(item.getItemId()) {
		case R.id.lec_menu_edit:
			showDialog(DIALOG_NEW_LIST);
			return true;
		case R.id.lec_menu_delete:
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
					mDba.deleteList(listId);
					fillLists();
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
			return new NewListDialog();
		}
		return null;
	}


	private class NewListDialog extends Dialog implements android.view.View.OnClickListener, OnKeyListener, OnDismissListener {

		private Button mCreateButton = null;
		private EditText mTitleEditor = null;

		public NewListDialog() {
			super(GroupsEdit.this, android.R.style.Theme_Dialog);
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.list_edit_new_list);
			this.setOwnerActivity(GroupsEdit.this);
			this.setTitle(R.string.lenl_title);
			
			// make sure only the dialog has focus
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
		             WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);


			// wire up the buttons
			Button cancel = (Button) findViewById(R.id.lenl_dialog_cancel);
			cancel.setOnClickListener(this);
			mCreateButton = (Button) findViewById(R.id.lenl_dialog_create);
			mCreateButton.setOnClickListener(this);
			
			//wire up the title text to enable/disable the create button
			mTitleEditor = (EditText) findViewById(R.id.lenl_dialog_item_title);
			mTitleEditor.setOnKeyListener(this);
			this.setOnDismissListener(this);
		}

		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.lenl_dialog_create:
				Log.d(TAG,"creating new item");
				mDba.createList(mTitleEditor.getText().toString());
				// udpate parent's view
				fillLists();
				break;
			}
			this.dismiss();
		}

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(((EditText)v).getText().length() > 0) {
				mCreateButton.setEnabled(true);			
			} else {
				mCreateButton.setEnabled(false);			
			}
			return false;
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			// clear for next time dialog is called
			mTitleEditor.setText("");
			mCreateButton.setEnabled(false);
		}
		
		
	}
}
