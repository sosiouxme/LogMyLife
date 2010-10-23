package net.sosiouxme.logmylife.custom;

import java.text.ParseException;
import java.util.Date;

import net.sosiouxme.logmylife.C;
import net.sosiouxme.logmylife.R;
import net.sosiouxme.logmylife.Util;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class EventCursorAdapter extends SimpleCursorAdapter {

	//TODO: look into implementing the Filterable interface
	private static final String TAG = "LML.EventCursorAdapter";

	public EventCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
	}

	@Override
	public void setViewText(TextView v, String text) {
		int id = v.getId();
		if (id == R.id.logTime || id == R.id.lastLog) {
			v.setTag(null); // make sure this is reset as it's used by updater
			if (text != null && text.length() > 0) {

				// try to reformat date text for the field
				try {
					Date d = C.dbDateFormat.parse(text);
					if (id == R.id.logTime)
						//text = mDateFormat.format(d) + " "
						//		+ mTimeFormat.format(d);
					    text = d.toLocaleString();
					else if (id == R.id.lastLog) {
						v.setTag(d);
						text = Util.getTimeSince(d, new Date());
					}
				} catch (ParseException e) {
					Log.d(TAG, "Date parsing failed for " + text);
				}
			}

		}
		super.setViewText(v, text);
	}

	public void requery() {
		Log.d(TAG, "requery");
		getCursor().requery();
	}

}
