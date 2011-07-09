package info.guardianproject.lildebi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OnBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		android.os.Debug.waitForDebugger();
		String action = intent.getAction();
		Log.i("OnBootReceiver", "OnBootReceiver.onReceive() received " + action);
		// TODO for now, we only check if the media has been mounted, we should
		// first check to see if we have received BOOT_COMPLETED first
		if (action != null && action.equals("android.intent.action.BOOT_COMPLETED")) {
			// TODO check intent extras to see if media was mounted read-only
			Log.i("OnBootReceiver", "we got it, let's go!");

			Intent i = new Intent();
			i.setAction("info.guardianproject.lildebi.OnBootService");
			context.startService(i);
		}
	}
}
