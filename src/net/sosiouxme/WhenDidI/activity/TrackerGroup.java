package net.sosiouxme.WhenDidI.activity;

import net.sosiouxme.WhenDidI.C;
import net.sosiouxme.WhenDidI.DbAdapter;
import net.sosiouxme.WhenDidI.GroupSpinner;
import net.sosiouxme.WhenDidI.R;
import net.sosiouxme.WhenDidI.custom.EventCursorAdapter;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class TrackerGroup extends ListActivity implements OnItemClickListener, OnItemSelectedListener {
	private static final String TAG = "WDI.TrackerGroup";
	private static final int DIALOG_ABOUT = 0;
	private static final int DIALOG_NEW = 1;
	private DbAdapter mDba;
	private long mCurrentGroupId = 0;
	private GroupSpinner mSpinner = null;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item_list);
		//getListView().addHeaderView(findViewById(R.id.il_list_spinner));
		EditText addNew = new EditText(this);
		addNew.setHint("Add new item");
		getListView().addFooterView(addNew);
		//getListView().addFooterView(findViewById(R.id.il_new_item));

		mDba = new DbAdapter(this).open();
		fillGroupSpinner();
		fillTrackerList();
	}

	/* get the cursor with all lists and attach to the spinner */
	private void fillGroupSpinner() {
		Log.d(TAG, "fillGroupSpinner");
		mSpinner = new GroupSpinner(this, (Spinner) findViewById(R.id.il_list_spinner), mDba);
		if(mCurrentGroupId == 0)
			mCurrentGroupId = mSpinner.getSelectedItemId();
		mSpinner.setGroupId(mCurrentGroupId);
		mSpinner.setOnItemSelectedListener(this);
	}
	
	@Override
	public void onItemSelected(AdapterView<?> av, View v, int i,
			long groupId) {
		Log.d(TAG, "onItemSelected");
		// switch to new group
		mCurrentGroupId = groupId;
		fillTrackerList();		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	/* attach cursor for the items in the current list to the listview */
	private void fillTrackerList() {
		Log.d(TAG, "fillTrackerList");
		Cursor cur = mDba.fetchItems(mCurrentGroupId);
		startManagingCursor(cur);
		EventCursorAdapter adapter = new EventCursorAdapter(this,
				R.layout.item_list_row,
				cur, // Give the cursor to the list adapter
				new String[] { C.db_ITEM_TITLE, C.db_ITEM_LAST_LOG },
				new int[] { R.id.ilr_itemTitle, R.id.logTime });
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
    			startActivity(new Intent(this, GroupsEdit.class));
    			return true;
    		case R.id.ilmenu_settings:
    			startActivity(new Intent(this, Prefs.class));
    			return true;
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
    
    void newItem() {
		Log.d(TAG, "newItem with listId " + mCurrentGroupId);
		Intent intent = new Intent(this, TrackerViewEdit.class);
		intent.setAction(Intent.ACTION_INSERT);
		intent.putExtra(C.db_ITEM_LIST, mCurrentGroupId);
		startActivity(intent);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Log.d(TAG, "onCreateDialog");
		switch(id) {
		case DIALOG_NEW:
			return new NewTrackerDialog();
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
		Intent intent = new Intent(this, TrackerViewEdit.class);
		intent.setAction(Intent.ACTION_EDIT);
		intent.putExtra(C.db_ITEM_LIST, mCurrentGroupId);
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
		TextView tv = (TextView) info.targetView.findViewById(R.id.logTime);
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
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		mSpinner.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		mDba.close();
		super.onDestroy();
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
					((EventCursorAdapter) getListAdapter()).requery();
				}
			})
		.create();
		d.setOwnerActivity(this); // why can't the builder do this?
		d.show();

	}

	private class NewTrackerDialog extends Dialog implements android.view.View.OnClickListener, OnKeyListener, OnDismissListener {

		private Button mCreateButton = null;
		private EditText mTitleEditor = null;
		private EditText mBodyEditor = null;

		public NewTrackerDialog() {
			super(TrackerGroup.this, android.R.style.Theme);
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			Log.d(TAG, "onCreate NewTrackerDialog");
			super.onCreate(savedInstanceState);
			setContentView(R.layout.item_list_new_item);
			this.setOwnerActivity(TrackerGroup.this);
			this.setTitle(R.string.ilni_title);
			
			// make sure only the dialog has focus
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
		             WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);


			// wire up the buttons
			Button cancel = (Button) findViewById(R.id.ilni_dialog_cancel);
			cancel.setOnClickListener(this);
			mCreateButton = (Button) findViewById(R.id.ilni_dialog_create);
			mCreateButton.setOnClickListener(this);
			
			//wire up the title text to enable/disable the create button
			mTitleEditor = (EditText) findViewById(R.id.ilni_dialog_item_title);
			mTitleEditor.setOnKeyListener(this);
			// and we'll need to reset both text fields when the dialog is dismissed
			mBodyEditor = (EditText) findViewById(R.id.ilni_dialog_item_body);
			this.setOnDismissListener(this);
		}

		@Override
		public void onClick(View v) {
			Log.d(TAG, "onClick NewTrackerDialog");
			switch(v.getId()) {
			case R.id.ilni_dialog_create:
				Log.d(TAG,"creating new item");
				mDba.createItem(mCurrentGroupId, 
						mTitleEditor.getText().toString(), 
						mBodyEditor.getText().toString());
				// udpate parent's view
				((EventCursorAdapter) getListAdapter()).requery();
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
			Log.d(TAG, "onDismiss NewTrackerDialog");

			// clear for next time dialog is called
			mTitleEditor.setText("");
			mBodyEditor.setText("");
			mCreateButton.setEnabled(false);
		}
		
		
	}
}
