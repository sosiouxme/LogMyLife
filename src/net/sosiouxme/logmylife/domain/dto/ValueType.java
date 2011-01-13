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

import java.text.DecimalFormat;

import android.database.Cursor;

public enum ValueType {

	NUMBER(0, true),
	INTEGER(1, false),
	MONEY(2, true);
	
	private final int id;
	private final boolean decimal; // may have decimal point in it?
	
	private ValueType(int id, boolean decimal) {
		this.id = id;
		this.decimal = decimal;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean isDecimal() {
		return decimal;
	}
	
	public static ValueType getById(long valueType) {
		return values()[(int) valueType];
	}

	public Number parseValue(String value) {
		Number num = null;
		try {
			if(isDecimal())
				num = Double.parseDouble(value);
			else
				num = Long.parseLong(value);
		} catch (Exception e) {
			// swallow parsing errors
		}
		return num;
	}
	
	public Number getValueFromCursor(Cursor c, int column) {
		if (c.isNull(column))
			return null;
		else if(isDecimal()) 
			return c.getDouble(column);
		else 
			return c.getLong(column);
	}
	
	public String formatValue(Number value) {
		if(value == null)
			return null;
		String text = null;
		switch(this) {
		case NUMBER:
			text = new DecimalFormat("#.#######################").format(value);
			break;
		case INTEGER:
			text = value.toString();
			break;
		case MONEY:
			text = new DecimalFormat("#.00").format(value);
			break;
		}
		return text;
	}
}
