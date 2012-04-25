package info.guardianproject.lildebi;

import java.io.File;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class LilDebi extends Activity implements OnCreateContextMenuListener {
	public static final String TAG = "LilDebi";

	private TextView statusTitle;
	private TextView statusText;
	private Button startStopButton;
	private ScrollView consoleScroll;
	private TextView consoleText;
	private EditText runCommandEditText;

	public static final String LOG_UPDATE = "LOG_UPDATE";
	public static final String COMMAND_FINISHED = "COMMAND_FINISHED";

	private CommandThread commandThread;
	private PowerManager.WakeLock wl;
	private boolean useWakeLock;
	private StringBuffer log;
	private BroadcastReceiver logUpdateReceiver;
	private BroadcastReceiver commandFinishedReceiver;
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
		runCommandEditText = (EditText) findViewById(R.id.runCommand);

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
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_run_test:
			command = "./test.sh " + NativeHelper.args;
			commandThread = new CommandThread();
			commandThread.start();
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
				Process sh = Runtime.getRuntime().exec("su - sh");
				OutputStream os = sh.getOutputStream();

				StreamThread it = new StreamThread(sh.getInputStream(), logUpdate);
				StreamThread et = new StreamThread(sh.getErrorStream(), logUpdate);

				it.start();
				et.start();

				Log.i(LilDebi.TAG, "cd " + NativeHelper.app_bin.getAbsolutePath());
				writeCommand(os, "cd " + NativeHelper.app_bin.getAbsolutePath());
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

	private void updateScreenStatus() {
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			Toast.makeText(getApplicationContext(), R.string.no_sdcard_message,
					Toast.LENGTH_LONG).show();
			statusTitle.setVisibility(View.VISIBLE);
			statusText.setVisibility(View.VISIBLE);
			statusText.setText(R.string.no_sdcard_status);
			startStopButton.setVisibility(View.GONE);
			runCommandEditText.setVisibility(View.GONE);
			runCommandEditText.setOnEditorActionListener(null);
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
						command = new String("./configure-downloaded-image.sh" + NativeHelper.args);
						commandThread = new CommandThread();
						commandThread.start();
					}
				});
				runCommandEditText.setVisibility(View.GONE);
				runCommandEditText.setOnEditorActionListener(null);
			} else if (new File(NativeHelper.mnt + "/etc").exists()) {
				// we have a configured and mounted Debian setup, stop it
				statusTitle.setVisibility(View.GONE);
				statusText.setVisibility(View.GONE);
				statusText.setText(R.string.mounted_message);
				startStopButton.setVisibility(View.VISIBLE);
				startStopButton.setText(R.string.title_stop);
				startStopButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						if (wl.isHeld())
							wl.release();
						command = new String("chroot " + NativeHelper.mnt
								+ " /bin/bash -c \"" + NativeHelper.preStopScript
								+ "\"; ./stop-debian.sh " + NativeHelper.args);
						commandThread = new CommandThread();
						commandThread.start();
					}
				});
				runCommandEditText.setVisibility(View.VISIBLE);
				runCommandEditText.setOnEditorActionListener(new OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						// IME_ACTION_DONE is for soft keyboard
						if (actionId == EditorInfo.IME_ACTION_DONE) {
							runUserCommand(runCommandEditText.getText().toString());
							return true;
						}
						return false;
					}
				});
				runCommandEditText.setOnKeyListener(new OnKeyListener() {

					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						if (event != null && 
								event.getAction() == KeyEvent.ACTION_UP &&
								keyCode == KeyEvent.KEYCODE_ENTER) {
							runUserCommand(runCommandEditText.getText().toString());
							return true;
						}
						return false;
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
						if (useWakeLock)
							wl.acquire();
						command = new String("./start-debian.sh" + NativeHelper.args
								+ " && chroot " + NativeHelper.mnt + " /bin/bash -c \""
								+ NativeHelper.postStartScript + "\"");
						commandThread = new CommandThread();
						commandThread.start();
					}
				});
				runCommandEditText.setVisibility(View.GONE);
				runCommandEditText.setOnEditorActionListener(null);
			}
		} else if (! isOnline()) {
			statusTitle.setVisibility(View.VISIBLE);
			statusText.setVisibility(View.VISIBLE);
			statusText.setText(R.string.no_network_message);
			startStopButton.setVisibility(View.GONE);
			runCommandEditText.setVisibility(View.GONE);
			runCommandEditText.setOnEditorActionListener(null);
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
			runCommandEditText.setVisibility(View.GONE);
			runCommandEditText.setOnEditorActionListener(null);
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

	private void runUserCommand(String userCommand) {
		log.append("# " + userCommand);
		command = "export PATH=/usr/sbin:/usr/bin:/sbin:/bin:/system/xbin:/system/bin; " + 
			"chroot /data/debian /bin/bash -c \"" +
			userCommand.replace("\"", "\\\"") + "\"";
		commandThread = new CommandThread();
		commandThread.start();
		runCommandEditText.setText("");
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

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
		log.append(savedInstanceState.getString("log"));
	}
}
