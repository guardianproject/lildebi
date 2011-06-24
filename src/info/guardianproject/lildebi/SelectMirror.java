package info.guardianproject.lildebi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SelectMirror extends Activity {
	private ListView mirrorList;
	private String[] mirrors = new String[] { "ftp.at.debian.org", "ftp.au.debian.org",
			"ftp.ba.debian.org", "ftp.be.debian.org", "ftp.bg.debian.org",
			"ftp.br.debian.org", "ftp.by.debian.org", "ftp.ca.debian.org",
			"ftp.ch.debian.org", "ftp.cl.debian.org", "ftp.cn.debian.org",
			"ftp.cz.debian.org", "ftp.de.debian.org", "ftp.dk.debian.org",
			"ftp.ee.debian.org", "ftp.es.debian.org", "ftp.fi.debian.org",
			"ftp.fr.debian.org", "ftp.gr.debian.org", "ftp.hk.debian.org",
			"ftp.hr.debian.org", "ftp.hu.debian.org", "ftp.ie.debian.org",
			"ftp.is.debian.org", "ftp.it.debian.org", "ftp.jp.debian.org",
			"ftp.kr.debian.org", "ftp.lt.debian.org", "ftp.mx.debian.org",
			"ftp.nc.debian.org", "ftp.nl.debian.org", "ftp.no.debian.org",
			"ftp.nz.debian.org", "ftp.pl.debian.org", "ftp.pt.debian.org",
			"ftp.ro.debian.org", "ftp.ru.debian.org", "ftp.se.debian.org",
			"ftp.si.debian.org", "ftp.sk.debian.org", "ftp.th.debian.org",
			"ftp.tr.debian.org", "ftp.tw.debian.org", "ftp.ua.debian.org",
			"ftp.uk.debian.org", "ftp.us.debian.org", "mirror.cc.columbia.edu",
			"ftp.gtlib.gatech.edu" };

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_mirror);
		mirrorList = (ListView) findViewById(R.id.mirrorList);
		mirrorList.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, mirrors));
		mirrorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				Intent result = new Intent();
				String mirror = ((ArrayAdapter<String>) mirrorList.getAdapter())
						.getItem(i);
				result.putExtra(InstallActivity.MIRROR, mirror);
				setResult(RESULT_OK, result);
				finish();
			}
		});
	}

	public static void callMe(Activity activity) {
		Intent intent = new Intent(activity, SelectMirror.class);
		activity.startActivityForResult(intent, 123);
	}
}