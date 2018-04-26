package br.com.dalcatech.allfood.myapplication.utils;

import android.os.Build;

public class ActivityEffectFactory {
    public ActivityEffectFactory() {
    }

    public static ActivityEffect get() {
        int apiLevel = Build.VERSION.SDK_INT;
        return (ActivityEffect)(apiLevel > 4?new ActivityEffectV5():new ActivityEffectV4());
    }
}
