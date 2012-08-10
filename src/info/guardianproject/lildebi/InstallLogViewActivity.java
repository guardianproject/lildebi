package info.guardianproject.lildebi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

public class InstallLogViewActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.install_log_view);
		new LoadLogTask().execute(null);
	}

	private class LoadLogTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			StringBuilder log = new StringBuilder("");
			try {
				BufferedReader in = new BufferedReader(new FileReader(
						NativeHelper.install_log));
				String line = "";
				while (line != null) {
					log.append(line + "\n");
					line = in.readLine();
					if (isCancelled())
						break;
				}
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return log.toString();
		}

		protected void onPostExecute(String result) {
			final TextView installLog = (TextView) findViewById(R.id.installLogView);
			installLog.setText(result);
		}
	}
}
