package com.eanyatonic.cctvViewer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadActivity extends AppCompatActivity {
    public static final String EXTRA_FILE_URL = "file_url";

    private static final int CONNECT_TIMEOUT_MS = 15_000;
    private static final int READ_TIMEOUT_MS = 30_000;

    private ProgressBar progressBar;
    private TextView downloadStatus;
    private Button retryButton;
    private Button cancelButton;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private volatile boolean stopped = false;
    private volatile boolean failed = false;
    private volatile boolean completed = false;
    private volatile HttpURLConnection connection;
    private Thread downloadThread;
    private String fileUrl;
    private File targetFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        progressBar = findViewById(R.id.progressBar);
        downloadStatus = findViewById(R.id.downloadStatus);
        retryButton = findViewById(R.id.retryButton);
        cancelButton = findViewById(R.id.cancelButton);

        fileUrl = getIntent().getStringExtra(EXTRA_FILE_URL);
        retryButton.setOnClickListener(v -> startDownload());
        cancelButton.setOnClickListener(v -> cancelDownload());

        startDownload();
    }

    private void startDownload() {
        if (downloadThread != null && downloadThread.isAlive()) {
            return;
        }
        File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        String fileName = fileNameFromUrl(fileUrl);
        if (dir == null) {
            fail("无法访问下载目录");
            return;
        }
        if (fileName == null) {
            fail("下载地址无效");
            return;
        }

        targetFile = new File(dir, fileName);
        if (targetFile.exists() && !targetFile.delete()) {
            fail("无法清理旧下载文件");
            return;
        }
        stopped = false;
        failed = false;
        completed = false;
        progressBar.setIndeterminate(true);
        progressBar.setProgress(0);
        downloadStatus.setText("下载中…");
        retryButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        downloadThread = new Thread(this::download);
        downloadThread.start();
    }

    private void download() {
        HttpURLConnection conn = null;
        InputStream in = null;
        FileOutputStream out = null;
        try {
            URL url = new URL(fileUrl);
            conn = (HttpURLConnection) url.openConnection();
            connection = conn;
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setInstanceFollowRedirects(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) CCTV_Viewer");
            conn.setRequestProperty("Accept-Encoding", "identity");

            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                fail("下载失败（HTTP " + code + "）");
                return;
            }
            long total = conn.getContentLength();
            if (total > 0) {
                handler.post(() -> {
                    if (stopped) {
                        return;
                    }
                    progressBar.setIndeterminate(false);
                    progressBar.setMax(100);
                    progressBar.setProgress(0);
                });
            }

            in = conn.getInputStream();
            out = new FileOutputStream(targetFile);
            byte[] buffer = new byte[64 * 1024];
            long downloaded = 0;
            long lastUpdate = 0;
            int read;
            while (!stopped && (read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                downloaded += read;
                long now = System.currentTimeMillis();
                if (now - lastUpdate >= 300 || (total > 0 && downloaded == total)) {
                    lastUpdate = now;
                    updateProgress(downloaded, total);
                }
            }
            out.flush();
            out.close();
            out = null;
            in.close();
            in = null;

            if (stopped) {
                return;
            }
            if (total > 0 && downloaded != total) {
                fail("下载不完整，请重试");
                return;
            }
            if (!X5Kernel.isCompleteApk(targetFile)) {
                fail("下载文件校验失败，请重试");
                return;
            }
            completed = true;
            handler.post(this::startInstallActivity);
        } catch (IOException e) {
            Log.e("X5Download", "download failed", e);
            if (!stopped) {
                fail("网络错误，下载中断");
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
            connection = null;
        }
    }

    private void updateProgress(long downloaded, long total) {
        if (total <= 0) {
            return;
        }
        int percent = (int) Math.min(100, downloaded * 100 / total);
        handler.post(() -> {
            if (stopped) {
                return;
            }
            progressBar.setIndeterminate(false);
            progressBar.setProgress(percent);
            downloadStatus.setText("下载中… " + percent + "%");
        });
    }

    private void startInstallActivity() {
        if (stopped) {
            return;
        }
        stopped = true;
        downloadStatus.setText("下载完成，正在安装…");
        retryButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        startActivity(new Intent(this, LoadingActivity.class));
        finish();
    }

    private void fail(String msg) {
        if (stopped || failed) {
            return;
        }
        failed = true;
        deleteTargetFile();
        handler.post(() -> {
            if (stopped) {
                return;
            }
            progressBar.setIndeterminate(false);
            downloadStatus.setText(msg);
            retryButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
        });
    }

    private static String fileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            String path = new URL(url).getPath();
            int slash = path.lastIndexOf('/');
            String name = slash >= 0 ? path.substring(slash + 1) : path;
            return name.isEmpty() ? null : name;
        } catch (Exception e) {
            return null;
        }
    }

    private void cancelDownload() {
        stopped = true;
        disconnect();
        deleteTargetFile();
        Toast.makeText(this, "已取消下载，继续使用系统内核", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void disconnect() {
        HttpURLConnection conn = connection;
        if (conn != null) {
            try {
                conn.disconnect();
            } catch (Exception ignored) {
            }
        }
    }

    private void deleteTargetFile() {
        if (targetFile != null && targetFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            targetFile.delete();
        }
    }

    @Override
    public void onBackPressed() {
        cancelDownload();
    }

    @Override
    protected void onDestroy() {
        stopped = true;
        disconnect();
        handler.removeCallbacksAndMessages(null);
        if (!completed) {
            deleteTargetFile();
        }
        super.onDestroy();
    }
}
