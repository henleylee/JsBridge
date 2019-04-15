package com.henley.jsbridge.demo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.ValueCallback;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.henley.jsbridge.browse.BridgeHandler;
import com.henley.jsbridge.browse.BridgeWebView;
import com.henley.jsbridge.browse.Callback;
import com.henley.jsbridge.browse.JavaCallHandler;
import com.henley.jsbridge.browse.JsHandler;

import java.util.ArrayList;


public class SampleActivity extends Activity implements OnClickListener {

    private final String TAG = "MainActivity";
    BridgeWebView webView;
    int RESULT_CODE = 0;
    ValueCallback<Uri> mUploadMessage;
    private Toast toast;
    private Callback mfunction;
    private static final ArrayList<String> mHandlers = new ArrayList<>();

    static {
        mHandlers.add("login");
        mHandlers.add("callNative");
        mHandlers.add("callJs");
        mHandlers.add("open");
    }

    static class Location {
        String address;
    }

    static class User {
        String name;
        Location location;
        String testStr;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        webView = (BridgeWebView) findViewById(R.id.webView);
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);

        // 打开页面，也可以支持网络url
        webView.loadUrl("file:///android_asset/demo.html");

        // 设置默认处理程序，用于处理由JavaScript发送的没有指定处理程序名称的消息
        webView.setDefaultHandler(new BridgeHandler() {
            @Override
            public void handler(String data, Callback callback) {
                showToast(data);
                if (callback != null) {
                    callback.onCallback("Java 收到消息：" + data);
                }
            }
        });

        // 注册处理程序，以供JavaScript调用
        webView.registerHandler(mHandlers, new JsHandler() {
            @Override
            public void onHandler(String handlerName, String data, Callback callback) {
                String responseData = null;
                if (handlerName.equals("login")) { // 响应“测试登录”
                    showToast(data);
                    responseData = "登录成功";
                } else if (handlerName.equals("callNative")) { // 响应“调用Native方法”
                    showToast(data);
                    responseData = "www.baidu.com";
                } else if (handlerName.equals("open")) { // 响应“打开文件”
                    mfunction = callback;
                    pickFile();
                }
                if (callback != null && !TextUtils.isEmpty(responseData)) {
                    callback.onCallback(responseData);
                }
            }
        });

        User user = new User();
        Location location = new Location();
        location.address = "WebViewJavascriptBridge";
        user.location = location;
        user.name = "Java";

        // 调用JavaScript注册的处理程序
        webView.callHandler("functionInJs", new Gson().toJson(user), new JavaCallHandler() {
            @Override
            public void onHandler(String handlerName, String jsResponseData) {
                showToast(jsResponseData);
            }
        });


    }

    public void pickFile() {
        Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooserIntent.setType("image/*");
        startActivityForResult(chooserIntent, RESULT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == RESULT_CODE) {
            if (intent != null) {
                String uriStr = intent.getData().toString();
                mfunction.onCallback(uriStr);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:// 调用js
                webView.callHandler("functionInJs", ((Button) v).getText().toString(), new Callback() {

                    @Override
                    public void onCallback(String data) {
                        showToast(data);
                    }
                });
                break;
            case R.id.button2://发送消息给js
                webView.send("Hello JS! 我是JAVA!");
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
    }

    private void showToast(CharSequence message) {
        if (toast == null) {
            toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        toast.show();
    }
}
