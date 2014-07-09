package info.guardianproject.lildebi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MediaUnmountedReceiver extends BroadcastReceiver {

	private LilDebiAction action;

	public void onReceive(Context context, Intent intent) {
		if (!NativeHelper.installInInternalStorage && NativeHelper.isStarted()) {
			action = new LilDebiAction(context, null);
			action.stopDebian();
		}
	}
}

