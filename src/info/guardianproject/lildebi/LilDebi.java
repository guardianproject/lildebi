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
	private String homeDir;
	private String imagename;
	private TextView statusText;
	private Button startStopButton;
	private ScrollView consoleScroll;
	private TextView consoleText;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		homeDir = DebiHelper.buildHomeDirPath(this);
		imagename = new String(Environment.getExternalStorageDirectory() + "/debian.img");
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
		File f = new File(imagename);
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
				String script;
				if (! debianInstalled)
				{
					// TODO get this launching the install screen
//	                Intent intent = new Intent(this, InstallDebian.class);
//	                startActivity(intent);
					return;
				}
/*
				if (debianMounted)
					script = "su - " + homeDir + "/stop-debian.sh";
				else
					script = "su - " + homeDir + "/start-debian.sh";
				try
				{
					Process sh = Runtime.getRuntime().exec(script);
					sh.waitFor();
				}
				catch (Exception e)
				{
					App.loge("Error in " + script, e);
				}
				finally
				{
					App.logi(script + "failed!");
				}
				*/
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
