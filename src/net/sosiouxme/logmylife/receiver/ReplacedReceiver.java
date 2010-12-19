package net.sosiouxme.logmylife.receiver;

import net.sosiouxme.logmylife.domain.DbAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/*
 * Receive the android.intent.action.PACKAGE_REPLACED broadcast event
 * so as to schedule the next alert when the package is replaced.
 */

public class ReplacedReceiver extends BroadcastReceiver {

	private static final String TAG = "LML.RepR";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "In ReplacedReceiver.onReceive with data " + intent.getDataString());
		DbAdapter db = new DbAdapter(context);
		AlertReceiver.setNextAlert(context, db);
		db.close();
	}

}
