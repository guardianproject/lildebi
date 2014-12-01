package info.guardianproject.lildebi;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootCompletedReceiver {

    private LilDebiAction action;

    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        if (NativeHelper.installInInternalStorage && !NativeHelper.isStarted()) {
            if (prefs.getBoolean(
                    context.getString(R.string.pref_start_automatically_key), false)) {
                if (new File(NativeHelper.image_path).exists()
                        && new File(NativeHelper.mnt).exists()) {
                    action = new LilDebiAction(context, null);
                    action.startDebian();
                }
            }
        }

    }

}
