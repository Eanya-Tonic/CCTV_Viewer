package com.eanyatonic.cctvViewer;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

public class MultiDexApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // 安装 MultiDex，允许多个 .dex 文件
        MultiDex.install(this);
    }
}
