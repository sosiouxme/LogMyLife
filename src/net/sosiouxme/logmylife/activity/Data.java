package net.sosiouxme.logmylife.activity;

import net.sosiouxme.logmylife.R;
import net.sosiouxme.logmylife.domain.DbAdapter;
import net.sosiouxme.logmylife.domain.ExportHelper;
import net.sosiouxme.logmylife.domain.ImportHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class Data extends Activity implements OnClickListener {

	private static final int DIALOG_IMPORT_OK = 0;
	private DbAdapter mDba;
	private TextView mResultText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_data);
		mDba = new DbAdapter(this);

		findViewById(R.id.export_button).setOnClickListener(this);
		findViewById(R.id.import_button).setOnClickListener(this);
		mResultText = (TextView) findViewById(R.id.result); //TODO: persist contents
		
		setResult(RESULT_OK);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDba.close();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.export_button:
			new Exporter().execute();
			break;
		case R.id.import_button:
			showDialog(DIALOG_IMPORT_OK);
			break;
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_IMPORT_OK:
			return new AlertDialog.Builder(this)
				.setMessage(R.string.import_warning_text)
				.setNegativeButton(R.string.import_dialog_cancel, null)
				.setPositiveButton(R.string.import_dialog_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						new Importer().execute();						
					}
				})
				.create();
		}
		return super.onCreateDialog(id);
	}
	
	private class Exporter extends ExportHelper {
		public Exporter() {
			super(Data.this, mDba.getDbPath());
		}
		
		@Override
		protected void onPostExecute(String errMsg) {
			super.onPostExecute(errMsg);
			   String result = (errMsg == null)
		   		? str(R.string.export_success) + EXPORT_FILE
		   		: str(R.string.export_fail) + errMsg;
		   Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
		   mResultText.setText(result);
		}
	}
	
	private class Importer extends ImportHelper {

		public Importer() {
			super(Data.this, mDba.getDbPath());
		}
		
		@Override
		protected void onPostExecute(String errMsg) {
			super.onPostExecute(errMsg);
			String result = (errMsg == null)
		   		? str(R.string.import_success)
		   		: str(R.string.import_fail) + errMsg;
		   	Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
		   	if(errMsg == null) { // succeeded - go back to Main w/ new DB
			   setResult(RESULT_FIRST_USER); // message: get a new DB handle
			   Data.this.finish();
		   	} else {
			   mResultText.setText(result);
		   	}
		}
	}
}
