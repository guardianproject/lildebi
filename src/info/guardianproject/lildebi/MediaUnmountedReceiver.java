package info.guardianproject.lildebi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// This receiver tries to stopDebian when sdcard is not present
public class MediaUnmountedReceiver extends BroadcastReceiver {

	private LilDebiAction action;

	public void onReceive(Context context, Intent intent) {
		// '!NativeHelper.isInstalled()' check tells us that the image is not
		// installed after SDcard is unmounted
		if (!NativeHelper.isInstalled() && NativeHelper.isStarted()) {
			action = new LilDebiAction(context, null);
			action.stopDebian();
		}
	}
}
