package com.henley.jsbridge.JsWeb;

import android.support.annotation.NonNull;
import android.webkit.WebView;

import com.henley.jsbridge.browse.BridgeWebViewClient;

import java.util.Map;

/**
 * @author Henley
 * @date 2017/7/10 10:42
 */
public abstract class CustomWebViewClient extends BridgeWebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url, onPageHeaders(url));
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        view.loadUrl(onPageError(failingUrl));
    }

    /**
     * 返回错误UrlHeader
     */
    abstract String onPageError(String url);

    /**
     * 返回Http请求
     */
    @NonNull
    abstract Map<String, String> onPageHeaders(String url);

}
