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
