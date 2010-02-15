package net.sosiouxme.WhenDidI.activity;

import net.sosiouxme.WhenDidI.C;
import net.sosiouxme.WhenDidI.DbAdapter;
import net.sosiouxme.WhenDidI.R;
import net.sosiouxme.WhenDidI.WhenDidI;
import net.sosiouxme.WhenDidI.custom.GroupSpinner;
import net.sosiouxme.WhenDidI.custom.RequireTextFor;
import net.sosiouxme.WhenDidI.custom.GroupSpinner.OnGroupSelectedListener;
import net.sosiouxme.WhenDidI.dialog.TrackerDeleteDialog;
import net.sosiouxme.WhenDidI.model.dto.Tracker;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class TrackerEdit extends Activity implements android.view.View.OnClickListener, OnGroupSelectedListener {

	private static final String TAG = "WDI.TrackerEdit";
	private Tracker mTracker = null;
	private DbAdapter mDba = null;
	private EditText metName;
	private EditText metBody;
	private GroupSpinner mSpinner;
	private long mCurrentGroupId = 0;
	private boolean saveOnFinish = true; // by default, back button will save

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// get a DB handle
		mDba = new DbAdapter(this).open();

		long trackerId = 0;
		Bundle e = getIntent().getExtras();
		if(e!= null) trackerId = e.getLong(C.db_ID);
		
		if(trackerId > 0) {
			// get item info
			mTracker = mDba.fetchTracker(trackerId);
			if(mTracker==null) {
				finish(); // nothing to do...
				return;
			}
		}
		
		// Set the layout for this activity.
		setContentView(R.layout.a_tracker_edit);
		
		// locate and fill the necessary elements of the layout
		metName = (EditText) findViewById(R.id.name);
		metBody = (EditText) findViewById(R.id.body);

		if(mTracker != null) {			
			// set the data in the views
			metName.setText(mTracker.name);
			metBody.setText(mTracker.body);
		}
		
		// wire up the buttons
		Button cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
		Button okButton = (Button) findViewById(R.id.ok);
		metName.addTextChangedListener(new RequireTextFor(okButton, metName));
		okButton.setOnClickListener(this);
		
		fillGroupSpinner();
	}
	
	/* get the cursor with all lists and attach to the spinner */
	private void fillGroupSpinner() {
		Log.d(TAG, "fillGroupSpinner");
		mSpinner = new GroupSpinner(this,
				(Spinner) findViewById(R.id.group_spinner), mDba);
		mSpinner.setOnGroupSelectedListener(this);
		mCurrentGroupId = mSpinner.getSelectedItemId();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.tracker_edit, menu);
		if(mTracker != null) {
			// modify menus to show edit mode
			menu.findItem(R.id.cancel_new).setVisible(false);
			menu.findItem(R.id.cancel_existing).setVisible(true);
			menu.findItem(R.id.delete).setVisible(true);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.save:
			if(saveTracker()) {
				saveOnFinish = false;
				finish();
			}
			break;
		case R.id.cancel_new:
		case R.id.cancel_existing:
			saveOnFinish = false;
			finish();
			return true;
		case R.id.delete:
			deleteTracker();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ok:
			if (saveTracker()) {
				saveOnFinish = false;
				finish();
			}
			break;
		case R.id.cancel:
			saveOnFinish = false;
			finish();
			break;
		}
	}
	
	

	@Override
	protected void onPause() {
		if(isFinishing() && saveOnFinish ) {
			// if we are finishing and need to save (e.g. back button), do so
			saveTracker();
		}
		super.onPause();
	}

	private void deleteTracker() {
		final long id = mTracker.id;
		TrackerDeleteDialog.create(this, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "DeleteDialog onClick " + id);
				mDba.deleteTracker(id);
				saveOnFinish = false;
				((WhenDidI) getApplication()).showToast(C.TOAST_TRACKER_DELETED);
				finish();
			}
		}).show();
	}

	// creates/updates the tracker UNLESS the name is empty
	private boolean saveTracker() {
		String name = metName.getText().toString();
		if (name.length() > 0) {
			if(mTracker == null) {
				mDba.createTracker(mCurrentGroupId, name, metBody.getText().toString());
				((WhenDidI) getApplication()).showToast(C.TOAST_TRACKER_CREATED);
			} else {
				mTracker.setName(name);
				mTracker.setBody(metBody.getText().toString());
				mTracker.setGroupId(mCurrentGroupId);
				mDba.updateTracker(mTracker);
				((WhenDidI) getApplication()).showToast(C.TOAST_TRACKER_UPDATED);
			}
			return true;
		}
		return false;
	}
	
	@Override
	protected void onDestroy() {
		mDba.close();
		super.onDestroy();
	}



	@Override
	public void onGroupSelected(long groupId) {
		mCurrentGroupId = groupId;		
	}
}
