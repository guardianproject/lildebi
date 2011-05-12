package info.guardianproject.lildebi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: 4/1/11
 * Time: 4:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class SelectInstallMirror extends Activity
{
	public static final String MIRROR = "MIRROR";
	private ListView mirrorList;
	private String[] mirrors = new String[]{
			"http://www.someserver.org",
			"http://www.asdf.com",
			"http://www.qwert.com"
	};

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_install_mirror);
		mirrorList = (ListView)findViewById(R.id.mirrorList);
		mirrorList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mirrors));
		mirrorList.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				Intent result = new Intent();
				String mirror = ((ArrayAdapter<String>) mirrorList.getAdapter()).getItem(i);
				result.putExtra(MIRROR, mirror);
				setResult(RESULT_OK, result);
				finish();
			}
		});
	}

	public static void callMe(Activity activity)
	{
		Intent intent = new Intent(activity, SelectInstallMirror.class);
		activity.startActivityForResult(intent, 123);
	}
}