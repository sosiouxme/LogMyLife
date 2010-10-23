package net.sosiouxme.logmylife.domain.dto;

import android.content.ContentValues;

public abstract class AbstractDTO {

	public final long id;
    protected ContentValues changed = new ContentValues();

    public AbstractDTO() {
		this.id = -1; //not created yet
	}
    
	public AbstractDTO(long id) {
		this.id = id;
	}
	
	public boolean isNew() {
		return id == -1;
	}
	
	public long getId() {
		return id;
	}

	public void clearChanged() {
		changed.clear();
	}

	public ContentValues getChanged() {
		return changed;
	}
	
}