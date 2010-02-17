package net.sosiouxme.WhenDidI.model.dto;

import net.sosiouxme.WhenDidI.C;

public class Tracker extends AbstractDTO {

	public long groupId;
	public String name;
	public String body;
	public long valueType;
	public long lastLogId;
	public LogEntry lastLog = null;
	
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
	
	public long getValueType() {
		return valueType;
	}

	public void setName(String name) {
		this.name = name;
		changed.add(C.db_TRACKER_NAME);
	}

	public void setBody(String body) {
		this.body = body;
		changed.add(C.db_TRACKER_BODY);
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
		changed.add(C.db_TRACKER_GROUP);
	}
	
	public void setLastLogId(long lastLogId) {
		this.lastLogId = lastLogId;
		changed.add(C.db_TRACKER_LAST_LOG_ID);
	}

	public void setLastLog(LogEntry lastLog) {
		this.lastLog = lastLog;
	}

	public void setValueType(long valueType) {
		this.valueType = valueType;
		changed.add(C.db_TRACKER_LAST_LOG_ID);
	}
	
	public void copyFrom(Tracker t) {
		setGroupId(t.groupId);
		setName(t.name);
		setBody(t.body);
		setValueType(t.valueType);
		setLastLogId(t.lastLogId);
		setLastLog(t.lastLog);
	}
}
