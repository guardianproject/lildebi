package info.guardianproject.lildebi;

import android.util.Log;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: 4/1/11
 * Time: 3:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class App {
    public static void logi(String val)
    {
        Log.i(App.class.getName(), val);
    }
    public static void loge(String val, Exception e)
    {
        Log.e(App.class.getName(), val, e);
    }

}
