package info.guardianproject.lildebi;

import java.io.OutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class LilDebiAction {
	public static final String TAG = "LilDebiAction";

	private PowerManager.WakeLock wl;
	private boolean useWakeLock;

	static StringBuffer log = null;
	public String command;
	private CommandThread commandThread;
	private Handler commandThreadHandler;
	private Context context;

	public static final int LOG_UPDATE = 654321;
	public static final int COMMAND_FINISHED = 123456;

	LilDebiAction(Context context, Handler commandThreadHandler) {
		this.context = context;
		this.commandThreadHandler = commandThreadHandler;

		if (log == null)
			log = new StringBuffer();

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		useWakeLock = prefs.getBoolean(
				context.getString(R.string.pref_prevent_sleep_key), false);

		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				"StartStopWakeLock");
	}

	public void startDebian() {
		if (useWakeLock)
			wl.acquire();
		
		command = new String("LANG=" + NativeHelper.getLANG() +
		        " ./start-debian.sh" + NativeHelper.getArgs()
				+ " && " + NativeHelper.app_bin + "/chroot " + NativeHelper.mnt
				+ " /bin/bash -c \"" + NativeHelper.postStartScript + "\"");
		commandThread = new CommandThread();
		commandThread.start();
		Toast.makeText(context, R.string.starting_debian, Toast.LENGTH_LONG)
				.show();
	}

	public void stopDebian() {
		if (wl.isHeld())
			wl.release();
		
		command = new String(NativeHelper.app_bin + "/chroot "
				+ NativeHelper.mnt + " /bin/bash -c \""
				+ NativeHelper.preStopScript + "\"; ./stop-debian.sh "
				+ NativeHelper.getArgs());
		commandThread = new CommandThread();
		commandThread.start();
		Toast.makeText(context, R.string.stopping_debian, Toast.LENGTH_LONG)
				.show();
	}

	public void removeDebianSetup() {
		command = "./delete-all-debian-setup.sh " + NativeHelper.getArgs();
		commandThread = new CommandThread();
		commandThread.start();
	}

	public void configureDownloadedImage() {
		command = new String("./configure-downloaded-image.sh"
				+ NativeHelper.getArgs());
		commandThread = new CommandThread();
		commandThread.start();
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

				StreamThread it = new StreamThread(sh.getInputStream(),
						logUpdate);
				StreamThread et = new StreamThread(sh.getErrorStream(),
						logUpdate);

				it.start();
				et.start();

				writeCommand(os, "cd " + NativeHelper.app_bin.getAbsolutePath());
				writeCommand(os,
						"export PATH=" + NativeHelper.app_bin.getAbsolutePath());
				writeCommand(os, command);
				writeCommand(os, "exit");

				sh.waitFor();
				Log.i(LilDebi.TAG, "Done!");
			} catch (Exception e) {
				Log.e(LilDebi.TAG, "Error!!!", e);
			} finally {
				synchronized (LilDebiAction.this) {
					commandThread = null;
				}
				if (commandThreadHandler != null) {
					Message msg = commandThreadHandler.obtainMessage();
					msg.arg1 = COMMAND_FINISHED;
					commandThreadHandler.sendMessage(msg);
				}
			}
		}
	}

	public static void writeCommand(OutputStream os, String command)
			throws Exception {
		Log.i(TAG, command);
		os.write((command + "\n").getBytes("ASCII"));
	}

	class LogUpdate extends StreamThread.StreamUpdate {

		@Override
		public void update(String val) {
			log.append(val);
			if (commandThreadHandler != null) {
				Message msg = commandThreadHandler.obtainMessage();
				msg.arg1 = LilDebiAction.LOG_UPDATE;
				commandThreadHandler.sendMessage(msg);
			}
		}
	}
}