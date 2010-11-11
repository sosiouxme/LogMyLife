package net.sosiouxme.logmylife.domain.dto;

import java.io.Serializable;
import java.util.Date;

import net.sosiouxme.logmylife.C;

public class LogEntry extends AbstractDTO implements Serializable {

	private static final long serialVersionUID = 5259202378934469262L;
	public long trackerId;
	public String body;
	public Date logDate;
	public Integer value;
	public boolean isBreak;
	
	public LogEntry(long id) {
		super(id);
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
	
	public Integer getValue() {
		return value;
	}

	public boolean isBreak() {
		return isBreak;
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

	public void setValue(Integer value) {
		this.value = value;
		changed.put(C.db_LOG_VALUE, value == null ? null : value);
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
