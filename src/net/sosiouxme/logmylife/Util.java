package net.sosiouxme.logmylife;

import java.util.Date;

/**
 * A few utility methods that didn't seem to go anywhere else.
 * 
 * @author Luke Meyer, Copyright 2010 See LICENSE file for this file's GPLv3
 *         distribution license.
 */

public class Util {
	/*
	 * public static <K,V> Map<K,V> newMap() { return new HashMap<K,V>(); }
	 * 
	 * public static <K> Set<K> newSet() { return new HashSet<K>(); }
	 */

	/**
	 * An English string representing about how long ago the "from" date is from
	 * the "now" date.
	 */
	public static String getTimeSince(Date from, Date now) {
		long diff = (now.getTime() - from.getTime()) / 1000; // seconds

		if (diff < 0)
			return "In the future";
		if (diff <= 2)
			return "Just now";
		if (diff < 120)
			return "" + diff + " seconds ago";

		diff = diff / 60; // minutes
		if (diff < 120)
			return "" + diff + " minutes ago";

		diff = diff / 60; // hours
		if (diff < 48)
			return "" + diff + " hours ago";

		diff = diff / 24; // days (ignoring DST)
		if (diff < 14)
			return "" + diff + " days ago";
		if (diff > 365)
			return "" + (diff / 365) + " years ago"; // roughly
		else
			return "" + (diff / 7) + " weeks ago";
	}


	
	public static String toString(Object o) {
		return (o == null) ? null : o.toString();
	}

	public static String toString(Object o, String defaultString) {
		return (o == null) ? defaultString : o.toString();
	}
}
