package info.guardianproject.lildebi;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: 4/18/11
 * Time: 10:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class DebiHelper
{
    public static File buildHomeDir(Context c)
    {
        return new File(buildHomeDirPath(c));
    }

    public static String buildHomeDirPath(Context c)
    {
        return "/data/data/" + c.getPackageName();
    }

    public static void unzipDebiFiles(Context context)
    {
        try
        {
            AssetManager am = context.getAssets();
            final String[] assetList = am.list("");

//            final InputStream allZipIS = am.open("all.zip");
//            ZipInputStream zin = new ZipInputStream(new BufferedInputStream(allZipIS));
//            ZipEntry entry;

            final File baseFolder = buildHomeDir(context);

            for (String asset : assetList)
            {
                if(
                        asset.equals("images") ||
                        asset.equals("sounds") ||
                        asset.equals("webkit")
                        )
                    continue;

                int BUFFER = 2048;
                final File file = new File(baseFolder, asset);
                final InputStream assetIS = am.open(asset);

                if(file.exists())
                    file.delete();

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
    }
}
