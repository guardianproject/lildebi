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
			NativeHelper.isInstallRunning = true;
			log = new StringBuffer();
			installThread = new InstallThread();
			installThread.start();
		}

		return START_STICKY;
	}

	class InstallThread extends Thread {

		private LogUpdate logUpdate;

		public void writeCommand(OutputStream os, String command) throws Exception {
			Log.i(LilDebi.TAG, "writeCommand: " + command);
			log.append("# " + command + "\n");
			os.write((command + "\n").getBytes("ASCII"));
		}

		@Override
		public void run() {
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

				String stdArgs = NativeHelper.getArgs();
				String command = "./create-debian-setup.sh " + stdArgs;
				command += "\\\n&& " + "./stop-debian.sh " + stdArgs;
				command += "\\\n&& " + "./unmounted-install-tweaks.sh " + stdArgs;
				command += "\\\n&& " + "./start-debian.sh " + stdArgs;
				command += "\\\n&& " + NativeHelper.app_bin + "/chroot " + NativeHelper.mnt
						+ " " + NativeHelper.app_bin + "/complete-debian-setup.sh "
						+ stdArgs;
				writeCommand(os, "cd " + NativeHelper.app_bin.getAbsolutePath());
				writeCommand(os, command);
				// Avoid keeping the resource mounted because of some failure
				writeCommand(os, "./stop-debian.sh " + stdArgs);
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
				NativeHelper.isInstallRunning = false;
				sendBroadcast(new Intent(INSTALL_FINISHED));
			}
			try {
				FileWriter logfile = new FileWriter(NativeHelper.install_log);
				logfile.append(dumpLog());
				logfile.close();
			} catch (Exception e) {
				Log.e(LilDebi.TAG, "Error writing install log file: " + NativeHelper.install_log, e);
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
}
