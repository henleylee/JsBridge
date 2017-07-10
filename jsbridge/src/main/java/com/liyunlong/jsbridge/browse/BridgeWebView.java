package com.liyunlong.jsbridge.browse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebView;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Bridge辅助类
 *
 * @author liyunlong
 * @date 2017/7/10 11:26
 */
public class BridgeWebView extends WebView implements WebViewJavascriptBridge {

    private static final String TAG = "BridgeWebView";
    private BridgeWebViewHelper bridgeWebViewHelper;

    public BridgeWebView(Context context) {
        this(context, null);
    }

    public BridgeWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BridgeWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initWebView();
        bridgeWebViewHelper = new BridgeWebViewHelper(this);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        this.setVerticalScrollBarEnabled(false);
        this.setHorizontalScrollBarEnabled(false);
        this.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        this.setWebViewClient(new BridgeWebViewClient());
    }

    public BridgeWebViewHelper getBridgeWebViewHelper() {
        return bridgeWebViewHelper;
    }

    /**
     * 返回启动消息集合
     */
    @Override
    public List<Message> getStartupMessages() {
        return bridgeWebViewHelper.getStartupMessages();
    }

    /**
     * 设置启动消息集合(启动消息处理完毕后会置为null)
     */
    @Override
    public void setStartupMessage(List<Message> startupMessages) {
        bridgeWebViewHelper.setStartupMessage(startupMessages);
    }

    /**
     * 设置默认处理程序
     *
     * @param handler 默认处理程序，用于处理由JavaScript发送的没有指定处理程序名称的消息(如果由JavaScript发送的消息指定了处理程序名称，则将由本地注册命名的处理程序处理)
     */
    @Override
    public void setDefaultHandler(BridgeHandler handler) {
        bridgeWebViewHelper.setDefaultHandler(handler);
    }

    public void loadUrl(String jsUrl, Callback returnCallback) {
        bridgeWebViewHelper.loadUrl(jsUrl, returnCallback);
    }

    public void loadUrl(String jsUrl, Map<String, String> additionalHttpHeaders, Callback returnCallback) {
        bridgeWebViewHelper.loadUrl(jsUrl, additionalHttpHeaders, returnCallback);
    }

    /**
     * 批量注册处理程序以供JavaScript调用并响应JavaScript发送的消息
     *
     * @param handlerNames 处理程序名称集合
     * @param handler      处理程序(用于响应由JavaScript发送的指定处理程序名称的消息)
     */
    @Override
    public void registerHandler(Collection<String> handlerNames, JsHandler handler) {
        bridgeWebViewHelper.registerHandler(handlerNames, handler);
    }

    /**
     * 注册处理程序以供JavaScript调用并响应JavaScript发送的消息
     *
     * @param handlerName 处理程序名称
     * @param handler     处理程序(用于响应由JavaScript发送的指定处理程序名称的消息)
     */
    @Override
    public void registerHandler(final String handlerName, final JsHandler handler) {
        bridgeWebViewHelper.registerHandler(handlerName, handler);
    }

    /**
     * 注册处理程序以供JavaScript调用并响应JavaScript发送的消息
     *
     * @param handlerNames 处理程序名称集合
     * @param handler      处理程序(用于处理由JavaScript发送的没有指定处理程序名称的消息)
     */
    @Override
    public void registerHandler(Collection<String> handlerNames, BridgeHandler handler) {
        bridgeWebViewHelper.registerHandler(handlerNames, handler);
    }

    /**
     * 注册处理程序以供JavaScript调用并响应JavaScript发送的消息
     *
     * @param handlerName 处理程序名称
     * @param handler     处理程序(用于处理由JavaScript发送的没有指定处理程序名称的消息)
     */
    @Override
    public void registerHandler(String handlerName, BridgeHandler handler) {
        bridgeWebViewHelper.registerHandler(handlerName, handler);
    }

    /**
     * 批量调用JavaScript注册的处理程序
     *
     * @param handlerInfos 方法名称与参数的Map(方法名称为key，参数为value)
     * @param handler      处理程序(用于处理由JavaScript响应的消息)
     */
    @Override
    public void callHandler(Map<String, String> handlerInfos, JavaCallHandler handler) {
        bridgeWebViewHelper.callHandler(handlerInfos, handler);
    }

    /**
     * 调用JavaScript注册的处理程序
     *
     * @param handlerName 处理程序名称
     * @param javaData    Native端传递给JS端的参数(JSON字符串)
     * @param handler     处理程序(用于处理由JavaScript响应的消息)
     */
    @Override
    public void callHandler(final String handlerName, String javaData, final JavaCallHandler handler) {
        bridgeWebViewHelper.callHandler(handlerName, javaData, handler);
    }

    /**
     * 批量调用JavaScript注册的处理程序
     *
     * @param handlerInfos 方法名称与参数的Map(处理程序名称为Key，参数为Value)
     * @param callback     回调接口(用于处理由JavaScript响应的消息)
     */
    @Override
    public void callHandler(Map<String, String> handlerInfos, Callback callback) {
        bridgeWebViewHelper.callHandler(handlerInfos, callback);
    }

    /**
     * 调用JavaScript注册的处理程序
     *
     * @param handlerName 方法名称
     * @param data        Native端传递给JS端的参数(JSON字符串)
     * @param callback    回调接口(用于处理由JavaScript响应的消息)
     */
    @Override
    public void callHandler(String handlerName, String data, Callback callback) {
        bridgeWebViewHelper.callHandler(handlerName, data, callback);
    }

    /**
     * 向JavaScript发送消息
     *
     * @param data 消息内容
     */
    @Override
    public void send(String data) {
        bridgeWebViewHelper.send(data);
    }

    /**
     * 向JavaScript发送消息
     *
     * @param data     消息内容
     * @param callback 回调方法(用于处理由JavaScript响应的消息)
     */
    @Override
    public void send(String data, Callback callback) {
        bridgeWebViewHelper.send(data, callback);
    }

}
