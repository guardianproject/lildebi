package info.guardianproject.lildebi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class LilDebi extends Activity implements OnCreateContextMenuListener
{
	private String imagename;
	private Button startStopButton;
	private ScrollView consoleScroll;
	private TextView consoleText;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		imagename = new String(Environment.getExternalStorageDirectory() + "/debian.img");
		setContentView(R.layout.lildebi);
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
