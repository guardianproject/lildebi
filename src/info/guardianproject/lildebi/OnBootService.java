package info.guardianproject.lildebi;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class OnBootService extends Service {

	private OnBootThread onBootThread;
	public static final String START_DEBIAN_FINISHED = "START_DEBIAN_FINISHED";
	private static final int DEBIAN_STARTED_NOTIFICATION = 1;
	private StringBuffer log;
	private boolean startOnBoot;

	public class LocalBinder extends Binder {
		public OnBootService getService() {
			return OnBootService.this;
		}
	}

	public String dumpLog() {
		synchronized (this) {
			return log == null ? null : log.toString();
		}
	}

	private final IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		NativeHelper.setup(getApplication());
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		startOnBoot = prefs.getBoolean(getString(R.string.pref_start_on_boot_key), false);
	}

	public boolean isRunning() {
		synchronized (this) {
			return onBootThread != null && onBootThread.isAlive();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (startOnBoot) {
			synchronized (this) {
				log = new StringBuffer();
				onBootThread = new OnBootThread();
				onBootThread.start();
			}
			return START_STICKY;
		} else {
			return START_NOT_STICKY;
		}
	}

	class OnBootThread extends Thread {

		private LogUpdate logUpdate;

		@Override
		public void run() {
			android.os.Debug.waitForDebugger();
			// first, wait for the External Storage/SD Card to be mounted
			int retries = 0;
			while (!NativeHelper.isSdCardPresent()) {
				retries++;
				if (retries > 100) {
					stopSelf();
					return;
				}
				try {
					sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			String startScript = NativeHelper.app_bin.getAbsolutePath()
					+ "/start-debian.sh " + NativeHelper.args;
			if (!new File(NativeHelper.imagename).exists()) {
				// TODO send notification that imagename doesn't exist
				stopSelf();
				return;
			}
			logUpdate = new LogUpdate();
			try {
				String suCmd = "su -s " + NativeHelper.sh.getAbsolutePath();
				Log.i(LilDebi.TAG, "exec: " + suCmd);
				Process sh = Runtime.getRuntime().exec(suCmd);
				OutputStream os = sh.getOutputStream();

				StreamThread it = new StreamThread(sh.getInputStream(), logUpdate);
				StreamThread et = new StreamThread(sh.getErrorStream(), logUpdate);

				it.start();
				et.start();

				Log.i(LilDebi.TAG, startScript);
				writeCommand(os, startScript);
				writeCommand(os, "exit");

				sh.waitFor();
				sendNotification();
			} catch (Exception e) {
				Log.e(LilDebi.TAG, "Error!!!", e);
			} finally {
				stopSelf();
				synchronized (OnBootService.this) {
					onBootThread = null;
				}
				sendBroadcast(new Intent(START_DEBIAN_FINISHED));
			}
			try {
				FileWriter logfile = new FileWriter(NativeHelper.app_log + "/onboot.log");
				logfile.append(log.toString());
				logfile.close();
			} catch (Exception e) {
				Log.e(LilDebi.TAG, "Error writing onboot.log file", e);
			}
		}
	}

	class LogUpdate extends StreamThread.StreamUpdate {

		StringBuffer sb = new StringBuffer();

		@Override
		public void update(String val) {
			log.append(val);
		}
	}

	public static void writeCommand(OutputStream os, String command) throws Exception {
		os.write((command + "\n").getBytes("ASCII"));
	}

	private void sendNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.icon, "Debian started",
				System.currentTimeMillis());
		notification.flags |= Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
		Intent myIntent = new Intent(this, LilDebi.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(OnBootService.this, 0,
				myIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		notification.setLatestEventInfo(this, "Debian started",
				"Lil' Debi has started your Debian chroot.", pendingIntent);
		notificationManager.notify(DEBIAN_STARTED_NOTIFICATION, notification);
	}
}
