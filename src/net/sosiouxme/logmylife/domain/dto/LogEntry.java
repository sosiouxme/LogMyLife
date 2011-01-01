package net.sosiouxme.logmylife.domain.dto;

import java.io.Serializable;
import java.util.Date;

import net.sosiouxme.logmylife.C;

public class LogEntry extends AbstractDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	public long trackerId;
	public String body = null;
	public Date logDate;
	public String value = null;
	public boolean isBreak = false;
	/** type of value to record with the log (types hardwired now, will be a table eventually */
	public long valueType = 0;

	public LogEntry(long id) {
		super(id);
	}
	
	public static LogEntry newLogFor(Tracker t) {
		if(t == null || t.isNew()) 
			throw new IllegalArgumentException("Can't create log entry before tracker is in DB");
		LogEntry log = new LogEntry(-1);
		log.setTrackerId(t.getId());
		log.changed.put(C.db_LOG_BODY, log.body);
		log.setLogDate(new Date());
		log.changed.put(C.db_LOG_VALUE, log.value);
		log.changed.put(C.db_LOG_IS_BREAK, log.isBreak);
		log.setValueType(t.getLogValueType());
		
		return log;
	}
	
	public long getId() {
		return id;
	}

	public long getTrackerId() {
		return trackerId;
	}

	public String getBody() {
		return body;
	}

	public Date getLogDate() {
		return logDate;
	}
	
	public String getValue() {
		return value;
	}

	public boolean isBreak() {
		return isBreak;
	}

	public long getValueType() {
		return valueType;
	}

	public void setValueType(long valueType) {
		this.valueType = valueType;
		changed.put(C.db_LOG_VALUE_TYPE, valueType);
	}

	public void setBody(String body) {
		this.body = body;
		changed.put(C.db_LOG_BODY,body);
	}

	public void setLogDate(Date logDate) {
		this.logDate = logDate;
		changed.put(C.db_LOG_TIME, logDate == 
									null ? null 
									: C.dbDateFormat.format(logDate));
	}

	public void setTrackerId(long trackerId) {
		this.trackerId = trackerId;
		changed.put(C.db_LOG_TRACKER, trackerId);
	}

	public void setValue(String value) {
		this.value = value;
		changed.put(C.db_LOG_VALUE, value);
	}

	public void setBreak(boolean isBreak) {
		this.isBreak = isBreak;
		changed.put(C.db_LOG_IS_BREAK, isBreak);
	}

	public void copyFrom(LogEntry le) {
		setTrackerId(le.trackerId);
		setBody(le.body);
		setLogDate(le.logDate);
		setValue(le.value);
		setBreak(le.isBreak);
	}
}
