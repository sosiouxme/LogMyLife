package net.sosiouxme.WhenDidI.domain.dto;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import net.sosiouxme.WhenDidI.C;
import android.os.Parcel;
import android.os.Parcelable;

public class Alarm extends AbstractDTO implements Parcelable, Serializable {

	// necessary to make this serializable
	private static final long serialVersionUID = 1L;
	// necessary to make this retrievable from a state bundle
	public static final Parcelable.Creator<Alarm> CREATOR = new Parcelable.Creator<Alarm>() {

		public Alarm createFromParcel(Parcel in) {
			return (Alarm) in.readSerializable();
		}

		public Alarm[] newArray(int size) {
			return new Alarm[size];
		}
		
	};

	public long trackerId = -1;
	// Specify the interval for the alarm to go off after, one component at a time
	/** Number of months to add in the alarm interval */
	public int ivalMonths = 0;
	/** Number of weeks to add in the alarm interval */
	public int ivalWeeks = 0;
	/** Number of days to add in the alarm interval */
	public int ivalDays = 0;
	/** Number of hours to add in the alarm interval */
	public int ivalHours = 0;
	/** Number of minutes to add in the alarm interval */
	public int ivalMinutes = 0;
	/** Number of seconds to add in the alarm interval */
	public int ivalSeconds = 0;
	/** The next time the alarm for this tracker will go off 
	 * (DateTime stored as a long) */
	public Date nextTime = null;
	/** The ringtone to play when the alarm goes off (empty if default) */
	public String ringtone = null;
	/** Whether the alarm is enabled and will notify */
	public boolean isEnabled = true;
	
	public Alarm(long id) {
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

	public boolean getIsEnabled() {
		return isEnabled;
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
			changed.put(C.db_ALARM_INTERVAL_MONTHS, this.ivalMonths = ivalMonths);
	}

	public void setIvalWeeks(int ivalWeeks) {
		if(this.ivalWeeks != ivalWeeks)
			changed.put(C.db_ALARM_INTERVAL_WEEKS, this.ivalWeeks = ivalWeeks);
	}

	public void setIvalDays(int ivalDays) {
		if(this.ivalDays != ivalDays)
			changed.put(C.db_ALARM_INTERVAL_DAYS, this.ivalDays = ivalDays);
	}

	public void setIvalHours(int ivalHours) {
		if(this.ivalHours != ivalHours)
			changed.put(C.db_ALARM_INTERVAL_HOURS, this.ivalHours = ivalHours);
	}

	public void setIvalMinutes(int ivalMinutes) {
		if(this.ivalMinutes != ivalMinutes)
			changed.put(C.db_ALARM_INTERVAL_MINUTES, this.ivalMinutes = ivalMinutes);
	}

	public void setIvalSeconds(int ivalSeconds) {
		if(this.ivalSeconds != ivalSeconds)
			changed.put(C.db_ALARM_INTERVAL_SECONDS, this.ivalSeconds = ivalSeconds);
	}

	public void setTrackerId(long trackerId) {
		if(this.trackerId != trackerId)
			changed.put(C.db_ALARM_TRACKER, this.trackerId = trackerId);
	}

	public void setNextTime(Date nextTime) {
		if (nextTime != null && !nextTime.equals(this.nextTime)) return;
		this.nextTime = nextTime;
		changed.put(C.db_ALARM_NEXT_TIME, nextTime.getTime());
	}	

	public void setRingtone(String ringtone) {
		if(ringtone != null && !ringtone.equals(this.ringtone)) return;
		this.ringtone = ringtone;
		changed.put(C.db_ALARM_RINGTONE,ringtone);
	}

	public void setIsEnabled(boolean enabled) {
		if(enabled == isEnabled) return;
		isEnabled = enabled;
		changed.put(C.db_ALARM_ENABLED,enabled);
	}

	// stuff to allow this to be parcelable, i.e. to save as part of state
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeSerializable(this);
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

	public void setTotalAlarmValue(Interval i, int value) {
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
}
