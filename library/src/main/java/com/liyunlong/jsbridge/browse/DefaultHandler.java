package com.liyunlong.jsbridge.browse;

public class DefaultHandler implements BridgeHandler {

    @Override
    public void handler(String data, Callback callback) {
        if (callback != null) {
            callback.onCallback(data);
        }
    }

}
