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

import java.util.Date;

import net.sosiouxme.logmylife.R;
import net.sosiouxme.logmylife.Util;
import android.content.Context;
import android.database.Cursor;
import android.widget.TextView;

public class TrackerCursorAdapter extends EventCursorAdapter {

	public TrackerCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
	}

	@Override
	public void setViewText(TextView v, String text) {
		switch(v.getId()) {		
		case R.id.lastLog:
			v.setTag(null); // make sure tag is reset as it's used by lastLog "ago" updater
			text = getTextForDate(v, text);
			break;
		case R.id.logDetails:
			conditionallyHideView(v, text);
			break;
		}
		super.setViewText(v, text);
	}

	@Override
	protected String formatDate(TextView v, Date d) {
		v.setTag(d); // set tag so lastLog "ago" updater can read it
		return Util.getTimeSince(d, new Date());
	}

	
}
