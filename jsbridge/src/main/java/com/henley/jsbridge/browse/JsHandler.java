package com.henley.jsbridge.browse;

/**
 * 处理程序(用于响应由JavaScript发送的指定处理程序名称的消息)
 *
 * @author Henley
 * @date 2017/7/10 10:45
 */
public interface JsHandler {

    void onHandler(String handlerName, String data, Callback callback);

}
