package com.example.pertemuan5_shafa;

import android.app.Application;
import android.content.Context;

import com.onesignal.OneSignal;

public class MyApp extends Application {
    Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        // Inisialisasi OneSignal
        OneSignal.initWithContext(this);
        OneSignal.setAppId("f0b98772-3ee0-4809-a26f-a52530b068ef");



    }
}