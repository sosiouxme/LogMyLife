package net.sosiouxme.WhenDidI.custom;

import net.sosiouxme.WhenDidI.R;
import net.sosiouxme.WhenDidI.activity.TrackerGroup;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;

public abstract class RequiredFieldDialog extends Dialog implements OnClickListener, OnKeyListener, OnDismissListener {

	private final int mTextViewId;
	private final int mButtonViewId;
	private final Activity mOwner;
	private final int mContentViewId;
	private final int mTitleId;
	private Button mButton = null;
	private EditText mEditText = null;
	
	public RequiredFieldDialog(Activity owner, int contentViewId, int titleId, 
							int textViewId, int buttonViewId, int themeId) {
		super(owner, themeId);
		mOwner = owner;
		mContentViewId = contentViewId;
		mTitleId = titleId;
		mTextViewId = textViewId;
		mButtonViewId = buttonViewId;
	}
	
	public RequiredFieldDialog(Activity owner, int contentViewId, int titleId, int textViewId, int buttonViewId) {
		this(owner, contentViewId, titleId, textViewId, buttonViewId, android.R.style.Theme);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(mContentViewId);
		this.setOwnerActivity(mOwner);
		this.setTitle(mTitleId);
		
		// make sure only the dialog has focus
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
	             WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);


		// wire up the buttons
		
		/* TODO: this was the button that broke the camel's back. i shouldn't
		 * have to specify all the buttons in the constructor, but i need to
		 * know what they are in order to wire them up with listeners.
		 * bah! what i really wanted was an AlertDialog with an EditText and
		 * some magic validation dust.
		 */
		Button cancel = (Button) findViewById(mButtonViewId);
		cancel.setOnClickListener(this);
		
		mButton = (Button) findViewById(mButtonViewId);
		mButton.setOnClickListener(this);
		
		//wire up the title text to enable/disable the create button
		mEditText = (EditText) findViewById(mTextViewId);
		mEditText.setOnKeyListener(this);
		// and we'll need to reset both text fields when the dialog is dismissed
		this.setOnDismissListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == mButtonViewId)
			onAccepted();
		this.dismiss();
	}

	protected abstract void onAccepted();

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if(((EditText)v).getText().length() > 0) {
			mButton.setEnabled(true);			
		} else {
			mButton.setEnabled(false);			
		}
		return false;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// clear for next time dialog is called
		mEditText.setText("");
		mButton.setEnabled(false);
	}
}
