package net.sosiouxme.logmylife.domain.dto;

import net.sosiouxme.logmylife.C;

public class Tracker extends AbstractDTO {

	public long groupId;
	public String name;
	public String body = null;
	public long lastLogId;
	public LogEntry lastLog = null;
	public String logValueLabel = null;
	public static final int LABEL_LEFT = 0;
	public static final int LABEL_RIGHT = 1;
	public int logValueLabelPos = LABEL_RIGHT;

	/** A flag that can be set to skip the alert just once */
	public boolean skipNextAlert;
	
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

	// note: this is filled in when the DB creates the DTO, if there is one
	public LogEntry getLastLog() {
		return lastLog;
	}

	public boolean getSkipNextAlert() {
		return skipNextAlert;
	}

	public String getLogValueLabel() {
		return logValueLabel;
	}


	public int getLogValueLabelPos() {
		return logValueLabelPos;
	}

	public void setLogValueLabelPos(int logValueLabelPos) {
		if (logValueLabelPos == this.logValueLabelPos) return;
		this.logValueLabelPos = logValueLabelPos;
		changed.put(C.db_TRACKER_VALUE_LABEL_POS, logValueLabelPos);
	}

	public void setLogValueLabel(String logValueLabel) {
		if (logValueLabel != null && logValueLabel.equals(this.logValueLabel)) return;
		this.logValueLabel = logValueLabel;
		changed.put(C.db_TRACKER_VALUE_LABEL, logValueLabel);
	}

	public void setSkipNextAlert(boolean skipNextAlert) {
		if(this.skipNextAlert != skipNextAlert)
			changed.put(C.db_TRACKER_SKIP_NEXT_ALERT, this.skipNextAlert = skipNextAlert);
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
		lastLogId = t.lastLogId;
		lastLog = t.lastLog;
		setLogValueLabel(t.logValueLabel);
		setLogValueLabelPos(t.logValueLabelPos);
		setSkipNextAlert(skipNextAlert);
	}
}
