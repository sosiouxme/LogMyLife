package net.sosiouxme.logmylife.custom;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import net.sosiouxme.logmylife.C;
import net.sosiouxme.logmylife.R;
import net.sosiouxme.logmylife.Util;
import net.sosiouxme.logmylife.activity.Settings;
import net.sosiouxme.logmylife.domain.dto.Tracker;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class EventCursorAdapter extends SimpleCursorAdapter {

	//TODO: look into implementing the Filterable interface
	private static final String TAG = "LML.EventCursorAdapter";
	private Tracker mTracker = null;
	private final DateFormat mDateTimeFormat;
	
	public EventCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		mDateTimeFormat = Settings.getDateTimeFormat(context);
	}


	public EventCursorAdapter(Context context, Tracker tracker, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		mTracker = tracker;
		mDateTimeFormat = Settings.getDateTimeFormat(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		if(convertView == null && mTracker != null) { // this was a newly-generated view
			TextView logValLab = (TextView) v.findViewById(R.id.logValLabelPost);
			logValLab.setText(mTracker.getLogValueLabel());
		}
		return v;
	}

	@Override
	public void setViewText(TextView v, String text) {
		int id = v.getId();
		if (id == R.id.logTime || id == R.id.lastLog) {
			v.setTag(null); // make sure tag is reset as it's used by lastLog "ago" updater
			if (text != null && text.length() > 0) {

				// try to reformat date text for the field
				try {
					Date d = C.dbDateFormat.parse(text);
					if (id == R.id.logTime)
					    text = mDateTimeFormat.format(d);
					else if (id == R.id.lastLog) {
						v.setTag(d); // set tag so lastLog "ago" updater can read it
						text = Util.getTimeSince(d, new Date());
					}
				} catch (ParseException e) {
					Log.w(TAG, "Date parsing failed for " + text, e);
				}
			}
		}
		if (id == R.id.logDetails) {
			v.setVisibility(text == null || text.equals("") ? View.GONE : View.VISIBLE);
		}
		super.setViewText(v, text);
	}

	public void requery() {
		Log.d(TAG, "requery");
		getCursor().requery();
	}

}
