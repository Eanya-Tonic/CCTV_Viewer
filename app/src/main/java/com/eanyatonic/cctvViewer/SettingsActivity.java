package com.eanyatonic.cctvViewer;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    private static boolean isCpu64Bit() {
        for (String abi : Build.SUPPORTED_ABIS) {
            if (abi.contains("64")) return true;
        }
        return false;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        private SwitchPreference sysWebViewPreference;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            // 获取 SwitchPreference
            sysWebViewPreference = findPreference("sys_webview");
            boolean exists = AssetUtil.fileExistsInAssets(getContext(), "045738_x5.tbs.apk");

            if (sysWebViewPreference != null) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean switchValue = sharedPreferences.getBoolean("sys_webview", true);

                // 如果开关被关闭，禁用它
                if (!switchValue) {
                    sysWebViewPreference.setEnabled(false);
                    sysWebViewPreference.setSummary("系统 WebView 已禁用");
                }

                // 如果设备是64位，禁用它
                if (isCpu64Bit()) {
                    sysWebViewPreference.setEnabled(false);
                    sysWebViewPreference.setSummary("X5 WebView 已禁用（64位设备不支持）");
                }

                // 如果 X5 WebView 文件不存在，禁用它
                if (!exists) {
                    sysWebViewPreference.setEnabled(false);
                    sysWebViewPreference.setSummary("程序未集成本地X5 WebView安装包");
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            // 注册 SharedPreferences 监听器
            Objects.requireNonNull(getPreferenceScreen().getSharedPreferences())
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            // 取消注册监听器
            Objects.requireNonNull(getPreferenceScreen().getSharedPreferences())
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // 当任何设置项改变时，显示吐司提示
            Toast.makeText(getContext(), "重启应用后修改生效", Toast.LENGTH_SHORT).show();
        }
    }
}
