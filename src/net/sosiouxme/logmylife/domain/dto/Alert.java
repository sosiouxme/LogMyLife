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

package net.sosiouxme.logmylife.domain.dto;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import android.net.Uri;

import net.sosiouxme.logmylife.C;

public class Alert extends AbstractDTO implements Serializable {

	// necessary to make this serializable
	private static final long serialVersionUID = 1L;

	public long trackerId = -1;
	// Specify the interval for the alert to go off after, one component at a time
	/** Number of months to add in the alert interval */
	public int ivalMonths = 0;
	/** Number of weeks to add in the alert interval */
	public int ivalWeeks = 0;
	/** Number of days to add in the alert interval */
	public int ivalDays = 0;
	/** Number of hours to add in the alert interval */
	public int ivalHours = 0;
	/** Number of minutes to add in the alert interval */
	public int ivalMinutes = 0;
	/** Number of seconds to add in the alert interval */
	public int ivalSeconds = 0;
	/** The next time the alert for this tracker will go off 
	 * (DateTime stored as a long) */
	public Date nextTime = null;
	/** The ringtone to play when the alert goes off (empty if default) */
	public String ringtone = null;
	/** Whether the alert is enabled and will notify */
	public boolean isEnabled = true;
	/** Whether to skip the next alert */
	public boolean skipNext = false;
	
	public Alert(long id) {
		super(id);
	}

	public long getTrackerId() {
		return trackerId;
	}
	
	public Date getNextTime() {
		return nextTime;
	}

	public String getRingtone() {
		return ringtone;
	}

	public Uri getRingtoneUri() {
		return ringtone == null ? null : Uri.parse(ringtone);
	}

	public boolean getIsEnabled() {
		return isEnabled;
	}

	public boolean getSkipNext() {
		return skipNext;
	}

	public int getIvalMonths() {
		return ivalMonths;
	}

	public int getIvalWeeks() {
		return ivalWeeks;
	}

	public int getIvalDays() {
		return ivalDays;
	}

	public int getIvalHours() {
		return ivalHours;
	}

	public int getIvalMinutes() {
		return ivalMinutes;
	}

	public int getIvalSeconds() {
		return ivalSeconds;
	}

	public void setIvalMonths(int ivalMonths) {
		if(this.ivalMonths != ivalMonths)
			changed.put(C.db_ALERT_INTERVAL_MONTHS, this.ivalMonths = ivalMonths);
	}

	public void setIvalWeeks(int ivalWeeks) {
		if(this.ivalWeeks != ivalWeeks)
			changed.put(C.db_ALERT_INTERVAL_WEEKS, this.ivalWeeks = ivalWeeks);
	}

	public void setIvalDays(int ivalDays) {
		if(this.ivalDays != ivalDays)
			changed.put(C.db_ALERT_INTERVAL_DAYS, this.ivalDays = ivalDays);
	}

	public void setIvalHours(int ivalHours) {
		if(this.ivalHours != ivalHours)
			changed.put(C.db_ALERT_INTERVAL_HOURS, this.ivalHours = ivalHours);
	}

	public void setIvalMinutes(int ivalMinutes) {
		if(this.ivalMinutes != ivalMinutes)
			changed.put(C.db_ALERT_INTERVAL_MINUTES, this.ivalMinutes = ivalMinutes);
	}

	public void setIvalSeconds(int ivalSeconds) {
		if(this.ivalSeconds != ivalSeconds)
			changed.put(C.db_ALERT_INTERVAL_SECONDS, this.ivalSeconds = ivalSeconds);
	}

	public void setTrackerId(long trackerId) {
		if(this.trackerId != trackerId)
			changed.put(C.db_ALERT_TRACKER, this.trackerId = trackerId);
	}

	public void setNextTime(Date nextTime) {
		if (nextTime != null && nextTime.equals(this.nextTime)) return;
		this.nextTime = nextTime;
		changed.put(C.db_ALERT_NEXT_TIME, nextTime == null ? null : C.dbDateFormat.format(nextTime));
	}	

