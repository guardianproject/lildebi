package info.guardianproject.lildebi;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.OutputStream;

public class LilDebiInstall extends Activity
{
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
    private View installButton;
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
        setContentView(R.layout.install_debby);
        installSource = (TextView)findViewById(R.id.installSource);
        installSource.setText("http://www.someserver.org");
        installButton = findViewById(R.id.installButton);
        progressBar = findViewById(R.id.progressBar);
        installLog = (TextView)findViewById(R.id.installLog);
        textScroll = (ScrollView)findViewById(R.id.textScroll);
        handler = new Handler();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        doBindService();
        registerReceivers();
        updateLog();
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
                final String log = mBoundService.dumpLog();
                if(log != null && log.trim().length() > 0)
                    installLog.setText(log);
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
                startService(new Intent(LilDebiInstall.this, InstallService.class));
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
                SelectInstallMirror.callMe(LilDebiInstall.this);
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
            String mirror = data.getStringExtra(SelectInstallMirror.MIRROR);
            installSource.setText(mirror);
        }
    }

    public static void writeCommand(OutputStream os, String command) throws Exception
    {
        os.write((command + "\n").getBytes("ASCII"));
    }
}
