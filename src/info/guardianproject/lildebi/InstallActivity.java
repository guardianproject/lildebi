package info.guardianproject.lildebi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class InstallActivity extends Activity implements View.OnCreateContextMenuListener {
	public static final String TAG = "InstallActivity";

	public static final String RELEASE = "RELEASE";
	public static final String MIRROR = "MIRROR";
	public static final String ARCH = "ARCH";
	public static final String IMAGESIZE = "IMAGESIZE";

	private TextView selectedRelease;
	private TextView selectedMirror;
	private TextView selectedArch;
	private TextView imageSizeText;
	private TextView megaBytes;
	private EditText imagesize;
	private InstallService mBoundService;
	private PowerManager.WakeLock wl;
	private static int MinimumFreeSize = 250;
	private String installButtonText;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mBoundService = ((InstallService.LocalBinder) service).getService();
			Log.i(LilDebi.TAG, "calling serviceBound");
			wireButtons();
		}

		public void onServiceDisconnected(ComponentName className) {
			mBoundService = null;
			unwireButtons();
			setProgressBarIndeterminateVisibility(false);
		}
	};
	private boolean mIsBound;
	private Button installButton;
	private TextView installLog;
	private ScrollView textScroll;
	private Handler handler;
	private BroadcastReceiver installLogUpdateRec;
	private BroadcastReceiver installFinishedRec;

	void doBindService() {
		Log.i(LilDebi.TAG, "bindService");
		bindService(new Intent(this, InstallService.class), mConnection,
				Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.install_activity);
		selectedRelease = (TextView) findViewById(R.id.selectedRelease);
		selectedMirror = (TextView) findViewById(R.id.selectedMirror);
		selectedArch = (TextView) findViewById(R.id.selectedArch);
		imageSizeText = (TextView) findViewById(R.id.imagesizetext);
		imagesize = (EditText) findViewById(R.id.imagesize);
		megaBytes = (TextView) findViewById(R.id.megabytes);
		installButton = (Button) findViewById(R.id.installButton);
		installButtonText = installButton.getText().toString();
		installLog = (TextView) findViewById(R.id.installLog);
		textScroll = (ScrollView) findViewById(R.id.textScroll);
		handler = new Handler();
		
		if (NativeHelper.installInInternalStorage) {
			imageSizeText.setVisibility(View.GONE);
			imagesize.setVisibility(View.GONE);
			megaBytes.setVisibility(View.GONE);
		} else {
			imageSizeText.setVisibility(View.VISIBLE);
			imagesize.setVisibility(View.VISIBLE);
			megaBytes.setVisibility(View.VISIBLE);
		}

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "InstallWakeLock");

		// make sure the user can't set the image size greater than the total free space
		imagesize.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				try {
					installButton.setEnabled(true);
					if (NativeHelper.installInInternalStorage)
					    minimumFreeSizeTest(NativeHelper.getInstallPathFreeMegaBytes());
					else
					    setImageSizeInMB(Long.parseLong(imagesize.getText().toString()));

				} catch (NumberFormatException e) {
					// this means we got blank value or something like that
					installButton.setEnabled(false);
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		wl.acquire();
		if (!isExt2Supported()) {
			unwireButtons();
			renameInstallButton(R.string.uninstall);
			installButton.requestFocus();
			installButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					UnsupportedDeviceActivity.callMe(InstallActivity.this);
				}
			});
		} else {
            if (!NativeHelper.isInstallRunning) {
                // make sure the default image size isn't larger than the SDcard's free space
                if (NativeHelper.installInInternalStorage) {
                    minimumFreeSizeTest(NativeHelper.getInstallPathFreeMegaBytes());
                } else {
                    long size = 0;
                    try {
                        size = Integer.parseInt(imagesize.getText().toString());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        /*
                         * This means we got blank value or something like that, and
                         * the install button should already have been disabled
                         * before the Activity was paused.
                         */
                    }
                    setImageSizeInMB(size);
                }
            }
			refreshButtons();
			doBindService();
			installButton.requestFocus();
            /* display existing log file */
            new AsyncTask<Void, Void, Void>() {
                private final StringBuilder log = new StringBuilder();

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        BufferedReader r = new BufferedReader(new FileReader(
                                NativeHelper.install_log));
                        String line;
                        while ((line = r.readLine()) != null) {
                            log.append(line + "\n");
                        }
                        r.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }


                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                    installLog.setText(log.toString());
                }
            }.execute();
		}
	}

	@Override
	public void onBackPressed() {
	    if (!NativeHelper.isInstallRunning)
	        super.onBackPressed();
	}

	@Override
	protected void onPause() {
		super.onPause();
		doUnbindService();
		wl.release();
	}

    private void setImageSizeInMB(long requestedSize) {
        boolean changedSize = false;
        // adjust size is request is bigger than available space
        int resId = R.string.smaller_imagesize_message;
        long freeSize = NativeHelper.getInstallPathFreeMegaBytes();
        if (freeSize < requestedSize) {
            requestedSize = freeSize - 10; // leave 10MB free
            changedSize = true;
        }
        // FAT has a 4gb - 1 byte file size limit
        long sizeLimit = (4 * 1024) - 1;
        if (NativeHelper.limitTo4GB && requestedSize > sizeLimit) {
            resId = R.string.image_size_limit_message;
            requestedSize = sizeLimit;
            changedSize = true;
        }
        if (changedSize) {
            Toast.makeText(getApplicationContext(), resId, Toast.LENGTH_LONG).show();
            imagesize.setText(String.valueOf(requestedSize));
        }

		minimumFreeSizeTest(requestedSize);
    }

	private void minimumFreeSizeTest(long requestedSize) {
		if (requestedSize < MinimumFreeSize) {
			installButton.setEnabled(false);
			installButton.setText(R.string.not_enough_space);
		} else {
			installButton.setEnabled(true);
			installButton.setText(installButtonText);
		}
	}

	private void writeInstallConf() {
		String conf = new String();
		conf += "release=" + selectedRelease.getText().toString() + "\n";
		conf += "mirror=http://" + selectedMirror.getText().toString() + "/debian/\n";
		conf += "arch=" + selectedArch.getText().toString() + "\n";
		conf += "imagesize=" + imagesize.getText().toString() + "\n";
		conf += "LANG=" + NativeHelper.getLANG() + "\n";

		try {
			FileUtils.writeStringToFile(NativeHelper.install_conf, conf);
		} catch (IOException e) {
			String msg = "Failed to write install config: "	+ NativeHelper.install_conf;
			Log.e(TAG, msg);
			LilDebiAction.log.append(msg + "\n");
			e.printStackTrace();
		}
	}

	private void wireButtons() {
		installButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				registerReceivers();
				setProgressBarIndeterminateVisibility(true);
				setResult(NativeHelper.STARTING_INSTALL);
				writeInstallConf();
				Intent intent = new Intent(InstallActivity.this, InstallService.class);
				startService(intent);
				Log.i(TAG, "Starting install service");
				handler.postDelayed(new Runnable() {
					public void run() {
						refreshButtons();
					}
				}, 300);
			}
		});

		selectedRelease.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				SelectRelease.callMe(InstallActivity.this);
			}
		});

		selectedMirror.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				SelectMirror.callMe(InstallActivity.this);
			}
		});

		selectedArch.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				SelectArch.callMe(InstallActivity.this);
			}
		});

		refreshButtons();
	}

	private void registerReceivers() {
			installLogUpdateRec = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
                    installLog.append(intent.getExtras().getString(Intent.EXTRA_TEXT));
                    textScroll.fullScroll(View.FOCUS_DOWN);
				}
			};
			registerReceiver(installLogUpdateRec,
			        new IntentFilter(InstallService.INSTALL_LOG_UPDATE));

			installFinishedRec = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					intent.setAction(Intent.ACTION_VIEW);
					intent.setClass(getApplicationContext(), LilDebi.class);
					startActivity(intent);
					finish();
					unregisterReceivers();
				}
			};
			registerReceiver(installFinishedRec,
			        new IntentFilter(InstallService.INSTALL_FINISHED));
	}

	private void unregisterReceivers() {
		if (installLogUpdateRec != null)
			unregisterReceiver(installLogUpdateRec);

		if (installFinishedRec != null)
			unregisterReceiver(installFinishedRec);
	}

	private void unwireButtons() {
		selectedRelease.setEnabled(false);
		selectedMirror.setEnabled(false);
		selectedMirror.setOnClickListener(null);
		selectedArch.setEnabled(false);
		selectedArch.setOnClickListener(null);
		imagesize.setEnabled(false);
		installButton.setOnClickListener(null);
		installButton.setVisibility(View.GONE);
	}

	private void renameInstallButton(int resid) {
		installButton.setEnabled(true);
		installButton.setVisibility(View.VISIBLE);
		installButton.setText(getString(resid));
	}

	private void refreshButtons() {
		if (mBoundService != null && mBoundService.isRunning()) {
			selectedRelease.setEnabled(false);
			selectedMirror.setEnabled(false);
			selectedArch.setEnabled(false);
			imagesize.setEnabled(false);
			installButton.setVisibility(View.GONE);
		} else {
			selectedRelease.setEnabled(true);
			selectedMirror.setEnabled(true);
			selectedArch.setEnabled(true);
			if (NativeHelper.installInInternalStorage) {
				imageSizeText.setVisibility(View.GONE);
				imagesize.setVisibility(View.GONE);
				megaBytes.setVisibility(View.GONE);
			} else {
				imageSizeText.setVisibility(View.VISIBLE);
				imagesize.setVisibility(View.VISIBLE);
				megaBytes.setVisibility(View.VISIBLE);
			}
			installButton.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (data.hasExtra(RELEASE))
				selectedRelease.setText(data.getStringExtra(RELEASE));
			if (data.hasExtra(MIRROR))
				selectedMirror.setText(data.getStringExtra(MIRROR));
			if (data.hasExtra(ARCH))
				selectedArch.setText(data.getStringExtra(ARCH));
		}
	}

	private boolean isExt2Supported() {
		Context context = getApplicationContext();
		boolean foundExt2 = false;
		try {
			FileInputStream fstream = new FileInputStream("/proc/filesystems");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in), 8192);
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("ext2")) {
					foundExt2 = true;
					break;
				}
			}
			br.close();
			fstream.close();
			if (new File("/system/lib/modules/ext2.ko").exists())
				foundExt2 = true;
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		if (! foundExt2)
			Toast.makeText(context, R.string.no_ext2_message, Toast.LENGTH_LONG).show();
		return foundExt2;
	}

	public static void writeCommand(OutputStream os, String command) throws Exception {
		os.write((command + "\n").getBytes("ASCII"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_preferences:
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
			return true;
		}
		return false;
	}
}
