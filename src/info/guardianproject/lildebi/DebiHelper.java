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

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: 4/18/11
 * Time: 10:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class DebiHelper
{
	public static File dataDir;
	public static String sdcard;
	public static String imagename;
	public static String mnt;
	public static String[] envp;

	public static void unzipDebiFiles(Context context)
	{
		try
		{
			AssetManager am = context.getAssets();
			final String[] assetList = am.list("");

			//            final InputStream allZipIS = am.open("all.zip");
			//            ZipInputStream zin = new ZipInputStream(new BufferedInputStream(allZipIS));
			//            ZipEntry entry;

			for (String asset : assetList)
			{
				if(
						asset.equals("images") ||
						asset.equals("sounds") ||
						asset.equals("webkit")
				)
					continue;

				int BUFFER = 2048;
				final File file = new File(DebiHelper.dataDir, asset);
				final InputStream assetIS = am.open(asset);

				if(file.exists())
				{
					file.delete();
					App.logi("DebiHelper.unzipDebiFiles() deleting " + file.getAbsolutePath());
				}

				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

				int count;
				byte[] data = new byte[BUFFER];

				while ((count = assetIS.read(data, 0, BUFFER)) != -1)
				{
					//System.out.write(x);
					dest.write(data, 0, count);
				}

				dest.flush();
				dest.close();
				
				assetIS.close();
			}
		}
		catch (IOException e)
		{
			App.loge("Can't unzip", e);
		}
		chmod(0644, new File(dataDir, "usr-share-debootstrap.tar.bz2"));
		chmod(0644, new File(dataDir, "lildebi-common"));
		chmod(0755, new File(dataDir, "create-debian-setup.sh"));
		chmod(0755, new File(dataDir, "remove-debian-setup.sh"));
		chmod(0755, new File(dataDir, "start-debian.sh"));
		chmod(0755, new File(dataDir, "stop-debian.sh"));
		chmod(0755, new File(dataDir, "test.sh"));
		chmod(0755, new File(dataDir, "busybox"));
		chmod(0755, new File(dataDir, "pkgdetails"));
	}
	
	public static void runCommandInAppPayload(String command)
	{
		Runtime runtime = Runtime.getRuntime();
		try
		{
			Process sh = runtime.exec(command, envp, dataDir);
			sh.waitFor();
		}
		catch (Exception e)
		{
			App.loge("Error running " + command, e);
		}
		finally
		{
			App.logi(command + "failed!");
		}
	}
	
	public static void chmod(int mode, File path)
	{
		try
		{
			Class<?> fileUtils = Class.forName("android.os.FileUtils");
			Method setPermissions =
				fileUtils.getMethod("setPermissions", String.class, int.class, int.class, int.class);
			int a = (Integer) setPermissions.invoke(null, path.getAbsolutePath(), mode, -1, -1);
			if(a != 0)
			{
				App.logi("ERROR: android.os.FileUtils.setPermissions() returned " + a + " for '" + path + "'");
			}
		}
		catch(ClassNotFoundException e)
		{
			App.logi("android.os.FileUtils.setPermissions() failed - ClassNotFoundException.");
		}
		catch(IllegalAccessException e)
		{
			App.logi("android.os.FileUtils.setPermissions() failed - IllegalAccessException.");
		}
		catch(InvocationTargetException e)
		{
			App.logi("android.os.FileUtils.setPermissions() failed - InvocationTargetException.");
		}
		catch(NoSuchMethodException e)
		{
			App.logi("android.os.FileUtils.setPermissions() failed - NoSuchMethodException.");
		}
	}

}
