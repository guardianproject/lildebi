package info.guardianproject.lildebi;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

public class InstallService extends Service {
    private InstallThread installThread;

    public static final String INSTALL_LOG_UPDATE = "INSTALL_LOG_UPDATE";
    public static final String INSTALL_FINISHED = "INSTALL_FINISHED";

    public class LocalBinder extends Binder {
        public InstallService getService() {
            return InstallService.this;
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
        Intent i = new Intent(this, InstallActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(getString(R.string.title_install_log_view))
                .setContentIntent(pi)
                .setLights(0xffff00ff, 1, 0)
                .setOngoing(true);
        startForeground(13117, builder.build());
        synchronized (this) {
            NativeHelper.isInstallRunning = true;
            installThread = new InstallThread();
            installThread.start();
        }

        return START_STICKY;
    }

    class InstallThread extends Thread {

        private LogUpdate logUpdate;

        public void writeCommand(OutputStream os, String command) throws Exception {
            Log.i(LilDebi.TAG, "writeCommand: " + command);
            logUpdate.update("# " + command + "\n");
            os.write((command + "\n").getBytes("ASCII"));
        }

        @Override
        public void run() {
            logUpdate = new LogUpdate();
            logUpdate.open();
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
                command += "\\\n&& " + NativeHelper.app_profiles + "./"+ LilDebi.selProfile + stdArgs;
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
                logUpdate.close();
                stopSelf();
                synchronized (InstallService.this) {
                    installThread = null;
                }
                NativeHelper.isInstallRunning = false;
                sendBroadcast(new Intent(INSTALL_FINISHED));
            }
        }
    }

    class LogUpdate extends StreamThread.StreamUpdate {

        StringBuffer sb = new StringBuffer();
        FileWriter logWriter;

        public void open() {
            try {
                logWriter = new FileWriter(NativeHelper.install_log);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void close() {
            try {
                logWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void update(String value) {
            if (logWriter != null) {
                try {
                    logWriter.append(value);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Intent intent = new Intent(INSTALL_LOG_UPDATE);
            intent.putExtra(Intent.EXTRA_TEXT, value);
            sendBroadcast(intent);
        }
    }
}
