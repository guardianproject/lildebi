package info.guardianproject.lildebi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SelectDistro extends Activity
{
	private ListView distroList;
	private String[] distros = new String[]{
			"oldstable",
			"stable",
			"testing",
			"unstable"
	};

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_distro);
		distroList = (ListView)findViewById(R.id.distroList);
		distroList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, distros));
		distroList.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				Intent result = new Intent();
				String distro = ((ArrayAdapter<String>) distroList.getAdapter()).getItem(i);
				result.putExtra(InstallActivity.DISTRO, distro);
				setResult(RESULT_OK, result);
				finish();
			}
		});
	}

	public static void callMe(Activity activity)
	{
		Intent intent = new Intent(activity, SelectDistro.class);
		activity.startActivityForResult(intent, 123);
	}
}