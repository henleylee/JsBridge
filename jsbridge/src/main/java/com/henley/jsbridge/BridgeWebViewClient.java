package com.henley.jsbridge;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.List;

/**
 * BridgeWebViewClient
 *
 * @author Henley
 * @date 2017/7/10 10:41
 */
public class BridgeWebViewClient extends WebViewClient {

    private static final String TAG = BridgeWebViewClient.class.getSimpleName();
    /**
     * 是否已经注入Javascript并且处理启动消息
     */
    private boolean hasLoadJsAndDispatchMessage = false;

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        url = JsBridgeHelper.decode(url);
        if (handleBridgeMessage(view, url)) {
            return true;
        }
        return this.onCustomShouldOverrideUrlLoading(url) || super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (view instanceof BridgeWebView && !hasLoadJsAndDispatchMessage) {
            BridgeWebView bridgeWebView = (BridgeWebView) view;
            JsBridgeHelper.webViewLoadLocalJs(view, JsBridgeHelper.TO_LOAD_JS); // 注入本地Javascript
            List<Message> messageList = bridgeWebView.getStartupMessages(); // 获取启动消息集合
            if (messageList != null && !messageList.isEmpty()) { // 判断消息集合是否为null
                BridgeWebViewHelper helper = bridgeWebView.getBridgeHelper(); // BridgeWebView辅助类
                // 循环遍历处理消息
                for (Message message : messageList) {
                    helper.dispatchMessage(message);
                }
                messageList.clear();
                bridgeWebView.setStartupMessage(null); // 将启动消息集合置空
            }
            hasLoadJsAndDispatchMessage = true;
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
    }

    protected boolean handleBridgeMessage(WebView view, String url) {
        if (view instanceof BridgeWebView) {
            BridgeWebView bridgeWebView = (BridgeWebView) view;
            BridgeWebViewHelper helper = bridgeWebView.getBridgeHelper(); // BridgeWebView辅助类
            if (url.startsWith(JsBridgeHelper.JSBRIDGE_RETURN_DATA)) { // 判断是否是返回数据
                helper.handleReturnData(url); // 处理返回数据
                return true;
            } else if (url.startsWith(JsBridgeHelper.JSBRIDGE_PROTOCOL_SCHEME)) { // 刷新消息队列
                helper.flushMessageQueue();
                return true;
            }
        }
        return false;
    }

    protected boolean onCustomShouldOverrideUrlLoading(String url) {
        return false;
    }

}
