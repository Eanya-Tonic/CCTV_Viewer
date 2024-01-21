package com.eanyatonic.cctvViewer;

import static com.eanyatonic.cctvViewer.FileUtils.copyAssets;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private com.tencent.smtt.sdk.WebView webView; // 导入 X5 WebView

    private String[] liveUrls = {
            "https://tv.cctv.com/live/cctv1/",
            "https://tv.cctv.com/live/cctv2/",
            "https://tv.cctv.com/live/cctv3/",
            "https://tv.cctv.com/live/cctv4/",
            "https://tv.cctv.com/live/cctv5/",
            "https://tv.cctv.com/live/cctv6/",
            "https://tv.cctv.com/live/cctv7/",
            "https://tv.cctv.com/live/cctv8/",
            "https://tv.cctv.com/live/cctvjilu",
            "https://tv.cctv.com/live/cctv10/",
            "https://tv.cctv.com/live/cctv11/",
            "https://tv.cctv.com/live/cctv12/",
            "https://tv.cctv.com/live/cctv13/",
            "https://tv.cctv.com/live/cctvchild",
            "https://tv.cctv.com/live/cctv15/",
            "https://tv.cctv.com/live/cctv16/",
            "https://tv.cctv.com/live/cctv17/",
            "https://tv.cctv.com/live/cctv5plus/",
            "https://tv.cctv.com/live/cctveurope",
            "https://tv.cctv.com/live/cctvamerica/",
            "https://www.yangshipin.cn/#/tv/home?pid=600002309",
            "https://www.yangshipin.cn/#/tv/home?pid=600002521",
            "https://www.yangshipin.cn/#/tv/home?pid=600002483",
            "https://www.yangshipin.cn/#/tv/home?pid=600002520",
            "https://www.yangshipin.cn/#/tv/home?pid=600002475",
            "https://www.yangshipin.cn/#/tv/home?pid=600002508",
            "https://www.yangshipin.cn/#/tv/home?pid=600002485",
            "https://www.yangshipin.cn/#/tv/home?pid=600002509",
            "https://www.yangshipin.cn/#/tv/home?pid=600002498",
            "https://www.yangshipin.cn/#/tv/home?pid=600002506",
            "https://www.yangshipin.cn/#/tv/home?pid=600002531",
            "https://www.yangshipin.cn/#/tv/home?pid=600002481",
            "https://www.yangshipin.cn/#/tv/home?pid=600002516",
            "https://www.yangshipin.cn/#/tv/home?pid=600002525",
            "https://www.yangshipin.cn/#/tv/home?pid=600002484",
            "https://www.yangshipin.cn/#/tv/home?pid=600002490",
            "https://www.yangshipin.cn/#/tv/home?pid=600002503",
            "https://www.yangshipin.cn/#/tv/home?pid=600002505",
            "https://www.yangshipin.cn/#/tv/home?pid=600002532",
            "https://www.yangshipin.cn/#/tv/home?pid=600002493",
            "https://www.yangshipin.cn/#/tv/home?pid=600002513",
    };

    private String[] channelNames = {
            "1 CCTV-1 综合",
            "2 CCTV-2 财经",
            "3 CCTV-3 综艺",
            "4 CCTV-4 中文国际",
            "5 CCTV-5 体育",
            "6 CCTV-6 电影",
            "7 CCTV-7 军事农业",
            "8 CCTV-8 电视剧",
            "9 CCTV-9 纪录",
            "10 CCTV-10 科教",
            "11 CCTV-11 戏曲",
            "12 CCTV-12 社会与法",
            "13 CCTV-13 新闻",
            "14 CCTV-14 少儿",
            "15 CCTV-15 音乐",
            "16 CCTV-16 奥林匹克",
            "17 CCTV-17 农业农村",
            "18 CCTV-5+ 体育赛事",
            "19 CCTV Europe",
            "20 CCTV America",
            "21 北京卫视",
            "22 江苏卫视",
            "23 东方卫视",
            "24 浙江卫视",
            "25 湖南卫视",
            "26 湖北卫视",
            "27 广东卫视",
            "28 广西卫视",
            "29 黑龙江卫视",
            "30 海南卫视",
            "31 重庆卫视",
            "32 深圳卫视",
            "33 四川卫视",
            "34 河南卫视",
            "35 福建东南卫视",
            "36 贵州卫视",
            "37 江西卫视",
            "38 辽宁卫视",
            "39 安徽卫视",
            "40 河北卫视",
            "41 山东卫视",
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

    // 频道显示view
    private TextView overlayTextView;

    private String info = "";

    // 在 MainActivity 中添加一个 Handler
    private final Handler handler = new Handler();

    private boolean doubleMenuPressedOnce = false;
    private boolean doubleMenuPressedTwice = false;

    private boolean doubleEnterPressedOnce = false;
    private boolean doubleEnterPressedTwice = false;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化 WebView
        webView = findViewById(R.id.webView);

        // 初始化显示正在输入的数字的 TextView
        inputTextView = findViewById(R.id.inputTextView);

        // 初始化 loadingOverlay
        loadingOverlay = findViewById(R.id.loadingOverlay);

        // 初始化 overlayTextView
        overlayTextView = findViewById(R.id.overlayTextView);

        // 加载上次保存的位置
        loadLastLiveIndex();

        copyAssets(this, "045738_x5.tbs.apk", "/data/user/0/com.eanyatonic.cctvViewer/app_tbs/045738_x5.tbs.apk");

        boolean canLoadX5 = QbSdk.canLoadX5(getApplicationContext());
        Log.d("canLoadX5", String.valueOf(canLoadX5));
//        if (canLoadX5) {
            QbSdk.installLocalTbsCore(getApplicationContext(), 45738, "/data/user/0/com.eanyatonic.cctvViewer/app_tbs/045738_x5.tbs.apk");
//        }

        HashMap<String, Object> map = new HashMap<>(2);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        QbSdk.initTbsSettings(map);


        // 配置 WebView 设置
        com.tencent.smtt.sdk.WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");

        // 启用 JavaScript 自动点击功能
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(com.tencent.smtt.sdk.WebSettings.LOAD_NORMAL);
        }

        // 设置 WebViewClient 和 WebChromeClient
        webView.setWebViewClient(new com.tencent.smtt.sdk.WebViewClient() {
            @Override
            public void onReceivedSslError(com.tencent.smtt.sdk.WebView webView, com.tencent.smtt.export.external.interfaces.SslErrorHandler handler, com.tencent.smtt.export.external.interfaces.SslError error) {
                handler.proceed(); // 忽略 SSL 错误
            }

            // 设置 WebViewClient，监听页面加载完成事件
            @Override
            public void onPageFinished(com.tencent.smtt.sdk.WebView view, String url) {
                    // 页面加载完成后执行 JavaScript 脚本

                    // 清空info
                    info = "";

                    if(currentLiveIndex <= 19) {
                        // 获取节目预告和当前节目
                        view.evaluateJavascript("document.querySelector('#jiemu > li.cur.act').innerText", value -> {
                            // 处理获取到的元素值
                            if (!value.equals("null") && !value.isEmpty()) {
                                String elementValueNow = value.replace("\"", ""); // 去掉可能的引号
                                info += elementValueNow + "\n";
                            }
                        });
                        view.evaluateJavascript("document.querySelector('#jiemu > li:nth-child(4)').innerText", value -> {
                            // 处理获取到的元素值
                            if (!value.equals("null") && !value.isEmpty()) {
                                String elementValueNext = value.replace("\"", ""); // 去掉可能的引号
                                info += elementValueNext;
                            }
                        });
                    } else if (currentLiveIndex <= 40) {
                        // 获取当前节目
                        view.evaluateJavascript("document.getElementsByClassName(\"tvSelectJiemu\")[0].innerHTML + \" \" + document.getElementsByClassName(\"tvSelectJiemu\")[1].innerHTML", value -> {
                            if (!value.equals("null") && !value.isEmpty()) {
                                String elementValueNow = value.replace("\"", ""); // 去掉可能的引号
                                info += elementValueNow;
                            }
                        });
                    }

                String script1 =
                            """
                                    // 定义休眠函数
                                    function sleep(ms) {
                                        return new Promise(resolve => setTimeout(resolve, ms));
                                    }
                                                    
                                    // 页面加载完成后执行 JavaScript 脚本
                                    let interval=setInterval(async function executeScript() {
                                        console.log('页面加载完成！');
                                                    
                                        // 休眠 1000 毫秒（1秒）
                                        await sleep(1000);
                                                    
                                        // 休眠 50 毫秒
                                        await sleep(50);
                                                    
                                        console.log('设置音量并点击音量按钮');
                                        var btn = document.querySelector('#player_sound_btn_player');
                                        btn.setAttribute('volume', 100);
                                        // btn.click();
                                        // btn.click();
                                        // btn.click();
                                                    
                                        // 休眠 50 毫秒
                                        await sleep(50);
                                                    
                                        console.log('点击全屏按钮');
                                        var fullscreenBtn = document.querySelector('#player_pagefullscreen_yes_player');
                                        fullscreenBtn.click();
                                        
                                        // 休眠 50 毫秒
                                        await sleep(50);
                                                    
                                        console.log('点击分辨率按钮');
                                        var elem = document.querySelector('#resolution_item_720_player');
                                        try {
                                            elem.click();
                                            }
                                        catch (error) {
                                            clearInterval(interval);
                                            }
                                        clearInterval(interval);
                                    }, 3000);
                                    """;

                    String script2 =
                            """
                                    // 定义休眠函数
                                    function sleep(ms) {
                                        return new Promise(resolve => setTimeout(resolve, ms));
                                    }
                                                    
                                    // 页面加载完成后执行 JavaScript 脚本
                                    let interval=setInterval(async function executeScript() {
                                        console.log('页面加载完成！');
                                                    
                                        // 休眠 1000 毫秒（1秒）
                                        await sleep(3000);
                                        
                                        console.log('点击全屏按钮');
                                        var btn = document.querySelector('.videoFull');
                                        btn.click();
                                        
                                        clearInterval(interval);
                                    }, 3000);
                            """;

                    if(currentLiveIndex <= 19){
                        view.evaluateJavascript(script1, null);
                    } else if (currentLiveIndex <= 40) {
                        new Handler().postDelayed(() -> {
                        view.evaluateJavascript(script2, null);
                        }, 3000);
                    }

                new Handler().postDelayed(() -> {
                        // 模拟触摸
                        simulateTouch(view, 0.5f, 0.5f);

                        // 隐藏加载的 View
                        loadingOverlay.setVisibility(View.GONE);

                        // 显示覆盖层，传入当前频道信息
                        showOverlay(channelNames[currentLiveIndex] + "\n" + info);
                    }, 5000);
                }
            });



        // 禁用缩放
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);

        // 在 Android TV 上，需要禁用焦点自动导航
        webView.setFocusable(false);

        // 设置 WebView 客户端
        webView.setWebChromeClient(new WebChromeClient());

        // 加载初始网页
        loadLiveUrl();

        // 启动定时任务，每隔一定时间执行一次
        startPeriodicTask();
    }

    // 频道选择列表
    private void showChannelList() {
        // 构建频道列表对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择频道");

        // 设置频道列表项
        builder.setItems(channelNames, (dialog, which) -> {
            // 在此处处理选择的频道
            currentLiveIndex = which;
            loadLiveUrl();
            saveCurrentLiveIndex(); // 保存当前位置
        });

        // 显示对话框
        builder.create().show();
    }

    // 启动自动播放定时任务
    private void startPeriodicTask() {
        // 使用 postDelayed 方法设置定时任务
        handler.postDelayed(periodicTask, 5000); // 5000 毫秒，即 5 秒钟
    }

    // 定时任务具体操作
    private final Runnable periodicTask = new Runnable() {
        @Override
        public void run() {
            // 获取 div 元素的 display 属性，并执行相应的操作
            getDivDisplayPropertyAndDoSimulateTouch();

            // 完成后再次调度定时任务
            handler.postDelayed(this, 5000); // 5000 毫秒，即 5 秒钟
        }
    };

    // 获取 div 元素的 display 属性并执行相应的操作
    private void getDivDisplayPropertyAndDoSimulateTouch() {
        if (webView != null) {
            if(currentLiveIndex <= 19){
                webView.evaluateJavascript("document.getElementById('play_or_pause_play_player').style.display", value -> {
                // 处理获取到的 display 属性值
                if (value.equals("\"block\"")) {
                    // 执行点击操作
                    simulateTouch(webView, 0.5f, 0.5f);
                }
            });
            } else if (currentLiveIndex <= 40) {
                String scriptPlay =
                    """
                    try{
                    document.querySelector('.play.play1').click();
                    } catch(e) {
                    }
                    """;
                webView.evaluateJavascript(scriptPlay, null);
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_MENU || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                    // 执行上一个直播地址的操作
                    navigateToPreviousLive();
                    return true;  // 返回 true 表示事件已处理，不传递给 WebView
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                    // 执行下一个直播地址的操作
                    navigateToNextLive();
                    return true;  // 返回 true 表示事件已处理，不传递给 WebView
                } else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT){
                      String scriptZoomIn =
                              """
                              // 获取当前页面的缩放比例
                              function getZoom() {
                                return parseFloat(document.body.style.zoom) || 1;
                              }
                                                            
                              // 设置页面的缩放比例
                              function setZoom(zoom) {
                                document.body.style.zoom = zoom;
                              }
                                                            
                              // 页面放大函数
                              function zoomIn() {
                                var zoom = getZoom();
                                setZoom(zoom + 0.1);
                              }
                              
                              zoomIn();
                              """;
                      webView.evaluateJavascript(scriptZoomIn, null);
                }else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT){
                    String scriptZoomOut =
                            """
                            // 获取当前页面的缩放比例
                            function getZoom() {
                              return parseFloat(document.body.style.zoom) || 1;
                            }
                                                          
                            // 设置页面的缩放比例
                            function setZoom(zoom) {
                              document.body.style.zoom = zoom;
                            }
                                                          
                            // 页面缩小函数
                            function zoomOut() {
                              var zoom = getZoom();
                              if (zoom > 0.2) {
                                setZoom(zoom - 0.1);
                              }
                            }
                            
                            zoomOut();
                            """;
                    webView.evaluateJavascript(scriptZoomOut, null);
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {

                    if (doubleEnterPressedOnce) {
                        // 双击操作

                        String script1 =
                                """   
                                console.log('点击全屏按钮');
                                document.querySelector('#player_pagefullscreen_yes_player').click();
                                """;

                        String script2 =
                                """
                                console.log('点击全屏按钮');
                                if(document.querySelector('.videoFull').id == ''){
                                    document.querySelector('.videoFull').click();
                                }else{
                                    document.querySelector('.videoFull_ac').click();
                                }
                                """;

                        if(currentLiveIndex <= 19){
                            webView.evaluateJavascript(script1, null);
                        } else if (currentLiveIndex <= 40) {
                            new Handler().postDelayed(() -> {
                                webView.evaluateJavascript(script2, null);
                            }, 500);
                        }

                        doubleEnterPressedTwice = true;
                        return true;  // 返回 true 表示事件已处理，不传递给 WebView
                    }

                    doubleEnterPressedOnce = true;

                    new Handler().postDelayed(() -> {
                        doubleEnterPressedOnce = false;
                        if(!doubleEnterPressedTwice) {

                            // 执行暂停操作
                            if(currentLiveIndex <= 19){
                                simulateTouch(webView, 0.5f, 0.5f);
                            } else if (currentLiveIndex <= 40) {
                                String scriptPause =
                                        """
                                        try{
                                        document.querySelector('.play.play2').click();
                                        } catch(e) {
                                        document.querySelector('.play.play1').click();
                                        }
                                        """;
                                webView.evaluateJavascript(scriptPause, null);
                            }
                            // 显示节目列表
                            showOverlay(channelNames[currentLiveIndex] + "\n" + info);
                        }
                        doubleEnterPressedTwice = false;
                    }, 1000);

                    return true;  // 返回 true 表示事件已处理，不传递给 WebView
                }else if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
                    if (doubleMenuPressedOnce) {
                        // 双击菜单键操作
                        // 刷新 WebView 页面
                        if (webView != null) {
                            webView.reload();
                        }
                        doubleMenuPressedTwice = true;
                        return true;  // 返回 true 表示事件已处理，不传递给 WebView
                    }

                    doubleMenuPressedOnce = true;

                    new Handler().postDelayed(() -> {
                        doubleMenuPressedOnce = false;
                        if(!doubleMenuPressedTwice) {
                            // 单击菜单键操作
                            // 显示频道列表
                            showChannelList();
                        }
                        doubleMenuPressedTwice = false;
                    }, 1000);

                    return true;  // 返回 true 表示事件已处理，不传递给 WebView
                }
                return true;  // 返回 true 表示事件已处理，不传递给 WebView
            }else if (event.getKeyCode() >= KeyEvent.KEYCODE_0 && event.getKeyCode() <= KeyEvent.KEYCODE_9) {
                int numericKey = event.getKeyCode() - KeyEvent.KEYCODE_0;

                // 将按下的数字键追加到缓冲区
                digitBuffer.append(numericKey);

                // 使用 Handler 来在超时后处理输入的数字
                new Handler().postDelayed(() -> handleNumericInput(), DIGIT_TIMEOUT);

                // 更新显示正在输入的数字的 TextView
                updateInputTextView();

                return true;  // 事件已处理，不传递给 WebView
            }
        }

        return super.dispatchKeyEvent(event);  // 如果不处理，调用父类的方法继续传递事件
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


    private void loadLiveUrl() {
        if (currentLiveIndex >= 0 && currentLiveIndex < liveUrls.length) {
            // 显示加载的View
            loadingOverlay.setVisibility(View.VISIBLE);

            webView.setInitialScale(getMinimumScale());
            webView.loadUrl(liveUrls[currentLiveIndex]);
            if(currentLiveIndex > 19){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(webView != null) {
                            webView.setInitialScale(getMinimumScale());
                            webView.reload();
                        }
                    }
                }, 1000);
            }
        }
    }

    private void navigateToPreviousLive() {
        currentLiveIndex = (currentLiveIndex - 1 + liveUrls.length) % liveUrls.length;
        loadLiveUrl();
        saveCurrentLiveIndex(); // 保存当前位置
    }

    private void navigateToNextLive() {
        currentLiveIndex = (currentLiveIndex + 1) % liveUrls.length;
        loadLiveUrl();
        saveCurrentLiveIndex(); // 保存当前位置
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

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "再按一次返回键退出应用", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        // 如果两秒内再次按返回键，则退出应用
    }

    private void showOverlay(String channelInfo) {
        // 设置覆盖层内容
        overlayTextView.setText(channelInfo);

        findViewById(R.id.overlayTextView).setVisibility(View.VISIBLE);

        // 使用 Handler 延时隐藏覆盖层
        new Handler().postDelayed(() -> {
            findViewById(R.id.overlayTextView).setVisibility(View.GONE);
        }, 8000);
    }

    @Override
    protected void onDestroy() {
        // 在销毁活动时，释放 WebView 资源
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}

