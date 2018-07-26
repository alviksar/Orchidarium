package xyz.alviksar.orchidarium.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import xyz.alviksar.orchidarium.R;
import xyz.alviksar.orchidarium.model.OrchidEntity;

public class OrchidariumPreferences {

    private static final String PREF_CONTENTS_OF_THE_CART = "pref_contents_of_the_cart";
    private static final String PREF_GOODS_DELIMITER = "#";

    /**
     * Helper method to handle setting the mode for the main activity in Preferences
     *
     * @param context Context used to get the SharedPreferences
     * @param mode    the mode of showing
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
     * @return The mode that current user has set in SharedPreferences or default value.
     */
    public static String getMode(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(context.getString(R.string.pref_key_mode),
                context.getString(R.string.pref_mode_default));
    }

    /**
     * Helper method to handle setting the mode for the main activity in Preferences
     *
     * @param context Context used to get the SharedPreferences
     * @param symbol  Currency
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
     * @return The currency symbol that current user has set in SharedPreferences or default value
     */
    public static String getCurrencySymbol(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Currency currency = Currency.getInstance(Locale.getDefault());
        String symbol = currency.getSymbol();
        return sp.getString(context.getString(R.string.pref_key_currency_symbol), symbol);
    }

    /**
     * Puts the orchid into the cart
     *
     * @param context Context used to get the SharedPreferences
     * @param orchid  Chosen orchid
     */
    public static void addToCart(Context context, OrchidEntity orchid) {
        if (inCart(context, orchid)) return;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String cart = sp.getString(PREF_CONTENTS_OF_THE_CART, "");
        cart +=  orchid.getId() + PREF_GOODS_DELIMITER;
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREF_CONTENTS_OF_THE_CART, cart);
        editor.apply();
    }

    /**
     * Removes the orchid from the cart
     *
     * @param context Context used to get the SharedPreferences
     * @param orchid  Chosen orchid
     */
    public static void removeFromCart(Context context, OrchidEntity orchid) {
        if (!inCart(context, orchid)) return;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String cart = sp.getString(PREF_CONTENTS_OF_THE_CART, "");
        String goods[] = cart.split(PREF_GOODS_DELIMITER);
        StringBuilder new_cart = new StringBuilder();
        for (String good_key : goods) {
            if (!good_key.equals(orchid.getId()))
                new_cart.append(orchid.getId()).append(PREF_GOODS_DELIMITER);
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREF_CONTENTS_OF_THE_CART, new_cart.toString());
        editor.apply();
    }

    /**
     * Checks if orchid placed in cart
     *
     * @param context Context used to access SharedPreferences
     * @return true if orchid placed in cart, false otherwise
     */
    public static boolean inCart(Context context, OrchidEntity orchid) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String cart = sp.getString(PREF_CONTENTS_OF_THE_CART, "");
        /*
        If you create your own keys, they must be UTF-8 encoded, can be a maximum of 768 bytes,
        and cannot contain ., $, #, [, ], /, or ASCII control characters 0-31 or 127
        https://firebase.google.com/docs/database/ios/structure-data
        */
        String goods[] = cart.split(PREF_GOODS_DELIMITER);
        for (String good_key : goods) {
            if (good_key.equals(orchid.getId())) return true;
        }
        return false;
    }

}
