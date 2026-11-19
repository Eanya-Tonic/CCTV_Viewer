package com.eanyatonic.cctvViewer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
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

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        private Preference x5KernelPreference;
        private SwitchPreference useX5Preference;
        private Preference systemInfo;
        private float uiScale = 1f;
        private float textScale = 1f;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            X5Kernel.migrateLegacyPrefs(requireContext());

            x5KernelPreference = findPreference("x5_kernel");
            useX5Preference = findPreference("use_x5");
            if (x5KernelPreference != null) {
                x5KernelPreference.setOnPreferenceClickListener(preference -> {
                    startX5Setup();
                    return true;
                });
            }
            updateX5State();

            systemInfo = findPreference("info");
            if (systemInfo != null) {
                systemInfo.setSummary("Android " + Build.VERSION.RELEASE + " " + (X5Kernel.isCpu64Bit() ? "64位" : "32位"));
            }
            applyColorEntries();
        }

        // 下载/安装流程只从设置页发起，启动时不再联网下载
        private void startX5Setup() {
            Context ctx = requireContext();
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
            if (!X5Kernel.isCpuSupported() && !sp.getBoolean("debug_mode", false)) {
                Toast.makeText(ctx, "当前设备架构不支持 X5 内核", Toast.LENGTH_SHORT).show();
                return;
            }
            if (X5Kernel.hasDownloadedApk(ctx)) {
                startActivity(new Intent(ctx, LoadingActivity.class));
            } else if (!X5Kernel.isCpu64Bit() && X5Kernel.hasBundledAsset(ctx)) {
                startActivity(new Intent(ctx, LoadingActivity.class)
                        .putExtra(LoadingActivity.EXTRA_INSTALL_ASSET, true));
            } else {
                startActivity(new Intent(ctx, DownloadActivity.class)
                        .putExtra(DownloadActivity.EXTRA_FILE_URL, X5Kernel.downloadUrl()));
            }
        }

        private void updateX5State() {
            if (x5KernelPreference == null || useX5Preference == null) {
                return;
            }
            Context ctx = getContext();
            if (ctx == null) {
                return;
            }
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
            boolean debug = sp.getBoolean("debug_mode", false);
            if (!X5Kernel.isCpuSupported() && !debug) {
                x5KernelPreference.setEnabled(false);
                x5KernelPreference.setSummary("当前设备（" + X5Kernel.primaryAbi()
                        + "）不支持 X5 内核，将继续使用系统内核");
                useX5Preference.setEnabled(false);
                useX5Preference.setSummary("当前设备不支持 X5 内核");
                return;
            }
            x5KernelPreference.setEnabled(true);

            boolean ready = X5Kernel.isReady(ctx);
            boolean downloaded = X5Kernel.hasDownloadedApk(ctx);
            boolean bundled = !X5Kernel.isCpu64Bit() && X5Kernel.hasBundledAsset(ctx);
            int version = X5Kernel.installedVersion(ctx);

            if (ready) {
                String ver = version > 0 ? "（v" + version + "）" : "";
                x5KernelPreference.setSummary("已安装" + ver + "，点击可重新安装");
            } else if (downloaded) {
                x5KernelPreference.setSummary("安装包已下载，点击安装");
            } else if (bundled) {
                x5KernelPreference.setSummary("点击安装内置 X5 内核（无需下载）");
            } else {
                x5KernelPreference.setSummary("未安装，点击下载并安装");
            }

            useX5Preference.setEnabled(ready || debug);
            useX5Preference.setSummary(ready
                    ? "切换内核后需重启应用"
                    : (debug ? "X5 未安装，开启后启动会回退系统内核" : "请先安装 X5 内核"));
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            DisplayMetrics dm = getResources().getDisplayMetrics();
            uiScale = Math.min(dm.widthPixels, dm.heightPixels) / 1080f;
            updateTextScale();
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

        // 只处理设置行（含 widget_frame）；分类标题只改颜色/字号、不加背景
        private void styleRow(View row) {
            if (row.findViewById(android.R.id.widget_frame) == null) {
                View title = row.findViewById(android.R.id.title);
                if (title instanceof TextView) {
                    ((TextView) title).setTextColor(accentColor());
                    setPx((TextView) title, 16);
                }
                return;
            }
            row.setBackground(rowBackground());
            tintSwitches(row, accentColor());
            View title = row.findViewById(android.R.id.title);
            if (title instanceof TextView) {
                setPx((TextView) title, 22);
            }
            View summary = row.findViewById(android.R.id.summary);
            if (summary instanceof TextView) {
                setPx((TextView) summary, 16);
            }
        }

        private void setPx(TextView tv, float base) {
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, base * uiScale * textScale);
        }

        private void updateTextScale() {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            int size;
            switch (sp.getString("text_size", "1")) {
                case "0":
                    size = 18;
                    break;
                case "2":
                    size = 25;
                    break;
                case "3":
                    size = 30;
                    break;
                default:
                    size = 22;
            }
            textScale = size / 22f;
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
            // 安装/下载页返回后刷新内核状态
            updateX5State();
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
            if ("text_size".equals(key)) {
                updateTextScale();
                applyAccentToSettings();
                Toast.makeText(getContext(), "已生效", Toast.LENGTH_SHORT).show();
                return;
            }
            if ("theme_color".equals(key)) {
                applyAccentToSettings();
                // 对话框收起/列表重绑后再补刷一次，避免聚焦行残留旧色
                getListView().postDelayed(this::applyAccentToSettings, 200);
                Toast.makeText(getContext(), "已生效", Toast.LENGTH_SHORT).show();
                return;
            }
            if (X5Kernel.PREF_USE_X5.equals(key)) {
                updateX5State();
                Toast.makeText(getContext(), "重启应用后生效", Toast.LENGTH_SHORT).show();
                return;
            }
            if ("debug_mode".equals(key)) {
                updateX5State();
                Toast.makeText(getContext(), "已生效", Toast.LENGTH_SHORT).show();
                return;
            }
            if ("dual_webview".equals(key)) {
                // 涉及双缓冲，需重启
                Toast.makeText(getContext(), "重启应用后生效", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "已生效", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
