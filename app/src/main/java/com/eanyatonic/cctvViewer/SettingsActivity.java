package com.eanyatonic.cctvViewer;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

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

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        private SwitchPreference sysWebViewPreference;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            // 获取 SwitchPreference
            sysWebViewPreference = findPreference("sys_webview");

            if (sysWebViewPreference != null) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean switchValue = sharedPreferences.getBoolean("sys_webview", true);

                // 如果开关被关闭，禁用它
                if (!switchValue) {
                    sysWebViewPreference.setEnabled(false);
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
