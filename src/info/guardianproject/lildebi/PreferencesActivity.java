package info.guardianproject.lildebi;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;

public class PreferencesActivity extends android.preference.PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	EditTextPreference postStartEditText;
	EditTextPreference preStopEditText;

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (key.equals(getString(R.string.pref_post_start_key))) {
			NativeHelper.postStartScript = prefs.getString(key,
					getString(R.string.default_post_start_script));
			setSummaries();
		} else if (key.equals(getString(R.string.pref_pre_stop_key))) {
			NativeHelper.preStopScript = prefs.getString(key,
					getString(R.string.default_pre_stop_script));
			setSummaries();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		postStartEditText = (EditTextPreference) findPreference(getString(R.string.pref_post_start_key));
		preStopEditText = (EditTextPreference) findPreference(getString(R.string.pref_pre_stop_key));
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
		prefs.registerOnSharedPreferenceChangeListener(this);
		setSummaries();
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	private void setSummaries() {
		if (NativeHelper.postStartScript
				.equals(getString(R.string.default_post_start_script))) {
			postStartEditText.setSummary(getString(R.string.pref_post_start_summary));
		} else {
			postStartEditText.setSummary(NativeHelper.postStartScript);
		}
		if (NativeHelper.preStopScript.equals(getString(R.string.default_pre_stop_script))) {
			preStopEditText.setSummary(getString(R.string.pref_pre_stop_summary));
		} else {
			preStopEditText.setSummary(NativeHelper.preStopScript);
		}
	}
}
