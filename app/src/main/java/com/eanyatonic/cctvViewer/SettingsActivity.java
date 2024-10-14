package com.eanyatonic.cctvViewer;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (String abi : Build.SUPPORTED_ABIS) {
                if (abi.contains("64")) return true;
            }
        }
        return false;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        private SwitchPreference sysWebViewPreference;
        private ListPreference x5WebViewVersion;
        private Preference systemInfo;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            // 获取 SwitchPreference
            sysWebViewPreference = findPreference("sys_webview");
            x5WebViewVersion = findPreference("x5_webview_version");
            boolean exists = AssetUtil.fileExistsInAssets(getContext(), "045738_x5.tbs.apk");

            if (x5WebViewVersion != null) {
                if (!exists || isCpu64Bit()) {
                    {
                        x5WebViewVersion.setValue("1");
                    }
                }

                if (sysWebViewPreference != null) {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                    boolean switchValue = sharedPreferences.getBoolean("sys_webview", true);
                    boolean debugModeValue = sharedPreferences.getBoolean("debug_mode", false);

                    // 如果开关被关闭，禁用它
                    if (!switchValue && !debugModeValue) {
                        sysWebViewPreference.setEnabled(false);
                        sysWebViewPreference.setSummary("系统 WebView 已禁用");
                    }
                }
            }
            systemInfo = findPreference("info");
            if (systemInfo != null) {
                systemInfo.setSummary("Android " + Build.VERSION.RELEASE + " " + (isCpu64Bit() ? "64位" : "32位"));
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
