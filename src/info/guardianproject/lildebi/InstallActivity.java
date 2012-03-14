package info.guardianproject.lildebi;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.torproject.android.service.TorServiceUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class InstallActivity extends Activity implements View.OnCreateContextMenuListener {
	public static final String RELEASE = "RELEASE";
	public static final String MIRROR = "MIRROR";
	public static final String IMAGESIZE = "IMAGESIZE";

	private TextView selectedRelease;
	private TextView selectedMirror;
	private EditText imagesize;
	private InstallService mBoundService;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mBoundService = ((InstallService.LocalBinder) service).getService();
			Log.i(LilDebi.TAG, "calling serviceBound");
			wireButtons();
		}

		public void onServiceDisconnected(ComponentName className) {
			mBoundService = null;
			unwireButtons();
		}
	};
	private boolean mIsBound;
	private Button installButton;
	private View progressBar;
	private TextView installLog;
	private ScrollView textScroll;
	private Handler handler;
	private BroadcastReceiver installLogUpdateRec;
	private BroadcastReceiver installFinishedRec;

	void doBindService() {
		Log.i(LilDebi.TAG, "bindService");
		bindService(new Intent(this, InstallService.class), mConnection,
				Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.install_activity);
		selectedRelease = (TextView) findViewById(R.id.selectedRelease);
		selectedMirror = (TextView) findViewById(R.id.selectedMirror);
		imagesize = (EditText) findViewById(R.id.imagesize);
		installButton = (Button) findViewById(R.id.installButton);
		progressBar = findViewById(R.id.progressBar);
		installLog = (TextView) findViewById(R.id.installLog);
		textScroll = (ScrollView) findViewById(R.id.textScroll);
		handler = new Handler();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!isExt2Supported()) {
			unwireButtons();
			renameInstallButton(R.string.uninstall);
			// TODO focus the button otherwise the imagesize EditText focuses
			// and pops up the keyboard
			installButton.requestFocus();
			installButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					UnsupportedDeviceActivity.callMe(InstallActivity.this);
				}
			});
		} else if (!TorServiceUtils.checkRootAccess()) {
			Toast.makeText(getApplicationContext(), R.string.needs_superuser_message,
					Toast.LENGTH_LONG).show();
			unwireButtons();
			renameInstallButton(R.string.get_superuser);
			installButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					// http://stackoverflow.com/questions/2518740/launch-market-place-with-id-of-an-application-that-doesnt-exist-in-the-android-m
					// final String APP_MARKET_URL =
					// "market://search?q=pname:com.noshufou.android.su";
					final String APP_MARKET_URL = "market://details?q=id:com.noshufou.android.su";
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(APP_MARKET_URL));
					startActivity(intent);
					finish();
				}
			});
		} else {
			refreshButtons();
			doBindService();
			registerReceivers();
			updateLog();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		doUnbindService();
		unregisterReceivers();
	}

	private void updateLog() {
		handler.post(new Runnable() {
			public void run() {
				if (mBoundService != null) {
					final String log = mBoundService.dumpLog();
					if (log != null && log.trim().length() > 0)
						installLog.setText(log);
				}
			}
		});
		handler.postDelayed(new Runnable() {
			public void run() {
				textScroll.scrollTo(0, installLog.getHeight());
			}
		}, 300);
	}

	private void wireButtons() {
		installButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				setResult(NativeHelper.STARTING_INSTALL);
				Intent intent = new Intent(InstallActivity.this, InstallService.class);
				intent.putExtra(RELEASE, selectedRelease.getText().toString());
				intent.putExtra(MIRROR, selectedMirror.getText().toString());
				intent.putExtra(IMAGESIZE, imagesize.getText().toString());
				startService(intent);
				Log.i(LilDebi.TAG, "Starting install service");
				handler.postDelayed(new Runnable() {
					public void run() {
						refreshButtons();
					}
				}, 300);
			}
		});

		selectedRelease.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				SelectRelease.callMe(InstallActivity.this);
			}
		});

		selectedMirror.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				SelectMirror.callMe(InstallActivity.this);
			}
		});

		refreshButtons();
	}

	private void registerReceivers() {
		{
			installLogUpdateRec = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					updateLog();
				}
			};

			IntentFilter filter = new IntentFilter(InstallService.INSTALL_LOG_UPDATE);
			registerReceiver(installLogUpdateRec, filter);
		}

		{
			installFinishedRec = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					intent.setAction(Intent.ACTION_VIEW);
					intent.setClass(getApplicationContext(), LilDebi.class);
					startActivity(intent);
					finish();
				}
			};

			IntentFilter filter = new IntentFilter(InstallService.INSTALL_FINISHED);
			registerReceiver(installFinishedRec, filter);
		}
	}

	private void unregisterReceivers() {
		if (installLogUpdateRec != null)
			unregisterReceiver(installLogUpdateRec);

		if (installFinishedRec != null)
			unregisterReceiver(installFinishedRec);
	}

	private void unwireButtons() {
		selectedRelease.setEnabled(false);
		selectedMirror.setEnabled(false);
		selectedMirror.setOnClickListener(null);
		imagesize.setEnabled(false);
		installButton.setOnClickListener(null);
		installButton.setVisibility(View.GONE);
		progressBar.setVisibility(View.GONE);
	}

	private void renameInstallButton(int resid) {
		installButton.setEnabled(true);
		installButton.setVisibility(View.VISIBLE);
		installButton.setText(getString(resid));
	}

	private void refreshButtons() {
		if (mBoundService != null && mBoundService.isRunning()) {
			selectedRelease.setEnabled(false);
			selectedMirror.setEnabled(false);
			imagesize.setEnabled(false);
			installButton.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
		} else {
			selectedRelease.setEnabled(true);
			selectedMirror.setEnabled(true);
			imagesize.setEnabled(true);
			installButton.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (data.hasExtra(RELEASE))
				selectedRelease.setText(data.getStringExtra(RELEASE));
			if (data.hasExtra(MIRROR))
				selectedMirror.setText(data.getStringExtra(MIRROR));
		}
	}

	private boolean isExt2Supported() {
		Context context = getApplicationContext();
		try {
			FileInputStream fstream = new FileInputStream("/proc/filesystems");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("ext2")) {
					return true;
				}
			}
			if (new File("/system/lib/modules/ext2.ko").exists())
				return true;
			Toast.makeText(context, R.string.no_ext2_message, Toast.LENGTH_LONG).show();
			return false;
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
			return false;
		}
	}

	public static void writeCommand(OutputStream os, String command) throws Exception {
		os.write((command + "\n").getBytes("ASCII"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_preferences:
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
			return true;
		}
		return false;
	}
}
