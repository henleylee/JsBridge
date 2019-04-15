package com.henley.jsbridge.browse;

/**
 * 默认处理程序(用于处理由JavaScript发送的没有指定处理程序名称的消息)
 */
public class DefaultHandler implements BridgeHandler {

    @Override
    public void handler(String data, Callback callback) {
        if (callback != null) {
            callback.onCallback(data);
        }
    }

}
