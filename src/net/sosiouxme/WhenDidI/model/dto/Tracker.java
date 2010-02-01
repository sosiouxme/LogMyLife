package net.sosiouxme.WhenDidI.model.dto;

import java.util.Date;

import net.sosiouxme.WhenDidI.C;

public class Tracker extends AbstractDTO {

	public long groupId;
	public String name;
	public String body;
	public Date lastLogDate;
	
	public Tracker(long id) {
		super(id);
	}

	public String getName() {
		return name;
	}

	public String getBody() {
		return body;
	}

	public Date getLastLogDate() {
		return lastLogDate;
	}

	public long getGroupId() {
		return groupId;
	}
	
	public void setName(String name) {
		this.name = name;
		changed.add(C.db_TRACKER_NAME);
	}

	public void setBody(String body) {
		this.body = body;
		changed.add(C.db_TRACKER_BODY);
	}

	public void setLastLogDate(Date lastLogDate) {
		this.lastLogDate = lastLogDate;
		changed.add(C.db_TRACKER_LAST_LOG);
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
		changed.add(C.db_TRACKER_GROUP);
	}

	public void copyFrom(Tracker t) {
		setGroupId(t.groupId);
		setName(t.name);
		setBody(t.body);
		setLastLogDate(t.lastLogDate);
	}
}
