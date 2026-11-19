package com.eanyatonic.cctvViewer;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * EPG 节目单数据层。
 * 数据源：央视官方接口 api.cntv.cn，按频道按天返回 JSON，覆盖全部 41 个频道。
 * 仅使用系统内置的 HttpURLConnection 与 org.json，不引入任何第三方依赖。
 */
public class EpgRepository {

    private static final String TAG = "EpgRepository";

    /** 回调始终在主线程触发；失败时 epg 为 null。 */
    public interface Callback {
        void onResult(ChannelEpg epg);
    }

    public static class Program {
        public String title;
        public long start;      // 开始时间，epoch 秒
        public long end;        // 结束时间，epoch 秒
        public String showTime; // 接口给出的 HH:mm（+0800）
    }

    public static class ChannelEpg {
        public String isLive;   // 当前节目名
        public String date;     // yyyyMMdd
        public final List<Program> programs = new ArrayList<>();

        public int currentIndex() {
            long now = System.currentTimeMillis() / 1000;
            for (int i = 0; i < programs.size(); i++) {
                Program p = programs.get(i);
                if (p.start <= now && now < p.end) {
                    return i;
                }
            }
            return -1;
        }

        public Program current() {
            int i = currentIndex();
            return i >= 0 ? programs.get(i) : null;
        }

        /** 当天无当前节目时用前一天最后一个节目兜底（电视台一般不会在零点空播）。 */
        public Program fallbackCurrent;

        /** 第一个开始时间晚于当前时刻的节目（零点换日空档时用）。 */
        public int upcomingIndex() {
            long now = System.currentTimeMillis() / 1000;
            for (int i = 0; i < programs.size(); i++) {
                if (programs.get(i).start > now) {
                    return i;
                }
            }
            return -1;
        }

        public Program upcoming() {
            int i = upcomingIndex();
            return i >= 0 ? programs.get(i) : null;
        }
    }

    // 与 MainActivity.channelNames 顺序一一对应（0-40）
    private static final String[] CODES = {
            "cctv1", "cctv2", "cctv3", "cctv4", "cctv5", "cctv6", "cctv7", "cctv8",
            "cctvjilu", "cctv10", "cctv11", "cctv12", "cctv13", "cctvchild", "cctv15",
            "cctv16", "cctv17", "cctv5plus", "cctveurope", "cctvamerica",
            "btv1", "jiangsu", "dongfang", "zhejiang", "hunan", "hubei", "guangdong",
            "guangxi", "heilongjiang", "travel", "chongqing", "shenzhen", "sichuan",
            "henan", "dongnan", "guizhou", "jiangxi", "liaoning", "anhui", "hebei",
            "shandong"
    };

