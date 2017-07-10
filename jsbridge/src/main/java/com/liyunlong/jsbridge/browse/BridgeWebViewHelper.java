package com.liyunlong.jsbridge.browse;

import android.os.Looper;
import android.text.TextUtils;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BridgeWebView辅助类
 *
 * @author liyunlong
 * @date 2017/7/10 11:53
 */
public final class BridgeWebViewHelper implements WebViewJavascriptBridge {

    private Map<String, Callback> responseCallbacks = new HashMap<>();
    private Map<String, BridgeHandler> messageHandlers = new HashMap<>(); // 处理程序Map
    private BridgeHandler defaultHandler = new DefaultHandler(); // 默认处理程序
    private List<Message> startupMessage = new ArrayList<>(); // 启动消息集合
    private long uniqueId = 0; // 唯一标识
    private WebView mWebView;

    public BridgeWebViewHelper(WebView webView) {
        this.mWebView = webView;
    }

    /**
     * 返回启动消息集合
     */
    @Override
    public List<Message> getStartupMessages() {
        return startupMessage;
    }

    /**
     * 设置启动消息集合(启动消息处理完毕后会置为null)
     */
    @Override
    public void setStartupMessage(List<Message> startupMessages) {
        this.startupMessage = startupMessages;
    }

    /**
     * 设置默认处理程序
     *
     * @param handler 默认处理程序，用于处理由JavaScript发送的没有指定处理程序名称的消息(如果由JavaScript发送的消息指定了处理程序名称，则将由本地注册命名的处理程序处理)
     */
    @Override
    public void setDefaultHandler(BridgeHandler handler) {
        this.defaultHandler = handler;
    }

    public void loadUrl(String jsUrl, Callback returnCallback) {
        loadUrl(jsUrl, null, returnCallback);
    }

    public void loadUrl(String jsUrl, Map<String, String> additionalHttpHeaders, Callback returnCallback) {
        if (jsUrl.startsWith("file:") || additionalHttpHeaders == null) {
            mWebView.loadUrl(jsUrl);
        } else {
            mWebView.loadUrl(jsUrl, additionalHttpHeaders);
        }
        if (returnCallback == null) {
            return;
        }
        responseCallbacks.put(JsBridgeHelper.parseFunctionName(jsUrl), returnCallback);
    }

    /**
     * 批量注册处理程序以供JavaScript调用并响应JavaScript发送的消息
     *
     * @param handlerNames 处理程序名称集合
     * @param handler      处理程序(用于响应由JavaScript发送的指定处理程序名称的消息)
     */
    @Override
    public void registerHandler(Collection<String> handlerNames, JsHandler handler) {
        if (handler != null) {
            for (String handlerName : handlerNames) {
                registerHandler(handlerName, handler);
            }
        }
    }

    /**
     * 注册处理程序以供JavaScript调用并响应JavaScript发送的消息
     *
     * @param handlerName 处理程序名称
     * @param handler     处理程序(用于响应由JavaScript发送的指定处理程序名称的消息)
     */
    @Override
    public void registerHandler(final String handlerName, final JsHandler handler) {
        registerHandler(handlerName, new BridgeHandler() {
            @Override
            public void handler(String data, Callback callback) {
                if (handler != null) {
                    handler.onHandler(handlerName, data, callback);
                }
            }
        });
    }

    /**
     * 注册处理程序以供JavaScript调用并响应JavaScript发送的消息
     *
     * @param handlerNames 处理程序名称集合
     * @param handler      处理程序(用于处理由JavaScript发送的没有指定处理程序名称的消息)
     */
    @Override
    public void registerHandler(Collection<String> handlerNames, BridgeHandler handler) {
        if (handlerNames != null && !handlerNames.isEmpty()) {
            for (String handlerName : handlerNames) {
                registerHandler(handlerName, handler);
            }
        }
    }

    /**
     * 注册处理程序以供JavaScript调用并响应JavaScript发送的消息
     *
     * @param handlerName 处理程序名称
     * @param handler     处理程序(用于处理由JavaScript发送的没有指定处理程序名称的消息)
     */
    @Override
    public void registerHandler(String handlerName, BridgeHandler handler) {
        if (handler != null) {
            messageHandlers.put(handlerName, handler);
        }
    }

    /**
     * 批量调用JavaScript注册的处理程序
     *
     * @param handlerInfos 方法名称与参数的Map(方法名称为key，参数为value)
     * @param handler      处理程序(用于处理由JavaScript响应的消息)
     */
    @Override
    public void callHandler(Map<String, String> handlerInfos, JavaCallHandler handler) {
        if (handler != null) {
            for (Map.Entry<String, String> entry : handlerInfos.entrySet()) {
                callHandler(entry.getKey(), entry.getValue(), handler);
            }
        }
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
        callHandler(handlerName, javaData, new Callback() {
            @Override
            public void onCallback(String data) {
                if (handler != null) {
                    handler.onHandler(handlerName, data);
                }
            }
        });
    }

