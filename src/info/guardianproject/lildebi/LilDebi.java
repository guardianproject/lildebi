package info.guardianproject.lildebi;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class LilDebi extends Activity implements OnCreateContextMenuListener
{
	private boolean debianInstalled;
	private boolean debianMounted;
	private TextView statusText;
	private Button startStopButton;
	private ScrollView consoleScroll;
	private TextView consoleText;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		DebiHelper.dataDir = getDir("bin", MODE_PRIVATE);
		DebiHelper.sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
		DebiHelper.imagename = DebiHelper.sdcard + "/debian.img";
		DebiHelper.mnt = "/data/debian";
		DebiHelper.envp = new String[4];
		DebiHelper.envp[0] = "dataDir=" + DebiHelper.dataDir.getAbsolutePath();
		DebiHelper.envp[1] = "sdcard=" + DebiHelper.sdcard;
		DebiHelper.envp[2] = "imagename=" + DebiHelper.imagename;
		DebiHelper.envp[3] = "mnt=" + DebiHelper.mnt;
		setContentView(R.layout.lildebi);
		statusText = (TextView) findViewById(R.id.statusText);
		startStopButton = (Button) findViewById(R.id.startStopButton);
		consoleScroll = (ScrollView)findViewById(R.id.consoleScroll);
		consoleText = (TextView)findViewById(R.id.consoleText);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		String state = Environment.getExternalStorageState();
		if (! Environment.MEDIA_MOUNTED.equals(state))
			Toast.makeText(getApplicationContext(),
					"The SD card/external storage is not mounted, cannot start Debian.", 
					Toast.LENGTH_LONG).show();
		File f = new File(DebiHelper.imagename);
		debianInstalled = f.exists();
		if (debianInstalled)
		{
			File f2 = new File("/data/debian/etc");
			if (f2.exists())
			{
				debianMounted = true;
				statusText.setText("Debian mounted");
				startStopButton.setText(R.string.title_stop);
			}
			else
			{
				debianMounted = false;
				statusText.setText("Debian not mounted");
				startStopButton.setText(R.string.title_start);
			}
		}
		else
		{
			debianMounted = false;
			statusText.setText("Debian not installed");
			startStopButton.setText("Install...");
		}
		startStopButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				DebiHelper.runCommandInAppPayload("sh ./test.sh");
				if (! debianInstalled)
				{
	                Intent intent = new Intent(getApplicationContext(), InstallActivity.class);
	                startActivity(intent);
					return;
				}
				if (debianMounted)
					DebiHelper.runCommandInAppPayload("su - ./stop-debian.sh");
				else
					DebiHelper.runCommandInAppPayload("su - ./start-debian.sh");
			}
		});
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.preferences_menu, menu);
        return true;
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_preferences:
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

}
