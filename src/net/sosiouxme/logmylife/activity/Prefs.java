package net.sosiouxme.logmylife.activity;

import net.sosiouxme.logmylife.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
Activity that lets the user set app-wide preferences (sort of a TODO)

@author Luke Meyer, Copyright 2010
See LICENSE file for this file's GPLv3 distribution license.
*/

public class Prefs extends PreferenceActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}

}
