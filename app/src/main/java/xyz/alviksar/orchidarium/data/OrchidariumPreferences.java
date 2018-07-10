package xyz.alviksar.orchidarium.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import xyz.alviksar.orchidarium.R;

public class OrchidariumPreferences {
        /**
         * Helper method to handle setting the mode for the main activity in Preferences
         *
         * @param context  Context used to get the SharedPreferences
         * @param mode     the mode of showing
         */
        public static void setMode(Context context, String mode) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(context.getString(R.string.pref_key_mode), mode);
            editor.apply();
        }

        /**
         * Returns the mode currently set in Preferences
         *
         * @param context Context used to access SharedPreferences
         * @return  The mode that current user has set in SharedPreferences or default value.
         */
        public static String getMode(Context context) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            return sp.getString(context.getString(R.string.pref_key_mode),
                    context.getString(R.string.pref_mode_default));
        }
}
