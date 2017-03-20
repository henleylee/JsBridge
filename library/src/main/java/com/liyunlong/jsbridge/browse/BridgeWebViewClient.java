package com.liyunlong.jsbridge.browse;

import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

/**
 * Created by bruce on 10/28/15.
 */
public class BridgeWebViewClient extends WebViewClient {

    private static final String TAG = BridgeWebViewClient.class.getSimpleName();
    private boolean hasLoadJsandDispatchMessage = false;

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Url = " + url);
        if (view instanceof BridgeWebView) {
            BridgeWebView webView = (BridgeWebView) view;
            if (url.startsWith(BridgeUtil.CUSTOM_RETURN_DATA)) { // 如果是返回数据
                webView.handlerReturnData(url);
                return true;
            } else if (url.startsWith(BridgeUtil.CUSTOM_PROTOCOL_SCHEME)) { // 刷新消息队列
                webView.flushMessageQueue();
                return true;
            }
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (view instanceof BridgeWebView && !hasLoadJsandDispatchMessage) {
            BridgeWebView webView = (BridgeWebView) view;
            BridgeUtil.webViewLoadLocalJs(view, BridgeUtil.TO_LOAD_JS); // 注入本地Javascript
            List<Message> messageList = webView.getStartupMessage();
            if (messageList != null && !messageList.isEmpty()) {
                // 循环遍历处理消息
                for (Message message : messageList) {
                    webView.dispatchMessage(message);
                }
                messageList.clear();
                webView.setStartupMessage(null);
            }
            hasLoadJsandDispatchMessage = true;
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
    }
}