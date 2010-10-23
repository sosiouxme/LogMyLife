package net.sosiouxme.logmylife.receiver;

import net.sosiouxme.logmylife.C;
import net.sosiouxme.logmylife.activity.TrackerDetail;
import net.sosiouxme.logmylife.domain.DbAdapter;
import net.sosiouxme.logmylife.domain.dto.Alarm;
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
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

	private static final String TAG = "LML.AlarmReceiver";

	public void onReceive(Context context, Intent intent) {
        long trackerId = intent.getLongExtra(C.db_ALARM_TRACKER, -1);
		DbAdapter Dba = new DbAdapter(context);
		Tracker tracker = (trackerId > 0) ? Dba.fetchTracker(trackerId) : null;
        Toast.makeText(context, "Alarm firing for " + tracker.name, Toast.LENGTH_SHORT).show();

		// reset alarm for next time a tracker is due
		Alarm nextAlarm = Dba.fetchNextAlarm();
		if(nextAlarm != null) {
			setAlarm(context, nextAlarm.getTrackerId(), nextAlarm.nextTime.getTime());
		}
		Dba.close();

		// create the notification to show the user
		Notification notification = new Notification(
				android.R.drawable.stat_notify_error,
				"LogMyLife Alarm - " + tracker.name,
				System.currentTimeMillis()
				);		
		notification.flags = Notification.DEFAULT_SOUND 
		   | Notification.FLAG_AUTO_CANCEL
		   ;
		/*
	 	notification.sound = (Uri) intent.getParcelableExtra("Ringtone"); 
		notification.vibrate = (long[]) intent.getExtras().get("vibrationPatern");
		*or*
		notification.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_SHOW_LIGHTS;
		notification.ledARGB = Color.GREEN;
		notification.ledOnMS = 1000;
		notification.ledOffMS = 500;
		notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
		*/
		// set up the activity that will start when the user clicks the notification
		Intent showTrackerIntent = new Intent(context, TrackerDetail.class)
											.putExtra(C.db_ID, trackerId)
				// the data keeps the PendingIntents distinguishable:
											.setData(Uri.parse("tracker://"+trackerId));
		PendingIntent showTrackerPI = PendingIntent.getActivity(context, 0,	showTrackerIntent, 0);
		String trackerMsg = (tracker == null) ? "Unknown alarm" : tracker.name;
		notification.setLatestEventInfo(context, "LogMyLife Alarm", trackerMsg, showTrackerPI);

		// use notification manager to send the notification
		NotificationManager notifMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notifMgr.notify((int)trackerId, notification); // re-use trackerId as notification Id
	}

	/**
	 * Hands an alarm to the alarm manager to go off later.
	 * 
	 * @param context
	 *            Application context
	 * @param trackerId
	 *            rowId of the tracker that the alarm refers to
	 * @param time
	 *            Time at which to go off (system time in milliseconds)
	 */

	public static void setAlarm(Context context, long trackerId, long time) {
		Log.d(TAG, "Setting alarm for tracker " + trackerId);
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra(C.db_ALARM_TRACKER, trackerId);
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

	public static void clearAlarm(Context context) {
		setAlarm(context, 0, 0);
	}
}