	public void setRingtone(String ringtone) {
		if(ringtone != null && ringtone.equals(this.ringtone)) return;
		this.ringtone = ringtone;
		changed.put(C.db_ALERT_RINGTONE,ringtone);
	}
	
	public void setRingtone(Uri u) {
		setRingtone(u == null ? null : u.toString());
	}
	
	public void setIsEnabled(boolean enabled) {
		if(enabled == isEnabled) return;
		isEnabled = enabled;
		changed.put(C.db_ALERT_ENABLED,enabled);
	}

	public void setSkipNext(boolean skip) {
		if(skip == skipNext) return;
		skipNext = skip;
		changed.put(C.db_ALERT_SKIP_NEXT,skip);
	}

	// TODO: all this stuff regarding intervals might really not belong in a DTO.
	// Should probably move to some util class or something.
	
	public int getSingleIval(Interval i) {
		switch (i) {
		case MONTHS:
			return ivalMonths;
		case WEEKS:
			return ivalWeeks;
		case DAYS:
			return ivalDays;
		case HOURS:
			return ivalHours;
		case MINUTES:
			return ivalMinutes;
		case SECONDS:
			return ivalSeconds;
		}
		return 0;
	}

	public void setSingleIval(Interval i, int value) {
		switch (i) {
		case MONTHS:
			setIvalMonths(value);
			break;
		case WEEKS:
			setIvalWeeks(value);
			break;
		case DAYS:
			setIvalDays(value);
			break;
		case HOURS:
			setIvalHours(value);
			break;
		case MINUTES:
			setIvalMinutes(value);
			break;
		case SECONDS:
			setIvalSeconds(value);
			break;
		}
	}

	public void clearIvals() {
		setIvalMonths(0);
		setIvalWeeks(0);
		setIvalDays(0);
		setIvalHours(0);
		setIvalMinutes(0);
		setIvalSeconds(0);
	}

	public void setTotalAlertValue(Interval i, int value) {
		clearIvals();
		setSingleIval(i, value);
	}

	public Interval getFirstIntervalSet() {
		for (Interval ival : Interval.values()) {
			if (getSingleIval(ival) > 0)
				return ival;
		}
		return Interval.MONTHS;
	}

	public enum Interval {
		MONTHS(Calendar.MONTH), WEEKS(Calendar.WEEK_OF_YEAR), DAYS(Calendar.DAY_OF_YEAR),
		HOURS(Calendar.HOUR_OF_DAY), MINUTES(Calendar.MINUTE), SECONDS(Calendar.SECOND);
		// which is the order they must be on the menu
		
		private final int calendarField;
		
		private Interval(int field) {
			calendarField = field;
		}
		
		public int getCalField() {
			return calendarField;
		}

		public static Interval getIntervalForPos(int pos) {
			for(Interval i: Interval.values()) {
				if(i.ordinal() == pos) 
					return i;
			}
			return null;
		}
	}
	
	public Date getIntervalFrom(Date stamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(stamp);
		for(Interval i: Interval.values()) {
			cal.add(i.getCalField(), getSingleIval(i));
		}
		return cal.getTime();
	}
	
	public Date calculateNextTime(Date lastTime) {
		if(lastTime == null) return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(lastTime);
		for (Interval i : Interval.values()) {
			int value = getSingleIval(i);
			if(value < 1) continue;
			cal.add(i.getCalField(), value);			
		}
		return cal.getTime();
	}
	
	public static Date calculateNextTime(Date lastTime, Interval i, int value) {
		if(value < 1) return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(lastTime);
		cal.add(i.getCalField(), value);
		return cal.getTime();
	}
	
	public void setNextTimeFromLast(LogEntry lastLog) {
		setNextTime((lastLog == null) ? null : calculateNextTime(lastLog.getLogDate()));
	}
}
