package xyz.alviksar.orchidarium.ui;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.google.firebase.messaging.FirebaseMessaging;

import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.data.OrchidariumPreferences;


public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {
//        Preference.OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

//        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
//        PreferenceScreen prefScreen = getPreferenceScreen();
//        int count = prefScreen.getPreferenceCount();
//
//        // Go through all of the preferences, and set up their preference summary.
//        for (int i = 0; i < count; i++) {
//            Preference p = prefScreen.getPreference(i);
//            // You don't need to set up preference summaries for checkbox preferences because
//            // they are already set up in xml using summaryOff and summary On
//            if (!(p instanceof CheckBoxPreference)) {
//                String value = sharedPreferences.getString(p.getKey(), "");
//                setPreferenceSummary(p, value);
//            }
//        }

//        Preference preference = findPreference(getString(R.string.pref_key_font_size));
//        preference.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Figure out which preference was changed
        if (s.equals(getString(R.string.pref_key_notify_me))) {
            // Notification preference is changed
            boolean subscribe = sharedPreferences.getBoolean(s,
                    getResources().getBoolean(R.bool.pref_default_notification));
            String key = OrchidariumPreferences.NOTIFICATION_TOPIC;
            if (subscribe) {
                FirebaseMessaging.getInstance().subscribeToTopic(key);
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(key);
            }
        }
//        Preference preference = findPreference(s);
//        if (null != preference) {
//            // Updates the summary for the preference
//            if (!(preference instanceof CheckBoxPreference)) {
//                String value = sharedPreferences.getString(preference.getKey(), "");
//                setPreferenceSummary(preference, value);
//            }
//        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

}
