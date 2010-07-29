package net.sosiouxme.WhenDidI.receiver;

import net.sosiouxme.WhenDidI.C;
import net.sosiouxme.WhenDidI.Util;
import net.sosiouxme.WhenDidI.activity.TrackerDetail;
import net.sosiouxme.WhenDidI.domain.DbAdapter;
import net.sosiouxme.WhenDidI.domain.dto.Alarm;
import net.sosiouxme.WhenDidI.domain.dto.Tracker;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {
        long trackerId = intent.getLongExtra(C.db_ALARM_TRACKER, -1);
		DbAdapter Dba = new DbAdapter(context);
		Tracker tracker = (trackerId > 0) ? Dba.fetchTracker(trackerId) : null;
        Toast.makeText(context, "Alarm firing for " + tracker.name, Toast.LENGTH_SHORT).show();

		// reset alarm for next time a tracker is due
		Alarm nextAlarm = Dba.fetchNextAlarm();
		if(nextAlarm != null) {
			Tracker t = Dba.fetchTracker(nextAlarm.trackerId);
			// TODO: here's an idea of how the alarm will be set
			Util.setAlarm(context, t.id, nextAlarm.nextTime.getTime());
		}
		Dba.close();

		// create the notification to show the user
		Notification notification = new Notification(
				android.R.drawable.stat_notify_error,
				"WhenDidI Alarm - " + tracker.name,
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
		notification.setLatestEventInfo(context, "WhenDidI Alarm", trackerMsg, showTrackerPI);

		// use notification manager to send the notification
		NotificationManager notifMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notifMgr.notify((int)trackerId, notification); // re-use trackerId as notification Id
	}

}
