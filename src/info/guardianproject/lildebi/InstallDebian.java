package info.guardianproject.lildebi;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class InstallDebian extends Activity
{
	public static final String DISTRO = "DISTRO";
	public static final String MIRROR = "MIRROR";
	public static final String IMAGESIZE = "IMAGESIZE";

	private boolean ext2SupportChecked = false;
	private TextView installSource;
	private InstallService mBoundService;

	private ServiceConnection mConnection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			mBoundService = ((InstallService.LocalBinder) service).getService();
			App.logi("calling serviceBound");
			wireButtons();
		}

		public void onServiceDisconnected(ComponentName className)
		{
			mBoundService = null;
			unwireButtons();
		}
	};
	private boolean mIsBound;
	private Button installButton;
	private View progressBar;
	private TextView installLog;
	private ScrollView textScroll;
	private Handler handler;
	private BroadcastReceiver installLogUpdateRec;
	private BroadcastReceiver installFinishedRec;

	void doBindService()
	{
		App.logi("bindService");
		bindService(new Intent(this, InstallService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService()
	{
		if (mIsBound)
		{
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.install_debian);
		installSource = (TextView)findViewById(R.id.installSource);
		installSource.setText("http://www.someserver.org");
		installButton = (Button) findViewById(R.id.installButton);
		progressBar = findViewById(R.id.progressBar);
		installLog = (TextView)findViewById(R.id.installLog);
		textScroll = (ScrollView)findViewById(R.id.textScroll);
		handler = new Handler();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if (isExt2Supported())
		{
			doBindService();
			registerReceivers();
			updateLog();
		} 
		else
		{
			installButton.setText("Uninstall...");
			installButton.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View view)
				{
					UnsupportedDeviceActivity.callMe(InstallDebian.this);
				}
			});
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		doUnbindService();
		unregisterReceivers();
	}

	private void updateLog()
	{
		handler.post(new Runnable()
		{
			public void run()
			{
				if (mBoundService != null) 
				{
					final String log = mBoundService.dumpLog();
					if(log != null && log.trim().length() > 0)
						installLog.setText(log);
				}
			}
		});
		handler.postDelayed(new Runnable()
		{
			public void run()
			{
				textScroll.scrollTo(0, installLog.getHeight());
			}
		}, 300);
	}

	private void wireButtons()
	{
		installButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				Intent intent = new Intent(InstallDebian.this, InstallService.class);
				intent.putExtra(DISTRO, "stable");
				intent.putExtra(MIRROR, installSource.getText().toString());
				intent.putExtra(IMAGESIZE, 256);
				startService(intent);
				App.logi("Starting install service");
				handler.postDelayed(new Runnable()
				{
					public void run()
					{
						refreshButtons();
					}
				}, 300);
			}
		});

		installSource.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				SelectInstallMirror.callMe(InstallDebian.this);
			}
		});

		refreshButtons();
	}

	private void registerReceivers()
	{
		{
			installLogUpdateRec = new BroadcastReceiver()
			{
				@Override
				public void onReceive(Context context, Intent intent)
				{
					updateLog();
				}
			};

			IntentFilter filter = new IntentFilter(InstallService.INSTALL_LOG_UPDATE);
			registerReceiver(installLogUpdateRec, filter);
		}

		{
			installFinishedRec = new BroadcastReceiver()
			{
				@Override
				public void onReceive(Context context, Intent intent)
				{
					wireButtons();
				}
			};

			IntentFilter filter = new IntentFilter(InstallService.INSTALL_FINISHED);
			registerReceiver(installFinishedRec, filter);
		}
	}

	private void unregisterReceivers()
	{
		if(installLogUpdateRec != null)
			unregisterReceiver(installLogUpdateRec);

		if(installFinishedRec != null)
			unregisterReceiver(installFinishedRec);
	}

	private void unwireButtons()
	{
		installButton.setOnClickListener(null);
		installButton.setVisibility(View.GONE);

		progressBar.setVisibility(View.GONE);

		installSource.setOnClickListener(null);
	}

	private void refreshButtons()
	{
		installButton.setVisibility(mBoundService.isRunning() ? View.GONE : View.VISIBLE);
		progressBar.setVisibility(mBoundService.isRunning() ? View.VISIBLE : View.GONE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode == RESULT_OK)
		{
			String mirror = data.getStringExtra(MIRROR);
			installSource.setText(mirror);
		}
	}

	private boolean isExt2Supported()
	{
		Context context = getApplicationContext();
		ext2SupportChecked = true;
		try
		{
			FileInputStream fstream = new FileInputStream("/proc/filesystems");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null)
			{
				if (line.contains("ext2"))
				{
					return true;
				}
			}
			Toast.makeText(context,
					"/proc/filesystems does not list 'ext2' as a supported filesystem", 
					Toast.LENGTH_LONG).show();
			return false;
		}
		catch (Exception e)
		{
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
			return false;
		}
	}

	public static void writeCommand(OutputStream os, String command) throws Exception
	{
		os.write((command + "\n").getBytes("ASCII"));
	}
}
