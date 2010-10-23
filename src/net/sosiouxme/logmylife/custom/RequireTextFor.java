package net.sosiouxme.logmylife.custom;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;
/* 
 * Ties the OK button to the editor in such a way that the OK
 * button is disabled unless something is present in the editor.
 * 
 */
public class RequireTextFor implements TextWatcher {

	private Button mOkButton = null;
	
	public RequireTextFor(Button mOkButton, TextView text) {
		super();
		this.mOkButton = mOkButton;

		// wire the buttons listener
		mOkButton.setEnabled(text.getText().length() > 0);

	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		mOkButton.setEnabled(s.length() > 0);
	}

	public void afterTextChanged(Editable et) {
		// nothing to do, just implementing interface method		
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// nothing to do, just implementing interface method
	}
}
