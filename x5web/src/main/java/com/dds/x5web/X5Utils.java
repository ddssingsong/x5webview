package com.dds.x5web;

import android.app.Application;
import android.util.Log;

import com.tencent.smtt.sdk.QbSdk;

/**
 * Created by dds on 2019/3/14.
 * android_shuai@163.com
 */
public class X5Utils {

    public static void init(Application application) {
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.d("app", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                Log.d("app", " onCoreInitFinished");
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(application, cb);
    }
}
