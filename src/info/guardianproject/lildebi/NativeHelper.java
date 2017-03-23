package info.guardianproject.lildebi;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
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
    public static File app_profiles;
    public static File app_usergenprofiles;
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
    
    public static File file;
    public static String profileName;
    public static String generatedFileName;
    public static List<String> myProfileList = new ArrayList<String>();
    public static List<String> myGenProfileList = new ArrayList<String>();
    public static String DIRECTORY_PATH = "/data/data/info.guardianproject.lildebi/app_profiles";
    public static String USEGERGENPROFILES_PATH = "/data/data/info.guardianproject.lildebi/app_usergenprofiles/";
    public static String exportPath = "export TERM=linux \nexport HOME=/root \nexport PATH=$app_bin:/usr/bin:/usr/sbin:/bin:/sbin:$PATH \napt-get update \n";
    public static String Name;
    public static String path_to_install_script;
    public static String pre_start_script;
    public static String post_start_script;
    public static String gui_support;
    public static String fileName;

    public static void setup(Context context) {
        app_bin = context.getDir("bin", Context.MODE_PRIVATE).getAbsoluteFile();
        app_log = context.getDir("log", Context.MODE_PRIVATE).getAbsoluteFile();
        app_profiles = context.getDir("profiles", Context.MODE_PRIVATE).getAbsoluteFile();
        app_usergenprofiles = context.getDir("usergenprofiles", Context.MODE_PRIVATE).getAbsoluteFile();
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
        prefName = context.getString(R.string.pref_limit_to_4gb_key);
        limitTo4GB = prefs.getBoolean(prefName, true);
        postStartScript = prefs.getString(
                context.getString(R.string.pref_post_start_key),
                context.getString(R.string.default_post_start_script));
        preStopScript = prefs.getString(
                context.getString(R.string.pref_pre_stop_key),
                context.getString(R.string.default_pre_stop_script));
        boolean detectedInternalInstall = false;
        /* attempt to auto-detect existing internal install */
        if (!isStarted()
                && new File(NativeHelper.mnt + "/etc").exists()
                && !new File(image_path).exists())
            detectedInternalInstall = true;
        prefName = context.getString(R.string.pref_install_on_internal_storage_key);
        installInInternalStorage = prefs.getBoolean(prefName, detectedInternalInstall);
        if (installInInternalStorage)
            NativeHelper.image_path = NativeHelper.mnt;
        
        try{//writing default profiles
        	Log.d("try block", "in try");
        	File root = new File(DIRECTORY_PATH);
        	File gpxfile = new File(root, "ssh");
        	FileWriter profilewriter = new FileWriter(gpxfile);
        	StringBuilder profilestring = new StringBuilder();
        	profilestring.append(exportPath);
        	profilestring.append("apt-get install ssh -y");
        	profilewriter.append(profilestring);
        	profilewriter.flush();
        	profilewriter.close();

        	File gpxfile1 = new File(root, "apache2");
        	FileWriter profilewriter1 = new FileWriter(gpxfile1);
        	StringBuilder profilestring1 = new StringBuilder();
        	profilestring1.append(exportPath);
        	profilestring1.append("apt-get install apache2 -y");
        	profilewriter1.append(profilestring1);
        	profilewriter1.flush();
        	profilewriter1.close();

        	File gpxfile2 = new File(root, "wireshark");
        	FileWriter profilewriter2 = new FileWriter(gpxfile2);
        	StringBuilder profilestring2 = new StringBuilder();
        	profilestring2.append(exportPath);
        	profilestring2.append("apt-get install wireshark -y");
        	profilewriter2.append(profilestring2);
        	profilewriter2.flush();
        	profilewriter2.close();

        }catch (Exception e) {
        	e.printStackTrace();     
        }
        try {
        	File folder = new File(USEGERGENPROFILES_PATH);
        	File[] listOfFiles = folder.listFiles();
        	int countOfProfiles=0;
        	for (File file : listOfFiles) {
        		//get the count of profiles created by user 
        		if (file.isFile()) {
        			profileName = file.getName();
        			myProfileList.add(profileName);
        			Log.d("all profiles ", profileName);
        			countOfProfiles++;
        		}
        	}
        	String n[] = new String[countOfProfiles];
        	for (int i = 0; i < countOfProfiles; i++) {
        		n[i] = myProfileList.get(i) ;
        		Log.d("[i] value", n[i]);
        		Log.d("No. of profiles", ""+countOfProfiles);
        		Log.d("printing data for ", ""+n[i]);
        		file = new File( USEGERGENPROFILES_PATH + n[i]);
        		Log.d("file name", file.getPath());
        		Properties prop = new Properties();
        		InputStream input = null;
        		try {//reading the data from user generated profiles
        			input = new FileInputStream(file);
        			prop.load(input);
        			Name = prop.getProperty("Name");
        			path_to_install_script = prop.getProperty("path_to_install_script");
        			pre_start_script = prop.getProperty("pre_start_script");
        			post_start_script = prop.getProperty("post_start_script");
        			gui_support = prop.getProperty("gui_support");
        			File root = new File(DIRECTORY_PATH);
        			//store the data into script files accordingly
        			fileName = Name;
        			File gpxfile = new File(root, fileName);
        			FileWriter writer = new FileWriter(gpxfile);
        			StringBuilder s = new StringBuilder();
        			s.append(exportPath);
        			s.append("apt-get install -y "+Name+"\n");
        			s.append("sed -i '$ d' /etc/rc.local ");
        			s.append("echo '"+pre_start_script+"'>> /etc/rc.local ");
        			s.append("echo 'exit 0' > /etc/rc.local ");
        			writer.append(s);
        			writer.flush();
        			writer.close();

        			Log.d("Name", Name);
        			Log.d("path_to_install_script", path_to_install_script);
        			Log.d("pre_start_script", pre_start_script);
        			Log.d("post_start_script", post_start_script);
        			Log.d("gui_support", gui_support);
        		} catch (IOException ex) {
        			ex.printStackTrace();
        		}         			
        	}
        }catch (Exception e) {
        	e.printStackTrace();     
        }
        try {//allow scripts to execute by adding permissions to execute
        	File folder = new File(DIRECTORY_PATH);
        	File[] listOfFiles = folder.listFiles();
        	int countOfGeneratedFiles =0;
        	for (File file : listOfFiles) {
        		if (file.isFile()) {
        			generatedFileName = file.getName();
        			myGenProfileList.add(generatedFileName);
        			chmod(0755, new File(app_profiles, generatedFileName));
        			Log.d("all generated files ", generatedFileName);
        			countOfGeneratedFiles++;
        		}
        	}
        	Log.d("countOfGeneratedFiles", ""+countOfGeneratedFiles);
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

    public static boolean isStarted() {
        if (installInInternalStorage) {
            /*
             * When installed on the internal storage, the "started" state means
             * that the bind mounts are setup, so this checks for a directory
             * that will only exist if the bind mount is setup.
             */
            return new File(NativeHelper.mnt + "/system/bin").exists();
        } else {
            /*
             * When installed on a loopback image file, then the "started" state
             * means that the loopback image file is mounted, so this checks for
             * a file that can only be in the loopback image.
             */
            return new File(NativeHelper.mnt + "/etc").exists();
        }
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

    public static String getLANG() {
        Locale locale = Locale.getDefault();
        if (locale == null)
            return "";
        else
            return locale.getLanguage() + "_" + locale.getCountry();
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
        chmod(0755, new File(app_bin, "delete-all-debian-setup.sh"));
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

    public static long getInstallPathFreeMegaBytes() {
        String path;
        StatFs stat;
        long freeSizeInBytes;
        File parent = new File(image_path).getParentFile();
        try {
            path = parent.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            path = parent.getAbsolutePath();
        }
        stat = new StatFs(path);
        freeSizeInBytes = stat.getAvailableBlocks() * (long) stat.getBlockSize();
        return freeSizeInBytes / 1024 / 1024;
    }
}
