package com.eanyatonic.cctvViewer;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import com.eanyatonic.cctvViewer.FileUtils;

import android.Manifest;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

// X5内核代码
import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.util.HashMap;
import java.util.Objects;

// WebView内核代码
//import android.webkit.SslErrorHandler;
//import android.webkit.WebChromeClient;
//import android.webkit.WebSettings;
//import android.webkit.WebView;
//import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {

    private AudioManager audioManager;

    private WebView webView0; // 导入 WebView
    private WebView webView1; // 导入备用 WebView
    private boolean canLoadX5 = false;

    private int currentWebView = 0; // 正在使用的webView
    private boolean isChanging = false; // 是否正在换台

    private final String[] liveUrls = { "https://tv.cctv.com/live/cctv1/", "https://tv.cctv.com/live/cctv2/",
            "https://tv.cctv.com/live/cctv3/", "https://tv.cctv.com/live/cctv4/", "https://tv.cctv.com/live/cctv5/",
            "https://tv.cctv.com/live/cctv6/", "https://tv.cctv.com/live/cctv7/", "https://tv.cctv.com/live/cctv8/",
            "https://tv.cctv.com/live/cctvjilu", "https://tv.cctv.com/live/cctv10/", "https://tv.cctv.com/live/cctv11/",
            "https://tv.cctv.com/live/cctv12/", "https://tv.cctv.com/live/cctv13/",
            "https://tv.cctv.com/live/cctvchild", "https://tv.cctv.com/live/cctv15/",
            "https://tv.cctv.com/live/cctv16/", "https://tv.cctv.com/live/cctv17/",
            "https://tv.cctv.com/live/cctv5plus/", "https://tv.cctv.com/live/cctveurope",
            "https://tv.cctv.com/live/cctvamerica/", "https://www.yangshipin.cn/tv/home?pid=600002309",
            "https://www.yangshipin.cn/tv/home?pid=600002521", "https://www.yangshipin.cn/tv/home?pid=600002483",
            "https://www.yangshipin.cn/tv/home?pid=600002520", "https://www.yangshipin.cn/tv/home?pid=600002475",
            "https://www.yangshipin.cn/tv/home?pid=600002508", "https://www.yangshipin.cn/tv/home?pid=600002485",
            "https://www.yangshipin.cn/tv/home?pid=600002509", "https://www.yangshipin.cn/tv/home?pid=600002498",
            "https://www.yangshipin.cn/tv/home?pid=600002506", "https://www.yangshipin.cn/tv/home?pid=600002531",
            "https://www.yangshipin.cn/tv/home?pid=600002481", "https://www.yangshipin.cn/tv/home?pid=600002516",
            "https://www.yangshipin.cn/tv/home?pid=600002525", "https://www.yangshipin.cn/tv/home?pid=600002484",
            "https://www.yangshipin.cn/tv/home?pid=600002490", "https://www.yangshipin.cn/tv/home?pid=600002503",
            "https://www.yangshipin.cn/tv/home?pid=600002505", "https://www.yangshipin.cn/tv/home?pid=600002532",
            "https://www.yangshipin.cn/tv/home?pid=600002493", "https://www.yangshipin.cn/tv/home?pid=600002513", };

    private final String[] channelNames = { "1 CCTV-1 综合", "2 CCTV-2 财经", "3 CCTV-3 综艺", "4 CCTV-4 中文国际（亚）",
            "5 CCTV-5 体育", "6 CCTV-6 电影", "7 CCTV-7 国防军事", "8 CCTV-8 电视剧", "9 CCTV-9 纪录", "10 CCTV-10 科教",
            "11 CCTV-11 戏曲", "12 CCTV-12 社会与法", "13 CCTV-13 新闻", "14 CCTV-14 少儿", "15 CCTV-15 音乐", "16 CCTV-16 奥林匹克",
            "17 CCTV-17 农业农村", "18 CCTV-5+ 体育赛事", "19 CCTV-4 中文国际（欧）", "20 CCTV-4 中文国际（美）", "21 北京卫视", "22 江苏卫视",
            "23 东方卫视", "24 浙江卫视", "25 湖南卫视", "26 湖北卫视", "27 广东卫视", "28 广西卫视", "29 黑龙江卫视", "30 海南卫视", "31 重庆卫视",
            "32 深圳卫视", "33 四川卫视", "34 河南卫视", "35 福建东南卫视", "36 贵州卫视", "37 江西卫视", "38 辽宁卫视", "39 安徽卫视", "40 河北卫视",
            "41 山东卫视", };

    // 央视各台在央视频的 pid（null = 无：CCTV-4 欧/美）；3/6/8 为 VIP 源，必要时可切回央视网
    private final String[] yspCctvPids = {
            "600001859", // CCTV-1
            "600001800", // CCTV-2
            "600001801", // CCTV-3
            "600001814", // CCTV-4 亚
            "600001818", // CCTV-5
            "600108442", // CCTV-6
            "600004092", // CCTV-7
            "600001803", // CCTV-8
            "600004078", // CCTV-9
            "600001805", // CCTV-10
            "600001806", // CCTV-11
            "600001807", // CCTV-12
            "600001811", // CCTV-13
            "600001809", // CCTV-14
            "600001815", // CCTV-15
            "600098637", // CCTV-16
            "600001810", // CCTV-17
            "600001817", // CCTV-5+
            null,        // CCTV-4 欧
            null         // CCTV-4 美
    };

    private int currentLiveIndex;

    private static final String PREF_NAME = "MyPreferences";
    private static final String PREF_KEY_LIVE_INDEX = "currentLiveIndex";

    private boolean doubleBackToExitPressedOnce = false;

    private StringBuilder digitBuffer = new StringBuilder(); // 用于缓存按下的数字键
    private static final long DIGIT_TIMEOUT = 3000; // 超时时间（毫秒）

    private TextView inputTextView; // 用于显示正在输入的数字的 TextView

    // 初始化透明的View
    private View loadingOverlay;

    // 频道显示view（EPG 浮层）
    private LinearLayout epgOverlay;
    private TextView overlayChannelText;
    private TextView overlayNowText;
    private ProgressBar overlayProgress;
    private TextView overlayRemainText;
    private TextView overlayNextText;

    // EPG 节目单数据层与抽屉右侧视图
    private EpgRepository epgRepo;
    private ScrollView EpgDateScroll;
    private LinearLayout EpgDateList;
    private ScrollView EpgScheduleScroll;
    private LinearLayout EpgScheduleList;

    private int epgChannelIndex = 0;       // 右侧 EPG 正在展示的频道
    private int epgDateOffset = 0;         // 日期列选中的相对天数偏移
    private int epgDateSelectedIndex = 0;  // 日期列焦点行
    private int epgScheduleSelectedIndex = 0;
    private boolean overlaySkeletonShowing = false;
    private static final int EPG_DAY_PAST = 6;   // 日期列范围：今天-6 .. 明天+1
    private static final int EPG_DAY_COUNT = 8;

    // 在 MainActivity 中添加一个 Handler
    private final Handler handler = new Handler();
    private final Runnable hideOverlayRunnable = () -> epgOverlay.setVisibility(View.GONE);
    private boolean switchPending = false;
    private int webView0Index = -1;
    private int webView1Index = -1;
    private Runnable videoPollRunnable;
    private boolean activityDestroyed = false;
    private final Runnable pendingLoadRunnable = () -> {
        if (activityDestroyed) {
            return;
        }
        switchPending = false;
        loadLiveUrl();
    };
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    private boolean isMenuOverlayVisible = false;
    private boolean isDrawerOverlayVisible = false;

    private LinearLayout menuOverlay;
    private LinearLayout DrawerLayout;
    private LinearLayout DrawerLayoutDetailed;
    private LinearLayout SubMenuCCTV;
    private LinearLayout SubMenuLocal;
    private TextView CoreText;

    private int menuOverlaySelectedIndex = 0;
    private int DrawerLayoutSelectedIndex = 0;
    private int SubMenuCCTVSelectedIndex = 0;
    private int SubMenuLocalSelectedIndex = 0;

    // 可自定义设置项
    private int TEXT_SIZE = 22;
    private float uiScale = 1f; // 屏幕短边/1080，用于把字号换算成 px
    private int accentColor = 0xFF5DA9E9; // 主题强调色（焦点/进度条等）
    private int activeCategory = 0; // 当前展开的一级类别：0 央视 / 1 地方
    private boolean videoMuted = false;
    private Boolean enableDualWebView = true;
    private Boolean enableDirectChannelChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 以 1080p 短边为基准计算缩放，字号用 px 而非 sp，避免高密度屏（4K/手机）字体过大
        DisplayMetrics uiMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(uiMetrics);
        uiScale = Math.min(uiMetrics.widthPixels, uiMetrics.heightPixels) / 1080f;

        // 加载设置
        // 获取 SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // 读取字体大小
        TEXT_SIZE = mapTextSizeOption(sharedPreferences.getString("text_size", "1"));

        // 读取直接频道切换设置
        enableDirectChannelChange = sharedPreferences.getBoolean("direct_channel_change", true);

        // 读取主题色
        accentColor = parseColor(sharedPreferences.getString("theme_color", "#FF5DA9E9"));

        // 读取双缓冲设置
        enableDualWebView = sharedPreferences.getBoolean("dual_webview", false);

        // 读取是否加载网页图片
        boolean loadImages = sharedPreferences.getBoolean("load_images", true);

        // 读取WebView设置
        Boolean forceSysWebView = sharedPreferences.getBoolean("sys_webview", true);

        // 获取 AudioManager 实例
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // 初始化 WebView
        webView0 = findViewById(R.id.webView0);
        webView1 = findViewById(R.id.webView1);

        // 初始化显示正在输入的数字的 TextView
        inputTextView = findViewById(R.id.inputTextView);

        // 初始化 loadingOverlay
        loadingOverlay = findViewById(R.id.loadingOverlay);

        // 初始化 EPG 浮层
        epgOverlay = findViewById(R.id.epgOverlay);
        overlayChannelText = findViewById(R.id.overlayChannelText);
        overlayNowText = findViewById(R.id.overlayNowText);
        overlayProgress = findViewById(R.id.overlayProgress);
        overlayRemainText = findViewById(R.id.overlayRemainText);
        overlayNextText = findViewById(R.id.overlayNextText);
        // 浮层加宽 + 左右内边距对齐圆角（各占位行固定，高度自然稳定）
        epgOverlay.getLayoutParams().width = (int) (760 * uiScale);
        int overlayPadV = (int) (24 * uiScale);
        int cornerPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50,
                getResources().getDisplayMetrics());
        int overlayPadH = cornerPx + (int) (8 * uiScale);
        epgOverlay.setPadding(overlayPadH, overlayPadV, overlayPadH, overlayPadV);
        setTvTextSize(overlayChannelText, 24);
        setTvTextSize(overlayNowText, 20);
        setTvTextSize(overlayRemainText, 16);
        setTvTextSize(overlayNextText, 18);
        applyProgressTint();
        // 当前节目单行限宽，过长横向滚动；下一节目单行截断
        overlayNowText.setSingleLine(true);
        overlayNowText.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        overlayNowText.setMarqueeRepeatLimit(-1);
        overlayNowText.setHorizontallyScrolling(true);
        overlayNowText.setSelected(true);
        overlayNowText.setMaxWidth((int) (660 * uiScale));
        overlayNextText.setSingleLine(true);
        overlayNextText.setEllipsize(TextUtils.TruncateAt.END);
        overlayNextText.setMaxWidth((int) (660 * uiScale));

        // 初始化 EPG 数据层与抽屉右侧视图
        epgRepo = new EpgRepository();
        EpgDateScroll = findViewById(R.id.EpgDateScroll);
        EpgDateList = findViewById(R.id.EpgDateList);
        EpgScheduleScroll = findViewById(R.id.EpgScheduleScroll);
        EpgScheduleList = findViewById(R.id.EpgScheduleList);
        buildDateColumn();

        // 初始化 菜单
        menuOverlay = findViewById(R.id.menuOverlay);
        for (int i = 0; i < menuOverlay.getChildCount(); i++) {
            LinearLayout pill = (LinearLayout) menuOverlay.getChildAt(i);
            pill.setBackground(pillBackground());
            setTvTextSize((TextView) pill.getChildAt(1), 18);
        }

        // 初始化 DrawerLayout
        DrawerLayout = findViewById(R.id.DrawerLayout);

        // 初始化 DrawerLayoutDetailed
        DrawerLayoutDetailed = findViewById(R.id.DrawerLayoutDetailed);

        // 初始化 CCTV 子菜单
        SubMenuCCTV = findViewById(R.id.subMenuCCTV);

        // 初始化 Local 子菜单
        SubMenuLocal = findViewById(R.id.subMenuLocal);

        // 初始化 CoreText
        CoreText = findViewById(R.id.CoreText);
        setTvTextSize(CoreText, 14);

        LinearLayout DrawerLayout = findViewById(R.id.DrawerLayout);

        // 中央台频道列表
        String[] firstDrawer = {
                "央视频道", "地方频道", "网页菜单", "设置"
        };

        // 动态生成中央台按钮和地方台按钮
        for (String channel : firstDrawer) {
            Button button = new Button(this);
            button.setText(channel);
            button.setTag(channel);
            // 创建 LayoutParams 并设置 margin
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(4, 4, 4, 4);
            // 设置按钮属性
            button.setLayoutParams(layoutParams);
            button.setPadding(16, 16, 16, 16);
            button.setTextColor(getResources().getColor(android.R.color.white));
            button.setBackground(channelItemBackground());
            setTvTextSize(button, TEXT_SIZE);
            DrawerLayout.addView(button);
        }

        LinearLayout subMenuCCTV = findViewById(R.id.subMenuCCTV);
        LinearLayout subMenuLocal = findViewById(R.id.subMenuLocal);

        // 频道列与节目表列同宽；抽屉总宽由内容撑开（wrap_content），比半屏略宽
        int channelColWidth = (int) (400 * uiScale);
        findViewById(R.id.CCTVScroll).getLayoutParams().width = channelColWidth;
        findViewById(R.id.LocalScroll).getLayoutParams().width = channelColWidth;
        findViewById(R.id.EpgScheduleScroll).getLayoutParams().width = channelColWidth;

        // 中央台频道列表
        String[] cctvChannels = {
                "CCTV-1 综合", "CCTV-2 财经", "CCTV-3 综艺", "CCTV-4 中文国际（亚）",
                "CCTV-5 体育", "CCTV-6 电影", "CCTV-7 国防军事", "CCTV-8 电视剧",
                "CCTV-9 纪录", "CCTV-10 科教", "CCTV-11 戏曲", "CCTV-12 社会与法",
                "CCTV-13 新闻", "CCTV-14 少儿", "CCTV-15 音乐", "CCTV-16 奥林匹克",
                "CCTV-17 农业农村", "CCTV-5+ 体育赛事", "CCTV-4 中文国际（欧）", "CCTV-4 中文国际（美）"
        };

        // 动态生成中央台频道项（名称+可滚动当前节目副标题）
        for (String channel : cctvChannels) {
            subMenuCCTV.addView(buildChannelItem(channel));
        }

        // 地方台频道列表
        String[] localChannels = {
                "北京卫视", "江苏卫视", "东方卫视", "浙江卫视", "湖南卫视", "湖北卫视",
                "广东卫视", "广西卫视", "黑龙江卫视", "海南卫视", "重庆卫视", "深圳卫视",
                "四川卫视", "河南卫视", "福建东南卫视", "贵州卫视", "江西卫视", "辽宁卫视",
                "安徽卫视", "河北卫视", "山东卫视"
        };

        // 动态生成地方台频道项（名称+可滚动当前节目副标题）
        for (String channel : localChannels) {
            subMenuLocal.addView(buildChannelItem(channel));
        }

        // https://developer.android.com/reference/android/webkit/WebView.html#getCurrentWebViewPackage()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Android 8.0+
            PackageInfo pkgInfo = WebView.getCurrentWebViewPackage();
            if (pkgInfo != null) {
                CoreText.setText("系统 WebView " + pkgInfo.versionName);
            }
        }

        // X5内核代码
        if (!forceSysWebView) {
            QbSdk.unForceSysWebView();
            requestPermission();

            Log.d("versionX5",String.valueOf(QbSdk.getTbsVersion(getApplicationContext())));
            canLoadX5 = QbSdk.canLoadX5(getApplicationContext());
            Log.d("canLoadX5", String.valueOf(canLoadX5));
            if (canLoadX5) {
                CoreText.setText("腾讯 X5 内核");
            } else {
                Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
                startActivity(intent);
                finish(); // 销毁 MainActivity
            }
        }
        else{
            QbSdk.forceSysWebView();
        }

        HashMap<String, Object> map = new HashMap<>(2);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        QbSdk.initTbsSettings(map);

        // 配置 WebView 设置
        WebSettings webSettings = webView0.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setLoadsImagesAutomatically(loadImages);
        webSettings.setBlockNetworkImage(!loadImages);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");

        // 启用缓存
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // 启用 JavaScript 自动点击功能
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        // 配置 WebView 设置
        WebSettings webSettings1 = webView1.getSettings();
        webSettings1.setJavaScriptEnabled(true);
        webSettings1.setDomStorageEnabled(true);
        webSettings1.setDatabaseEnabled(true);
        webSettings1.setLoadsImagesAutomatically(loadImages);
        webSettings1.setBlockNetworkImage(!loadImages);
        webSettings1.setMediaPlaybackRequiresUserGesture(false);
        webSettings1.setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");

        // 启用缓存
        webSettings1.setCacheMode(WebSettings.LOAD_DEFAULT);

        // 启用 JavaScript 自动点击功能
        webSettings1.setJavaScriptCanOpenWindowsAutomatically(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // X5内核代码
            webSettings.setMixedContentMode(com.tencent.smtt.sdk.WebSettings.LOAD_NORMAL);
            webSettings1.setMixedContentMode(com.tencent.smtt.sdk.WebSettings.LOAD_NORMAL);
            // 系统WebView内核代码
            // webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // 设置 WebViewClient 和 WebChromeClient
        webView0.setWebViewClient(new WebViewClient() {
            // X5内核代码
            @Override
            public void onReceivedSslError(com.tencent.smtt.sdk.WebView webView,
                    com.tencent.smtt.export.external.interfaces.SslErrorHandler handler,
                    com.tencent.smtt.export.external.interfaces.SslError error) {
                handler.proceed(); // 忽略 SSL 错误
            }

            // 系统Webview内核代码
            // @Override
            // public void onReceivedSslError(WebView view, SslErrorHandler handler,
            // SslError error) {
            // handler.proceed(); // 忽略 SSL 错误
            // }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // 页面加载时执行 JavaScript 脚本
                view.evaluateJavascript(
                        """
                                // 低频清理 + 退出上限，避免 4ms 空转抢占 CPU（issue #140）
                                if (!window.__fastLoadingActive) {
                                    window.__fastLoadingActive = true;
                                    var fastLoadingStart = Date.now();
                                    function FastLoading() {
                                        const fullscreenBtn = document.querySelector('#player_pagefullscreen_yes_player') || document.querySelector('.videoFull');
                                        if (fullscreenBtn) { window.__fastLoadingActive = false; return; }
                                        if (Date.now() - fastLoadingStart > 10000) { window.__fastLoadingActive = false; return; }

                                        // 清空特定的脚本 src 属性
                                        const scriptKeywords = ['login', 'index', 'daohang', 'grey', 'jquery'];
                                        Array.from(document.getElementsByTagName('script')).forEach(script => {
                                            if (scriptKeywords.some(keyword => script.src.includes(keyword))) {
                                                script.src = '';
                                            }
                                        });

                                        // 清空具有特定 class 的 div 内容
                                        const classNames = ['newmap', 'newtopbz', 'newtopbzTV', 'column_wrapper'];
                                        classNames.forEach(className => {
                                            Array.from(document.getElementsByClassName(className)).forEach(div => {
                                                div.innerHTML = '';
                                            });
                                        });

                                        var flInterval = (Date.now() - fastLoadingStart < 2000) ? 100 : 500;
                                        setTimeout(FastLoading, flInterval);
                                    }
                                    FastLoading();
                                }

                                """,
                        value -> {
                        });
                super.onPageStarted(view, url, favicon);
            }

            // 设置 WebViewClient，监听页面加载完成事件
            @Override
            public void onPageFinished(WebView view, String url) {
                if (Objects.equals(url, "about:blank")) {
                    return;
                }
                final int fin = (view == webView0) ? webView0Index : webView1Index;
                view.evaluateJavascript(
                        """

                                     var autoFullscreenStart = Date.now();
                                     function EnforceVolume(){
                                         var vs = document.querySelectorAll('video');
                                         for (var i=0;i<vs.length;i++){
                                             try { vs[i].muted = false; vs[i].volume = 1; } catch(e){}
                                         }
                                     }
                                     function AutoFullscreen(){
                                         var fullscreenBtn = document.querySelector('#player_pagefullscreen_yes_player')||document.querySelector('.videoFull');
                                         if(fullscreenBtn!=null){
                                          fullscreenBtn.click();
                                          EnforceVolume();
                                          var volEnd = Date.now() + 8000;
                                          (function keepVolume(){
                                             EnforceVolume();
                                             if (Date.now() < volEnd) setTimeout(keepVolume, 500);
                                          })();
                                         }else if(Date.now() - autoFullscreenStart < 10000){
                                             setTimeout(
                                                ()=>{ AutoFullscreen();}
                                            ,100);
                                         }
                                     }
                                AutoFullscreen()
                                """,
                        value -> {
                        });
                // 等视频真正出画再撤遮罩
                startVideoReadyWatch(view, fin);
            }
        });

        webView1.setWebViewClient(new WebViewClient() {
            // X5内核代码
            @Override
            public void onReceivedSslError(com.tencent.smtt.sdk.WebView webView,
                    com.tencent.smtt.export.external.interfaces.SslErrorHandler handler,
                    com.tencent.smtt.export.external.interfaces.SslError error) {
                handler.proceed(); // 忽略 SSL 错误
            }

            // 系统Webview内核代码
            // @Override
            // public void onReceivedSslError(WebView view, SslErrorHandler handler,
            // SslError error) {
            // handler.proceed(); // 忽略 SSL 错误
            // }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // 页面加载时执行 JavaScript 脚本
                view.evaluateJavascript(
                        """
                                // 低频清理 + 退出上限，避免 4ms 空转抢占 CPU（issue #140）
                                if (!window.__fastLoadingActive) {
                                    window.__fastLoadingActive = true;
                                    var fastLoadingStart = Date.now();
                                    function FastLoading() {
                                        const fullscreenBtn = document.querySelector('#player_pagefullscreen_yes_player') || document.querySelector('.videoFull');
                                        if (fullscreenBtn) { window.__fastLoadingActive = false; return; }
                                        if (Date.now() - fastLoadingStart > 10000) { window.__fastLoadingActive = false; return; }

                                        // 清空特定的脚本 src 属性
                                        const scriptKeywords = ['login', 'index', 'daohang', 'grey', 'jquery'];
                                        Array.from(document.getElementsByTagName('script')).forEach(script => {
                                            if (scriptKeywords.some(keyword => script.src.includes(keyword))) {
                                                script.src = '';
                                            }
                                        });

                                        // 清空具有特定 class 的 div 内容
                                        const classNames = ['newmap', 'newtopbz', 'newtopbzTV', 'column_wrapper'];
                                        classNames.forEach(className => {
                                            Array.from(document.getElementsByClassName(className)).forEach(div => {
                                                div.innerHTML = '';
                                            });
                                        });

                                        var flInterval = (Date.now() - fastLoadingStart < 2000) ? 100 : 500;
                                        setTimeout(FastLoading, flInterval);
                                    }
                                    FastLoading();
                                }

                                """,
                        value -> {
                        });
                super.onPageStarted(view, url, favicon);
            }

            // 设置 WebViewClient，监听页面加载完成事件
            @Override
            public void onPageFinished(WebView view, String url) {
                if (Objects.equals(url, "about:blank")) {
                    return;
                }
                final int fin = (view == webView0) ? webView0Index : webView1Index;
                view.evaluateJavascript(
                        """

                                     var autoFullscreenStart = Date.now();
                                     function EnforceVolume(){
                                         var vs = document.querySelectorAll('video');
                                         for (var i=0;i<vs.length;i++){
                                             try { vs[i].muted = false; vs[i].volume = 1; } catch(e){}
                                         }
                                     }
                                     function AutoFullscreen(){
                                         var fullscreenBtn = document.querySelector('#player_pagefullscreen_yes_player')||document.querySelector('.videoFull');
                                         if(fullscreenBtn!=null){
                                          fullscreenBtn.click();
                                          EnforceVolume();
                                          var volEnd = Date.now() + 8000;
                                          (function keepVolume(){
                                             EnforceVolume();
                                             if (Date.now() < volEnd) setTimeout(keepVolume, 500);
                                          })();
                                         }else if(Date.now() - autoFullscreenStart < 10000){
                                             setTimeout(
                                                ()=>{ AutoFullscreen();}
                                            ,100);
                                         }
                                     }
                                AutoFullscreen()
                                """,
                        value -> {
                        });
                // 等视频真正出画再撤遮罩
                startVideoReadyWatch(view, fin);
            }
        });

        // 禁用缩放
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings1.setSupportZoom(false);
        webSettings1.setBuiltInZoomControls(false);
        webSettings1.setDisplayZoomControls(false);

        // 在 Android TV 上，需要禁用焦点自动导航
        webView0.setFocusable(false);
        webView1.setFocusable(false);

        // 设置 WebView 客户端
        webView0.setWebChromeClient(new WebChromeClient());
        webView1.setWebChromeClient(new WebChromeClient());

        // 按照设置关闭双缓冲
        if (!enableDualWebView) {
            webView0.destroy();
            webView0 = null;
        }

        // 仅 debug 构建开启 WebView 远程调试
        enableWebViewDebuggingIfDebuggable();

        // 监听设置变化，实时生效（无需重启）
        prefListener = (prefs, key) -> {
            if ("text_size".equals(key)) {
                TEXT_SIZE = mapTextSizeOption(prefs.getString("text_size", "1"));
                applyTextSizes();
            } else if ("direct_channel_change".equals(key)) {
                enableDirectChannelChange = prefs.getBoolean(key, true);
            } else if ("load_images".equals(key)) {
                applyImageSetting(prefs.getBoolean(key, true));
            } else if ("theme_color".equals(key)) {
                accentColor = parseColor(prefs.getString("theme_color", "#FF5DA9E9"));
                applyThemeColor();
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(prefListener);

        // 加载上次保存的位置
        loadLastLiveIndex();

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_SHOW_UI);
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_SHOW_UI);
            } else if (menuOverlay.hasFocus()) {
                // menuOverlay具有焦点
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_B) {
                    // 按下返回键
                    showMenuOverlay();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT
                        || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    // 方向键,切换五个按钮选择
                    if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (menuOverlaySelectedIndex == 0) {
                            menuOverlaySelectedIndex = 4;
                        } else {
                            menuOverlaySelectedIndex--;
                        }
                    } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        if (menuOverlaySelectedIndex == 4) {
                            menuOverlaySelectedIndex = 0;
                        } else {
                            menuOverlaySelectedIndex++;
                        }
                    }
                    menuOverlay.getChildAt(menuOverlaySelectedIndex).requestFocus();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    // 中间键,执行按钮操作
                    switch (menuOverlaySelectedIndex) {
                        case 1:
                            // 刷新页面
                            getCurrentWebview().reload();
                            showMenuOverlay();
                            break;
                        case 2:
                            // 播放
                            if (!usesYsp(currentLiveIndex)) {
                                simulateTouch(getCurrentWebview(), 0.5f, 0.5f);
                            } else {
                                String scriptPause = """
                                        try{
                                        document.querySelector('.play.play2').click();
                                        } catch(e) {
                                        document.querySelector('.play.play1').click();
                                        }
                                        """;
                                getCurrentWebview().evaluateJavascript(scriptPause, null);
                            }
                            showMenuOverlay();
                            break;
                        case 3:
                            // 切换全屏
                            String script1 = """
                                    console.log('点击全屏按钮');
                                    document.querySelector('#player_pagefullscreen_yes_player').click();
                                    """;

                            String script2 = """
                                    console.log('点击全屏按钮');
                                    if(document.querySelector('.videoFull').id == ''){
                                        document.querySelector('.videoFull').click();
                                    }else{
                                        document.querySelector('.videoFull_ac').click();
                                    }
                                    """;

                            if (!usesYsp(currentLiveIndex)) {
                                getCurrentWebview().evaluateJavascript(script1, null);
                            } else {
                                new Handler().postDelayed(() -> {
                                    if (activityDestroyed) {
                                        return;
                                    }
                                    getCurrentWebview().evaluateJavascript(script2, null);
                                }, 500);
                            }
                            break;
                        case 0:
                            // 切换播放源（央视网 / 央视频），按台记忆
                            if (hasBothSources(currentLiveIndex)) {
                                toggleCurrentSource();
                                showMenuOverlay();
                            } else {
                                Toast.makeText(this, "当前频道仅一个源", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 4:
                            // 静音切换
                            toggleMute();
                            showMenuOverlay();
                            break;
                    }
                    return true;
                }
                return true;
            }
            if (DrawerLayout.hasFocus() && !SubMenuCCTV.hasFocus() && !SubMenuLocal.hasFocus()
                    && !DrawerLayoutDetailed.hasFocus()) {
                // DrawerLayout具有焦点
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT
                        || event.getKeyCode() == KeyEvent.KEYCODE_B) {
                    // 按下返回键
                    showChannelList();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP
                        || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                    // 方向键,切换频道选择
                    if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                        if (DrawerLayoutSelectedIndex == 0) {
                            DrawerLayoutSelectedIndex = 3;
                        } else {
                            DrawerLayoutSelectedIndex--;
                        }
                    } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (DrawerLayoutSelectedIndex == 3) {
                            DrawerLayoutSelectedIndex = 0;
                        } else {
                            DrawerLayoutSelectedIndex++;
                        }
                    }
                    DrawerLayout.getChildAt(DrawerLayoutSelectedIndex).requestFocus();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    // 中间键,执行按钮操作
                    switch (DrawerLayoutSelectedIndex) {
                        case 0:
                            // 中央频道
                            DrawerLayoutDetailed.setVisibility(View.VISIBLE);
                            findViewById(R.id.subMenuCCTV).setVisibility(View.VISIBLE);
                            findViewById(R.id.CCTVScroll).setVisibility(View.VISIBLE);
                            findViewById(R.id.subMenuLocal).setVisibility(View.GONE);
                            findViewById(R.id.LocalScroll).setVisibility(View.GONE);
                            SubMenuCCTV.getChildAt(SubMenuCCTVSelectedIndex).requestFocus();
                            activeCategory = 0;
                            refreshTopSelection();
                            refreshChannelSelection();
                            setEpgColumnsVisible(true);
                            populateEpg(SubMenuCCTVSelectedIndex);
                            fetchSubtitles();
                            break;
                        case 1:
                            // 地方频道
                            DrawerLayoutDetailed.setVisibility(View.VISIBLE);
                            findViewById(R.id.subMenuCCTV).setVisibility(View.GONE);
                            findViewById(R.id.CCTVScroll).setVisibility(View.GONE);
                            findViewById(R.id.subMenuLocal).setVisibility(View.VISIBLE);
                            findViewById(R.id.LocalScroll).setVisibility(View.VISIBLE);
                            SubMenuLocal.getChildAt(SubMenuLocalSelectedIndex).requestFocus();
                            activeCategory = 1;
                            refreshTopSelection();
                            refreshChannelSelection();
                            setEpgColumnsVisible(true);
                            populateEpg(SubMenuLocalSelectedIndex + 20);
                            fetchSubtitles();
                            break;
                        case 2:
                            // 网页菜单：拉起底部功能菜单
                            showChannelList();
                            showMenuOverlay();
                            break;
                        case 3:
                            // 设置：直接进入设置界面
                            showChannelList();
                            startActivity(new Intent(this, SettingsActivity.class));
                            break;
                    }
                    return true;
                }
            } else if (SubMenuCCTV.hasFocus()) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_B) {
                    // 返回键直接关闭抽屉
                    showChannelList();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                    DrawerLayout.getChildAt(DrawerLayoutSelectedIndex).requestFocus();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP
                        || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                    // 方向键,切换频道选择
                    if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                        if (SubMenuCCTVSelectedIndex == 0) {
                            SubMenuCCTVSelectedIndex = 19;
                        } else {
                            SubMenuCCTVSelectedIndex--;
                        }
                    } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (SubMenuCCTVSelectedIndex == 19) {
                            SubMenuCCTVSelectedIndex = 0;
                        } else {
                            SubMenuCCTVSelectedIndex++;
                        }
                    }
                    SubMenuCCTV.getChildAt(SubMenuCCTVSelectedIndex).requestFocus();
                    populateEpg(SubMenuCCTVSelectedIndex);
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    // 确认键换台
                    currentLiveIndex = SubMenuCCTVSelectedIndex;
                    loadLiveUrl();
                    saveCurrentLiveIndex();
                    showChannelList();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    // 右键进入 EPG 日期列
                    enterDateColumn();
                    return true;
                }
            } else if (SubMenuLocal.hasFocus()) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_B) {
                    // 返回键直接关闭抽屉
                    showChannelList();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                    DrawerLayout.getChildAt(DrawerLayoutSelectedIndex).requestFocus();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP
                        || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                    // 方向键,切换频道选择
                    if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                        if (SubMenuLocalSelectedIndex == 0) {
                            SubMenuLocalSelectedIndex = 20;
                        } else {
                            SubMenuLocalSelectedIndex--;
                        }
                    } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (SubMenuLocalSelectedIndex == 20) {
                            SubMenuLocalSelectedIndex = 0;
                        } else {
                            SubMenuLocalSelectedIndex++;
                        }
                    }
                    SubMenuLocal.getChildAt(SubMenuLocalSelectedIndex).requestFocus();
                    populateEpg(SubMenuLocalSelectedIndex + 20);
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    // 确认键换台
                    currentLiveIndex = SubMenuLocalSelectedIndex + 20;
                    loadLiveUrl();
                    saveCurrentLiveIndex();
                    showChannelList();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    // 右键进入 EPG 日期列
                    enterDateColumn();
                    return true;
                }
            } else if (DrawerLayoutDetailed.hasFocus()) {
                return true;
            } else if (EpgDateList.hasFocus()) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_B) {
                    showChannelList();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                    focusChannelFromEpg();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP
                        || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                    if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                        epgDateSelectedIndex = (epgDateSelectedIndex - 1 + EPG_DAY_COUNT) % EPG_DAY_COUNT;
                    } else {
                        epgDateSelectedIndex = (epgDateSelectedIndex + 1) % EPG_DAY_COUNT;
                    }
                    epgDateOffset = epgDateSelectedIndex - EPG_DAY_PAST;
                    EpgDateList.getChildAt(epgDateSelectedIndex).requestFocus();
                    refreshDateSelection();
                    populateEpg(epgChannelIndex);
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT
                        || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    enterScheduleColumn();
                    return true;
                }
                return true;
            } else if (EpgScheduleList.hasFocus()) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_B) {
                    showChannelList();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                    enterDateColumn();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP
                        || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                    int count = EpgScheduleList.getChildCount();
                    if (count > 0) {
                        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                            epgScheduleSelectedIndex = (epgScheduleSelectedIndex - 1 + count) % count;
                        } else {
                            epgScheduleSelectedIndex = (epgScheduleSelectedIndex + 1) % count;
                        }
                        View v = EpgScheduleList.getChildAt(epgScheduleSelectedIndex);
                        if (v != null) {
                            v.requestFocus();
                        }
                    }
                    return true;
                }
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                // 执行上一个直播地址的操作
                navigateToPreviousLive();
                return true; // 返回 true 表示事件已处理，不传递给 WebView
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                // 执行下一个直播地址的操作
                navigateToNextLive();
                return true; // 返回 true 表示事件已处理，不传递给 WebView
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                // 换台菜单（加载遮罩期间也可打开）
                showChannelList();
                return true; // 返回 true 表示事件已处理，不传递给 WebView
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_MENU || event.getKeyCode() == KeyEvent.KEYCODE_M) {
                // 显示菜单（加载遮罩期间也可打开）
                showMenuOverlay();

                return true; // 返回 true 表示事件已处理，不传递给 WebView
            } else if ((event.getKeyCode() >= KeyEvent.KEYCODE_0 && event.getKeyCode() <= KeyEvent.KEYCODE_9)
                    && !isChanging) {
                int numericKey = event.getKeyCode() - KeyEvent.KEYCODE_0;

                // 将按下的数字键追加到缓冲区
                digitBuffer.append(numericKey);

                // 使用 Handler 来在超时后处理输入的数字
                new Handler().postDelayed(() -> handleNumericInput(), DIGIT_TIMEOUT);

                // 更新显示正在输入的数字的 TextView
                updateInputTextView();

                return true; // 事件已处理，不传递给 WebView
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                // 观看时左右键调整音量（左减）
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_SHOW_UI);
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                // 右加
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_SHOW_UI);
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_B) {
                // 浮层显示时，返回键先关掉浮层
                if (epgOverlay.getVisibility() == View.VISIBLE) {
                    handler.removeCallbacks(hideOverlayRunnable);
                    epgOverlay.setVisibility(View.GONE);
                    return true;
                }
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    System.exit(0);
                    return true;
                }

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "再按一次返回键退出应用", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
                // 如果两秒内再次按返回键，则退出应用
            }
        }
        return true; // 不处理其余事件
    }

    // 显示底部菜单
    private void showMenuOverlay() {
        if (!isMenuOverlayVisible) {
            updateSourcePill();
            updateMutePill();
            menuOverlay.getChildAt(menuOverlaySelectedIndex).requestFocus();
            menuOverlay.setVisibility(View.VISIBLE);
            isMenuOverlayVisible = true;
        } else {
            menuOverlay.setVisibility(View.GONE);
            menuOverlaySelectedIndex = 0;
            isMenuOverlayVisible = false;
        }
    }

    // 频道选择列表
    private void showChannelList() {
        // 显示频道抽屉
        if (!isDrawerOverlayVisible) {
            DrawerLayoutDetailed.setVisibility(View.VISIBLE);
            DrawerLayout.setVisibility(View.VISIBLE);
            isDrawerOverlayVisible = true;
            if (currentLiveIndex < 20) {
                SubMenuCCTV.setVisibility(View.VISIBLE);
                findViewById(R.id.CCTVScroll).setVisibility(View.VISIBLE);
                SubMenuCCTV.getChildAt(currentLiveIndex).requestFocus();
                SubMenuCCTVSelectedIndex = currentLiveIndex;
                DrawerLayoutSelectedIndex = 0;
            } else {
                SubMenuLocal.setVisibility(View.VISIBLE);
                findViewById(R.id.LocalScroll).setVisibility(View.VISIBLE);
                SubMenuLocal.getChildAt(currentLiveIndex - 20).requestFocus();
                SubMenuLocalSelectedIndex = currentLiveIndex - 20;
                DrawerLayoutSelectedIndex = 1;
            }
            buildDateColumn();
            activeCategory = DrawerLayoutSelectedIndex == 0 ? 0 : 1;
            refreshTopSelection();
            refreshChannelSelection();
            setEpgColumnsVisible(true);
            int highlighted = DrawerLayoutSelectedIndex == 0 ? SubMenuCCTVSelectedIndex : SubMenuLocalSelectedIndex + 20;
            populateEpg(highlighted);
            fetchSubtitles();
        } else {
            DrawerLayout.setVisibility(View.GONE);
            SubMenuCCTV.setVisibility(View.GONE);
            SubMenuLocal.setVisibility(View.GONE);
            findViewById(R.id.LocalScroll).setVisibility(View.GONE);
            findViewById(R.id.CCTVScroll).setVisibility(View.GONE);
            DrawerLayoutDetailed.setVisibility(View.GONE);
            setEpgColumnsVisible(false);
            isDrawerOverlayVisible = false;
        }
    }

    private void showChannelListDPAD(int selectIndex) {
        // 显示频道抽屉
        if (!isDrawerOverlayVisible) {
            DrawerLayoutDetailed.setVisibility(View.VISIBLE);
            DrawerLayout.setVisibility(View.VISIBLE);
            isDrawerOverlayVisible = true;
            if (selectIndex < 20) {
                SubMenuCCTV.setVisibility(View.VISIBLE);
                findViewById(R.id.CCTVScroll).setVisibility(View.VISIBLE);
                SubMenuCCTV.getChildAt(selectIndex).requestFocus();
                SubMenuCCTVSelectedIndex = selectIndex;
                DrawerLayoutSelectedIndex = 0;
            } else {
                SubMenuLocal.setVisibility(View.VISIBLE);
                findViewById(R.id.LocalScroll).setVisibility(View.VISIBLE);
                SubMenuLocal.getChildAt(selectIndex - 20).requestFocus();
                SubMenuLocalSelectedIndex = selectIndex - 20;
                DrawerLayoutSelectedIndex = 1;
            }
            buildDateColumn();
            activeCategory = DrawerLayoutSelectedIndex == 0 ? 0 : 1;
            refreshTopSelection();
            refreshChannelSelection();
            setEpgColumnsVisible(true);
            int highlighted = DrawerLayoutSelectedIndex == 0 ? SubMenuCCTVSelectedIndex : SubMenuLocalSelectedIndex + 20;
            populateEpg(highlighted);
            fetchSubtitles();
        } else {
            DrawerLayout.setVisibility(View.GONE);
            SubMenuCCTV.setVisibility(View.GONE);
            SubMenuLocal.setVisibility(View.GONE);
            findViewById(R.id.LocalScroll).setVisibility(View.GONE);
            findViewById(R.id.CCTVScroll).setVisibility(View.GONE);
            DrawerLayoutDetailed.setVisibility(View.GONE);
            setEpgColumnsVisible(false);
            isDrawerOverlayVisible = false;
        }
    }

    // 频道项：名称一行 + 当前节目副标题一行（过长横向滚动）
    private LinearLayout buildChannelItem(String name) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setTag(name);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(4, 4, 4, 4);
        item.setLayoutParams(lp);
        item.setPadding(16, 12, 16, 12);
        item.setBackground(channelItemBackground());
        item.setFocusable(true);

        TextView nameView = new TextView(this);
        nameView.setText(name);
        nameView.setTextColor(getResources().getColor(android.R.color.white));
        nameView.setSingleLine(true);
        nameView.setEllipsize(TextUtils.TruncateAt.END);
        setTvTextSize(nameView, TEXT_SIZE);
        item.addView(nameView);

        TextView subView = new TextView(this);
        subView.setTextColor(getResources().getColor(android.R.color.white));
        setTvTextSize(subView, TEXT_SIZE - 4);
        subView.setSingleLine(true);
        subView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        subView.setMarqueeRepeatLimit(-1);
        subView.setHorizontallyScrolling(true);
        subView.setSelected(true);
        subView.setTag("subtitle");
        item.addView(subView);
        return item;
    }

    // 构建日期列（今天-6 .. 明天+1）
    private void buildDateColumn() {
        EpgDateList.removeAllViews();
        for (int i = 0; i < EPG_DAY_COUNT; i++) {
            int offset = i - EPG_DAY_PAST;
            Button b = new Button(this);
            b.setText(EpgRepository.dateWeekLabel(offset) + "\n" + EpgRepository.dateMdLabel(offset));
            b.setTag(offset);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(4, 4, 4, 4);
            b.setLayoutParams(lp);
            b.setPadding(16, 12, 16, 12);
            b.setTextColor(getResources().getColor(android.R.color.white));
            b.setBackground(channelItemBackground());
            setTvTextSize(b, TEXT_SIZE);
            b.setFocusable(true);
            EpgDateList.addView(b);
        }
        epgDateOffset = 0;
        epgDateSelectedIndex = EPG_DAY_PAST;
        refreshDateSelection();
    }

    // 按当前选中日期填充某频道的节目表
    private void populateEpg(final int channelIndex) {
        epgChannelIndex = channelIndex;
        final int offset = epgDateOffset;
        final String date = EpgRepository.dateKey(offset);
        renderSchedule(epgRepo.cached(channelIndex, date));
        epgRepo.fetch(channelIndex, date, epg -> {
            if (epgChannelIndex == channelIndex && epgDateOffset == offset) {
                renderSchedule(epg);
            }
        });
    }

    private void renderSchedule(EpgRepository.ChannelEpg epg) {
        epgScheduleSelectedIndex = 0;
        if (epg == null || epg.programs.isEmpty()) {
            EpgScheduleList.removeAllViews();
            TextView empty = new TextView(this);
            empty.setText("暂无节目信息");
            empty.setTextColor(getResources().getColor(android.R.color.white));
            setTvTextSize(empty, TEXT_SIZE);
            empty.setPadding(16, 16, 16, 16);
            empty.setFocusable(true);
            empty.setTag(-1);
            EpgScheduleList.addView(empty);
            return;
        }
        int n = epg.programs.size();
        int curIdx = EpgRepository.dateKey(0).equals(epg.date) ? epg.currentIndex() : -1;
        // 清掉上次的空态占位
        if (EpgScheduleList.getChildCount() > 0) {
            Object t0 = EpgScheduleList.getChildAt(0).getTag();
            if (t0 instanceof Integer && (Integer) t0 == -1) {
                EpgScheduleList.removeAllViews();
            }
        }
        for (int i = 0; i < n; i++) {
            EpgRepository.Program p = epg.programs.get(i);
            TextView row;
            if (i < EpgScheduleList.getChildCount()) {
                row = (TextView) EpgScheduleList.getChildAt(i); // 复用已有行
            } else {
                row = createScheduleRow();
                EpgScheduleList.addView(row);
            }
            row.setText(p.showTime + "  " + p.title);
            int kind = (i == curIdx) ? 1 : 0;
            Object tag = row.getTag();
            if (!(tag instanceof Integer) || (Integer) tag != kind) {
                row.setBackground(kind == 1 ? focusBackground(6, 0xE6000000) : channelItemBackground());
                row.setTag(kind);
            }
            row.setSelected(kind == 1 || row.hasFocus());
        }
        while (EpgScheduleList.getChildCount() > n) {
            EpgScheduleList.removeViewAt(EpgScheduleList.getChildCount() - 1);
        }
        if (curIdx >= 0) {
            epgScheduleSelectedIndex = curIdx;
        }
        final int scrollIdx = epgScheduleSelectedIndex;
        EpgScheduleScroll.post(() -> {
            View v = EpgScheduleList.getChildAt(scrollIdx);
            if (v != null) {
                EpgScheduleScroll.smoothScrollTo(0, v.getTop());
            }
        });
    }

    private TextView createScheduleRow() {
        TextView row = new TextView(this);
        setTvTextSize(row, TEXT_SIZE);
        row.setTextColor(getResources().getColor(android.R.color.white));
        row.setFocusable(true);
        row.setSingleLine(true);
        row.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        row.setMarqueeRepeatLimit(-1);
        row.setHorizontallyScrolling(true);
        row.setPadding(16, 12, 16, 16);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(4, 4, 4, 4);
        row.setLayoutParams(lp);
        row.setBackground(channelItemBackground());
        row.setTag(0);
        row.setOnFocusChangeListener((v, hasFocus) -> {
            Object t = v.getTag();
            boolean cur = t instanceof Integer && (Integer) t == 1;
            v.setSelected(hasFocus || cur);
        });
        return row;
    }

    // 为当前子菜单的所有频道拉取当前节目，作为按钮副标题（无当前则取前一天最后一个节目）
    private void fetchSubtitles() {
        final String today = EpgRepository.dateKey(0);
        final String yesterday = EpgRepository.dateKey(-1);
        int start = epgChannelIndex < 20 ? 0 : 20;
        int end = epgChannelIndex < 20 ? 20 : liveUrls.length;
        for (int i = start; i < end; i++) {
            final int idx = i;
            epgRepo.fetch(idx, today, epg -> {
                EpgRepository.Program cur = epg == null ? null : epg.current();
                if (cur != null && cur.title != null && !cur.title.isEmpty()) {
                    updateChannelSubtitle(idx, cur.title);
                    return;
                }
                final String live = epg == null ? null : epg.isLive;
                epgRepo.fetch(idx, yesterday, prev -> {
                    String title = null;
                    if (prev != null && !prev.programs.isEmpty()) {
                        title = prev.programs.get(prev.programs.size() - 1).title;
                    } else if (live != null && !live.isEmpty()) {
                        title = live;
                    }
                    if (title != null && !title.isEmpty()) {
                        updateChannelSubtitle(idx, title);
                    }
                });
            });
        }
    }

    private void updateChannelSubtitle(int globalIndex, String live) {
        if (live == null || live.isEmpty()) {
            return;
        }
        LinearLayout item = globalIndex < 20
                ? (LinearLayout) SubMenuCCTV.getChildAt(globalIndex)
                : (LinearLayout) SubMenuLocal.getChildAt(globalIndex - 20);
        if (item == null) {
            return;
        }
        TextView sub = (TextView) item.findViewWithTag("subtitle");
        if (sub != null) {
            sub.setText(live);
        }
    }

    private void enterDateColumn() {
        epgDateSelectedIndex = Math.max(0, Math.min(EPG_DAY_COUNT - 1, epgDateOffset + EPG_DAY_PAST));
        View v = EpgDateList.getChildAt(epgDateSelectedIndex);
        if (v != null) {
            v.requestFocus();
        }
    }

    private void enterScheduleColumn() {
        if (epgScheduleSelectedIndex < 0 || epgScheduleSelectedIndex >= EpgScheduleList.getChildCount()) {
            epgScheduleSelectedIndex = 0;
        }
        View v = EpgScheduleList.getChildAt(epgScheduleSelectedIndex);
        if (v != null) {
            v.requestFocus();
        }
    }

    private void focusChannelFromEpg() {
        if (epgChannelIndex < 20) {
            SubMenuCCTV.getChildAt(SubMenuCCTVSelectedIndex).requestFocus();
        } else {
            SubMenuLocal.getChildAt(SubMenuLocalSelectedIndex).requestFocus();
        }
    }

    // 字号按屏幕高度换算成 px（1080p 基准），保证 1080p/4K/模拟器比例一致
    private void setTvTextSize(TextView tv, float base) {
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, base * uiScale);
    }

    private int mapTextSizeOption(String option) {
        switch (option) {
            case "0":
                return 18;
            case "2":
                return 25;
            case "3":
                return 30;
            default:
                return 22;
        }
    }

    // 字体大小改变后，实时重设已创建的各个菜单/节目单文字
    private void applyTextSizes() {
        if (menuOverlay == null) {
            return;
        }
        LinearLayout first = findViewById(R.id.DrawerLayout);
        for (int i = 0; i < first.getChildCount(); i++) {
            View v = first.getChildAt(i);
            if (v instanceof Button) {
                setTvTextSize((Button) v, TEXT_SIZE);
            }
        }
        applyChannelColTextSize((LinearLayout) findViewById(R.id.subMenuCCTV));
        applyChannelColTextSize((LinearLayout) findViewById(R.id.subMenuLocal));
        LinearLayout dates = findViewById(R.id.EpgDateList);
        for (int i = 0; i < dates.getChildCount(); i++) {
            setTvTextSize((TextView) dates.getChildAt(i), TEXT_SIZE);
        }
        LinearLayout sched = findViewById(R.id.EpgScheduleList);
        for (int i = 0; i < sched.getChildCount(); i++) {
            setTvTextSize((TextView) sched.getChildAt(i), TEXT_SIZE);
        }
        for (int i = 0; i < menuOverlay.getChildCount(); i++) {
            LinearLayout pill = (LinearLayout) menuOverlay.getChildAt(i);
            pill.setBackground(pillBackground());
            setTvTextSize((TextView) pill.getChildAt(1), 18);
        }
    }

    private void applyImageSetting(boolean on) {
        if (enableDualWebView && webView0 != null) {
            webView0.getSettings().setLoadsImagesAutomatically(on);
            webView0.getSettings().setBlockNetworkImage(!on);
        }
        if (webView1 != null) {
            webView1.getSettings().setLoadsImagesAutomatically(on);
            webView1.getSettings().setBlockNetworkImage(!on);
        }
    }

    private int parseColor(String s) {
        try {
            return Color.parseColor(s);
        } catch (Exception e) {
            return 0xFF5DA9E9;
        }
    }

    private int dpToPx(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    // 「聚焦=主题色实心 / 选中=深底+主题色描边 / 默认=深色」圆角背景
    private Drawable focusBackground(int radiusDp, int normalColor) {
        StateListDrawable sld = new StateListDrawable();
        GradientDrawable focused = new GradientDrawable();
        focused.setCornerRadius(dpToPx(radiusDp));
        focused.setColor(accentColor);
        GradientDrawable selected = new GradientDrawable();
        selected.setCornerRadius(dpToPx(radiusDp));
        selected.setColor(normalColor);
        selected.setStroke(dpToPx(2), accentColor);
        GradientDrawable normal = new GradientDrawable();
        normal.setCornerRadius(dpToPx(radiusDp));
        normal.setColor(normalColor);
        sld.addState(new int[]{android.R.attr.state_focused}, focused);
        sld.addState(new int[]{android.R.attr.state_selected}, selected);
        sld.addState(new int[]{}, normal);
        return sld;
    }

    // 频道列：当前正在播放的频道标记为选中
    private void refreshChannelSelection() {
        LinearLayout cctvCol = findViewById(R.id.subMenuCCTV);
        for (int i = 0; i < cctvCol.getChildCount(); i++) {
            cctvCol.getChildAt(i).setSelected(i == currentLiveIndex);
        }
        LinearLayout localCol = findViewById(R.id.subMenuLocal);
        for (int i = 0; i < localCol.getChildCount(); i++) {
            localCol.getChildAt(i).setSelected((i + 20) == currentLiveIndex);
        }
    }

    // 一级菜单：当前展开的类别标记为选中
    private void refreshTopSelection() {
        LinearLayout first = findViewById(R.id.DrawerLayout);
        for (int i = 0; i < first.getChildCount(); i++) {
            first.getChildAt(i).setSelected(i == activeCategory && i < 2);
        }
    }

    private void refreshDateSelection() {
        for (int i = 0; i < EpgDateList.getChildCount(); i++) {
            EpgDateList.getChildAt(i).setSelected(i == epgDateSelectedIndex);
        }
    }

    private Drawable channelItemBackground() {
        return focusBackground(6, 0xFF333333);
    }

    private Drawable pillBackground() {
        return focusBackground(32, 0xFF333333);
    }

    private void applyProgressTint() {
        if (overlayProgress != null && overlayProgress.getProgressDrawable() != null) {
            overlayProgress.getProgressDrawable().setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        }
    }

    private void applyChannelBackground(LinearLayout col) {
        for (int i = 0; i < col.getChildCount(); i++) {
            col.getChildAt(i).setBackground(channelItemBackground());
        }
    }

    // 主题色变更后，重设所有已创建控件的焦点背景与进度条颜色
    private void applyThemeColor() {
        LinearLayout first = findViewById(R.id.DrawerLayout);
        for (int i = 0; i < first.getChildCount(); i++) {
            View v = first.getChildAt(i);
            if (v instanceof Button) {
                v.setBackground(channelItemBackground());
            }
        }
        applyChannelBackground((LinearLayout) findViewById(R.id.subMenuCCTV));
        applyChannelBackground((LinearLayout) findViewById(R.id.subMenuLocal));
        LinearLayout dates = findViewById(R.id.EpgDateList);
        for (int i = 0; i < dates.getChildCount(); i++) {
            dates.getChildAt(i).setBackground(channelItemBackground());
        }
        if (menuOverlay != null) {
            for (int i = 0; i < menuOverlay.getChildCount(); i++) {
                menuOverlay.getChildAt(i).setBackground(pillBackground());
            }
        }
        applyProgressTint();
        // 清掉行标记，强制按新主题色重建节目表背景
        for (int i = 0; i < EpgScheduleList.getChildCount(); i++) {
            EpgScheduleList.getChildAt(i).setTag(null);
        }
        if (epgChannelIndex >= 0 && epgChannelIndex < liveUrls.length) {
            populateEpg(epgChannelIndex);
        }
    }

    private void applyChannelColTextSize(LinearLayout col) {
        for (int i = 0; i < col.getChildCount(); i++) {
            LinearLayout item = (LinearLayout) col.getChildAt(i);
            setTvTextSize((TextView) item.getChildAt(0), TEXT_SIZE);
            setTvTextSize((TextView) item.getChildAt(1), TEXT_SIZE - 4);
        }
    }

    private void setEpgColumnsVisible(boolean visible) {
        int v = visible ? View.VISIBLE : View.GONE;
        EpgDateScroll.setVisibility(v);
        EpgScheduleScroll.setVisibility(v);
    }

    private void handleNumericInput() {
        // 将缓冲区中的数字转换为整数
        if (digitBuffer.length() > 0) {
            int numericValue = Integer.parseInt(digitBuffer.toString());

            // 检查数字是否在有效范围内
            if (numericValue > 0 && numericValue <= liveUrls.length) {
                currentLiveIndex = numericValue - 1;
                loadLiveUrl();
                saveCurrentLiveIndex(); // 保存当前位置
            }

            // 重置缓冲区
            digitBuffer.setLength(0);

            // 取消显示正在输入的数字
            inputTextView.setVisibility(View.INVISIBLE);
        }
    }

    private void updateInputTextView() {
        // 在 TextView 中显示当前正在输入的数字
        inputTextView.setVisibility(View.VISIBLE);
        inputTextView.setText("换台：" + digitBuffer.toString());
    }

    private void loadLastLiveIndex() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        currentLiveIndex = preferences.getInt(PREF_KEY_LIVE_INDEX, 0); // 默认值为0
        loadLiveUrl(); // 加载上次保存的位置的直播地址
    }

    private void saveCurrentLiveIndex() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_KEY_LIVE_INDEX, currentLiveIndex);
        editor.apply();
    }

    private WebView getCurrentWebview() {
        if (!enableDualWebView) {
            return webView1;
        }
        if (currentWebView == 0) {
            return webView0;
        } else {
            return webView1;
        }
    }

    private void loadLiveUrl() {
        if (activityDestroyed) {
            return;
        }
        if (currentLiveIndex >= 0 && currentLiveIndex < liveUrls.length) {
            // 主提示：正在切换到 XX
            String name = channelNames[currentLiveIndex].replaceFirst("^\\d+\\s*", "");
            String sourceLabel = usesYsp(currentLiveIndex) ? "央视频" : "央视网";
            ((TextView) findViewById(R.id.loadingText)).setText("正在切换到 " + name + "（" + sourceLabel + "）");
            // 收起可能残留的 EPG 浮层，避免透出
            handler.removeCallbacks(hideOverlayRunnable);
            epgOverlay.setVisibility(View.GONE);
            cancelVideoPoll();
            // 显示加载的View
            loadingOverlay.setVisibility(View.VISIBLE);

            isChanging = true;

            // 预取当前台及相邻台的节目单，换台后浮层可立即显示（缓存命中）
            String epgToday = EpgRepository.dateKey(0);
            int total = liveUrls.length;
            epgRepo.fetch(currentLiveIndex, epgToday, epg -> { });
            epgRepo.fetch((currentLiveIndex + 1) % total, epgToday, epg -> { });
            epgRepo.fetch((currentLiveIndex - 1 + total) % total, epgToday, epg -> { });

            if (currentWebView == 0) {
                currentWebView = 1;
            } else {
                currentWebView = 0;
            }

            if (enableDualWebView) {
                if (currentWebView == 0) {
                    webView0Index = currentLiveIndex;
                } else {
                    webView1Index = currentLiveIndex;
                }
            } else {
                webView1Index = currentLiveIndex;
            }

            getCurrentWebview().setInitialScale(getMinimumScale());
            getCurrentWebview().loadUrl(channelUrl(currentLiveIndex));
        }
    }

    // 该频道是否有央视频源（卫视恒有；央视看 pid 是否存在）
    private boolean yspAvailable(int index) {
        if (index < 0) {
            return false;
        }
        if (index >= 20) {
            return true;
        }
        return yspCctvPids != null && index < yspCctvPids.length && yspCctvPids[index] != null;
    }

    // 该频道当前是否走央视频（央视按记忆，默认央视频）
    private boolean usesYsp(int index) {
        if (index < 0) {
            return false;
        }
        if (index >= 20) {
            return true;
        }
        if (!yspAvailable(index)) {
            return false;
        }
        return "ysp".equals(PreferenceManager.getDefaultSharedPreferences(this)
                .getString("source_" + index, "ysp"));
    }

    private boolean hasBothSources(int index) {
        return index >= 0 && index < 20 && yspAvailable(index);
    }

    private String channelUrl(int index) {
        if (usesYsp(index)) {
            if (index >= 20) {
                return liveUrls[index];
            }
            return "https://www.yangshipin.cn/tv/home?pid=" + yspCctvPids[index];
        }
        return liveUrls[index];
    }

    private void toggleCurrentSource() {
        int index = currentLiveIndex;
        if (!hasBothSources(index)) {
            Toast.makeText(this, "当前频道仅一个源", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean toYsp = !usesYsp(index);
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString("source_" + index, toYsp ? "ysp" : "cctv").apply();
        updateSourcePill();
        loadLiveUrl();
    }

    // 底部菜单「切换源」按钮文案显示当前源
    private void updateSourcePill() {
        if (menuOverlay == null || menuOverlay.getChildCount() < 6) {
            return;
        }
        LinearLayout pill = (LinearLayout) menuOverlay.getChildAt(0);
        TextView label = (TextView) pill.getChildAt(1);
        if (hasBothSources(currentLiveIndex)) {
            label.setText(usesYsp(currentLiveIndex) ? "源：央视频" : "源：央视网");
        } else if (currentLiveIndex >= 20) {
            label.setText("源：央视频");
        } else {
            label.setText("源：央视网");
        }
    }

    // 系统级静音（媒体流）
    private void toggleMute() {
        boolean mutedNow;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mutedNow = audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
        } else {
            mutedNow = videoMuted;
        }
        videoMuted = !mutedNow;
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                videoMuted ? AudioManager.ADJUST_MUTE : AudioManager.ADJUST_UNMUTE,
                AudioManager.FLAG_SHOW_UI);
        updateMutePill();
    }

    private void updateMutePill() {
        if (menuOverlay == null || menuOverlay.getChildCount() < 5) {
            return;
        }
        boolean muted = videoMuted;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            muted = audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
            videoMuted = muted;
        }
        LinearLayout pill = (LinearLayout) menuOverlay.getChildAt(4);
        ((TextView) pill.getChildAt(1)).setText(muted ? "取消静音" : "静音");
    }

    private void navigateToPreviousLive() {
        int idx = (currentLiveIndex - 1 + liveUrls.length) % liveUrls.length;
        if (enableDirectChannelChange) {
            zapToChannel(idx);
        } else {
            showChannelListDPAD(idx);
        }
    }

    private void navigateToNextLive() {
        int idx = (currentLiveIndex + 1 + liveUrls.length) % liveUrls.length;
        if (enableDirectChannelChange) {
            zapToChannel(idx);
        } else {
            showChannelListDPAD(idx);
        }
    }

    // 连续换台：索引与提示立即更新，停手 300ms 后再真正加载最后选中的台
    private void zapToChannel(int idx) {
        currentLiveIndex = idx;
        saveCurrentLiveIndex();
        switchPending = true;
        isChanging = true;
        cancelVideoPoll();
        String name = channelNames[idx].replaceFirst("^\\d+\\s*", "");
        String sourceLabel = usesYsp(idx) ? "央视频" : "央视网";
        ((TextView) findViewById(R.id.loadingText)).setText("正在切换到 " + name + "（" + sourceLabel + "）");
        loadingOverlay.setVisibility(View.VISIBLE);
        epgOverlay.setVisibility(View.GONE);
        handler.removeCallbacks(hideOverlayRunnable);
        handler.removeCallbacks(pendingLoadRunnable);
        handler.postDelayed(pendingLoadRunnable, 300);
    }

    private void cancelVideoPoll() {
        if (videoPollRunnable != null) {
            handler.removeCallbacks(videoPollRunnable);
            videoPollRunnable = null;
        }
    }

    // 轮询当前 WebView 的 <video> 是否真的在播放，出画后（或超时）再撤遮罩
    private void startVideoReadyWatch(final WebView target, final int fin) {
        cancelVideoPoll();
        final long start = System.currentTimeMillis();
        videoPollRunnable = new Runnable() {
            @Override
            public void run() {
                if (activityDestroyed) {
                    return;
                }
                if (switchPending || fin != currentLiveIndex) {
                    return;
                }
                if (System.currentTimeMillis() - start > 10000) {
                    finishChannelLoad(target, fin);
                    return;
                }
                target.evaluateJavascript(
                        "var v=document.querySelector('video');(v&&v.readyState>=2&&!v.paused&&v.currentTime>0)?'true':'false'",
                        value -> {
                            if (activityDestroyed) {
                                return;
                            }
                            if (switchPending || fin != currentLiveIndex) {
                                return;
                            }
                            if (value != null && value.contains("true")) {
                                finishChannelLoad(target, fin);
                            } else {
                                handler.postDelayed(videoPollRunnable, 300);
                            }
                        });
            }
        };
        handler.postDelayed(videoPollRunnable, 300);
    }

    private void finishChannelLoad(WebView view, int fin) {
        if (activityDestroyed) {
            return;
        }
        if (switchPending || fin != currentLiveIndex) {
            return;
        }
        loadingOverlay.setVisibility(View.GONE);
        if (view == webView0) {
            webView0.setVisibility(View.VISIBLE);
            if (webView1 != null) {
                webView1.setVisibility(View.GONE);
                webView1.loadUrl("about:blank");
            }
        } else {
            webView1.setVisibility(View.VISIBLE);
            if (enableDualWebView && webView0 != null) {
                webView0.setVisibility(View.GONE);
                webView0.loadUrl("about:blank");
            }
        }
        isChanging = false;
        showEpgOverlay(currentLiveIndex);
    }

    private int getMinimumScale() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // 计算缩放比例，使用 double 类型进行计算
        double scale = Math.min((double) screenWidth / 1920.0, (double) screenHeight / 1080.0) * 100;

        Log.d("scale", "scale: " + scale);
        // 四舍五入并转为整数
        return (int) Math.round(scale);
    }

    // 在需要模拟触摸的地方调用该方法
    public void simulateTouch(View view, float x, float y) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;

        // 构造 ACTION_DOWN 事件
        MotionEvent downEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0);
        view.dispatchTouchEvent(downEvent);

        // 构造 ACTION_UP 事件
        MotionEvent upEvent = MotionEvent.obtain(downTime, eventTime + 100, MotionEvent.ACTION_UP, x, y, 0);
        view.dispatchTouchEvent(upEvent);

        // 释放事件对象
        downEvent.recycle();
        upEvent.recycle();
    }

    // 显示 EPG 浮层：当前节目时间段+进度条+剩余分钟，以及下一节目时间段
    private void showEpgOverlay(final int index) {
        epgOverlay.setVisibility(View.VISIBLE);
        renderOverlay(epgRepo.cached(index, EpgRepository.dateKey(0)), index);
        epgRepo.fetchForOverlay(index, epg -> {
            if (index == currentLiveIndex) {
                renderOverlay(epg, index);
            }
        });
        handler.removeCallbacks(hideOverlayRunnable);
        handler.postDelayed(hideOverlayRunnable, 3000);
    }

    private void renderOverlay(EpgRepository.ChannelEpg epg, int index) {
        overlayChannelText.setText(channelNames[index].replaceFirst("^\\d+\\s*", "")
                + "  ·  " + (usesYsp(index) ? "央视频" : "央视网"));
        if (epg == null) {
            showOverlaySkeleton();
            return;
        }
        clearSkeleton(overlayNowText);
        clearSkeleton(overlayNextText);
        EpgRepository.Program realCur = epg.current();
        EpgRepository.Program cur = realCur != null ? realCur : (epg == null ? null : epg.fallbackCurrent);
        EpgRepository.Program nxt;
        if (cur != null) {
            overlayNowText.setText(cur.showTime + " — " + EpgRepository.hhmm(cur.end) + "  " + cur.title);
            if (realCur != null) {
                long now = System.currentTimeMillis() / 1000;
                long total = cur.end - cur.start;
                int pct = total > 0 ? (int) Math.max(0, Math.min(100, (now - cur.start) * 100 / total)) : 0;
                overlayProgress.setProgress(pct);
                overlayRemainText.setText(Math.max(0, (cur.end - now) / 60) + "分钟");
                overlayProgress.setVisibility(View.VISIBLE);
                overlayRemainText.setVisibility(View.VISIBLE);
                int ci = epg.currentIndex();
                nxt = (ci >= 0 && ci + 1 < epg.programs.size()) ? epg.programs.get(ci + 1) : null;
            } else {
                // 前一天末档延续：不显示进度，下一档取今天第一个即将播出的节目
                overlayProgress.setVisibility(View.INVISIBLE);
                overlayRemainText.setVisibility(View.INVISIBLE);
                nxt = epg.upcoming();
            }
        } else {
            // 无当前也无前一天兜底：显示即将播出 / 正在播出 / 暂无
            EpgRepository.Program upcoming = epg == null ? null : epg.upcoming();
            if (upcoming != null) {
                overlayNowText.setText("即将播出 " + upcoming.showTime + "  " + upcoming.title);
                int ui = epg.upcomingIndex();
                nxt = (ui >= 0 && ui + 1 < epg.programs.size()) ? epg.programs.get(ui + 1) : null;
            } else {
                String live = epg == null ? null : epg.isLive;
                overlayNowText.setText(live != null && !live.isEmpty() ? "正在播出：" + live : "暂无节目信息");
                nxt = null;
            }
            overlayProgress.setVisibility(View.INVISIBLE);
            overlayRemainText.setVisibility(View.INVISIBLE);
        }
        if (nxt != null) {
            overlayNextText.setText(nxt.showTime + " — " + EpgRepository.hhmm(nxt.end) + "  " + nxt.title);
            overlayNextText.setVisibility(View.VISIBLE);
        } else {
            overlayNextText.setText("");
            overlayNextText.setVisibility(View.VISIBLE);
        }
        if (overlaySkeletonShowing) {
            overlaySkeletonShowing = false;
            fadeInOverlay();
        } else {
            overlayNowText.setAlpha(1f);
            overlayNextText.setAlpha(1f);
            overlayProgress.setAlpha(1f);
        }
    }

    // 未加载时用灰底骨架条占位
    private void showOverlaySkeleton() {
        overlaySkeletonShowing = true;
        overlayNowText.setAlpha(1f);
        overlayNextText.setAlpha(1f);
        overlayNowText.setBackgroundResource(R.drawable.skeleton_gray);
        overlayNowText.setText(" ");
        overlayNowText.setMinWidth((int) (400 * uiScale));
        overlayNextText.setBackgroundResource(R.drawable.skeleton_gray);
        overlayNextText.setText(" ");
        overlayNextText.setMinWidth((int) (300 * uiScale));
        overlayProgress.setVisibility(View.INVISIBLE);
        overlayRemainText.setVisibility(View.INVISIBLE);
    }

    private void clearSkeleton(TextView tv) {
        tv.setBackground(null);
        tv.setMinWidth(0);
    }

    private void fadeInOverlay() {
        overlayNowText.setAlpha(0f);
        overlayNextText.setAlpha(0f);
        overlayProgress.setAlpha(0f);
        overlayNowText.animate().alpha(1f).setDuration(250).start();
        overlayNextText.animate().alpha(1f).setDuration(250).start();
        overlayProgress.animate().alpha(1f).setDuration(250).start();
    }

    // 仅 debuggable（debug）构建开启 WebView 远程调试，release 不受影响
    private void enableWebViewDebuggingIfDebuggable() {
        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0) {
            return;
        }
        // X5 内核路径
        webView1.setWebContentsDebuggingEnabled(true);
        // 系统 WebView 路径（API 19+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            android.webkit.WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        activityDestroyed = true;
        if (prefListener != null) {
            PreferenceManager.getDefaultSharedPreferences(this)
                    .unregisterOnSharedPreferenceChangeListener(prefListener);
        }
        // 移除未执行的定时/轮询任务，避免持有 Activity
        handler.removeCallbacks(hideOverlayRunnable);
        handler.removeCallbacks(pendingLoadRunnable);
        cancelVideoPoll();
        if (epgRepo != null) {
            epgRepo.shutdown();
            epgRepo = null;
        }
        // 释放 WebView：移出视图树 + 销毁
        ViewGroup webContainer = findViewById(R.id.webViewContainer);
        if (webContainer != null) {
            webContainer.removeAllViews();
        }
        if (webView0 != null) {
            webView0.destroy();
            webView0 = null;
        }
        if (webView1 != null) {
            webView1.destroy();
            webView1 = null;
        }
        super.onDestroy();
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }
}
