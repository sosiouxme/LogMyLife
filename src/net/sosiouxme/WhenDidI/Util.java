package net.sosiouxme.WhenDidI;

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
}
