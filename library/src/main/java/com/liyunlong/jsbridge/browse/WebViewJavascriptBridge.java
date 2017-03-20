package com.liyunlong.jsbridge.browse;


public interface WebViewJavascriptBridge {

    void send(String data);

    void send(String data, Callback responseCallback);

}
