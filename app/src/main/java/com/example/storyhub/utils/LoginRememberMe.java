package com.example.storyhub.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class LoginRememberMe {

    private static final String PREF_NAME = "secure_storyhub_pref";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";

    SharedPreferences pref;

    public LoginRememberMe(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            pref = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveLogin(String email, String password, boolean remember) {
        pref.edit()
                .putString(KEY_EMAIL, email)
                .putString(KEY_PASSWORD, password)
                .putBoolean(KEY_REMEMBER, remember)
                .apply();
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    public String getPassword() {
        return pref.getString(KEY_PASSWORD, "");
    }

    public boolean isRemember() {
        return pref.getBoolean(KEY_REMEMBER, false);
    }

    public void clearLogin() {
        pref.edit().clear().apply();
    }
}