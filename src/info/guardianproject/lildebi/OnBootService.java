package info.guardianproject.lildebi;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class OnBootService extends Service {
	private OnBootThread onBootThread;

	public static final String START_DEBIAN_FINISHED = "START_DEBIAN_FINISHED";

	private StringBuffer log;

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
		Log.i(LilDebi.TAG, "OnBootService.onCreate() received ");
		DebiHelper.setup(getApplication());
	}

	public boolean isRunning() {
		synchronized (this) {
			return onBootThread != null && onBootThread.isAlive();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		synchronized (this) {
			log = new StringBuffer();
			onBootThread = new OnBootThread();
			onBootThread.start();
		}

		return START_STICKY;
	}

	class OnBootThread extends Thread {

		private LogUpdate logUpdate;

		@Override
		public void run() {
			android.os.Debug.waitForDebugger();
			// first, wait for the External Storage/SD Card to be mounted
			int retries = 0;
			while (!DebiHelper.isSdCardPresent()) {
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
			String startScript = DebiHelper.app_bin.getAbsolutePath()
					+ "/start-debian.sh " + DebiHelper.args;
			Log.i(LilDebi.TAG, "Running OnBootService");
			if (!new File(DebiHelper.imagename).exists()) {
				Log.e(LilDebi.TAG, DebiHelper.imagename + " does not exist!");
				stopSelf();
				return;
			}
			logUpdate = new LogUpdate();
			try {
				Process sh = Runtime.getRuntime().exec("su - sh");
				OutputStream os = sh.getOutputStream();

				StreamThread it = new StreamThread(sh.getInputStream(), logUpdate);
				StreamThread et = new StreamThread(sh.getErrorStream(), logUpdate);

				it.start();
				et.start();

				Log.i(LilDebi.TAG, startScript);
				writeCommand(os, startScript);
				writeCommand(os, "exit");

				sh.waitFor();
				Log.i(LilDebi.TAG, "Done!");
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
				FileWriter logfile = new FileWriter(getDir("log", MODE_PRIVATE)
						+ "/onboot.log");
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
}
