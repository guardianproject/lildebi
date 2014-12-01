package info.guardianproject.lildebi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class InstallLogViewActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.install_log_view);
        new LoadLogTask().execute((Void[])null);
        Button shareButton = (Button) findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                shareInstallLog(NativeHelper.install_log);
            }
        });

    }

    protected void shareInstallLog(File f) {
        if (!NativeHelper.isSdCardPresent()) {
            Toast.makeText(this, R.string.no_sdcard_message, Toast.LENGTH_LONG)
                    .show();
            return;
        }
        final File fToSend = new File(NativeHelper.publicFiles, f.getName());
        try {
            FileUtils.copyFile(f, fToSend);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent i = new Intent(android.content.Intent.ACTION_SEND);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "install.log from Lil' Debi");
        i.putExtra(Intent.EXTRA_TEXT,
                "Attached is an log sent by Lil' Debi.  For more info, see:\n"
                        + "https://github.com/guardianproject/lildebi\n\n"
                        + "manufacturer: " + Build.MANUFACTURER + "\n"
                        + "model: " + Build.MODEL + "\n"
                        + "product: " + Build.PRODUCT + "\n"
                        + "brand: " + Build.BRAND + "\n"
                        + "device: " + Build.DEVICE + "\n"
                        + "board: " + Build.BOARD + "\n"
                        + "ID: " + Build.ID + "\n"
                        + "CPU ABI: " + Build.CPU_ABI + "\n"
                        + "release: " + Build.VERSION.RELEASE + "\n"
                        + "incremental: " + Build.VERSION.INCREMENTAL + "\n"
                        + "codename: " + Build.VERSION.CODENAME + "\n"
                        + "SDK: " + Build.VERSION.SDK_INT + "\n"
                        );
        i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fToSend));
        startActivity(Intent.createChooser(i, "How do you want to share?"));
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
