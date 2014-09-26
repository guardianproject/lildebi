package info.guardianproject.lildebi;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Scanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.util.Log;

public class NativeHelper {
	public static int STARTING_INSTALL = 12345;

	public static File app_bin;
	public static File app_log;
	public static File install_log;
	public static File publicFiles;
	public static File sh;
	public static File install_conf;
	public static File versionFile;
	public static String sdcard;
	public static String image_path;
	public static String default_image_path;
	public static String mnt;

	public static String postStartScript;
	public static String preStopScript;

	public static boolean isInstallRunning = false;
	public static boolean installInInternalStorage;
	public static boolean limitTo4GB;

	public static void setup(Context context) {
		app_bin = context.getDir("bin", Context.MODE_PRIVATE).getAbsoluteFile();
		app_log = context.getDir("log", Context.MODE_PRIVATE).getAbsoluteFile();
		install_log = new File(app_log, "install.log");
		// this is the same as android-8's getExternalFilesDir() but works on android-1
		publicFiles = new File(Environment.getExternalStorageDirectory(),
				"Android/data/" + context.getPackageName() + "/files/");
		publicFiles.mkdirs();
		sh = new File(app_bin, "sh");
		install_conf = new File(app_bin, "install.conf");
		versionFile = new File(app_bin, "VERSION");
		// they did a real hackjob with the "external storage" aka SD Card path
		// in 4.3/4.4, so we have to add this kludge to make sure we have a path
		// we can work with.
		File dir = new File(System.getenv("EXTERNAL_STORAGE"));
		if (!dir.exists())
			dir = Environment.getExternalStorageDirectory();
		try {
			sdcard = dir.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
			sdcard = dir.getAbsolutePath();
		}

		mnt = "/data/debian";

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		default_image_path = sdcard + "/debian.img";
		String prefName = context.getString(R.string.pref_image_path_key);
		image_path = prefs.getString(prefName, default_image_path);
        prefName = context.getString(R.string.pref_install_on_internal_storage_key);
        installInInternalStorage = prefs.getBoolean(prefName, false);
        prefName = context.getString(R.string.pref_limit_to_4gb_key);
        limitTo4GB = prefs.getBoolean(prefName, true);
	}

	public static boolean isStarted() {
		if (new File(NativeHelper.mnt + "/system/bin").exists())
			return true;
		else
			return false;
	}
	
	public static boolean isInstalled() {
		if (installInInternalStorage)
			return (new File(NativeHelper.mnt + "/etc").exists());
		else
			return (new File(NativeHelper.image_path).exists());
	}

	public static String getArgs() {
		return new String(" " + app_bin.getAbsolutePath() + " " + sdcard + " "
				+ image_path + " " + mnt + " ");
	}

	private static int readVersionFile() {
		if (! versionFile.exists()) return 0;
		Scanner in;
		int versionCode = 0;
		try {
			in = new Scanner(versionFile);
			versionCode = Integer.parseInt(in.next());
			in.close();
		} catch (Exception e) {
			LilDebiAction.log.append("Can't read app version file: " + e.getLocalizedMessage() + "\n");
		}
		return versionCode;
	}

