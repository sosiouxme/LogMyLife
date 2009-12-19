package net.sosiouxme.WhenDidI.activity;

import net.sosiouxme.WhenDidI.DbAdapter;
import net.sosiouxme.WhenDidI.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class ItemList extends ListActivity implements OnItemClickListener {
	private static final String TAG = "WDI.ItemList";
	private DbAdapter mDba;
	private long mCurrentListId = 0;

	private final static int DELETE_DIALOG = 1;
	private long mContextItemId = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item_list);

		mDba = new DbAdapter(this).open();
		fillListSpinner();
		fillItemList();
	}

	/* get the cursor with all lists and attach to the picker */
	private void fillListSpinner() {
		Log.d(TAG, "fillListSpinner");
		Cursor cur = mDba.fetchLists();
		startManagingCursor(cur);
		SpinnerAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.list_spinner, 	// Use a template
											// that displays a
											// text view
				cur, // Give the cursor to the list adapter
				new String[] { DbAdapter.LIST_TITLE },	// Map the NAME column in the
														// list database to...
				new int[] { R.id.listSpinnerText }); 	// The "text1" view defined
													// in
													// the XML template
		
		//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner listSpinner = (Spinner) findViewById(R.id.list_spinner);
		listSpinner.setAdapter(adapter);
		long curId = mCurrentListId; // don't access member too much
		if(curId > 0) {
			// use this view's currently set list id
			for(int pos = 0; pos < adapter.getCount(); pos++) {
				if(adapter.getItemId(pos) == curId) {
					listSpinner.setSelection(pos);
					break;
				}
			}
		}
		mCurrentListId = listSpinner.getSelectedItemId();
	}
	
	// callback specified in spinner definition - when selected
	public void onListSpinnerSelect(Spinner s) {
		Log.d(TAG, "onListSpinnerSelect");
		mCurrentListId = s.getSelectedItemId();
		fillItemList();
	}

	/* attach cursor for the items in the current list to the listview */
	private void fillItemList() {
		Log.d(TAG, "fillItemList");
		Cursor cur = mDba.fetchItems(mCurrentListId);
		startManagingCursor(cur);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.item_list_row,
				cur, // Give the cursor to the list adapter
				new String[] { DbAdapter.ITEM_TITLE },
				new int[] { R.id.ilr_itemTitle });
		
		this.setListAdapter(adapter);
		
		// set up self as listener for when user clicks item
		this.getListView().setOnItemClickListener(this);
		// enable context menu for list items
		registerForContextMenu(getListView());
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.item_list, menu);
        return true;
    }
    
    @Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.d(TAG, "onMenuItemSelected");
    	switch(item.getItemId()) {
    		case R.id.ilmenu_new_item:
    			newItem();
    			return true;
    		case R.id.ilmenu_manage_lists:
    			startActivity(new Intent(this, ListEdit.class));
    			return true;
    		case R.id.ilmenu_settings:
    			startActivity(new Intent(this, Prefs.class));
    			return true;
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
    
    void newItem() {
		Log.d(TAG, "newItem with listId " + mCurrentListId);
		Intent intent = new Intent(this, ItemEdit.class);
		intent.setAction(Intent.ACTION_INSERT);
		intent.putExtra(DbAdapter.ITEM_LIST, mCurrentListId);
		startActivity(intent);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long itemId) {
		Log.d(TAG, "onItemClick " + itemId);
	
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Log.d(TAG, "onCreateContextMenu");
		super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.item_list_context, menu);
        mContextItemId = 1;
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
		Log.d(TAG, "onContextItemSelected");
		long itemId = ((AdapterContextMenuInfo) item.getMenuInfo()).id;
		switch(item.getItemId()) {
		case R.id.ilc_menu_quicklog:
			mDba.createLog(mContextItemId, null, "");
			return true;
		case R.id.ilc_menu_delete:
			mContextItemId = itemId; // stash this until the dialog is called
			showDialog(DELETE_DIALOG);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Log.d(TAG, "onCreateDialog");

		switch(id) {
		case DELETE_DIALOG:
			Dialog d = new DeleteDialog(mDba,mContextItemId).dialog();
			mContextItemId = 0; // reset now that dialog has it
			return d;
		}
		return null;
	}
	

	private class DeleteDialog implements OnClickListener, OnDismissListener {
		private DbAdapter mDba = null;
		private long mItemId = 0;
		
		public DeleteDialog(DbAdapter dba, long itemId) {
			mDba = dba;
			this.mItemId = itemId;
		}
		
		public Dialog dialog() {
			Log.d(TAG, "deleteDialog");
			Dialog d = new AlertDialog.Builder(ItemList.this)
			.setTitle(R.string.ilc_dialog_delete_title)
			.setMessage(R.string.ilc_dialog_delete_msg)
			.setNegativeButton(R.string.ilc_dialog_cancel_button, null)
			.setPositiveButton(R.string.ilc_dialog_delete_button, this)
			.create();
			d.setOnDismissListener(this); // why can't the builder do this?
			return d;
		}
		@Override
		public void onClick(DialogInterface dialog, int which) {
			Log.d(TAG, "DeleteDialog " + mItemId);
			if (mItemId > 0) {
				mDba.deleteItem(mItemId);
				fillItemList();
			}
		}

		@Override
		public void onDismiss(DialogInterface di) {
			// the dialog's listener refers to a specific item to be
			// deleted - don't want to keep this state. 
			removeDialog(DELETE_DIALOG);
		}
	}
}
