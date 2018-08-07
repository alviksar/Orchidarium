package xyz.alviksar.orchidarium.ui;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.google.firebase.messaging.FirebaseMessaging;

import xyz.alviksar.orchidarium.OrchidariumContract;
import xyz.alviksar.orchidarium.R;

/**
 * Provides fragment to UI for setting user preferences.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Figure out which preference was changed
        if (s.equals(getString(R.string.pref_key_notify_me))) {
            // Notification preference is changed
            boolean subscribe = sharedPreferences.getBoolean(s,
                    getResources().getBoolean(R.bool.pref_default_notification));
            String key = OrchidariumContract.NOTIFICATION_TOPIC;
            if (subscribe) {
                FirebaseMessaging.getInstance().subscribeToTopic(key);
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(key);
            }
        }
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
