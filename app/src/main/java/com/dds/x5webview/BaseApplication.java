package com.dds.x5webview;

import android.app.Application;

import com.dds.x5web.X5Utils;


public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        X5Utils.init(this);


    }

}
