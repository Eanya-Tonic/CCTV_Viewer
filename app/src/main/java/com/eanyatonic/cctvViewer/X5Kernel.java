package com.eanyatonic.cctvViewer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.preference.PreferenceManager;

import com.tencent.smtt.sdk.QbSdk;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

final class X5Kernel {
    static final String PREF_USE_X5 = "use_x5";
    static final String PREF_INSTALLED = "x5_installed";

    static final String ASSET_32 = "045738_x5.tbs.apk";
    static final String FILE_32 = "045738_x5.tbs_.apk";
    static final String FILE_64 = "046007_x5.tbs_.apk";
    static final String URL_32 = "http://void-tech.cn/wp-content/uploads/2024/10/045738_x5.tbs_.apk";
    static final String URL_64 = "http://void-tech.cn/wp-content/uploads/2024/10/046007_x5.tbs_.apk";
    static final int VERSION_32 = 45738;
    static final int VERSION_64 = 46007;

    private X5Kernel() {
    }

    static boolean isCpu64Bit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (String abi : Build.SUPPORTED_ABIS) {
                if (abi.contains("64")) return true;
            }
        }
        return false;
    }

    static String primaryAbi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String[] abis = Build.SUPPORTED_ABIS;
            return abis.length > 0 ? abis[0] : "";
        }
        return Build.CPU_ABI == null ? "" : Build.CPU_ABI;
    }

    // X5 只带 ARM 库，x86 模拟器/设备装上也加载不了
    static boolean isCpuSupported() {
        return primaryAbi().contains("arm");
    }

    static String apkFileName() {
        return isCpu64Bit() ? FILE_64 : FILE_32;
    }

    static String downloadUrl() {
        return isCpu64Bit() ? URL_64 : URL_32;
    }

    static int coreVersion() {
        return isCpu64Bit() ? VERSION_64 : VERSION_32;
    }

    static boolean hasDownloadedApk(Context context) {
        File dir = context.getExternalFilesDir("Download");
        if (dir == null) {
            return false;
        }
        File file = new File(dir, apkFileName());
        return file.isFile() && isCompleteApk(file);
    }

    static boolean isCompleteApk(File file) {
        try (ZipFile zip = new ZipFile(file)) {
            return zip.getEntry("AndroidManifest.xml") != null;
        } catch (Exception e) {
            return false;
        }
    }

    static boolean hasBundledAsset(Context context) {
        return AssetUtil.fileExistsInAssets(context, ASSET_32);
    }

    static boolean isReady(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp.getBoolean(PREF_INSTALLED, false)) {
            return true;
        }
        try {
            return QbSdk.canLoadX5(context.getApplicationContext());
        } catch (Throwable e) {
            return false;
        }
    }

    static int installedVersion(Context context) {
        try {
            return QbSdk.getTbsVersion(context.getApplicationContext());
        } catch (Throwable e) {
            return 0;
        }
    }

    static void migrateLegacyPrefs(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp.contains(PREF_USE_X5) || !sp.contains("sys_webview")) {
            return;
        }
        boolean ready = isReady(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PREF_USE_X5, !sp.getBoolean("sys_webview", true) && ready);
        if (ready) {
            editor.putBoolean(PREF_INSTALLED, true);
        }
        editor.apply();
    }
}
