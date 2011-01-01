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

	public static void conditionallyHideView(View view, String text) {
		view.setVisibility((text == null || text.length() == 0) ? View.GONE : View.VISIBLE);
	}

}
