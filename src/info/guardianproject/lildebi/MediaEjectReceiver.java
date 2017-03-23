package info.guardianproject.lildebi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// This receiver tries to killDebian when sdcard is present
public class MediaEjectReceiver extends BroadcastReceiver {

    private LilDebiAction action;

    public void onReceive(Context context, Intent intent) {
        if (!NativeHelper.installInInternalStorage) {
            if (NativeHelper.isStarted()) {
            action = new LilDebiAction(context, null);
            action.killDebian();
            }
            LilDebi.sdcardUnmounted();
        }
    }
}