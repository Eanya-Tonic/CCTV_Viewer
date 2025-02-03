package com.eanyatonic.cctvViewer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import android.os.Build;

import androidx.annotation.NonNull;
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

            // 获取 SharedPreferences
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean isFirstRun = sharedPreferences.getBoolean("is_first_run", true);

            // 获取 SwitchPreference
            sysWebViewPreference = findPreference("sys_webview");
            x5WebViewVersion = findPreference("x5_webview_version");
            boolean exists = AssetUtil.fileExistsInAssets(getContext(), "045738_x5.tbs.apk");

            if (x5WebViewVersion != null && (!exists || isCpu64Bit())) {
                x5WebViewVersion.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                        // 获取所有的选项
                        String[] entryValues = getResources().getStringArray(R.array.x5_webview_version_values);

                        // 比较用户选择的值是否为第一个选项
                        if (entryValues[0].equals(newValue)) {
                            // 禁用第一个选项，提示用户
                            if(!exists) {
                                Toast.makeText(getContext(), "程序未集成本地X5内核", Toast.LENGTH_SHORT).show();
                            } else if (isCpu64Bit()) {
                                Toast.makeText(getContext(), "64位系统无法安装32位X5内核", Toast.LENGTH_SHORT).show();
                            }
                            return false; // 阻止选项被选择
                        }
                        return true; // 允许其他选项被选中
                    }
                });
            }

            // 如果是第一次运行，执行代码并更新标志位
            if (isFirstRun && x5WebViewVersion != null) {
                if (!exists || isCpu64Bit()) {
                    x5WebViewVersion.setValue("1"); // 第一次运行时设置
                }

                // 设置 is_first_run 为 false，确保只执行一次
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("is_first_run", false);
                editor.apply();
            }

            if (sysWebViewPreference != null) {
                boolean switchValue = sharedPreferences.getBoolean("sys_webview", true);
                boolean debugModeValue = sharedPreferences.getBoolean("debug_mode", false);

                // 如果开关被关闭，禁用它
                if (!switchValue && !debugModeValue) {
                    sysWebViewPreference.setEnabled(false);
                    sysWebViewPreference.setSummary("X5 WebView 已激活");
                }
                // 如果 X5 WebView 文件不存在，禁用它
                String installX5Mode = sharedPreferences.getString("x5_webview_version", "0");
                if (!exists) {
                    assert installX5Mode != null;
                    if (installX5Mode.equals("0")) {
                        sysWebViewPreference.setEnabled(false);
                        sysWebViewPreference.setSummary("程序未集成本地X5 WebView安装包");
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
