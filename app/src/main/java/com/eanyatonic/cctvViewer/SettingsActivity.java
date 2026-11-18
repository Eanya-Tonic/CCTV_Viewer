package com.eanyatonic.cctvViewer;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.recyclerview.widget.RecyclerView;
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
            boolean debugModeValue = sharedPreferences.getBoolean("debug_mode", false);
            boolean exists = AssetUtil.fileExistsInAssets(getContext(), "045738_x5.tbs.apk");

            if (x5WebViewVersion != null && (!exists || isCpu64Bit()) && !debugModeValue) {
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
                debugModeValue = sharedPreferences.getBoolean("debug_mode", false);

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
            applyColorEntries();
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            // 行背景/开关颜色跟随主题色，并在行绑定时应用（列表会复用视图）
            getListView().addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
                @Override
                public void onChildViewAttachedToWindow(@NonNull View child) {
                    styleRow(child);
                }

                @Override
                public void onChildViewDetachedFromWindow(@NonNull View child) {
                }
            });
            // 焦点变化时按当前主题色即时上色，避免聚焦行残留旧颜色
            getListView().getViewTreeObserver().addOnGlobalFocusChangeListener((oldFocus, newFocus) -> {
                if (newFocus == null) {
                    return;
                }
                View row = newFocus;
                ViewParent parent = row.getParent();
                while (parent != null && parent != getListView()) {
                    if (!(parent instanceof View)) {
                        return; // 焦点不在本列表中（如弹窗），忽略
                    }
                    row = (View) parent;
                    parent = row.getParent();
                }
                if (parent == getListView()) {
                    styleRow(row);
                }
            });
            view.post(() -> {
                getListView().scrollToPosition(0);
                applyAccentToSettings();
            });
        }

        private int accentColor() {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            try {
                return Color.parseColor(sp.getString("theme_color", "#FF5DA9E9"));
            } catch (Exception e) {
                return 0xFF5DA9E9;
            }
        }

        private int dp(float v) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                    getResources().getDisplayMetrics());
        }

        private StateListDrawable rowBackground() {
            int accent = accentColor();
            StateListDrawable sld = new StateListDrawable();
            GradientDrawable focused = new GradientDrawable();
            focused.setCornerRadius(dp(6));
            focused.setColor(accent);
            GradientDrawable normal = new GradientDrawable();
            normal.setCornerRadius(dp(6));
            normal.setColor(0xFF333333);
            sld.addState(new int[]{android.R.attr.state_focused}, focused);
            sld.addState(new int[]{}, normal);
            return sld;
        }

        // 只处理设置行（含 widget_frame）；分类标题只改颜色、不加背景
        private void styleRow(View row) {
            if (row.findViewById(android.R.id.widget_frame) == null) {
                View title = row.findViewById(android.R.id.title);
                if (title instanceof TextView) {
                    ((TextView) title).setTextColor(accentColor());
                }
                return;
            }
            row.setBackground(rowBackground());
            tintSwitches(row, accentColor());
        }

        private void tintSwitches(View v, int accent) {
            if (v instanceof SwitchCompat) {
                SwitchCompat sc = (SwitchCompat) v;
                sc.setThumbTintList(ColorStateList.valueOf(accent));
                sc.setTrackTintList(ColorStateList.valueOf(
                        Color.argb(0x66, Color.red(accent), Color.green(accent), Color.blue(accent))));
                return;
            }
            if (v instanceof ViewGroup) {
                ViewGroup g = (ViewGroup) v;
                for (int i = 0; i < g.getChildCount(); i++) {
                    tintSwitches(g.getChildAt(i), accent);
                }
            }
        }

        // 主题色选择对话框：每个选项前加该颜色的圆点预览
        private void applyColorEntries() {
            Preference p = findPreference("theme_color");
            if (!(p instanceof ListPreference)) {
                return;
            }
            ListPreference tp = (ListPreference) p;
            CharSequence[] labels = getResources().getTextArray(R.array.theme_color);
            CharSequence[] values = getResources().getTextArray(R.array.theme_color_values);
            CharSequence[] entries = new CharSequence[labels.length];
            for (int i = 0; i < labels.length; i++) {
                int color;
                try {
                    color = Color.parseColor(values[i].toString());
                } catch (Exception e) {
                    color = 0xFF5DA9E9;
                }
                SpannableString ss = new SpannableString("●  " + labels[i]);
                ss.setSpan(new ForegroundColorSpan(color), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                entries[i] = ss;
            }
            tp.setEntries(entries);
        }

        private void applyAccentToSettings() {
            RecyclerView list = getListView();
            if (list == null) {
                return;
            }
            for (int i = 0; i < list.getChildCount(); i++) {
                styleRow(list.getChildAt(i));
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
            if ("theme_color".equals(key)) {
                applyAccentToSettings();
                // 对话框收起/列表重绑后再补刷一次，避免聚焦行残留旧色
                getListView().postDelayed(this::applyAccentToSettings, 200);
                Toast.makeText(getContext(), "已生效", Toast.LENGTH_SHORT).show();
                return;
            }
            if ("dual_webview".equals(key) || "sys_webview".equals(key) || "x5_webview_version".equals(key)) {
                // 涉及 WebView 内核/双缓冲，需重启
                Toast.makeText(getContext(), "重启应用后生效", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "已生效", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