    /**
     * 批量调用JavaScript注册的处理程序
     *
     * @param handlerInfos 方法名称与参数的Map(处理程序名称为Key，参数为Value)
     * @param callback     回调接口(用于处理由JavaScript响应的消息)
     */
    @Override
    public void callHandler(Map<String, String> handlerInfos, Callback callback) {
        if (handlerInfos != null && !handlerInfos.isEmpty()) {
            Set<Map.Entry<String, String>> entrySet = handlerInfos.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                callHandler(entry.getKey(), entry.getValue(), callback);
            }
        }
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
        doSend(handlerName, data, callback);
    }

    /**
     * 向JavaScript发送消息
     *
     * @param data 消息内容
     */
    @Override
    public void send(String data) {
        send(data, null);
    }

    /**
     * 向JavaScript发送消息
     *
     * @param data     消息内容
     * @param callback 回调方法(用于处理由JavaScript响应的消息)
     */
    @Override
    public void send(String data, Callback callback) {
        doSend(null, data, callback);
    }

    /**
     * 发送消息
     *
     * @param handlerName 方法名称
     * @param data        Native端传递给JS端的参数(JSON字符串)
     * @param callback    回调接口(用于处理由JavaScript响应的消息)
     */
    private void doSend(String handlerName, String data, Callback callback) {
        Message message = new Message();
        if (!TextUtils.isEmpty(data)) {
            message.setData(data);
        }
        if (callback != null) {
            String callbackId = JsBridgeHelper.generateCallbackId(++uniqueId);
            responseCallbacks.put(callbackId, callback);
            message.setCallbackId(callbackId);
        }
        if (!TextUtils.isEmpty(handlerName)) {
            message.setHandlerName(handlerName);
        }
        queueMessage(message);
    }

    /**
     * 将消息添加到启动消息集合或直接处理
     */
    private void queueMessage(Message message) {
        if (startupMessage != null) { // 判断启动消息集合是否为空
            startupMessage.add(message);
        } else {
            dispatchMessage(message);
        }
    }

    /**
     * 处理消息
     */
    void dispatchMessage(Message message) {
        String messageJson = message.toJson();
        if (!TextUtils.isEmpty(messageJson)) { // 判断Json字符串是否为空
            // 转义JSON字符串的特殊字符
            messageJson = messageJson.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2");
            messageJson = messageJson.replaceAll("(?<=[^\\\\])(\")", "\\\\\"");
        }
        String javascriptCommand = String.format(JsBridgeHelper.JS_HANDLE_MESSAGE_FROM_JAVA, messageJson);
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            mWebView.loadUrl(javascriptCommand); // 调用WebViewJavascriptBridge._handleMessageFromNative(messageJson)这个JS方法
        }
    }

    /**
     * 处理返回数据
     *
     * @param url Url
     */
    void handlerReturnData(String url) {
        String functionName = JsBridgeHelper.getFunctionFromReturnUrl(url);
        String data = JsBridgeHelper.getDataFromReturnUrl(url);
        Callback callback = responseCallbacks.get(functionName);
        if (callback != null) {
            callback.onCallback(data);
            responseCallbacks.remove(functionName);
        }
    }

    /**
     * 刷新消息队列
     */
    void flushMessageQueue() {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            loadUrl(JsBridgeHelper.JS_FETCH_QUEUE_FROM_JAVA, new Callback() {

                @Override
                public void onCallback(String data) {
                    try {
                        // 将消息发序列化为Message对象集合
                        List<Message> list = Message.toArrayList(data);
                        if (list != null && !list.isEmpty()) {
                            // 循环遍历处理消息
                            for (int i = 0; i < list.size(); i++) {
                                Message message = list.get(i);
                                handleMessage(message);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * 处理消息
     *
     * @param message 消息对象
     */
    private void handleMessage(Message message) {
        if (message == null) {
            return;
        }
        String responseId = message.getResponseId();
        if (!TextUtils.isEmpty(responseId)) { // 判断是否是response
            Callback callback = responseCallbacks.get(responseId);
            String responseData = message.getResponseData();
            callback.onCallback(responseData);
            responseCallbacks.remove(responseId);
        } else {
            Callback responseCallback;
            final String callbackId = message.getCallbackId();
            if (!TextUtils.isEmpty(callbackId)) {// 判断是否有callbackId
                responseCallback = new Callback() {
                    @Override
                    public void onCallback(String data) {
                        Message responseMsg = new Message();
                        responseMsg.setResponseId(callbackId);
                        responseMsg.setResponseData(data);
                        queueMessage(responseMsg);
                    }
                };
            } else {
                responseCallback = new SimpleCallback();
            }
            BridgeHandler handler;
            String handlerName = message.getHandlerName();
            if (!TextUtils.isEmpty(handlerName)) {
                handler = messageHandlers.get(handlerName);
            } else {
                handler = defaultHandler;
            }
            if (handler != null) {
                String data = message.getData();
                handler.handler(data, responseCallback);
            }
        }
    }

}
