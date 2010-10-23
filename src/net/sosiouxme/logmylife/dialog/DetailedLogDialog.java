package net.sosiouxme.logmylife.dialog;

import net.sosiouxme.logmylife.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;

public class DetailedLogDialog {
//TODO
	private static final String TAG = "LML.DetLogD";

	public static Dialog create(Activity activity, OnClickListener listener) {
		Log.d(TAG, "detailedLogDialog");
		Dialog d = new AlertDialog.Builder(activity)
		.setTitle(R.string.tdd_title)
		.setMessage(R.string.tdd_msg)
		.setNegativeButton(R.string.tdd_cancel_button, null)
		.setPositiveButton(R.string.tdd_delete_button, listener)
		.create();
		d.setOwnerActivity(activity); // why can't the builder do this?
		return d;
	}
}
