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
