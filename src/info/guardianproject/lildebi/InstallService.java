package info.guardianproject.lildebi;

import java.io.FileWriter;
import java.io.OutputStream;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class InstallService extends Service {
	private InstallThread installThread;

	public static final String INSTALL_LOG_UPDATE = "INSTALL_LOG_UPDATE";
	public static final String INSTALL_FINISHED = "INSTALL_FINISHED";

	private StringBuffer log;

	private String release;
	private String mirror;
	private String imagesize;

	public class LocalBinder extends Binder {
		public InstallService getService() {
			return InstallService.this;
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

	}

	public boolean isRunning() {
		synchronized (this) {
			return installThread != null && installThread.isAlive();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		synchronized (this) {
			release = intent.getStringExtra(InstallActivity.RELEASE);
			mirror = intent.getStringExtra(InstallActivity.MIRROR);
			imagesize = intent.getStringExtra(InstallActivity.IMAGESIZE);
			DebiHelper.isInstallRunning = true;
			log = new StringBuffer();
			installThread = new InstallThread();
			installThread.start();
		}

		return START_STICKY;
	}

	class InstallThread extends Thread {

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

				Log.i(LilDebi.TAG, "cd " + DebiHelper.app_bin.getAbsolutePath());
				writeCommand(os, "modprobe ext2");
				writeCommand(os, "cd " + DebiHelper.app_bin.getAbsolutePath());
				writeCommand(os, "./create-debian-setup.sh " + DebiHelper.args + release
						+ " http://" + mirror + "/debian/ " + imagesize);
				writeCommand(os, "exit");

				sh.waitFor();
				Log.i(LilDebi.TAG, "Done!");
			} catch (Exception e) {
				Log.e(LilDebi.TAG, "Error!!!", e);
			} finally {
				stopSelf();
				synchronized (InstallService.this) {
					installThread = null;
				}
				DebiHelper.isInstallRunning = false;
				sendBroadcast(new Intent(INSTALL_FINISHED));
			}
			try {
				FileWriter logfile = new FileWriter(DebiHelper.app_log + "/install.log");
				logfile.append(log.toString());
				logfile.close();
			} catch (Exception e) {
				Log.e(LilDebi.TAG, "Error writing install log file", e);
			}
		}
	}

	class LogUpdate extends StreamThread.StreamUpdate {

		StringBuffer sb = new StringBuffer();

		@Override
		public void update(String val) {
			log.append(val);
			sendBroadcast(new Intent(INSTALL_LOG_UPDATE));
		}
	}

	public static void writeCommand(OutputStream os, String command) throws Exception {
		os.write((command + "\n").getBytes("ASCII"));
	}
}
