package info.guardianproject.lildebi;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: 4/21/11
 * Time: 7:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class InstallService extends Service
{
	private InstallThread installThread;

	public static final String INSTALL_LOG_UPDATE = "INSTALL_LOG_UPDATE";
	public static final String INSTALL_FINISHED = "INSTALL_FINISHED";

	private StringBuffer log;

	private String distro;
	private String mirror;
	private String imagesize;
	
	public class LocalBinder extends Binder
	{
		public InstallService getService()
		{
			return InstallService.this;
		}
	}

	public String dumpLog()
	{
		synchronized (this)
		{
			return log == null ? null : log.toString();
		}
	}

	private final IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent)
	{
		return mBinder;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

	}

	public boolean isRunning()
	{
		synchronized (this)
		{
			return installThread != null && installThread.isAlive();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		synchronized (this)
		{	
			distro = intent.getStringExtra(InstallDebian.DISTRO);
			mirror = intent.getStringExtra(InstallDebian.MIRROR);
			imagesize = intent.getStringExtra(InstallDebian.IMAGESIZE);
			log = new StringBuffer();
			installThread = new InstallThread();
			installThread.start();
		}

		return START_STICKY;
	}

	class InstallThread extends Thread
	{

		private LogUpdate logUpdate;

		@Override
		public void run()
		{
			logUpdate = new LogUpdate();

			DebiHelper.unzipDebiFiles(InstallService.this);
			logUpdate.update("Install files copied\n");

			try {
				Process sh = Runtime.getRuntime().exec("su - sh");
				OutputStream os = sh.getOutputStream();

				StreamThread it = new StreamThread(sh.getInputStream(), logUpdate);
				StreamThread et = new StreamThread(sh.getErrorStream(), logUpdate);

				it.start();
				et.start();

				final String homeDir = DebiHelper.buildHomeDirPath(InstallService.this);

				writeCommand(os, "cd "+ homeDir);

				writeCommand(os, "chmod 644 usr-share-debootstrap.tar.bz2");
				writeCommand(os, "chmod 644 lildebi-common");
				writeCommand(os, "chmod 755 create-debian-setup.sh");
				writeCommand(os, "chmod 755 remove-debian-setup.sh");
				writeCommand(os, "chmod 755 start-debian.sh");
				writeCommand(os, "chmod 755 stop-debian.sh");
				writeCommand(os, "chmod 755 busybox");
				writeCommand(os, "chmod 755 pkgdetails");
				writeCommand(os, "./create-debian-setup.sh " + distro + " http://" + mirror + "/debian/ " + imagesize);

				writeCommand(os, "exit");

				sh.waitFor();
				App.logi("Done!");
			}
			catch (Exception e)
			{
				App.loge("Error!!!", e);
			}
			finally {
				stopSelf();
				synchronized (InstallService.this)
				{
					installThread = null;
				}
				sendBroadcast(new Intent(INSTALL_FINISHED));
			}
		}
	}

	class LogUpdate extends StreamThread.StreamUpdate
	{

		StringBuffer sb = new StringBuffer();

		@Override
		public void update(String val)
		{
			log.append(val);
			sendBroadcast(new Intent(INSTALL_LOG_UPDATE));
		}
	}

	public static void writeCommand(OutputStream os, String command) throws Exception
	{
		os.write((command + "\n").getBytes("ASCII"));
	}
}
