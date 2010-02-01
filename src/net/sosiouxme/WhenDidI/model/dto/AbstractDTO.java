package net.sosiouxme.WhenDidI.model.dto;

import java.util.Set;

import net.sosiouxme.WhenDidI.Util;

public abstract class AbstractDTO {

	public final long id;
	protected Set<String> changed = Util.newSet();

	public AbstractDTO(long id) {
		this.id = id;
	}

	public void clearChanged() {
		changed.clear();
	}

	public String[] getChanged() {
		return changed.toArray(new String[] {});
	}
	
}