package net.sosiouxme.WhenDidI.custom;

import java.text.ParseException;
import java.util.Date;

import net.sosiouxme.WhenDidI.C;
import net.sosiouxme.WhenDidI.R;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class EventCursorAdapter extends SimpleCursorAdapter {

	private static final String TAG = "WDI.EventCursorAdapter";

	public EventCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setViewText(TextView v, String text) {
		if(v.getId() == R.id.logTime && text != null && text.length() > 0) {
			// try to reformat date text as a local date
			try {
				Date d = C.dbDateFormat.parse(text.toString());
				text = d.toLocaleString();
			} catch (ParseException e) {
				Log.d(TAG, "Date parsing failed for " + text);
			}
		}
		super.setViewText(v, text);
	}

	public void requery() {
		Log.d(TAG, "requery");
		getCursor().requery();
	}

	
}
