package net.sosiouxme.WhenDidI.custom;

import net.sosiouxme.WhenDidI.R;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
/* 
 * Defines a dialog that looks for an EditText field and a Button
 * (with IDs R.id.editor and R.id.ok, respectively)
 * and ties the OK button to the editor in such a way that the OK
 * button is disabled unless something is present in the editor.
 * If a button with ID R.id.cancel exists, it can be used to
 * cancel the dialog.
 * 
 * Requires that subclasses call setContentView() with appropriate
 * content. Must also define onClickOk() to handle when the OK button
 * is successfully pushed.
 */
public abstract class RequiredFieldDialog extends Dialog
	implements OnClickListener, OnDismissListener {

	private static final String TAG = "WDI.ReqFieldDialog";
	protected Button mOkButton = null;
	protected EditText mEditor = null;
	
	public RequiredFieldDialog(Context owner, int themeId) {
		super(owner, themeId);
	}
	
	public RequiredFieldDialog(Context owner) {
		this(owner, android.R.style.Theme);
	}
	
	

	/*
	 * Defines the content for this dialog.
	 * @see android.app.Dialog#setContentView(int)
	 * Should include an EditText with ID "editor", a Button
	 * with ID "ok", and optionally a Button with ID "cancel".
	 */
	@Override
	public void setContentView(int viewId) {
		super.setContentView(viewId);
		
		// make sure only the dialog has focus
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
	             WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

		// wire the buttons listener
		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(this);
		mOkButton = (Button) findViewById(R.id.ok);
		mOkButton.setOnClickListener(this);
		mOkButton.setEnabled(false);
		
		//wire up the text to enable/disable the create button
		mEditor = (EditText) findViewById(R.id.editor);
		mEditor.addTextChangedListener(new RequireTextFor(mOkButton, mEditor));
		mEditor.setText("");

		setOnDismissListener(this);
	}

	@Override
	public void onClick(View v) {
		Log.d(TAG, "onClick RequiredFieldDialog");
		if(v == mOkButton)
			onClickOk();
		this.dismiss();
	}

	/*
	 * Callback for when the OK button is clicked.
	 */
	protected abstract void onClickOk();

	@Override
	public void onDismiss(DialogInterface dialog) {
		// clear for next time dialog is called
		mEditor.setText("");
		mOkButton.setEnabled(false);
	}
}
