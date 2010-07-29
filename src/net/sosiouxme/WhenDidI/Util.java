package net.sosiouxme.WhenDidI;

import java.util.Date;

import net.sosiouxme.WhenDidI.receiver.AlarmReceiver;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
A few utility methods that didn't seem to go anywhere else.

@author Luke Meyer, Copyright 2010
See LICENSE file for this file's GPLv3 distribution license.
*/

public class Util {
	/*
	public static <K,V> Map<K,V> newMap() {
	    return new HashMap<K,V>();
	}
	
	public static <K> Set<K> newSet() {
	    return new HashSet<K>();
	}
	*/
	
		/**
		 * An English string representing about how long ago the "from" date
		 * is from the "now" date.
		 */
	public static String getTimeSince(Date from, Date now) {
		long diff = (now.getTime() - from.getTime()) / 1000; // seconds
		
		if(diff < 0)
			return "In the future";
		if(diff <= 2)
			return "Just now";
		if(diff < 120)
			return "" + diff + " seconds ago";
		
		diff = diff / 60; // minutes
		if(diff < 120)
			return "" + diff + " minutes ago";
		
		diff = diff / 60; // hours
		if(diff < 48)
			return "" + diff + " hours ago";
		
		diff = diff / 24; // days (ignoring DST)
		if(diff < 14)
			return "" + diff + " days ago";
		if(diff > 365)
			return "" + (diff / 365) + " years ago"; // roughly
		else
			return "" + (diff / 7) + " weeks ago";
	}

		/**
		 * Hands an alarm to the alarm manager to go off later.
		 * 
		 * @param context Application context
		 * @param trackerId rowId of the tracker that the alarm refers to
		 * @param time Time at which to go off (system time in milliseconds)
		 */

	public static void setAlarm(Context context, long trackerId, long time) {
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra(C.db_ALARM_TRACKER, trackerId);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				context, AlarmManager.RTC_WAKEUP, intent, 
				PendingIntent.FLAG_CANCEL_CURRENT  // keeps from re-using the previous intent
				);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
	}
}
