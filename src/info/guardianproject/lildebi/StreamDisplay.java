package info.guardianproject.lildebi;

import android.os.Handler;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: 4/1/11
 * Time: 3:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class StreamDisplay extends Thread
{
	InputStream i;
	TextView display;
	ScrollView scrollView;
	Handler handler;

	StreamDisplay(InputStream i, TextView display, ScrollView scrollView, Handler handler)
	{
		this.i = i;
		this.display = display;
		this.scrollView = scrollView;
		this.handler = handler;
	}

	@Override
	public void run()
	{
		int next;
		try
		{
			byte[] readBuffer = new byte[512];
			int readCount = -1;
			while ((readCount = i.read(readBuffer)) > 0)
			{
				final String readString = new String(readBuffer, 0, readCount);
				handler.post(new Runnable()
				{
					public void run()
					{
						CharSequence currentText = display.getText();
						display.setText(currentText + readString);
						scrollView.scrollTo(0, display.getHeight() + 100);
					}
				});
				handler.postDelayed(new Runnable()
				{
					public void run()
					{
						scrollView.scrollTo(0, display.getHeight());
					}
				}, 300);
			}
		}
		catch (IOException e)
		{
			App.loge("", e);
		}
	}
}