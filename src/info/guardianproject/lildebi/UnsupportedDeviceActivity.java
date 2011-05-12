package info.guardianproject.lildebi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UnsupportedDeviceActivity extends Activity {

	Button uninstallButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unsupported_device);
		uninstallButton = (Button) findViewById(R.id.uninstallButton);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		uninstallButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
			}
		});
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	public static void callMe(Activity activity)
	{
		Intent intent = new Intent(activity, UnsupportedDeviceActivity.class);
		activity.startActivity(intent);
	}
}
