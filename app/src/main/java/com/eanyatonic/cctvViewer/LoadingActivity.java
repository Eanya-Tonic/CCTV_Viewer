package com.eanyatonic.cctvViewer;

import com.eanyatonic.cctvViewer.FileUtils;
import com.eanyatonic.cctvViewer.MainActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;

import java.util.Objects;

public class LoadingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // 获取 SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String installX5Mode = sharedPreferences.getString("x5_webview_version", "0");
        if(Objects.equals(installX5Mode, "0")){
            FileUtils.copyAssets(getApplicationContext(), "045738_x5.tbs.apk",
                    FileUtils.getTBSFileDir(getApplicationContext()).getPath() + "/045738_x5.tbs.apk");
        } else if (Objects.equals(installX5Mode, "1")) {
            // 自动下载x5
            if(isCpu64Bit()) {
                if(!FileUtils.isFileExistInDownloads(getApplicationContext(),"046007_x5.tbs_.apk")){
                    Intent intent = new Intent(LoadingActivity.this, DownloadActivity.class);
                    intent.putExtra("file_url", "http://void-tech.cn/wp-content/uploads/2024/10/046007_x5.tbs_.apk");  // 将下载链接传递过去
                    startActivity(intent);
                    finish();
                    return;
                }
            } else {
                if(!FileUtils.isFileExistInDownloads(getApplicationContext(),"045738_x5.tbs_.apk")) {
                    Intent intent = new Intent(LoadingActivity.this, DownloadActivity.class);
                    intent.putExtra("file_url", "http://void-tech.cn/wp-content/uploads/2024/10/045738_x5.tbs_.apk");  // 将下载链接传递过去
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        } else if (Objects.equals(installX5Mode, "2")) {
            if(!FileUtils.isFileExistInDownloads(getApplicationContext(),"046007_x5.tbs_.apk")){
                Intent intent = new Intent(LoadingActivity.this, DownloadActivity.class);
                intent.putExtra("file_url", "http://void-tech.cn/wp-content/uploads/2024/10/046007_x5.tbs_.apk");  // 将下载链接传递过去
                startActivity(intent);
                finish();
                return;
            }
        } else if (Objects.equals(installX5Mode,"3")) {
            if(!FileUtils.isFileExistInDownloads(getApplicationContext(),"045738_x5.tbs_.apk")) {
                Intent intent = new Intent(LoadingActivity.this, DownloadActivity.class);
                intent.putExtra("file_url", "http://void-tech.cn/wp-content/uploads/2024/10/045738_x5.tbs_.apk");  // 将下载链接传递过去
                startActivity(intent);
                finish();
                return;
            }
        }

        new Handler().postDelayed(() -> {
            // 安装TBS内核
            QbSdk.reset(getApplicationContext());
            if(Objects.equals(installX5Mode, "0")) {
                QbSdk.installLocalTbsCore(getApplicationContext(), 45738,
                        FileUtils.getTBSFileDir(getApplicationContext()).getPath() + "/045738_x5.tbs.apk");
            } else {
                if(isCpu64Bit() && Objects.equals(installX5Mode,"1") || Objects.equals(installX5Mode, "2")) {
                    QbSdk.installLocalTbsCore(getApplicationContext(), 46007,
                            getApplicationContext().getExternalFilesDir("Download") + "/046007_x5.tbs_.apk");
                } else {
                    QbSdk.installLocalTbsCore(getApplicationContext(), 45738,
                            getApplicationContext().getExternalFilesDir("Download") + "/045738_x5.tbs_.apk");
                }
            }
            QbSdk.setTbsListener(new TbsListener() {
                @Override
                public void onDownloadFinish(int i) {
                    Log.e("TAG", "进行了tbs:onDownloadFinish " + i);
                }

                @Override
                public void onDownloadProgress(int i) {
                    Log.e("TAG", "进行了tbs:onDownloadProgress " + i);
                }

                @Override
                public void onInstallFinish(int i) {
                    Log.e("TAG", "进行了tbs:onInstallFinish " + i);
                    Toast.makeText(getApplicationContext(), "进行了tbs:onInstallFinish " + i, Toast.LENGTH_SHORT).show();
                    boolean canLoadX5 = QbSdk.canLoadX5(getApplicationContext());
                    Log.d("canLoadX5", String.valueOf(canLoadX5));
                    Log.d("versionX5", String.valueOf(QbSdk.getTbsVersion(getApplicationContext())));
                    Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // 销毁 LoadingActivity
                }
            });
        }, 6000);
    }
    private static boolean isCpu64Bit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (String abi : Build.SUPPORTED_ABIS) {
                if (abi.contains("64")) return true;
            }
        }
        return false;
    }
}
