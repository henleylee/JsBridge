package com.liyunlong.jsbridge.browse.JsWeb;


import android.support.annotation.NonNull;
import android.webkit.WebView;

import com.liyunlong.jsbridge.browse.BridgeWebViewClient;

import java.util.Map;

/**
 * Class description
 *
 * @author YEZHENNAN220
 * @date 2016-07-08 13:54
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
     * return errorUrl
     *
     * @param url
     * @return
     */
    public abstract String onPageError(String url);

    /**
     * HttpHeaders
     * return
     *
     * @return
     */
    @NonNull
    public abstract Map<String, String> onPageHeaders(String url);

}
