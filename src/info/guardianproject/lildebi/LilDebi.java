package info.guardianproject.lildebi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class LilDebi extends Activity implements OnCreateContextMenuListener {
	public static final String TAG = "LilDebi";

	private TextView statusTitle;
	private TextView statusText;
	private Button startStopButton;
	private ScrollView consoleScroll;
	private TextView consoleText;

	public static final String LOG_UPDATE = "LOG_UPDATE";
	public static final String COMMAND_FINISHED = "COMMAND_FINISHED";

	private CommandThread commandThread;
	private PowerManager.WakeLock wl;
	private boolean useWakeLock;
	private StringBuffer log;
	private BroadcastReceiver logUpdateReceiver;
	private BroadcastReceiver commandFinishedReceiver;
	private BroadcastReceiver mediaEjectReceiver = null;
	public String command;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NativeHelper.setup(getApplicationContext());
		
		// if(! DebiHelper.app_bin.exists())
		NativeHelper.unzipDebiFiles(this);
		// TODO figure out how to manage the scripts on upgrades, etc.

		setContentView(R.layout.lildebi);
		statusTitle = (TextView) findViewById(R.id.statusTitle);
		statusText = (TextView) findViewById(R.id.statusText);
		startStopButton = (Button) findViewById(R.id.startStopButton);
		consoleScroll = (ScrollView) findViewById(R.id.consoleScroll);
		consoleText = (TextView) findViewById(R.id.consoleText);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		NativeHelper.postStartScript = prefs.getString(
				getString(R.string.pref_post_start_key),
				getString(R.string.default_post_start_script));
		NativeHelper.preStopScript = prefs.getString(getString(R.string.pref_pre_stop_key),
				getString(R.string.default_pre_stop_script));
		useWakeLock = prefs.getBoolean(getString(R.string.pref_prevent_sleep_key), false);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "StartStopWakeLock");

		log = new StringBuffer();
		if (savedInstanceState != null)
			log.append(savedInstanceState.getString("log"));
		else
			Log.i(TAG, "savedInstanceState was null");

		installBusyboxSymlinks();

		// if the user tries to unmount the SD Card, try to stop Debian first
		mediaEjectReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (NativeHelper.mounted)
					stopDebian();
			}
		};
		IntentFilter eject = new IntentFilter();
		eject.addDataScheme("file");
		eject.addAction(Intent.ACTION_MEDIA_EJECT);
		registerReceiver(mediaEjectReceiver, eject);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (NativeHelper.isInstallRunning) {
			// go back to the running install screen
			Intent intent = new Intent(this, InstallActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		updateScreenStatus();
		registerReceivers();
		updateLog();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceivers();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_preferences:
			startActivity(new Intent(this, PreferencesActivity.class));
			return true;
		case R.id.menu_install_log:
			startActivity(new Intent(this, InstallLogViewActivity.class));
			return true;
		case R.id.menu_delete:
			new AlertDialog.Builder(this).setMessage(R.string.confirm_delete_message)
					.setCancelable(false).setPositiveButton(R.string.doit,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									command = "./remove-debian-setup.sh "
											+ NativeHelper.args;
									commandThread = new CommandThread();
									commandThread.start();
								}
							}).setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();
								}
							}).show();
			return true;
		}
		return false;
	}

	class CommandThread extends Thread {
		private LogUpdate logUpdate;

		@Override
		public void run() {
			logUpdate = new LogUpdate();
			try {
				String suCmd = "su -s " + NativeHelper.sh.getAbsolutePath();
				Log.i(TAG, "exec: " + suCmd);
				Process sh = Runtime.getRuntime().exec(suCmd);
				OutputStream os = sh.getOutputStream();

				StreamThread it = new StreamThread(sh.getInputStream(), logUpdate);
				StreamThread et = new StreamThread(sh.getErrorStream(), logUpdate);

				it.start();
				et.start();

				Log.i(LilDebi.TAG, "cd " + NativeHelper.app_bin.getAbsolutePath());
				writeCommand(os, "cd " + NativeHelper.app_bin.getAbsolutePath());
				Log.i(LilDebi.TAG, "export PATH=" + NativeHelper.app_bin.getAbsolutePath());
				writeCommand(os, "export PATH=" + NativeHelper.app_bin.getAbsolutePath());
				Log.i(LilDebi.TAG, command);
				writeCommand(os, command);
				writeCommand(os, "exit");

				sh.waitFor();
				Log.i(LilDebi.TAG, "Done!");
			} catch (Exception e) {
				Log.e(LilDebi.TAG, "Error!!!", e);
			} finally {
				synchronized (LilDebi.this) {
					commandThread = null;
				}
				sendBroadcast(new Intent(COMMAND_FINISHED));
			}
		}
	}

	class LogUpdate extends StreamThread.StreamUpdate {

		StringBuffer sb = new StringBuffer();

		@Override
		public void update(String val) {
			log.append(val);
			sendBroadcast(new Intent(LOG_UPDATE));
		}
	}

	public static void writeCommand(OutputStream os, String command) throws Exception {
		os.write((command + "\n").getBytes("ASCII"));
	}

	private void configureDownloadedImage () {
		command = new String("./configure-downloaded-image.sh" + NativeHelper.args);
		commandThread = new CommandThread();
		commandThread.start();
	}

	private void startDebian() {
		if (useWakeLock)
			wl.acquire();
		command = new String("./start-debian.sh" + NativeHelper.args
				+ " && " + NativeHelper.app_bin + "/chroot "
				+ NativeHelper.mnt + " /bin/bash -c \""
				+ NativeHelper.postStartScript + "\"");
		commandThread = new CommandThread();
		commandThread.start();
		Toast.makeText(this, R.string.starting_debian, Toast.LENGTH_LONG).show();
	}

	private void stopDebian() {
		if (wl.isHeld())
			wl.release();
		command = new String(NativeHelper.app_bin + "/chroot "
				+ NativeHelper.mnt
				+ " /bin/bash -c \"" + NativeHelper.preStopScript
				+ "\"; ./stop-debian.sh " + NativeHelper.args);
		commandThread = new CommandThread();
		commandThread.start();
		Toast.makeText(this, R.string.stopping_debian, Toast.LENGTH_LONG).show();
	}

	private void updateScreenStatus() {
		String state = Environment.getExternalStorageState();
		NativeHelper.mounted = false;
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			Toast.makeText(getApplicationContext(), R.string.no_sdcard_message,
					Toast.LENGTH_LONG).show();
			statusTitle.setVisibility(View.VISIBLE);
			statusText.setVisibility(View.VISIBLE);
			statusText.setText(R.string.no_sdcard_status);
			startStopButton.setVisibility(View.GONE);
			return;
		}
		if (new File(NativeHelper.imagename).exists()) {
			if (!new File(NativeHelper.mnt).exists()) {
				// we have a manually downloaded debian.img file, config for it
				statusTitle.setVisibility(View.VISIBLE);
				statusText.setVisibility(View.VISIBLE);
				statusText.setText(R.string.not_configured_message);
				startStopButton.setVisibility(View.VISIBLE);
				startStopButton.setText(R.string.title_configure);
				startStopButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						configureDownloadedImage();
					}
				});
			} else if (new File(NativeHelper.mnt + "/etc").exists()) {
				// we have a configured and mounted Debian setup, stop it
				NativeHelper.mounted = true;
				statusTitle.setVisibility(View.GONE);
				statusText.setVisibility(View.GONE);
				statusText.setText(R.string.mounted_message);
				startStopButton.setVisibility(View.VISIBLE);
				startStopButton.setText(R.string.title_stop);
				startStopButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						stopDebian();
					}
				});
			} else {
				// we have a configured Debian setup that is not mounted, start it
				statusTitle.setVisibility(View.VISIBLE);
				statusText.setVisibility(View.VISIBLE);
				statusText.setText(R.string.not_mounted_message);
				startStopButton.setVisibility(View.VISIBLE);
				startStopButton.setText(R.string.title_start);
				startStopButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						startDebian();
					}
				});
			}
		} else if (! isOnline()) {
			statusTitle.setVisibility(View.VISIBLE);
			statusText.setVisibility(View.VISIBLE);
			statusText.setText(R.string.no_network_message);
			startStopButton.setVisibility(View.GONE);
		} else {
			// we've got nothing, run the install
			statusTitle.setVisibility(View.VISIBLE);
			statusText.setVisibility(View.VISIBLE);
			statusText.setText(R.string.not_installed_message);
			startStopButton.setVisibility(View.VISIBLE);
			startStopButton.setText(R.string.install);
			startStopButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					Intent intent = new Intent(getApplicationContext(),
							InstallActivity.class);
					startActivityForResult(intent, NativeHelper.STARTING_INSTALL);
					return;
				}
			});
		}
	}

	private boolean isOnline() {
		ConnectivityManager cm =
			(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	private void updateLog() {
		final String logContents = log.toString();
		if (logContents != null && logContents.trim().length() > 0)
			consoleText.setText(logContents);
		consoleScroll.scrollTo(0, consoleText.getHeight());
	}

	private void installBusyboxSymlinks() {
		if (! NativeHelper.sh.exists()) {
			Log.i(TAG, "Installing busybox symlinks into " + NativeHelper.app_bin);
			// setup busybox so we have the utils we need, guaranteed
			String cmd = "su -c \"" + new File(NativeHelper.app_bin, "busybox").getAbsolutePath() 
					+ " --install -s " + NativeHelper.app_bin.getAbsolutePath() + "\"\nexit\n";
			log.append("# " + cmd);
			try {
				Process su = Runtime.getRuntime().exec("su");
				OutputStream os = su.getOutputStream();
				os.write(cmd.getBytes("ASCII"));
				BufferedReader in = new BufferedReader(
						new InputStreamReader(su.getInputStream()));
				String line = null;
				while ((line = in.readLine()) != null)
					log.append(line);
			} catch (IOException e) {
				e.printStackTrace();
				log.append("Exception triggered by " + cmd);
			}
		}
	}

	private void registerReceivers() {
		logUpdateReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateLog();
			}
		};
		registerReceiver(logUpdateReceiver, new IntentFilter(LilDebi.LOG_UPDATE));

		commandFinishedReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateScreenStatus();
			}
		};
		registerReceiver(commandFinishedReceiver, new IntentFilter(
				LilDebi.COMMAND_FINISHED));
	}

	private void unregisterReceivers() {
		if (logUpdateReceiver != null)
			unregisterReceiver(logUpdateReceiver);

		if (commandFinishedReceiver != null)
			unregisterReceiver(commandFinishedReceiver);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		savedInstanceState.putString("log", log.toString());
		super.onSaveInstanceState(savedInstanceState);
	}
	// the saved state is restored in onCreate()

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
		log.append(savedInstanceState.getString("log"));
	}
}
