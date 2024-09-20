package com.eanyatonic.cctvViewer;

import android.os.Build;

public class SysTool {
    public static String showSysAach(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Build.SUPPORTED_ABIS[0];
        } else {
            return Build.CPU_ABI;
        }
    }
}
