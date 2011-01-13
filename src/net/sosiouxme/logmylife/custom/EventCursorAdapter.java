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

package net.sosiouxme.logmylife.custom;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import net.sosiouxme.logmylife.C;
import net.sosiouxme.logmylife.activity.Settings;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public abstract class EventCursorAdapter extends SimpleCursorAdapter {

	//TODO: look into implementing the Filterable interface
	protected static final String TAG = "LML.EventCursorAdapter";
	protected DateFormat mDateTimeFormat;
	
	public EventCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		refreshDateTimeFmt(context);
	}

	public void refreshDateTimeFmt(Context context) {
		mDateTimeFormat = Settings.getDateTimeFormat(context);
	}

	public void requery() {
		Log.d(TAG, "requery");
		getCursor().requery();
	}

	protected String getTextForDate(TextView v, String text) {
		if (text != null && text.length() > 0) {

			// try to reformat date text for the field
			try {
				Date d = C.dbDateFormat.parse(text);
				text = formatDate(v, d);
			} catch (ParseException e) {
				Log.w(TAG, "Date parsing failed for " + text, e);
			}
		}
		return text;
	}

	protected abstract String formatDate(TextView v, Date d);

	// hide the view if there won't be any text to display
	public static void conditionallyHideView(View view, String text) {
		conditionallyHideView(view, text == null || text.length() == 0);
	}
	
	public static void conditionallyHideView(View view, boolean hide) {
		view.setVisibility(hide ? View.GONE : View.VISIBLE);
	}

}
