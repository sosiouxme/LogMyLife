/*
    This file is part of LogMyLife, an application for logging events.
    Copyright (C) 2011 Luke Meyer

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program (see LICENSE file).
    If not, see http://www.gnu.org/licenses/
*/

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
