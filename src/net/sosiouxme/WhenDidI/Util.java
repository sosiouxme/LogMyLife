package net.sosiouxme.WhenDidI;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Util {
	public static <K,V> Map<K,V> newMap() {
	    return new HashMap<K,V>();
	}
	public static <K> Set<K> newSet() {
	    return new HashSet<K>();
	}
	
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
}
