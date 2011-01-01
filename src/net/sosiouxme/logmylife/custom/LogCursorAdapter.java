package net.sosiouxme.logmylife.custom;

import java.util.Date;

import net.sosiouxme.logmylife.C;
import net.sosiouxme.logmylife.R;
import net.sosiouxme.logmylife.domain.dto.Tracker;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LogCursorAdapter extends EventCursorAdapter {

//	private static final String TAG = "LML.LogCA";
	private final Tracker mTracker;  // specified when we're showing logs

	// specify tracker to be shared by all logs in list
	public LogCursorAdapter(Context context, Tracker tracker, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
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
		String value = item.getString(item.getColumnIndex(C.db_LOG_VALUE));
		conditionallyHideView(v.findViewById(R.id.valueContainer), value);

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
}
