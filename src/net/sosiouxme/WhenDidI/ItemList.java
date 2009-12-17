package net.sosiouxme.WhenDidI;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class ItemList extends ListActivity {
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
	
    // callback specified in spinner definition - when selected
	public void onListSpinnerSelect(Spinner s) {
		Log.d(TAG, "onListSpinnerSelect");
		mCurrentListId = s.getSelectedItemId();
		fillItemList();
	}
}