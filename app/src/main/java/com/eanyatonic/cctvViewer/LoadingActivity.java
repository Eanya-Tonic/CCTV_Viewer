package com.eanyatonic.cctvViewer;

import com.eanyatonic.cctvViewer.FileUtils;
import com.eanyatonic.cctvViewer.MainActivity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;

public class LoadingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        // 安装TBS内核
        QbSdk.reset(MainActivity.get());
        QbSdk.installLocalTbsCore(MainActivity.get(), 45738,
                FileUtils.getTBSFileDir(MainActivity.get()).getPath() + "/045738_x5.tbs.apk");
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
                Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // 销毁 LoadingActivity
            }
        });
    }
}
