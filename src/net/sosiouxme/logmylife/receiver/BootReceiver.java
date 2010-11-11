package net.sosiouxme.logmylife.receiver;

import net.sosiouxme.logmylife.domain.DbAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/*
 * Receive the android.intent.action.BOOT_COMPLETED broadcast event
 * so as to schedule the next alert at boot time.
 */

public class BootReceiver extends BroadcastReceiver {

	private static final String TAG = "LML.BootR";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "In BootReceiver.onReceive");
		DbAdapter db = new DbAdapter(context);
		AlertReceiver.setNextAlert(context, db);
		db.close();
	}

}
