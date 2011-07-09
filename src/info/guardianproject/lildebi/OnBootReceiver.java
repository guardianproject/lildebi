package info.guardianproject.lildebi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action != null && action.equals("android.intent.action.BOOT_COMPLETED")) {
			Intent i = new Intent();
			i.setAction("info.guardianproject.lildebi.OnBootService");
			context.startService(i);
		}
	}
}
