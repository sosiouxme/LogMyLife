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

package net.sosiouxme.logmylife.receiver;

import net.sosiouxme.logmylife.domain.DbAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/*
 * Receive the android.intent.action.PACKAGE_REPLACED broadcast event
 * so as to schedule the next alert when the package is replaced.
 */

public class ReplacedReceiver extends BroadcastReceiver {

	private static final String TAG = "LML.RepR";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "In ReplacedReceiver.onReceive with data " + intent.getDataString());
		DbAdapter db = new DbAdapter(context);
		AlertReceiver.setNextAlert(context, db);
		db.close();
	}

}
