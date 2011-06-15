
package info.guardianproject.lildebi;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;

public class PreferencesActivity extends android.preference.PreferenceActivity implements OnSharedPreferenceChangeListener
{
    CheckBoxPreference startOnBootCheckBox;
    EditTextPreference bootScriptEditText;

    /* save the preferences in Imps so they are accessible everywhere */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
    {    	
    	if (key.equals(getString(R.string.pref_start_on_boot_key)))
    	{
    		boolean startOnBoot = prefs.getBoolean(key, false);
    	}
    	else if (key.equals(getString(R.string.pref_boot_script_key)))
    	{
    		String script = prefs.getString(key, "/sbin/init");
    	}
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	addPreferencesFromResource(R.xml.preferences);    
    	startOnBootCheckBox = (CheckBoxPreference) findPreference(getString(R.string.pref_start_on_boot_key));
    	bootScriptEditText = (EditTextPreference) findPreference(getString(R.string.pref_boot_script_key));
    }

    @Override
    protected void onResume()
    {
    	super.onResume();
    	getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause()
    {
    	super.onPause();
    	getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    }

}
