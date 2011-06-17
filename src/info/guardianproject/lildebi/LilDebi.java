package info.guardianproject.lildebi;

import java.io.File;
import java.io.OutputStream;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class LilDebi extends Activity implements OnCreateContextMenuListener
{
	private boolean debianInstalled;
	private boolean debianMounted;
	private TextView statusText;
	private Button startStopButton;
	private ScrollView consoleScroll;
	private TextView consoleText;

	public static final String LOG_UPDATE = "LOG_UPDATE";
	public static final String COMMAND_FINISHED = "COMMAND_FINISHED";

	private CommandThread commandThread;
	private StringBuffer log;
	private BroadcastReceiver logUpdateReceiver;
	private BroadcastReceiver commandFinishedReceiver;
	public String command;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		DebiHelper.dataDir = getDir("bin", MODE_PRIVATE);
		DebiHelper.sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
		DebiHelper.imagename = DebiHelper.sdcard + "/debian.img";
		DebiHelper.mnt = "/data/debian";
		DebiHelper.args = new String(" " + DebiHelper.dataDir.getAbsolutePath() + " " + 
				DebiHelper.sdcard + " " + DebiHelper.imagename + " " + DebiHelper.mnt + " ");

		//if(! DebiHelper.dataDir.exists())
		DebiHelper.unzipDebiFiles(this);
		
		setContentView(R.layout.lildebi);
		statusText = (TextView) findViewById(R.id.statusText);
		startStopButton = (Button) findViewById(R.id.startStopButton);
		consoleScroll = (ScrollView)findViewById(R.id.consoleScroll);
		consoleText = (TextView)findViewById(R.id.consoleText);
		
		log = new StringBuffer();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if (DebiHelper.isInstallRunning)
		{
			// go back to the running install screen
			Intent intent = new Intent(this, InstallActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		String state = Environment.getExternalStorageState();
		if (! Environment.MEDIA_MOUNTED.equals(state))
			Toast.makeText(getApplicationContext(),
					"The SD card/external storage is not mounted, cannot start Debian.", 
					Toast.LENGTH_LONG).show();
		File f = new File(DebiHelper.imagename);
		debianInstalled = f.exists();
		if (debianInstalled)
		{
			File f2 = new File("/data/debian/etc");
			if (f2.exists())
			{
				debianMounted = true;
				statusText.setText("Debian mounted");
				startStopButton.setText(R.string.title_stop);
			}
			else
			{
				debianMounted = false;
				statusText.setText("Debian not mounted");
				startStopButton.setText(R.string.title_start);
			}
		}
		else
		{
			debianMounted = false;
			statusText.setText("Debian not installed");
			startStopButton.setText("Install...");
		}
		startStopButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				if (! debianInstalled)
				{
	                Intent intent = new Intent(getApplicationContext(), InstallActivity.class);
	                startActivityForResult(intent, DebiHelper.STARTING_INSTALL);
					return;
				}
				if (debianMounted)
					command = "./stop-debian.sh";
				else
					command = "./start-debian.sh";
				commandThread = new CommandThread();
				commandThread.start();
			}
		});
		registerReceivers();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		unregisterReceivers();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.preferences_menu, menu);
        return true;
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_preferences:
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_run_test:
				command = "./test.sh";
				commandThread = new CommandThread();
				commandThread.start();
                return true;
        }
        return false;
    }

	class CommandThread extends Thread
	{
		private LogUpdate logUpdate;

		@Override
		public void run()
		{
			logUpdate = new LogUpdate();
			try {
				Process sh = Runtime.getRuntime().exec("su - sh");
				OutputStream os = sh.getOutputStream();

				StreamThread it = new StreamThread(sh.getInputStream(), logUpdate);
				StreamThread et = new StreamThread(sh.getErrorStream(), logUpdate);

				it.start();
				et.start();

				App.logi("cd " + DebiHelper.dataDir.getAbsolutePath());
				writeCommand(os, "cd " + DebiHelper.dataDir.getAbsolutePath());
				App.logi(command + DebiHelper.args);
				writeCommand(os, command + DebiHelper.args);
				writeCommand(os, "exit");

				sh.waitFor();
				App.logi("Done!");
			}
			catch (Exception e)
			{
				App.loge("Error!!!", e);
			}
			finally {
				synchronized (LilDebi.this)
				{
					commandThread = null;
				}
				sendBroadcast(new Intent(COMMAND_FINISHED));
			}
		}
	}

	class LogUpdate extends StreamThread.StreamUpdate
	{

		StringBuffer sb = new StringBuffer();

		@Override
		public void update(String val)
		{
			log.append(val);
			sendBroadcast(new Intent(LOG_UPDATE));
		}
	}

	public static void writeCommand(OutputStream os, String command) throws Exception
	{
		os.write((command + "\n").getBytes("ASCII"));
	}

	private void updateLog()
	{
		final String logContents = log.toString();
		if(logContents != null && logContents.trim().length() > 0)
			consoleText.setText(logContents);
		consoleScroll.scrollTo(0, consoleText.getHeight());
	}

	private void registerReceivers()
	{
		{
			logUpdateReceiver = new BroadcastReceiver()
			{
				@Override
				public void onReceive(Context context, Intent intent)
				{
					updateLog();
				}
			};

			IntentFilter filter = new IntentFilter(LilDebi.LOG_UPDATE);
			registerReceiver(logUpdateReceiver, filter);
		}

		{
			commandFinishedReceiver = new BroadcastReceiver()
			{
				@Override
				public void onReceive(Context context, Intent intent)
				{
					// TODO wireButtons();
				}
			};

			IntentFilter filter = new IntentFilter(LilDebi.COMMAND_FINISHED);
			registerReceiver(commandFinishedReceiver, filter);
		}
	}

	private void unregisterReceivers()
	{
		if(logUpdateReceiver != null)
			unregisterReceiver(logUpdateReceiver);

		if(commandFinishedReceiver != null)
			unregisterReceiver(commandFinishedReceiver);
	}
}
