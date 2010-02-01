package net.sosiouxme.WhenDidI.dialog;

import net.sosiouxme.WhenDidI.R;
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