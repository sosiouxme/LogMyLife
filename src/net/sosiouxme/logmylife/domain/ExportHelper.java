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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.sosiouxme.logmylife.R;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class ExportHelper extends AbstractImportExportHelper {
    private static final String TAG = "LML.Export";

    public ExportHelper(Context ctx, String dbPath) {
    	super(ctx, dbPath);
	}
    
    // UI thread
    protected void onPreExecute() {
	   mDialog.setMessage(str(R.string.exporting_data));
	   mDialog.show();
	}

    // worker thread
	protected String doInBackground(final String... noargs) {

		try {
			File dbFile = new File(mDbPath);
			File sdCard = Environment.getExternalStorageDirectory();

			// create/locate necessary dirs/files on the SD card
			File exportDir = new File(sdCard, EXPORT_DIR);
			if (!exportDir.exists()) exportDir.mkdirs();
			File exportFile = new File(sdCard, EXPORT_FILE);
			File versionFile = new File(exportDir, VERSION_FILE);
			File dbExpFile = new File(exportDir, dbFile.getName());

			// create a version file for the version of the export format
			writeVersionFile(versionFile);

			// copy the database
			Log.d(TAG, "Copying database");
			dbExpFile.createNewFile();
			this.copyFile(dbFile, dbExpFile);

			// combine into an archive
			Log.d(TAG, "Creating archive file");
			exportFile.createNewFile();
			zipFilesToArchive(exportDir, exportFile);
			
			return null;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			return e.getMessage();
		}
    }

	private void writeVersionFile(File versionFile) throws IOException {
		Log.d(TAG, "Creating version file");
		versionFile.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(versionFile), BUFFER);
		bw.write(Integer.toString(EXPORT_FORMAT_VERSION));
		bw.close();
	}

	private void zipFilesToArchive(File exportDir, File exportFile)
			throws FileNotFoundException, IOException {
		ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(
				new FileOutputStream(exportFile), BUFFER));
		String files[] = exportDir.list();
		for (String name : files) {
			Log.d(TAG, "Zip archive adding: " + name);
			File file = new File(exportDir, name);
			BufferedInputStream origin = new BufferedInputStream(
					new FileInputStream(file), BUFFER);
			ZipEntry entry = new ZipEntry(name);
			zip.putNextEntry(entry);
			int count;
			byte data[] = new byte[BUFFER];
			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				zip.write(data, 0, count);
			}
			origin.close();
			zip.closeEntry();
			file.delete();
		}
		zip.close();

		// delete the directory
		exportDir.delete();
	}

 }
