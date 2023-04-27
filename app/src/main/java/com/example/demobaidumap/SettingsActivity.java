
package com.example.demobaidumap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// implements SharedPreferences.OnSharedPreferenceChangeListener
public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_NAME = "pre_key_name";
    public static final String KEY_SEX = "pre_key_sex";
    public static final String KEY_AGE = "pre_key_age";
    public static final String KEY_ALERT = "pre_key_alert";
    public static final String KEY_VIBRATE = "pre_key_vibrate";
    public static final String KEY_PHONE = "pre_key_phone";

    private SettingsFragment mSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        mSettingsFragment = new SettingsFragment();


        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, mSettingsFragment)
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        Log.e("Setting","coming");


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister the listener
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }




    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // 返回键
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.e("change pre",""+key);
        Preference phonePre = null;
            switch (key){
            case KEY_NAME:
                Preference namePre = mSettingsFragment.findPreference(key);
                namePre.setSummary(sharedPreferences.getString(key, ""));
                break;
            case KEY_SEX:
                Preference sexPre = mSettingsFragment.findPreference(key);
                sexPre.setSummary(sharedPreferences.getString(key, ""));
                break;
            case KEY_AGE:
                Preference agePre = mSettingsFragment.findPreference(key);
                agePre.setSummary(sharedPreferences.getString(key, ""));
                break;
            case KEY_ALERT:
                Preference alertPre = mSettingsFragment.findPreference(key);
                alertPre.setSummary(sharedPreferences.getString(key, ""));
                break;
            case KEY_VIBRATE:
                break;
            case KEY_PHONE:
                Log.e("phone set","ok");
                phonePre = mSettingsFragment.findPreference(key);
                if (phonePre != null) {
//                     phonePre.setSummary(sharedPreferences.getString(key, ""));
                }else{
                    Log.e("phonePre","null+"+key);
                }


                break;
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);


            EditTextPreference phonePref = findPreference(KEY_PHONE);
            Log.e("phonePref",KEY_PHONE+"@"+phonePref);

            phonePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // 处理pre_key_phone的值改变时的逻辑
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(KEY_PHONE, newValue.toString());
                    editor.apply();

                    Log.e("phonePre change","ok");
                    return true;
                }
            });

            EditTextPreference radiusPref = findPreference("rail_radius");
            radiusPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("rail_radius", newValue.toString());
                    editor.apply();
                    return true;
                }
            });

            ListPreference list_preference = findPreference("list_preference");
            list_preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String nowadays = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("list_preference"+nowadays, newValue.toString());
                    editor.apply();
                    return true;
                }
            });


        }
    }

}