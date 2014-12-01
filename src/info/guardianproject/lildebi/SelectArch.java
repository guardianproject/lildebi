package info.guardianproject.lildebi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SelectArch extends Activity {
    private ListView archList;
    private String[] archs = new String[] { "armhf", "armel" };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_arch);
        archList = (ListView) findViewById(R.id.archList);
        archList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, archs));
        archList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent result = new Intent();
                String arch = ((ArrayAdapter<String>) archList.getAdapter())
                        .getItem(i);
                result.putExtra(InstallActivity.ARCH, arch);
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }

    public static void callMe(Activity activity) {
        Intent intent = new Intent(activity, SelectArch.class);
        activity.startActivityForResult(intent, 123);
    }
}