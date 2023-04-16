package com.example.demobaidumap.ui.setting;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceManager;

import com.example.demobaidumap.R;

/**
 * Created by LiuWeixiang on 2017/3/1.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    public static final String KEY_NAME = "pre_key_name";
    public static final String KEY_SEX = "pre_key_sex";
    public static final String KEY_AGE = "pre_key_age";
    public static final String KEY_ALERT = "pre_key_alert";
    public static final String KEY_VIBRATE = "pre_key_vibrate";
    public static final String KEY_PHONE = "pre_key_phone";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.root_preferences);

        Log.e("setting","in");




    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.e("change pre",""+key);
//            switch (key){
//            case KEY_NAME:
//                Preference namePre = findPreference(key);
//                namePre.setSummary(sharedPreferences.getString(key, ""));
//                break;
//            case KEY_SEX:
//                Preference sexPre = findPreference(key);
//                sexPre.setSummary(sharedPreferences.getString(key, ""));
//                break;
//            case KEY_AGE:
//                Preference agePre = findPreference(key);
//                agePre.setSummary(sharedPreferences.getString(key, ""));
//                break;
//            case KEY_ALERT:
//                Preference alertPre = findPreference(key);
//                alertPre.setSummary(sharedPreferences.getString(key, ""));
//                break;
//            case KEY_VIBRATE:
//                break;
//            case KEY_PHONE:
//                Log.e("phone set","ok");
//                Preference phonePre = findPreference(key);
//                phonePre.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                    @Override
//                    public boolean onPreferenceChange(Preference preference, Object newValue) {
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.putString(key, newValue.toString());
//                        editor.apply();
//                        preference.setSummary(newValue.toString());
//                        return true;
//                    }
//                });
////                phonePre.setSummary(sharedPreferences.getString(key, ""));
//                break;
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
