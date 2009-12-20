package net.sosiouxme.WhenDidI.activity;


import net.sosiouxme.WhenDidI.C;
import net.sosiouxme.WhenDidI.DbAdapter;
import net.sosiouxme.WhenDidI.R;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class ItemViewEdit extends Activity {
	// Logger tag
    private static final String TAG = "WDI.ItemEdit";
    
    // The different distinct modes the activity can be run in.
    private static final int MODE_INSERT = 0;
    private static final int MODE_EDIT = 1;
    private int mMode = MODE_INSERT;

    // the Item being handled
    private long mItemId = 0;
    private long mListId = 0;
    private String mTitle;
    private String mBody;
    private EditText mtvTitle;
    private EditText mtvBody;
    
	private DbAdapter mDba;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // get a DB handle
        mDba = new DbAdapter(this).open();

        // Set the layout for this activity.
        setContentView(R.layout.item_view_edit);
        mtvTitle = (EditText) findViewById(R.id.item_title_edit);
        mtvBody = (EditText) findViewById(R.id.item_body_edit);

        // find out what was intended
        final Intent intent = getIntent();
        mListId = intent.getExtras().getLong(C.db_ITEM_LIST);

        // Do some setup based on the action being performed.
        final String action = intent.getAction();
        if (Intent.ACTION_EDIT.equals(action)) {
            // Requested to edit: set that state, and the data being edited.
            Log.d(TAG, "Requested to edit");
            mMode = MODE_EDIT;
            mItemId = intent.getExtras().getLong(C.db_ID);
            
            // retrieve the data for the item
            Cursor c = null;
			try {
				c = mDba.fetchItem(mItemId);
				c.moveToFirst();
				mTitle = c.getString(c.getColumnIndex(C.db_ITEM_TITLE));
				mBody = c.getString(c.getColumnIndex(C.db_ITEM_BODY));
			} finally {
				if (c != null) {
					c.close();
				}
			}

            // set the data in the views
            mtvTitle.setText(mTitle);
            mtvBody.setText(mBody);
        } else if (Intent.ACTION_INSERT.equals(action)) {
            // Requested to insert: set that state, and create a new entry
            // in the container.
            Log.d(TAG, "Requested to insert");
            mMode = MODE_INSERT;
        } else {
            Log.e(TAG, "Unknown action, bailing");
            finish();
            return;
        }

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.item_edit, menu);
        // menu is inflated for MODE_INSERT; modify if MODE_EDIT
        if(mMode == MODE_EDIT) {
        	menu.findItem(R.id.iemenu_cancel_new).setVisible(false);
        	menu.findItem(R.id.iemenu_cancel_existing).setVisible(true);
        	menu.findItem(R.id.iemenu_delete).setVisible(true);
        }
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.iemenu_save:
			saveItem();
			return true;
		case R.id.iemenu_cancel_new:
		case R.id.iemenu_cancel_existing:
			finish();
			return true;
		case R.id.iemenu_delete:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void saveItem() {
		mTitle = mtvTitle.getText().toString();
		mBody = mtvBody.getText().toString();
		if(mMode == MODE_INSERT) {
			mDba.createItem(mListId, mTitle, mBody);
		} else if(mMode == MODE_EDIT) {
			mDba.updateItem(mItemId, mTitle, mBody);
		}
		finish();
	}

}
