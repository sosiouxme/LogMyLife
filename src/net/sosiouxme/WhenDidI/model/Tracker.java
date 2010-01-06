package net.sosiouxme.WhenDidI.model;

import java.util.Date;
import java.util.Set;

import net.sosiouxme.WhenDidI.C;
import net.sosiouxme.WhenDidI.Util;

public class Tracker {

	public final long id;
	public long groupId;
	public String name;
	public String body;
	public Date lastLogDate;
	private Set<String> changed = Util.newSet();

	public Tracker(long id) {
		super();
		this.id = id;
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


	/* these actually do something */
	
	public String[] getChanged() {
		return changed.toArray(new String[] {});
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

}
