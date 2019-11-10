package com.morkim.tectonic.flow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import java.util.UUID;

@SuppressWarnings("unused")
public class UUIDGenerator {

    private static boolean alwaysRandom;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static UUID generate(int key, @NonNull Class<?> cls) {

        if (!alwaysRandom) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            if (sp.contains(cls.getName() + key))
                try {
                    return UUID.fromString(sp.getString(cls.getName() + key, ""));
                } catch (IllegalArgumentException ignored) {

                }
        }

        return UUID.randomUUID();
    }

    public static void setAlwaysRandom(boolean alwaysRandom) {
        UUIDGenerator.alwaysRandom = alwaysRandom;
    }

    public static void setContext(Context context) {
        UUIDGenerator.context = context;
    }
}