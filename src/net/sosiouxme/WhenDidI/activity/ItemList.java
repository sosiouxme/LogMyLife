package net.sosiouxme.WhenDidI.activity;

import net.sosiouxme.WhenDidI.DbAdapter;
import net.sosiouxme.WhenDidI.R;
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
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class ItemList extends ListActivity implements OnItemClickListener {
	private static final String TAG = "WDI.ItemList";
	private DbAdapter mDba;
	private long mCurrentListId = 0;


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
		Intent intent = new Intent(this, ItemEdit.class);
		intent.setAction(Intent.ACTION_EDIT);
		intent.putExtra(DbAdapter.ITEM_LIST, mCurrentListId);
		intent.putExtra(DbAdapter._ID, itemId);
		startActivity(intent);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Log.d(TAG, "onCreateContextMenu");
		super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.item_list_context, menu);
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
		Log.d(TAG, "onContextItemSelected");
		long itemId = ((AdapterContextMenuInfo) item.getMenuInfo()).id;
		switch(item.getItemId()) {
		case R.id.ilc_menu_quicklog:
			mDba.createLog(itemId, null, "");
			return true;
		case R.id.ilc_menu_delete:
			showDeleteDialog(itemId);
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	private void showDeleteDialog(final long itemId) {
		Log.d(TAG, "showDeleteDialog");
		Dialog d = new AlertDialog.Builder(this)
		.setTitle(R.string.ilc_dialog_delete_title)
		.setMessage(R.string.ilc_dialog_delete_msg)
		.setNegativeButton(R.string.ilc_dialog_cancel_button, null)
		.setPositiveButton(R.string.ilc_dialog_delete_button, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.d(TAG, "DeleteDialog onClick " + itemId);
						mDba.deleteItem(itemId);
						fillItemList();
					}
				})
		.create();
		d.setOwnerActivity(this); // why can't the builder do this?
		d.show();

	}

}
