package info.guardianproject.lildebi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.Window;
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

	private static final int LOG_UPDATE = 654321;
	private static final int COMMAND_FINISHED = 123456;

	static StringBuffer log = null;

	private CommandThread commandThread;
	private Handler commandThreadHandler;

	private PowerManager.WakeLock wl;
	private boolean useWakeLock;
	// we have to keep a copy around of these to prevent them from being GCed
	private BroadcastReceiver mediaMountedReceiver = null;
	private BroadcastReceiver mediaEjectReceiver = null;
	public String command;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
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

		if(log == null);
			log = new StringBuffer();
		if (savedInstanceState != null)
			log.append(savedInstanceState.getString("log"));
		else
			Log.i(TAG, "savedInstanceState was null");

		NativeHelper.installOrUpgradeAppBin(this);
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

		commandThreadHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.arg1 == COMMAND_FINISHED)
					updateScreenStatus();
				else if (msg.arg1 == LOG_UPDATE)
					updateLog();
			}
		};
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
		updateLog();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean(getString(R.string.pref_start_on_mount_key), false))
			registerMediaMountedReceiver();
		else
			unregisterMediaMountedReceiver();
	}

	protected void onDestroy() {
	    super.onDestroy();
	    unregisterReceiver(mediaEjectReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		menu.findItem(R.id.menu_jackpal_terminal).setVisible(
				isIntentAvailable("jackpal.androidterm.RUN_SCRIPT"));
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
		case R.id.menu_jackpal_terminal:
			Intent i = new Intent("jackpal.androidterm.RUN_SCRIPT");
			i.addCategory(Intent.CATEGORY_DEFAULT);
			i.putExtra("jackpal.androidterm.iInitialCommand", "su -c \""
					+ NativeHelper.app_bin + "/chroot /debian /bin/bash\"");
			startActivity(i);
			return true;
		case R.id.menu_delete:
			new AlertDialog.Builder(this).setMessage(R.string.confirm_delete_message)
					.setCancelable(false).setPositiveButton(R.string.doit,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									command = "./remove-debian-setup.sh "
											+ NativeHelper.getArgs();
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

				writeCommand(os, "cd " + NativeHelper.app_bin.getAbsolutePath());
				writeCommand(os, "export PATH=" + NativeHelper.app_bin.getAbsolutePath());
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
				Message msg = commandThreadHandler.obtainMessage();
				msg.arg1 = COMMAND_FINISHED;
				commandThreadHandler.sendMessage(msg);
			}
		}
	}

	class LogUpdate extends StreamThread.StreamUpdate {

		StringBuffer sb = new StringBuffer();

		@Override
		public void update(String val) {
			log.append(val);
			Message msg = commandThreadHandler.obtainMessage();
			msg.arg1 = LOG_UPDATE;
			commandThreadHandler.sendMessage(msg);
		}
	}

	public static void writeCommand(OutputStream os, String command) throws Exception {
		Log.i(TAG, command);
		os.write((command + "\n").getBytes("ASCII"));
	}

	private void configureDownloadedImage () {
		startStopButton.setEnabled(false);
		setProgressBarIndeterminateVisibility(true);
		command = new String("./configure-downloaded-image.sh" + NativeHelper.getArgs());
		commandThread = new CommandThread();
		commandThread.start();
	}

	private void startDebian() {
		startStopButton.setEnabled(false);
		setProgressBarIndeterminateVisibility(true);
		if (useWakeLock)
			wl.acquire();
		command = new String("./start-debian.sh" + NativeHelper.getArgs()
				+ " && " + NativeHelper.app_bin + "/chroot "
				+ NativeHelper.mnt + " /bin/bash -c \""
				+ NativeHelper.postStartScript + "\"");
		commandThread = new CommandThread();
		commandThread.start();
		Toast.makeText(this, R.string.starting_debian, Toast.LENGTH_LONG).show();
	}

	private void stopDebian() {
		startStopButton.setEnabled(false);
		setProgressBarIndeterminateVisibility(true);
		if (wl.isHeld())
			wl.release();
		command = new String(NativeHelper.app_bin + "/chroot "
				+ NativeHelper.mnt
				+ " /bin/bash -c \"" + NativeHelper.preStopScript
				+ "\"; ./stop-debian.sh " + NativeHelper.getArgs());
		commandThread = new CommandThread();
		commandThread.start();
		Toast.makeText(this, R.string.stopping_debian, Toast.LENGTH_LONG).show();
	}

	private void updateScreenStatus() {
		startStopButton.setEnabled(true);
		setProgressBarIndeterminateVisibility(false);
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
		if (new File(NativeHelper.image_path).exists()) {
			if (!new File(NativeHelper.mnt).exists()) {
				// we have a manually downloaded debian.img file, config for it
				LilDebi.log.append(String.format(
						getString(R.string.mount_point_not_found_format),
						NativeHelper.mnt) + "\n");
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
			File busybox = new File(NativeHelper.app_bin, "busybox");
			if (!busybox.exists()) {
				String msg = "busybox is missing from the apk!";
				Log.e(TAG, msg);
				log.append(msg + "\n");
				return;
			}
			Log.i(TAG, "Installing busybox symlinks into " + NativeHelper.app_bin);
			// setup busybox so we have the utils we need, guaranteed
			String cmd = busybox.getAbsolutePath()
					+ " --install -s " + NativeHelper.app_bin.getAbsolutePath();
			Log.i(TAG, cmd);
			log.append("# " + cmd + "\n\n");
			// this can't use CommandThread because CommandThread depends on busybox sh
			try {
				Process sh = Runtime.getRuntime().exec("/system/bin/sh");
				OutputStream os = sh.getOutputStream();
				os.write(cmd.getBytes("ASCII"));
				os.write(";\nexit\n".getBytes("ASCII"));
				BufferedReader in = new BufferedReader(
						new InputStreamReader(sh.getInputStream()));
				String line = null;
				while ((line = in.readLine()) != null)
					log.append(line);
			} catch (IOException e) {
				e.printStackTrace();
				log.append("Exception triggered by " + cmd);
			}
		}
	}

	void registerMediaMountedReceiver() {
		if (mediaMountedReceiver == null) {
			mediaMountedReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (new File(NativeHelper.image_path).exists() && new File(NativeHelper.mnt).exists())
						startDebian();
				}
			};
			IntentFilter filter = new IntentFilter();
			filter.addDataScheme("file");
			filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
			registerReceiver(mediaMountedReceiver, filter);
			mediaMountedReceiver.setDebugUnregister(true);
		}
	}

	void unregisterMediaMountedReceiver() {
		if (mediaMountedReceiver != null)
			try {
				unregisterReceiver(mediaMountedReceiver);
			} catch (IllegalArgumentException e) {
				// ugly workaround for bug in Android 2.1 and maybe higher
				// http://code.google.com/p/android/issues/detail?id=6191
				Log.w(TAG, "Android project issue 6191 workaround:");
				e.printStackTrace();
			}
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


	private boolean isIntentAvailable(String action) {
		final PackageManager packageManager = getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
}
