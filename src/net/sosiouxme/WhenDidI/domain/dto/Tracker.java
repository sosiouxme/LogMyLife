package net.sosiouxme.WhenDidI.domain.dto;

import net.sosiouxme.WhenDidI.C;

public class Tracker extends AbstractDTO {

	public long groupId;
	public String name;
	public String body;
	public long lastLogId;
	public LogEntry lastLog = null;
	/** A flag that can be set to skip the alarm just once */
	public boolean skipNextAlarm;
	
	public Tracker(long id) {
		super(id);
	}

	public String getName() {
		return name;
	}

	public String getBody() {
		return body;
	}

	public long getGroupId() {
		return groupId;
	}

	public long getLastLogId() {
		return lastLogId;
	}

	public LogEntry getLastLog() {
		return lastLog;
	}

	public boolean getSkipNextAlarm() {
		return skipNextAlarm;
	}

	public void setSkipNextAlarm(boolean skipNextAlarm) {
		if(this.skipNextAlarm != skipNextAlarm)
			changed.put(C.db_TRACKER_SKIP_NEXT_ALARM, this.skipNextAlarm = skipNextAlarm);
	}

	public void setName(String name) {
		if (name.equals(this.name)) return;
		this.name = name;
		changed.put(C.db_TRACKER_NAME,name);
	}

	public void setBody(String body) {
		if (body.equals(this.body)) return;
		this.body = body;
		changed.put(C.db_TRACKER_BODY,body);
	}

	public void setGroupId(long groupId) {
		if (groupId == this.groupId) return;
		this.groupId = groupId;
		changed.put(C.db_TRACKER_GROUP,groupId);
	}
	
	public void setLastLogId(long lastLogId) {
		throw new RuntimeException("Tracker LastLog should never be explicitly updated");
	}

	public void setLastLog(LogEntry lastLog) {
		this.lastLog = lastLog;
	}
	
	public void copyFrom(Tracker t) {
		setGroupId(t.groupId);
		setName(t.name);
		setBody(t.body);
		setLastLogId(t.lastLogId);
		setLastLog(t.lastLog);
		setSkipNextAlarm(skipNextAlarm);
	}
}
