package com.henley.jsbridge.browse;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * BridgeWebView发消息的接口(定义了发消息的方法)
 */
interface WebViewJavascriptBridge {

    /**
     * 返回启动消息集合
     */
    List<Message> getStartupMessages();

    /**
     * 设置启动消息集合(启动消息处理完毕后会置为null)
     */
    void setStartupMessage(List<Message> startupMessages);

    /**
     * 设置默认处理程序
     *
     * @param handler 默认处理程序，用于处理由JavaScript发送的没有指定处理程序名称的消息(如果由JavaScript发送的消息指定了处理程序名称，则将由本地注册命名的处理程序处理)
     */
    void setDefaultHandler(BridgeHandler handler);

    void loadUrl(String jsUrl, Callback returnCallback);

    void loadUrl(String jsUrl, Map<String, String> additionalHttpHeaders, Callback returnCallback);

    /**
     * 批量注册处理程序以供JavaScript调用并响应JavaScript发送的消息
     *
     * @param handlerNames 处理程序名称集合
     * @param handler      处理程序(用于响应由JavaScript发送的指定处理程序名称的消息)
     */
    void registerHandler(Collection<String> handlerNames, JsHandler handler);

    /**
     * 注册处理程序以供JavaScript调用并响应JavaScript发送的消息
     *
     * @param handlerName 处理程序名称
     * @param handler     处理程序(用于响应由JavaScript发送的指定处理程序名称的消息)
     */
    void registerHandler(final String handlerName, final JsHandler handler);

    /**
     * 注册处理程序以供JavaScript调用并响应JavaScript发送的消息
     *
     * @param handlerNames 处理程序名称集合
     * @param handler      处理程序(用于处理由JavaScript发送的没有指定处理程序名称的消息)
     */
    void registerHandler(Collection<String> handlerNames, BridgeHandler handler);

    /**
     * 注册处理程序以供JavaScript调用并响应JavaScript发送的消息
     *
     * @param handlerName 处理程序名称
     * @param handler     处理程序(用于处理由JavaScript发送的没有指定处理程序名称的消息)
     */
    void registerHandler(String handlerName, BridgeHandler handler);

    /**
     * 批量调用JavaScript注册的处理程序
     *
     * @param handlerInfos 方法名称与参数的Map(方法名称为key，参数为value)
     * @param handler      处理程序(用于处理由JavaScript响应的消息)
     */
    void callHandler(Map<String, String> handlerInfos, JavaCallHandler handler);

    /**
     * 调用JavaScript注册的处理程序
     *
     * @param handlerName 处理程序名称
     * @param javaData    Native端传递给JS端的参数(JSON字符串)
     * @param handler     处理程序(用于处理由JavaScript响应的消息)
     */
    void callHandler(final String handlerName, String javaData, final JavaCallHandler handler);

    /**
     * 批量调用JavaScript注册的处理程序
     *
     * @param handlerInfos 方法名称与参数的Map(处理程序名称为Key，参数为Value)
     * @param callback     回调接口(用于处理由JavaScript响应的消息)
     */
    void callHandler(Map<String, String> handlerInfos, Callback callback);

    /**
     * 调用JavaScript注册的处理程序
     *
     * @param handlerName 方法名称
     * @param data        Native端传递给JS端的参数(JSON字符串)
     * @param callback    回调接口(用于处理由JavaScript响应的消息)
     */
    void callHandler(String handlerName, String data, Callback callback);

    /**
     * 向JavaScript发送消息
     *
     * @param data 消息内容
     */
    void send(String data);

    /**
     * 向JavaScript发送消息
     *
     * @param data     消息内容
     * @param callback 回调方法(用于处理由JavaScript响应的消息)
     */
    void send(String data, Callback callback);
}
