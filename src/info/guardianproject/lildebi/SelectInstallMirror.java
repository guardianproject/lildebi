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
			"http://ftp.at.debian.org/debian/",
			"http://ftp.au.debian.org/debian/",
			"http://ftp.ba.debian.org/debian/",
			"http://ftp.be.debian.org/debian/",
			"http://ftp.bg.debian.org/debian/",
			"http://ftp.br.debian.org/debian/",
			"http://ftp.by.debian.org/debian/",
			"http://ftp.ca.debian.org/debian/",
			"http://ftp.ch.debian.org/debian/",
			"http://ftp.cl.debian.org/debian/",
			"http://ftp.cn.debian.org/debian/",
			"http://ftp.cz.debian.org/debian/",
			"http://ftp.de.debian.org/debian/",
			"http://ftp.dk.debian.org/debian/",
			"http://ftp.ee.debian.org/debian/",
			"http://ftp.es.debian.org/debian/",
			"http://ftp.fi.debian.org/debian/",
			"http://ftp.fr.debian.org/debian/",
			"http://ftp.gr.debian.org/debian/",
			"http://ftp.hk.debian.org/debian/",
			"http://ftp.hr.debian.org/debian/",
			"http://ftp.hu.debian.org/debian/",
			"http://ftp.ie.debian.org/debian/",
			"http://ftp.is.debian.org/debian/",
			"http://ftp.it.debian.org/debian/",
			"http://ftp.jp.debian.org/debian/",
			"http://ftp.kr.debian.org/debian/",
			"http://ftp.lt.debian.org/debian/",
			"http://ftp.mx.debian.org/debian/",
			"http://ftp.nc.debian.org/debian/",
			"http://ftp.nl.debian.org/debian/",
			"http://ftp.no.debian.org/debian/",
			"http://ftp.nz.debian.org/debian/",
			"http://ftp.pl.debian.org/debian/",
			"http://ftp.pt.debian.org/debian/",
			"http://ftp.ro.debian.org/debian/",
			"http://ftp.ru.debian.org/debian/",
			"http://ftp.se.debian.org/debian/",
			"http://ftp.si.debian.org/debian/",
			"http://ftp.sk.debian.org/debian/",
			"http://ftp.th.debian.org/debian/",
			"http://ftp.tr.debian.org/debian/",
			"http://ftp.tw.debian.org/debian/",
			"http://ftp.ua.debian.org/debian/",
			"http://ftp.uk.debian.org/debian/",
			"http://ftp.us.debian.org/debian/"
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