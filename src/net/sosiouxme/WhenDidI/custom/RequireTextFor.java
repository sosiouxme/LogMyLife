package net.sosiouxme.WhenDidI.custom;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
/* 
 * Ties the OK button to the editor in such a way that the OK
 * button is disabled unless something is present in the editor.
 * 
 */
public class RequireTextFor implements OnKeyListener {

	private Button mOkButton = null;
	
	public RequireTextFor(Button mOkButton, TextView text) {
		super();
		this.mOkButton = mOkButton;

		// wire the buttons listener
		mOkButton.setEnabled(text.getText().length() > 0);

	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		mOkButton.setEnabled(((EditText)v).getText().length() > 0);			
		return false;
	}
}
