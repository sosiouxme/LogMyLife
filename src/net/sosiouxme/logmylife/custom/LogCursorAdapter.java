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

import net.sosiouxme.logmylife.C;
import net.sosiouxme.logmylife.R;
import net.sosiouxme.logmylife.domain.dto.Tracker;
import net.sosiouxme.logmylife.domain.dto.ValueType;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class LogCursorAdapter extends EventCursorAdapter {

//	private static final String TAG = "LML.LogCA";
	private final Tracker mTracker;  // specified when we're showing logs

	// specify tracker to be shared by all logs in list
	public LogCursorAdapter(Context context, Tracker tracker, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		setViewBinder(new ValueBinder()); // special handling for value
		mTracker = tracker;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		if(convertView == null) {
			// set up labels once for a newly-generated view
			String label = mTracker.getLogValueLabel();
			if(label != null && label.length() > 0) {				
				TextView labelTV = (TextView) v.findViewById(
						(mTracker.getLogValueLabelPos() == Tracker.LABEL_LEFT) ? 
								R.id.logValLabelPre : R.id.logValLabelPost);
				labelTV.setText(label);
				labelTV.setVisibility(View.VISIBLE);
			}
		}
		// determine per item whether value needs showing at all
		Cursor item = ((Cursor) getItem(position));
		conditionallyHideView(v.findViewById(R.id.valueContainer), 
				item.isNull(item.getColumnIndex(C.db_LOG_VALUE)));
		return v;
	}

	@Override
	protected String formatDate(TextView v, Date d) {
		return mDateTimeFormat.format(d);
	}

	@Override
	public void setViewText(TextView v, String text) {
		switch(v.getId()) {		
		case R.id.logTime: 
			text = getTextForDate(v, text);
			break;
		case R.id.logBody:
			conditionallyHideView(v, text);
			break;
		}
		super.setViewText(v, text);
	}
	
	class ValueBinder implements SimpleCursorAdapter.ViewBinder {

		@Override
		public boolean setViewValue(View view, Cursor c, int columnIndex) {
			if(view.getId() == R.id.logValue) {
				ValueType type = ValueType.getById(c.getLong(c.getColumnIndex(C.db_LOG_VALUE_TYPE)));
				Number value = type.getValueFromCursor(c, c.getColumnIndex(C.db_LOG_VALUE));
				((TextView) view).setText(type.formatValue(value));
				return true;
			}
			// otherwise let it be handled normally
			return false;
		}
		
	}
}
