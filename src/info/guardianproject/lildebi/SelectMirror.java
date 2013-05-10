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
	private String[] mirrors = new String[] {
			"archive.mmu.edu.my", "be.mirror.eurid.eu", "deb-mirror.de",
			"debian.bhs.mirrors.ovh.net", "debian.bjtu.edu.cn", "debian.bsnet.se",
			"debian.cabletel.com.mk", "debian.ethz.ch", "debian.gnu.gen.tr",
			"debian.ignum.cz", "debian.lcs.mit.edu", "debian.lth.se",
			"debian.mirror.dkm.cz", "debian.mirror.vu.lt", "debian.mirrors.ovh.net",
			"debian.nautile.nc", "debian.netcologne.de", "debian.nsu.ru",
			"debian.pop-sc.rnp.br", "debian.simnet.is", "debian.sth.sze.hu",
			"debian.stream.uz", "debian.ues.edu.sv", "debian.ustc.edu.cn",
			"ftp-stud.hs-esslingen.de", "ftp.acc.umu.se", "ftp.antik.sk",
			"ftp.au.debian.org", "ftp.availo.se", "ftp.caliu.cat",
			"ftp.ch.debian.org", "ftp.citylink.co.nz", "ftp.cn.debian.org",
			"ftp.crihan.fr", "ftp.cvut.cz", "ftp.df.lth.se",
			"ftp.dk.debian.org", "ftp.ds.karen.hj.se", "ftp.fi.debian.org",
			"ftp.gr.debian.org", "ftp.halifax.rwth-aachen.de", "ftp.iitm.ac.in",
			"ftp.informatik.uni-frankfurt.de", "ftp.is.debian.org", "ftp.iut-bm.univ-fcomte.fr",
			"ftp.jaist.ac.jp", "ftp.linux.org.tr", "ftp.litnet.lt",
			"ftp.lt.debian.org", "ftp.lug.ro", "ftp.nc.debian.org",
			"ftp.no.debian.org", "ftp.nz.debian.org", "ftp.pwr.wroc.pl",
			"ftp.rhnet.is", "ftp.se.debian.org", "ftp.stw-bonn.de",
			"ftp.task.gda.pl", "ftp.tiscali.nl", "ftp.tku.edu.tw",
			"ftp.tr.debian.org", "ftp.tw.debian.org", "ftp.uni-kl.de",
			"ftp.utexas.edu", "ftp.vectranet.pl", "ftp2.de.debian.org",
			"ftp2.fr.debian.org", "giano.com.dist.unige.it", "kambing.ui.ac.id",
			"kartolo.sby.datautama.net.id", "kebo.vlsm.org", "linorg.usp.br",
			"lug.mtu.edu", "mirror.0x.sg", "mirror.1000mbps.com",
			"mirror.anl.gov", "mirror.ayous.org", "mirror.bytemark.co.uk",
			"mirror.csclub.uwaterloo.ca", "mirror.cse.iitk.ac.in", "mirror.datacenter.by",
			"mirror.de.leaseweb.net", "mirror.debian.ikoula.com", "mirror.fdcservers.net",
			"mirror.hmc.edu", "mirror.i3d.net", "mirror.its.dal.ca",
			"mirror.kku.ac.th", "mirror.mirohost.net", "mirror.neolabs.kz",
			"mirror.nl.leaseweb.net", "mirror.nttu.edu.tw", "mirror.optus.net",
			"mirror.overthewire.com.au", "mirror.peer1.net", "mirror.picosecond.org",
			"mirror.pregi.net", "mirror.rit.edu", "mirror.steadfast.net",
			"mirror.stshosting.co.uk", "mirror.thelinuxfix.com", "mirror.unej.ac.id",
			"mirror.unitedcolo.de", "mirror.units.it", "mirror.us.leaseweb.net",
			"mirror.waia.asn.au", "mirror.yandex.ru", "mirror2.corbina.ru",
			"mirrors.163.com", "mirrors.accretive-networks.net", "mirrors.acm.jhu.edu",
			"mirrors.advancedhosters.com", "mirrors.bloomu.edu", "mirrors.dotsrc.org",
			"mirrors.ece.ubc.ca", "mirrors.fe.up.pt", "mirrors.ispros.com.bd",
			"mirrors.kernel.org", "mirrors.melbourne.co.uk", "mirrors.nfsi.pt",
			"mirrors.sohu.com", "mirrors.tecnoera.com", "mirrors.telianet.dk",
			"mirrors.tummy.com", "mirrors.ucr.ac.cr", "mirrors.xmission.com",
			"nl.mirror.eurid.eu", "opensource.nchc.org.tw", "repository.linux.pf",
			"sft.if.usp.br", "shadow.ind.ntou.edu.tw", "www.anheng.com.cn",
	};

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