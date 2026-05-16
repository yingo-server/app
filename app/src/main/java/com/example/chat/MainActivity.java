package com.example.chat;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    // 你的线上页面地址
    private static final String TARGET_URL = "https://k.344977.xyz/AI";
    // 用于验证网络连通的地址（百度首页，稳定且轻量）
    private static final String PING_URL = "http://k.344977.xyz/test.html";
    // HTTP 连接超时时间（毫秒），建议 3~5 秒
    private static final int PING_TIMEOUT = 10000;

    private WebView webView;
    private TextView loadingText;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        loadingText = findViewById(R.id.loading_text);

        // 初始隐藏 WebView，显示加载提示
        webView.setVisibility(View.GONE);
        loadingText.setVisibility(View.VISIBLE);
        loadingText.setText("正在检查网络连接…");

        // 配置 WebView（先不加载任何内容）
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        // 在后台线程执行网络检测
        executor.execute(this::checkNetwork);
    }

    /**
     * 验证网络是否真实可用：尝试连接百度首页，仅验证连通性。
     */
    private void checkNetwork() {
        boolean reachable = false;
        try {
            URL url = new URL(PING_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(PING_TIMEOUT);
            connection.setReadTimeout(PING_TIMEOUT);
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(false);
            int responseCode = connection.getResponseCode();
            // 只要能收到任何响应（2xx或3xx），都认为网络可达
            if (responseCode >= 200 && responseCode < 400) {
                reachable = true;
            }
            connection.disconnect();
        } catch (Exception e) {
            // 异常表示网络不可达或超时
            reachable = false;
        }

        final boolean finalReachable = reachable;
        mainHandler.post(() -> {
            if (finalReachable) {
                // 网络正常，加载目标页面
                loadingText.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                webView.loadUrl(TARGET_URL);
            } else {
                // 网络不可达，弹出对话框并退出
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("网络不可用")
                        .setMessage("无法连接到互联网，请检查网络后重试。")
                        .setCancelable(false)
                        .setPositiveButton("退出", (dialog, which) -> {
                            finish();
                            System.exit(0);
                        })
                        .show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
