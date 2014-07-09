package info.guardianproject.lildebi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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

	private Handler commandThreadHandler;
	private LilDebiAction action;

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

		commandThreadHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.arg1 == LilDebiAction.COMMAND_FINISHED)
					updateScreenStatus();
				else if (msg.arg1 == LilDebiAction.LOG_UPDATE)
					updateLog();
			}
		};

		action = new LilDebiAction(this, commandThreadHandler);

		if (savedInstanceState != null)
			LilDebiAction.log.append(savedInstanceState.getString("log"));
		else
			Log.i(TAG, "savedInstanceState was null");

		NativeHelper.installOrUpgradeAppBin(this);
		installBusyboxSymlinks();
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
	}

	protected void onDestroy() {
	    super.onDestroy();
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
					+ NativeHelper.app_bin + "/chroot /debian /bin/bash -l\"");
			startActivity(i);
			return true;
		case R.id.menu_delete:
			new AlertDialog.Builder(this).setMessage(R.string.confirm_delete_message)
					.setCancelable(false).setPositiveButton(R.string.doit,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									action.removeDebianSetup();
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

	private void updateScreenStatus() {
		startStopButton.setEnabled(true);
		setProgressBarIndeterminateVisibility(false);
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			Toast.makeText(getApplicationContext(), R.string.no_sdcard_message,
					Toast.LENGTH_LONG).show();
			statusTitle.setVisibility(View.VISIBLE);
			statusText.setVisibility(View.VISIBLE);
			statusText.setText(R.string.no_sdcard_status);
			startStopButton.setVisibility(View.GONE);
			return;
		}
		if (NativeHelper.isInstalled()) {
			if (!new File(NativeHelper.mnt).exists()) {
				// we have a manually downloaded debian.img file, config for it
				LilDebiAction.log.append(String.format(
						getString(R.string.mount_point_not_found_format),
						NativeHelper.mnt) + "\n");
				statusTitle.setVisibility(View.VISIBLE);
				statusText.setVisibility(View.VISIBLE);
				statusText.setText(R.string.not_configured_message);
				startStopButton.setVisibility(View.VISIBLE);
				startStopButton.setText(R.string.title_configure);
				startStopButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						startStopButton.setEnabled(false);
						setProgressBarIndeterminateVisibility(true);
						action.configureDownloadedImage();
					}
				});
			} else if (NativeHelper.isStarted()) {
				// we have a configured and mounted Debian setup, stop it
				statusTitle.setVisibility(View.GONE);
				statusText.setVisibility(View.GONE);
				statusText.setText(R.string.mounted_message);
				startStopButton.setVisibility(View.VISIBLE);
				startStopButton.setText(R.string.title_stop);
				startStopButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						startStopButton.setEnabled(false);
						setProgressBarIndeterminateVisibility(true);
						action.stopDebian();
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
						startStopButton.setEnabled(false);
						setProgressBarIndeterminateVisibility(true);
						action.startDebian();
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
		final String logContents = LilDebiAction.log.toString();
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
				LilDebiAction.log.append(msg + "\n");
				return;
			}
			Log.i(TAG, "Installing busybox symlinks into " + NativeHelper.app_bin);
			// setup busybox so we have the utils we need, guaranteed
			String cmd = busybox.getAbsolutePath()
					+ " --install -s " + NativeHelper.app_bin.getAbsolutePath();
			Log.i(TAG, cmd);
			LilDebiAction.log.append("# " + cmd + "\n\n");
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
					LilDebiAction.log.append(line);
			} catch (IOException e) {
				e.printStackTrace();
				LilDebiAction.log.append("Exception triggered by " + cmd);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		savedInstanceState.putString("log", LilDebiAction.log.toString());
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