	private static void writeVersionFile(Context context) {
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			FileOutputStream fos = new FileOutputStream(versionFile);
			OutputStreamWriter out = new OutputStreamWriter(fos);
			out.write(String.valueOf(pInfo.versionCode) + "\n");
			out.close();
			fos.close();
		} catch (Exception e) {
			LilDebiAction.log.append("Can't write app version file: " + e.getLocalizedMessage() + "\n");
		}
	}

	private static int getCurrentVersionCode(Context context) {
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pInfo.versionCode;
		} catch (Exception e) {
			LilDebiAction.log.append("Can't get app version: " + e.getLocalizedMessage() + "\n");
			return 0;
		}
	}

	private static void renameOldAppBin() {
		String moveTo = app_bin.toString();
		Calendar now = Calendar.getInstance();
		int version = readVersionFile();
		if (version == 0) {
			moveTo += ".old";
		} else {
			moveTo += ".build" + String.valueOf(version);
		}
		moveTo += "." + String.valueOf(now.getTimeInMillis());
		LilDebiAction.log.append("Moving '" + app_bin + "' to '" + moveTo + "'\n");
		app_bin.renameTo(new File(moveTo));
		app_bin.mkdir(); // Android normally creates this at onCreate()
	}

	public static void unzipDebiFiles(Context context) {
		try {
			AssetManager am = context.getAssets();
			final String[] assetList = am.list("");

			for (String asset : assetList) {
				if (asset.equals("images")
						|| asset.equals("sounds")
						|| asset.equals("webkit")
						|| asset.equals("databases")  // Motorola
						|| asset.equals("kioskmode")) // Samsung
					continue;

				int BUFFER = 2048;
				final File file = new File(app_bin, asset);
				InputStream tmp;
				try {
					tmp = am.open(asset);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					continue;
				}
				final InputStream assetIS = tmp;

				if (file.exists()) {
					file.delete();
					LilDebiAction.log.append("DebiHelper.unzipDebiFiles() deleting " + file.getAbsolutePath() + "\n");
				}

				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

				int count;
				byte[] data = new byte[BUFFER];

				while ((count = assetIS.read(data, 0, BUFFER)) != -1) {
					// System.out.write(x);
					dest.write(data, 0, count);
				}

				dest.flush();
				dest.close();

				assetIS.close();
			}
		} catch (IOException e) {
			Log.e(LilDebi.TAG, "Can't unzip", e);
		}
		chmod(0644, new File(app_bin, "cdebootstrap.tar"));
		chmod(0644, new File(app_bin, "lildebi-common"));
		chmod(0755, new File(app_bin, "create-debian-setup.sh"));
		chmod(0755, new File(app_bin, "complete-debian-setup.sh"));
		chmod(0755, new File(app_bin, "unmounted-install-tweaks.sh"));
		chmod(0755, new File(app_bin, "remove-debian-setup.sh"));
		chmod(0755, new File(app_bin, "configure-downloaded-image.sh"));
		chmod(0755, new File(app_bin, "start-debian.sh"));
		chmod(0755, new File(app_bin, "stop-debian.sh"));
		chmod(0755, new File(app_bin, "shell"));
		chmod(0755, new File(app_bin, "test.sh"));
		chmod(0755, new File(app_bin, "gpgv"));
		chmod(0755, new File(app_bin, "busybox"));
		writeVersionFile(context);
	}

	public static void installOrUpgradeAppBin(Context context) {
		if (versionFile.exists()) {
			if (getCurrentVersionCode(context) > readVersionFile()) {
				LilDebiAction.log.append("Upgrading '" + app_bin + "'\n");
				// upgrade, rename current app_bin, then unpack
				renameOldAppBin();
				unzipDebiFiles(context);
			} else {
				Log.i(LilDebi.TAG, "Not upgrading '" + app_bin + "'\n");
			}
		} else {
			File[] list = app_bin.listFiles();
			if (list == null || list.length > 0) {
				LilDebiAction.log.append("Old, unversioned app_bin dir, upgrading.\n");
				renameOldAppBin();
			} else {
				LilDebiAction.log.append("Fresh app_bin install.\n");
			}
			unzipDebiFiles(context);
		}
	}

	public static void chmod(int mode, File path) {
		try {
			if (!path.exists())
				throw new IOException();
			Class<?> fileUtils = Class.forName("android.os.FileUtils");
			Method setPermissions = fileUtils.getMethod("setPermissions", String.class,
					int.class, int.class, int.class);
			int a = (Integer) setPermissions.invoke(null, path.getAbsolutePath(), mode,
					-1, -1);
			if (a != 0) {
				Log.i(LilDebi.TAG, "ERROR: android.os.FileUtils.setPermissions() returned " + a
						+ " for '" + path + "'");
			}
		} catch (ClassNotFoundException e) {
			Log.i(LilDebi.TAG, "android.os.FileUtils.setPermissions() failed - ClassNotFoundException.");
		} catch (IllegalAccessException e) {
			Log.i(LilDebi.TAG, "android.os.FileUtils.setPermissions() failed - IllegalAccessException.");
		} catch (InvocationTargetException e) {
			Log.i(LilDebi.TAG, "android.os.FileUtils.setPermissions() failed - InvocationTargetException.");
		} catch (NoSuchMethodException e) {
			Log.i(LilDebi.TAG, "android.os.FileUtils.setPermissions() failed - NoSuchMethodException.");
		} catch (IOException e) {
			Log.e(LilDebi.TAG, path + " does not exist!");
			e.printStackTrace();
		}
	}

	public static boolean isSdCardPresent() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

    public static long getInstallPathFreeBytes() {
        String path;
        StatFs stat;
        File parent = new File(image_path).getParentFile();
        try {
            path = parent.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            path = parent.getAbsolutePath();
        }
        stat = new StatFs(path);
        return (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
    }
}
