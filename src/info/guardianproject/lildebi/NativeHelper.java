package info.guardianproject.lildebi;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class NativeHelper {
	public static int STARTING_INSTALL = 12345;

	public static File app_bin;
	public static File app_log;
	public static File sh;
	public static String sdcard;
	public static String imagename;
	public static String mnt;
	public static String args;

	public static String postStartScript;
	public static String preStopScript;

	public static boolean isInstallRunning = false;

	public static void setup(Context context) {
		app_bin = context.getDir("bin", Context.MODE_PRIVATE).getAbsoluteFile();
		app_log = context.getDir("log", Context.MODE_PRIVATE).getAbsoluteFile();
		sh = new File(app_bin, "sh");
		sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
		imagename = sdcard + "/debian.img";
		mnt = "/debian";
		args = new String(" " + app_bin.getAbsolutePath() + " " + sdcard + " "
				+ imagename + " " + mnt + " ");
	}

	public static void unzipDebiFiles(Context context) {
		try {
			AssetManager am = context.getAssets();
			final String[] assetList = am.list("");

			// final InputStream allZipIS = am.open("all.zip");
			// ZipInputStream zin = new ZipInputStream(new
			// BufferedInputStream(allZipIS));
			// ZipEntry entry;

			for (String asset : assetList) {
				if (asset.equals("images")
						|| asset.equals("sounds")
						|| asset.equals("webkit")
						|| asset.equals("databases")
						|| asset.equals("kioskmode"))

					continue;

				int BUFFER = 2048;
				final File file = new File(NativeHelper.app_bin, asset);
				final InputStream assetIS = am.open(asset);

				if (file.exists()) {
					file.delete();
					Log.i(LilDebi.TAG, "DebiHelper.unzipDebiFiles() deleting "
							+ file.getAbsolutePath());
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
		chmod(0644, new File(app_bin, "debootstrap.tar.bz2"));
		chmod(0644, new File(app_bin, "lildebi-common"));
		chmod(0755, new File(app_bin, "create-debian-setup.sh"));
		chmod(0755, new File(app_bin, "remove-debian-setup.sh"));
		chmod(0755, new File(app_bin, "configure-downloaded-image.sh"));
		chmod(0755, new File(app_bin, "start-debian.sh"));
		chmod(0755, new File(app_bin, "stop-debian.sh"));
		chmod(0755, new File(app_bin, "shell"));
		chmod(0755, new File(app_bin, "test.sh"));
		chmod(0755, new File(app_bin, "pkgdetails"));
		chmod(0755, new File(app_bin, "gpgv"));
		chmod(0755, new File(app_bin, "busybox"));
	}

	public static void chmod(int mode, File path) {
		try {
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
		}
	}

	public static boolean isSdCardPresent() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	public static long getSdCardFreeBytes() {
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		return (long)stat.getAvailableBlocks() * (long)stat.getBlockSize();
	}
}
