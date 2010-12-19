package net.sosiouxme.logmylife.receiver;

import net.sosiouxme.logmylife.C;
import net.sosiouxme.logmylife.R;
import net.sosiouxme.logmylife.activity.Settings;
import net.sosiouxme.logmylife.activity.TrackerDetail;
import net.sosiouxme.logmylife.domain.DbAdapter;
import net.sosiouxme.logmylife.domain.dto.Alert;
import net.sosiouxme.logmylife.domain.dto.Tracker;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class AlertReceiver extends BroadcastReceiver {

	private static final String TAG = "LML.AlertReceiver";

	public void onReceive(Context context, Intent intent) {
        long alertId = intent.getLongExtra(C.db_ALERT_TABLE, -1);
		DbAdapter db = new DbAdapter(context);

		setNextAlert(context, db);

		// get this alert and make a notification
		Alert alert = (alertId > 0) ? db.fetchAlert(alertId) : null;
		Tracker tracker;
		if(alert == null) {
	        Log.w(TAG, "No alert matches intent!");
	        db.close();
	        return;
		} else {
			tracker = db.fetchTracker(alert.getTrackerId());
			if(tracker == null) {
		        Log.e(TAG, "No tracker matches alert " + alert.getId());
		        db.close();
		        return;
			}
		}
		db.close();

		// create the notification to show the user
		Notification notification = new Notification(
//				android.R.drawable.stat_notify_error,
				R.drawable.ic_stat_lml_notif,
				context.getString(R.string.alert_prefix) + tracker.getName(),
				System.currentTimeMillis()
				);		
		notification.flags = Notification.DEFAULT_SOUND 
		   | Notification.FLAG_AUTO_CANCEL
		   ;
		if(!Settings.getQuietHours(context).isQuietTime())
			notification.sound = alert.getRingtoneUri(); 
		// notification.vibrate = (long[]) intent.getExtras().get("vibrationPatern");
		/*or*
		notification.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_SHOW_LIGHTS;
		notification.ledARGB = Color.GREEN;
		notification.ledOnMS = 1000;
		notification.ledOffMS = 500;
		notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
		*/
		// set up the activity that will start when the user clicks the notification
		Intent showTrackerIntent = new Intent(context, TrackerDetail.class)
											.putExtra(C.db_ID, tracker.getId())
				// the data keeps the PendingIntents distinguishable:
											.setData(Uri.parse("alert://"+alertId));
		PendingIntent showTrackerPI = PendingIntent.getActivity(context, 0,	showTrackerIntent, 0);
		notification.setLatestEventInfo(context, context.getString(R.string.alert_message), tracker.getName(), showTrackerPI);

		// use notification manager to send the notification
		NotificationManager notifMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notifMgr.notify((int)alertId, notification); // re-use alertId as notification Id
	}

	public static void setNextAlert(Context context, DbAdapter db) {
		// reset alert for next time a tracker is due
		Alert nextAlert = db.fetchNextAlert();
		if(nextAlert != null) {
			setAlert(context, nextAlert.getId(), nextAlert.getNextTime().getTime());
		}
	}

	/**
	 * 
	 * Hands an alarm to the alarm manager to go off later.
	 * 
	 * @param context
	 *            Application context
	 * @param alertId
	 *            rowId of the tracker that the alert refers to
	 * @param time
	 *            Time at which to go off (system time in milliseconds)
	 */

	public static void setAlert(Context context, long alertId, long time) {
		Log.d(TAG, "Setting alert for alert " + alertId);
		Intent intent = new Intent(context, AlertReceiver.class);
		intent.putExtra(C.db_ALERT_TABLE, alertId);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				AlarmManager.RTC_WAKEUP, intent,
				PendingIntent.FLAG_CANCEL_CURRENT // keeps from re-using the
													// previous intent
				);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		if(time > 0 )
			alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
	}

	public static void clearAlert(Context context) {
		setAlert(context, 0, 0);
	}
}
