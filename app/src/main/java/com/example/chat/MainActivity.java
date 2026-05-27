package com.example.chat;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // 你的线上页面地址（WebView 打开的首页）
    private static final String TARGET_URL = "https://k.344977.xyz/moonjump";

    // 黑名单：这些域名及其子域名将在 WebView 内部打开，不跳转浏览器
    // 通常填写你自己的 AI 网站域名即可
    private static final String[] INTERNAL_DOMAINS = new String[]{
            "k.344977.xyz"   // 替换为你的实际域名，例如 "myapp.netlify.app"
    };

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);
                String host = uri.getHost();
                // 检查是否为黑名单域名（或其子域名），若是则在 WebView 内加载
                if (host != null && isInternalDomain(host)) {
                    view.loadUrl(url);
                    return true;
                }
                // 其他链接一律跳转外部浏览器
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // 加载本地错误页，不暴露域名
                view.loadUrl("file:///android_asset/error.html");
            }
        });

        // 下载支持
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimetype);
                request.setTitle("下载文件");
                request.setDescription("正在下载...");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(MainActivity.this, "开始下载", Toast.LENGTH_SHORT).show();
            }
        });

        // 直接加载目标网页，无网络检测
        webView.loadUrl(TARGET_URL);
    }

    /**
     * 判断主机名是否属于内部域名（黑名单）
     */
    private boolean isInternalDomain(String host) {
        for (String domain : INTERNAL_DOMAINS) {
            if (host.equals(domain) || host.endsWith("." + domain)) {
                return true;
            }
        }
        return false;
    }
}
