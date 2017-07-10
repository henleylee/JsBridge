package com.liyunlong.jsbridge.browse;

/**
 * 处理程序(用于处理由JavaScript发送的没有指定处理程序名称的消息)
 *
 * @author liyunlong
 * @date 2017/7/10 10:41
 */
public interface BridgeHandler {

    void handler(String data, Callback callback);

}
