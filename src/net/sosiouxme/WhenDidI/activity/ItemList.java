package net.sosiouxme.WhenDidI.activity;

import net.sosiouxme.WhenDidI.C;
import net.sosiouxme.WhenDidI.DbAdapter;
import net.sosiouxme.WhenDidI.R;
import net.sosiouxme.WhenDidI.custom.LogCursorAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class ItemList extends ListActivity implements OnItemClickListener {
	private static final String TAG = "WDI.ItemList";
	private static final int DIALOG_ABOUT = 0;
	private static final int DIALOG_NEW = 1;
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
				new String[] { C.db_LIST_TITLE },	// Map the NAME column in the
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
		LogCursorAdapter adapter = new LogCursorAdapter(this,
				R.layout.item_list_row,
				cur, // Give the cursor to the list adapter
				new String[] { C.db_ITEM_TITLE, C.db_ITEM_LAST_LOG },
				new int[] { R.id.ilr_itemTitle, R.id.ilr_itemLog });
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
    			showDialog(DIALOG_NEW);
    			return true;
    		case R.id.ilmenu_about:
    			showDialog(DIALOG_ABOUT);
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
		Intent intent = new Intent(this, ItemViewEdit.class);
		intent.setAction(Intent.ACTION_INSERT);
		intent.putExtra(C.db_ITEM_LIST, mCurrentListId);
		startActivity(intent);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_NEW:
			return new NewItemDialog(this);
		case DIALOG_ABOUT:
			return new AlertDialog.Builder(this)
			.setTitle(R.string.il_dialog_about_title)
			.setMessage(R.string.il_dialog_about_text)
			.setPositiveButton(R.string.il_dialog_about_button, null)
			.create()
			;
		}
		return null;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long itemId) {
		Log.d(TAG, "onItemClick " + itemId);
		Intent intent = new Intent(this, ItemViewEdit.class);
		intent.setAction(Intent.ACTION_EDIT);
		intent.putExtra(C.db_ITEM_LIST, mCurrentListId);
		intent.putExtra(C.db_ID, itemId);
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
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		long rowId = info.id;
		TextView tv = (TextView) info.targetView.findViewById(R.id.ilr_itemLog);
		switch(item.getItemId()) {
		case R.id.ilc_menu_quicklog:
			String time = mDba.createLog(rowId, null, null);
			if(time != null)
				tv.setText(time);
			return true;
		case R.id.ilc_menu_delete:
			showDeleteDialog(rowId, tv.getText().toString());
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	private void showDeleteDialog(final long itemId, String itemTitle) {
		Log.d(TAG, "showDeleteDialog");
		Dialog d = new AlertDialog.Builder(this)
		// TODO: customize message with itemTitle
		.setTitle(R.string.ilc_dialog_delete_title)
		.setMessage(R.string.ilc_dialog_delete_msg)
		.setNegativeButton(R.string.ilc_dialog_cancel_button, null)
		.setPositiveButton(R.string.ilc_dialog_delete_button,
			new OnClickListener() {
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

	private class NewItemDialog extends Dialog implements android.view.View.OnClickListener, OnKeyListener, OnDismissListener {

		private Button mCreateButton = null;
		private EditText mTitleEditor = null;
		private EditText mBodyEditor = null;

		public NewItemDialog(Context context) {
			super(context, android.R.style.Theme);
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.item_list_new_item);
			this.setOwnerActivity(ItemList.this);
			this.setTitle(R.string.ilni_title);
			
			// make sure only the dialog has focus
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
		             WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);


			// wire up the buttons
			Button cancel = (Button) findViewById(R.id.lini_dialog_cancel);
			cancel.setOnClickListener(this);
			mCreateButton = (Button) findViewById(R.id.lini_dialog_create);
			mCreateButton.setOnClickListener(this);
			
			//wire up the title text to enable/disable the create button
			mTitleEditor = (EditText) findViewById(R.id.lini_dialog_item_title);
			mTitleEditor.setOnKeyListener(this);
			// and we'll need to reset both text fields when the dialog is dismissed
			mBodyEditor = (EditText) findViewById(R.id.lini_dialog_item_body);
			this.setOnDismissListener(this);
		}

		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.lini_dialog_create:
				Log.d(TAG,"creating new item");
				mDba.createItem(mCurrentListId, 
						mTitleEditor.getText().toString(), 
						mBodyEditor.getText().toString());
				// udpate parent's view
				fillItemList();
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
			mBodyEditor.setText("");
			mCreateButton.setEnabled(false);
		}
		
		
	}
}
