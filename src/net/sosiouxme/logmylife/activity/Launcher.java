package net.sosiouxme.logmylife.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/*
 * The whole entire purpose of this class is to show the correct label
 * on the icon that launches the program. I wanted a different label
 * on the actual Main activity. So this just launches Main.
 */
public class Launcher extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startActivity(new Intent(this,Main.class));
		finish();
	}
}
