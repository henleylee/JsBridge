package com.henley.jsbridge;

/**
 * 处理程序(用于处理由JavaScript响应的消息)
 *
 * @author Henley
 * @date 2017/7/10 10:55
 */
public interface JavaCallHandler {

    void onHandler(String handlerName, String jsResponseData);
}
