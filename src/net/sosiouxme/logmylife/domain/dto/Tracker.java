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

import net.sosiouxme.logmylife.C;

public class Tracker extends AbstractDTO {

	public long groupId;
	/** The main visual identifier of the tracker */
	public String name;
	/** Random notes the user can add about the tracker */
	public String body = null;
	/** row id of the most recent log  against this tracker */
	public long lastLogId;
	/** log object for most recent log, if any */
	public LogEntry lastLog = null;
	/** when creating a log, do or don't associate a value with it */
	public boolean logUseValue = false;
	/** type of value to create logs with (hardwired now, will be a table eventually */
	public long logValueType = 0; // integer by default
	/** label for value to put in logs */
	public String logValueLabel = null;
	/** designate whether the label is displayed on the right or left of the value */
	public int logValueLabelPos = LABEL_RIGHT;
	public static final int LABEL_LEFT = 0;
	public static final int LABEL_RIGHT = 1;

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

	public long getLogValueType() {
		return logValueType;
	}

	public boolean getLogUseValue() {
		return logUseValue;
	}

	public void setLogUseValue(boolean logUseValue) {
		if(logUseValue == this.logUseValue) return;
		changed.put(C.db_TRACKER_USE_VALUE, logUseValue);
		this.logUseValue = logUseValue;
	}


	public void setLogValueType(long logValueType) {
		if(logValueType == this.logValueType) return;
		this.logValueType = logValueType;
		changed.put(C.db_TRACKER_VALUE_TYPE, logValueType);
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
		setLogValueType(t.logValueType);
		setLogValueLabel(t.logValueLabel);
		setLogValueLabelPos(t.logValueLabelPos);
		setSkipNextAlert(skipNextAlert);
	}
}
