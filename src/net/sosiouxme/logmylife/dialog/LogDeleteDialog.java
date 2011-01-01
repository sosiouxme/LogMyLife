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

package net.sosiouxme.logmylife.dialog;

import net.sosiouxme.logmylife.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface.OnClickListener;

public class LogDeleteDialog {
	
	public static Dialog create(Activity activity, OnClickListener listener) {
		Dialog d = new AlertDialog.Builder(activity)
		.setTitle(R.string.ldd_title)
		.setMessage(R.string.ldd_msg)
		.setNegativeButton(R.string.ldd_cancel_button, null)
		.setPositiveButton(R.string.ldd_delete_button, listener)
		.create();
		d.setOwnerActivity(activity); // why can't the builder do this?
		return d;

	}
}
