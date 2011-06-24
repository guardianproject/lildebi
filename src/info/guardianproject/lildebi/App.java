package info.guardianproject.lildebi;

import android.util.Log;

public class App {
	public static void logi(String val) {
		Log.i(App.class.getName(), val);
	}

	public static void loge(String val, Exception e) {
		Log.e(App.class.getName(), val, e);
	}

}
