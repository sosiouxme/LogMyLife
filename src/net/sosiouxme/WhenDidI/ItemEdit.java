package net.sosiouxme.WhenDidI;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class ItemEdit extends Activity {
	// Logger tag
    private static final String TAG = "WDI.ItemEdit";
    
    // Identifiers for our menu items.
    private static final int MENU_SAVE_ID = Menu.FIRST;
    private static final int MENU_CANCEL_ID = Menu.FIRST + 1;
    private static final int MENU_DELETE_ID = Menu.FIRST + 2;

    // The different distinct modes the activity can be run in.
    private static final int MODE_INSERT = 0;
    private static final int MODE_EDIT = 1;
    private int mMode = MODE_INSERT;

    // the Item being handled
    private long mItemId = 0;
    private long mListId = 0;
    private String mTitle;
    private String mBody;
    
	private DbAdapter mDba;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		mDba = new DbAdapter(this).open();
        final Intent intent = getIntent();

        // Do some setup based on the action being performed.

        final String action = intent.getAction();
        if (Intent.ACTION_EDIT.equals(action)) {
            // Requested to edit: set that state, and the data being edited.
            Log.d(TAG, "Requested to edit");
            mMode = MODE_EDIT;
            // TODO
        } else if (Intent.ACTION_INSERT.equals(action)) {
            // Requested to insert: set that state, and create a new entry
            // in the container.
            Log.d(TAG, "Requested to insert");
            mMode = MODE_INSERT;
            mListId = intent.getExtras().getLong(DbAdapter.ITEM_LIST);
        } else {
            Log.e(TAG, "Unknown action, bailing");
            finish();
            return;
        }

        // Set the layout for this activity.
        setContentView(R.layout.item_edit);
        
        // There will be a menu on this
        //registerForContextMenu(findViewById(R.layout.item_edit));

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MENU_SAVE_ID, 0, R.string.menu_save_item);
        if(mMode == MODE_INSERT) {
	        menu.add(Menu.NONE, MENU_CANCEL_ID, 0, R.string.menu_cancel_item);
	        menu.add(Menu.NONE, MENU_DELETE_ID, 0, R.string.menu_delete_item);
        } else {
	        menu.add(Menu.NONE, MENU_CANCEL_ID, 0, R.string.menu_revert_item);
        }
        return true;   
    }
    
}
