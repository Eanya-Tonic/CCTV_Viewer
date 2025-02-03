package com.eanyatonic.cctvViewer;


import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static android.os.UserManager.DISALLOW_INSTALL_APPS;

/**
 * 文件操作工具类
 * <p>
 * 将存储文件目录规范化，对应功能划分目录，后续操作缓存管理进行调用操作
 */
public class FileUtils {

    /**
     * 保存文件预览的目录
     *
     * @param context 上下文对象
     */
    public static File getTBSFileDir(Context context) {
        String dirName = "TBSFile";
        return context.getExternalFilesDir(dirName);
    }

    /**
     * 把asset的文件转化为本地文件
     *
     * @param context 上下文对象
     * @param oldPath 旧的文件路径
     * @param newPath 新的文件路径
     */
    public static boolean copyAssets(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);// 获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {// 如果是目录
                File file = new File(newPath);
                file.mkdirs();// 如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyAssets(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {// 如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 获取url文件后缀
     *
     * @param url 文件链接
     */
    public static String getSuffix(String url) {
        if ((url != null) && (url.length() > 0)) {
            int dot = url.lastIndexOf('.');
            if ((dot > -1) && (dot < (url.length() - 1))) {
                return url.substring(dot + 1);
            }
        }
        return "";
    }

    // 检查文件是否存在于下载目录
    public static boolean isFileExistInDownloads(Context context, String fileName) {
        // 获取下载目录
        File downloadDir = context.getExternalFilesDir("Download");
        if(downloadDir == null) {
            return false;
        }
        Log.d("FileUtils", "downloadDir: " + downloadDir.getAbsolutePath());

        // 创建目标文件对象
        File file = new File(downloadDir, fileName);

        // 返回文件是否存在
        return file.exists();
    }
    // 删除问题文件
    public static boolean deleteFile(Context context, String fileName) {
        File downloadDir = context.getExternalFilesDir("Download");
        if(downloadDir == null) {
            return false;
        }
        File file = new File(downloadDir, fileName);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }
}
