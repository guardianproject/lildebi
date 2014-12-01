package info.guardianproject.lildebi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SelectRelease extends Activity {
    private ListView releaseList;
    private String[] releases =
        new String[] { "oldstable", "stable", "testing", "unstable",
            "squeeze", "wheezy", "jessie", "sid" };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_release);
        releaseList = (ListView) findViewById(R.id.releaseList);
        releaseList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, releases));
        releaseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent result = new Intent();
                String release = ((ArrayAdapter<String>) releaseList.getAdapter())
                        .getItem(i);
                result.putExtra(InstallActivity.RELEASE, release);
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }

    public static void callMe(Activity activity) {
        Intent intent = new Intent(activity, SelectRelease.class);
        activity.startActivityForResult(intent, 123);
    }
}
