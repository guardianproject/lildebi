package info.guardianproject.lildebi;

import android.app.Application;

public class LilDebiApplication extends Application {
    public static final String TAG = "LilDebiApplication";

    public void onCreate() {
        super.onCreate();
        NativeHelper.setup(getApplicationContext());
        // TODO move this code from NativeHelper to here
    }
}
