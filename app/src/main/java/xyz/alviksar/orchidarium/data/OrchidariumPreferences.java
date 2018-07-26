package xyz.alviksar.orchidarium.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Currency;
import java.util.Locale;

import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.model.OrchidEntity;

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

    /**
     * Helper method to handle setting the mode for the main activity in Preferences
     *
     * @param context  Context used to get the SharedPreferences
     * @param symbol   Currency
     */
    public static void setCurrencySymbol(Context context, String symbol) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(context.getString(R.string.pref_key_currency_symbol), symbol);
        editor.apply();
    }

    /**
     * Returns the mode currently set in Preferences
     *
     * @param context Context used to access SharedPreferences
     * @return  The currency symbol that current user has set in SharedPreferences or default value.
     */
    public static String getCurrencySymbol(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Currency currency = Currency.getInstance(Locale.getDefault());
        String symbol = currency.getSymbol();
        return sp.getString(context.getString(R.string.pref_key_currency_symbol), symbol);
    }

    public static void addToCart(OrchidEntity orchid) {

    }

    public static boolean inCart(OrchidEntity orchid) {
        return  false;
    }

}
