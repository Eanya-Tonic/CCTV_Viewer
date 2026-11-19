package com.eanyatonic.cctvViewer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;

import java.io.File;

public class LoadingActivity extends AppCompatActivity {
    public static final String EXTRA_INSTALL_ASSET = "install_asset";
    private static final long INSTALL_TIMEOUT_MS = 30_000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean finished = false;
    private boolean installAsset = false;
    private File installFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        installAsset = getIntent().getBooleanExtra(EXTRA_INSTALL_ASSET, false);

        int version;
        if (installAsset) {
            File tbsDir = FileUtils.getTBSFileDir(this);
            if (tbsDir == null) {
                finishWithError("无法创建内核存储目录");
                return;
            }
            File assetCopy = new File(tbsDir, X5Kernel.ASSET_32);
            if (!assetCopy.exists()
                    && !FileUtils.copyAssets(getApplicationContext(), X5Kernel.ASSET_32, assetCopy.getPath())) {
                finishWithError("内置 X5 内核释放失败");
                return;
            }
            installFile = assetCopy;
            version = X5Kernel.VERSION_32;
        } else {
            File dir = getExternalFilesDir("Download");
            if (dir == null) {
                finishWithError("无法访问内核下载目录");
                return;
            }
            installFile = new File(dir, X5Kernel.apkFileName());
            version = X5Kernel.coreVersion();
            if (!installFile.exists()) {
                finishWithError("未找到 X5 内核安装包，请先下载");
                return;
            }
            if (!X5Kernel.isCompleteApk(installFile)) {
                finishWithError("X5 内核安装包不完整，请重新下载");
                return;
            }
        }

        handler.postDelayed(() -> startInstall(version), 500);
    }

    private void startInstall(int version) {
        if (finished || isFinishing() || isDestroyed()) {
            return;
        }
        QbSdk.unForceSysWebView();
        QbSdk.reset(getApplicationContext());
        // 先注册监听，避免安装回调早于监听设置而丢失
        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {
                Log.d("X5Install", "onDownloadFinish " + i);
            }

            @Override
            public void onDownloadProgress(int i) {
            }

            @Override
            public void onInstallFinish(int i) {
                onX5InstallFinished(i);
            }
        });
        try {
            QbSdk.installLocalTbsCore(getApplicationContext(), version, installFile.getPath());
        } catch (Throwable e) {
            Log.e("X5Install", "installLocalTbsCore failed", e);
            finishWithError("X5 内核安装调用失败");
            return;
        }
        handler.postDelayed(() -> {
            if (!finished) {
                finishWithError("X5 内核安装超时");
            }
        }, INSTALL_TIMEOUT_MS);
    }

    private void onX5InstallFinished(int i) {
        runOnUiThread(() -> {
            if (finished || isFinishing() || isDestroyed()) {
                return;
            }
            boolean canLoad = false;
            int tbsVersion = 0;
            try {
                canLoad = QbSdk.canLoadX5(getApplicationContext());
                tbsVersion = QbSdk.getTbsVersion(getApplicationContext());
            } catch (Throwable ignored) {
            }
            Log.d("X5Install", "onInstallFinish " + i + ", canLoad=" + canLoad + ", version=" + tbsVersion);
            if (i == 200 && canLoad) {
                finished = true;
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean(X5Kernel.PREF_INSTALLED, true).apply();
                Toast.makeText(this, "X5 内核安装成功，请在设置中开启「使用 X5 内核」并重启应用",
                        Toast.LENGTH_LONG).show();
                restoreMode();
                finish();
            } else if (i == 200) {
                // 安装流程完成但当前进程无法加载，保留安装包，重启后可再次尝试
                finished = true;
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean(X5Kernel.PREF_INSTALLED, false).apply();
                Toast.makeText(this, "X5 内核安装完成但当前无法加载，请重启应用后重试",
                        Toast.LENGTH_LONG).show();
                restoreMode();
                finish();
            } else {
                finishWithError("X5 内核安装失败（" + i + "）");
            }
        });
    }

    private void finishWithError(String msg) {
        if (finished || isFinishing() || isDestroyed()) {
            return;
        }
        finished = true;
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putBoolean(X5Kernel.PREF_INSTALLED, false).apply();
        if (!installAsset) {
            FileUtils.deleteFile(this, X5Kernel.apkFileName());
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        restoreMode();
        finish();
    }

    // 安装流程会临时取消强制系统内核，结束后按当前设置恢复，避免影响本进程后续 WebView
    private void restoreMode() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sp.getBoolean(X5Kernel.PREF_USE_X5, false)) {
            QbSdk.forceSysWebView();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
