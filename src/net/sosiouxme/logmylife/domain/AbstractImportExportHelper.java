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

package net.sosiouxme.logmylife.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public abstract class AbstractImportExportHelper extends
		AsyncTask<String, Void, String> {

	protected static final String EXPORT_DIR = "LogMyLife.export";
	public static final String EXPORT_FILE = "LogMyLife-export.zip";
	protected static final String VERSION_FILE = "LML.version";
	protected static final int BUFFER = 2048;
	protected static final int EXPORT_FORMAT_VERSION = 1;

	protected final Context mContext;
	protected final ProgressDialog mDialog;
	protected final String mDbPath;

	public AbstractImportExportHelper(Context ctx, String dbPath) {
    	mContext = ctx;
    	mDialog = new ProgressDialog(ctx);
    	mDbPath = dbPath;
	}

    // UI thread
    abstract protected void onPreExecute();
	
	// on worker thread (separate from UI thread)
	abstract protected String doInBackground(final String... noargs);
	
	// UI thread
	protected void onPostExecute(final String errMsg) {
		   if (mDialog.isShowing())
		      mDialog.dismiss();
		}

	public String str(int resource) {
		return mContext.getString(resource);
	}

	protected void copyFile(File source, File dest) throws IOException {
	    FileChannel sourceChannel = new FileInputStream(source).getChannel();
	    FileChannel destChannel = new FileOutputStream(dest).getChannel();
	    try {
	       sourceChannel.transferTo(0, sourceChannel.size(), destChannel);
	    } finally {
	       if (sourceChannel != null) sourceChannel.close();
	       if (destChannel != null) destChannel.close();
	    }
	 }

}
