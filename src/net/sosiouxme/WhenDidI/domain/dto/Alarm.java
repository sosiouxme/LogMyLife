package net.sosiouxme.WhenDidI.domain.dto;

import java.util.Date;

import net.sosiouxme.WhenDidI.C;

public class Alarm extends AbstractDTO {

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

}
