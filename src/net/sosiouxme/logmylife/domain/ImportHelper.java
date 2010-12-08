package net.sosiouxme.logmylife.domain;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.sosiouxme.logmylife.R;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class ImportHelper extends AbstractImportExportHelper {
    private static final String TAG = "LML.Import";

    public ImportHelper(Context ctx, String dbPath) {
    	super(ctx, dbPath);
	}
    
    // UI thread
    protected void onPreExecute() {
	   mDialog.setMessage(str(R.string.importing_data));
	   mDialog.show();
	}

    // worker thread
	protected String doInBackground(final String... noargs) {

		try {
			File dbFile = new File(mDbPath);
			File sdCard = Environment.getExternalStorageDirectory();

			// locate/create necessary files on the SD card
			File importDir = new File(sdCard, EXPORT_DIR);
			importDir.mkdirs();
			File importFile = new File(sdCard, EXPORT_FILE);

			// expand the archive file
	         extractArchive(importFile, importDir);
			
			// find out the version of the export format
			int version = findVersion(importDir);
			
			switch(version) {
			case EXPORT_FORMAT_VERSION:
				// copy the database
				Log.d(TAG, "Copying database");
				File dbImpFile = new File(importDir, dbFile.getName());
				this.copyFile(dbImpFile, dbFile);
				break;
			default:
				return str(R.string.import_unknown_version) + version;
			}

			// delete files extracted from archive
			deleteExtractedFiles(importDir);
			
			return null;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			return e.getMessage();
		}
    }

    // UI thread
	protected void onPostExecute(final String errMsg) {
	   if (mDialog.isShowing())
	      mDialog.dismiss();
	   String result = (errMsg == null)
	   		? str(R.string.import_success)
	   		: str(R.string.import_fail) + errMsg;
	   Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
	}

	private int findVersion(File importDir) throws FileNotFoundException,
			IOException, NumberFormatException {
		File versionFile = new File(importDir, VERSION_FILE);
		BufferedReader reader = new BufferedReader(new FileReader(versionFile));
		char[] buf = new char[BUFFER];
		int numRead = reader.read(buf);
		String readData = String.valueOf(buf, 0, numRead);
		reader.close();
		int version = Integer.parseInt(readData);
		return version;
	}

	private void deleteExtractedFiles(File importDir) {
		Log.d(TAG, "Deleting extracted files");
		for(File file : importDir.listFiles()) 
			file.delete();
		importDir.delete();
	}

	private void extractArchive(File importFile, File importDir)
			throws FileNotFoundException, IOException {
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(importFile)));
		 ZipEntry entry;
		 while((entry = zis.getNextEntry()) != null) {
		    Log.d(TAG, "Extracting from archive: " + entry);
		    int count;
		    byte data[] = new byte[BUFFER];
		    // write the files to the disk
		    
			BufferedOutputStream dest = new BufferedOutputStream(
					new FileOutputStream(new File(importDir, entry.getName())), BUFFER);
		    while ((count = zis.read(data, 0, BUFFER)) != -1) {
		       dest.write(data, 0, count);
		    }
		    dest.flush();
		    dest.close();
		 }
		 zis.close();
	}


 }
