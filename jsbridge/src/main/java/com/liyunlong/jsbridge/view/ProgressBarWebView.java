package com.liyunlong.jsbridge.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.widget.LinearLayout;

import com.liyunlong.jsbridge.JsWeb.CustomWebChromeClient;
import com.liyunlong.jsbridge.JsWeb.CustomWebViewClient;
import com.liyunlong.jsbridge.browse.BridgeHandler;
import com.liyunlong.jsbridge.browse.BridgeWebView;
import com.liyunlong.jsbridge.browse.Callback;
import com.liyunlong.jsbridge.browse.JavaCallHandler;
import com.liyunlong.jsbridge.browse.JsHandler;

import java.util.Collection;
import java.util.Map;

/**
 * 带有进度条的BridgeWebView
 *
 * @author liyunlong
 * @date 2017/7/10 11:35
 */
public class ProgressBarWebView extends LinearLayout {

    static final String TAG = ProgressBarWebView.class.getSimpleName();

    private NumberProgressBar mProgressBar;
    private BridgeWebView mWebView;

    public ProgressBarWebView(Context context) {
        this(context, null);
    }

    public ProgressBarWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressBarWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ProgressBarWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(LinearLayout.VERTICAL);
        // 初始化进度条
        if (mProgressBar == null) {
            mProgressBar = new NumberProgressBar(context, attrs);
        }
        addView(mProgressBar);

        // 初始化webview
        if (mWebView == null) {
            mWebView = new BridgeWebView(context);
        }

        mWebView.setWebChromeClient(new CustomWebChromeClient(mProgressBar));
        WebSettings webviewSettings = mWebView.getSettings();
        // 判断系统版本是不是5.0或之上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //让系统不屏蔽混合内容和第三方Cookie
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
            webviewSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        // 不支持缩放
        webviewSettings.setSupportZoom(true);

        // 自适应屏幕大小
        webviewSettings.setUseWideViewPort(true);
        webviewSettings.setLoadWithOverviewMode(true);
        mWebView.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        // WebView返回处理
        mWebView.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
                        mWebView.goBack();
                        return true;
                    }
                }
                return false;
            }
        });

        addView(mWebView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }


    public NumberProgressBar getProgressBar() {
        return mProgressBar;
    }


    public BridgeWebView getWebView() {
        return mWebView;
    }

    /**
     * 加载指定的Url
     */
    public void loadUrl(String url) {
        mWebView.loadUrl(url);
    }

    /**
     * 加载指定的Url
     */
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        mWebView.loadUrl(url, additionalHttpHeaders);
    }

    /**
     * 加载指定的Url
     */
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders, Callback returnCallback) {
        mWebView.loadUrl(url, additionalHttpHeaders, returnCallback);
    }

    /**
     * 设置WebViewClient
     */
    public void setWebViewClient(CustomWebViewClient client) {
        mWebView.setWebViewClient(client);
    }

    /**
     * 设置ChromeClient
     */
    public void setWebChromeClient(CustomWebChromeClient chromeClient) {
        mWebView.setWebChromeClient(chromeClient);
    }

    /**
     * 设置默认处理程序
     */
    public void setDefaultHandler(BridgeHandler handler) {
        mWebView.setDefaultHandler(handler);
    }

    public void send(String data) {
        mWebView.send(data);
    }

    public void send(String data, Callback responseCallback) {
        mWebView.send(data, responseCallback);
    }

    /**
     * 注册处理程序以供JavaScript调用并响应JavaScript发送的消息
     *
     * @param handlerName 处理程序名称
     * @param handler     处理程序(用于响应由JavaScript发送的指定处理程序名称的消息)
     */
    public void registerHandler(final String handlerName, final JsHandler handler) {
        mWebView.registerHandler(handlerName, handler);
    }

    /**
     * 批量注册处理程序以供JavaScript调用并响应JavaScript发送的消息
     *
     * @param handlerNames 处理程序名称集合
     * @param handler      处理程序(用于响应由JavaScript发送的指定处理程序名称的消息)
     */
    public void registerHandlers(final Collection<String> handlerNames, final JsHandler handler) {
        mWebView.registerHandler(handlerNames, handler);
    }

    /**
     * 调用JavaScript注册的处理程序
     *
     * @param handlerName 处理程序名称
     * @param javaData    Native端传递给JS端的参数(JSON字符串)
     * @param handler     处理程序(用于处理由JavaScript响应的消息)
     */
    public void callHandler(final String handlerName, String javaData, final JavaCallHandler handler) {
        mWebView.callHandler(handlerName, javaData, handler);
    }

    /**
     * 批量调用JavaScript注册的处理程序
     *
     * @param handlerInfos 方法名称与参数的Map(方法名称为key，参数为value)
     * @param handler      处理程序(用于处理由JavaScript响应的消息)
     */
    public void callHandler(final Map<String, String> handlerInfos, final JavaCallHandler handler) {
        mWebView.callHandler(handlerInfos, handler);
    }
}
