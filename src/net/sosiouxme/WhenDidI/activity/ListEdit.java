package net.sosiouxme.WhenDidI.activity;

import net.sosiouxme.WhenDidI.C;
import net.sosiouxme.WhenDidI.DbAdapter;
import net.sosiouxme.WhenDidI.R;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;

public class ListEdit extends ListActivity {
	private DbAdapter mDba;
	private static final String TAG = "WDI.ListEdit";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_edit);
		mDba = new DbAdapter(this).open();
		fillLists();
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
    			newList();
    			return true;
    		case R.id.lemenu_done:
    			finish();
    			return true;
    	}
    	return super.onMenuItemSelected(featureId, item);
    }

	private void newList() {
		// TODO Auto-generated method stub
		
	}
}