    private static final TimeZone CN = TimeZone.getTimeZone("GMT+8");
    private static final String[] WEEK = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};

    private final ExecutorService io = Executors.newFixedThreadPool(4);
    private final Handler main = new Handler(Looper.getMainLooper());

    /** 释放线程池与待回调，避免 Activity 销毁后仍被持有。 */
    public void shutdown() {
        io.shutdownNow();
        synchronized (pending) {
            pending.clear();
        }
        main.removeCallbacksAndMessages(null);
    }
    private final Map<String, ChannelEpg> cache = new HashMap<>();
    private final Map<String, List<Callback>> pending = new HashMap<>();

    public static String codeFor(int index) {
        return index >= 0 && index < CODES.length ? CODES[index] : null;
    }

    /** 相对今天偏移 days 天的日期键，yyyyMMdd（按 +0800 广播日）。 */
    public static String dateKey(int offsetDays) {
        Calendar cal = Calendar.getInstance(CN);
        cal.add(Calendar.DAY_OF_MONTH, offsetDays);
        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(cal.getTime());
    }

    /** 日期列上行标签：今天 / 明天 / 周X。 */
    public static String dateWeekLabel(int offsetDays) {
        if (offsetDays == 0) return "今天";
        if (offsetDays == 1) return "明天";
        Calendar cal = Calendar.getInstance(CN);
        cal.add(Calendar.DAY_OF_MONTH, offsetDays);
        return WEEK[cal.get(Calendar.DAY_OF_WEEK) - 1];
    }

    /** 日期列下行标签：MM-dd。 */
    public static String dateMdLabel(int offsetDays) {
        Calendar cal = Calendar.getInstance(CN);
        cal.add(Calendar.DAY_OF_MONTH, offsetDays);
        return new SimpleDateFormat("MM-dd", Locale.US).format(cal.getTime());
    }

    /** 把 epoch 秒格式化为 +0800 的 HH:mm。 */
    public static String hhmm(long epochSec) {
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm", Locale.US);
        fmt.setTimeZone(CN);
        return fmt.format(new java.util.Date(epochSec * 1000));
    }

    public ChannelEpg cached(int index, String date) {
        return cache.get(index + "|" + date);
    }

    /** 拉取某频道某天的节目单；命中缓存直接回调，否则后台请求并去重并发。 */
    public void fetch(final int index, final String date, Callback cb) {
        final String code = codeFor(index);
        if (code == null) {
            main.post(() -> cb.onResult(null));
            return;
        }
        final String key = index + "|" + date;
        ChannelEpg hit = cache.get(key);
        if (hit != null) {
            main.post(() -> cb.onResult(hit));
            return;
        }
        boolean launch;
        synchronized (pending) {
            List<Callback> waiters = pending.get(key);
            launch = waiters == null;
            if (launch) {
                waiters = new ArrayList<>();
                pending.put(key, waiters);
            }
            waiters.add(cb);
        }
        if (launch) {
            io.execute(() -> {
                ChannelEpg result = null;
                try {
                    result = parse(code, date, request(code, date));
                    if (result != null && !result.programs.isEmpty()) {
                        cache.put(key, result);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "拉取节目单失败 " + code + " " + date, e);
                }
                List<Callback> waiters;
                synchronized (pending) {
                    waiters = pending.remove(key);
                }
                final ChannelEpg delivered = result;
                if (waiters != null) {
                    for (Callback c : waiters) {
                        main.post(() -> c.onResult(delivered));
                    }
                }
            });
        }
    }

    /**
     * 供浮层使用：今天有当前节目直接用；否则取昨天最后一个节目作为“延续中的当前节目”兜底。
     */
    public void fetchForOverlay(final int index, final Callback cb) {
        fetch(index, dateKey(0), epgToday -> {
            if (epgToday != null && epgToday.current() != null) {
                cb.onResult(epgToday);
                return;
            }
            fetch(index, dateKey(-1), epgPrev -> {
                if (epgToday != null) {
                    if (epgPrev != null && !epgPrev.programs.isEmpty()) {
                        epgToday.fallbackCurrent = epgPrev.programs.get(epgPrev.programs.size() - 1);
                    }
                    cb.onResult(epgToday);
                } else {
                    cb.onResult(epgPrev);
                }
            });
        });
    }

    private String request(String code, String date) throws Exception {
        String url = "https://api.cntv.cn/epg/getEpgInfoByChannelNew?c=" + code
                + "&serviceId=tvcctv&d=" + date;
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        try {
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    // optString 会把 JSON 的 null 转成字符串 "null"，统一清洗掉
    private static String str(JSONObject o, String key) {
        String v = o.optString(key);
        return "null".equals(v) ? "" : v;
    }

    private ChannelEpg parse(String code, String date, String body) throws Exception {
        JSONObject root = new JSONObject(body);
        JSONObject data = root.optJSONObject("data");
        JSONObject ch = data == null ? null : data.optJSONObject(code);
        if (ch == null) {
            return null;
        }
        ChannelEpg epg = new ChannelEpg();
        epg.isLive = str(ch, "isLive");
        epg.date = date;
        JSONArray list = ch.optJSONArray("list");
        if (list != null) {
            for (int i = 0; i < list.length(); i++) {
                JSONObject o = list.optJSONObject(i);
                if (o == null) continue;
                Program p = new Program();
                p.title = str(o, "title");
                p.start = o.optLong("startTime");
                p.end = o.optLong("endTime");
                p.showTime = str(o, "showTime");
                epg.programs.add(p);
            }
        }
        return epg;
    }
}