/*
    This file is part of LogMyLife, an application for logging events.
    Copyright (C) 2011 Luke Meyer

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program (see LICENSE file).
    If not, see http://www.gnu.org/licenses/
*/

package net.sosiouxme.logmylife;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.webkit.WebView;

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
	
	/**
	 * 
	 * @param a Activity to attach this dialog to
	 * @param filename file in assets/dialogs/ dir which contains the HTML
	 * @return Builder for creating an AlertDialog with the HTML content
	 */
	public static Builder getHtmlDialogBuilder(Activity a, String filename) {
		WebView wv = (WebView) a.getLayoutInflater().inflate(R.layout.d_generic_info, null);
		wv.loadUrl("file:///android_asset/dialogs/" + filename);
		return new AlertDialog.Builder(a)
					.setView(wv)
					.setPositiveButton(R.string.info_dialog_dismiss_button, null);
	}

	/**
	 * 
	 * @param a Activity to attach this dialog to
	 * @param resId string for file in assets/dialogs/ dir which contains the HTML
	 * @return Builder for creating an AlertDialog with the HTML content
	 */
	public static Builder getHtmlDialogBuilder(Activity a, int rId) {
		return getHtmlDialogBuilder(a, a.getString(rId));
	}
}
