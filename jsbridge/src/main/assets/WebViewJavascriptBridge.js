//notation: js file can only use this kind of comments
//since comments will cause error when use in webview.loadurl,
//comments will be remove by java use regexp
(function() {
    if (window.WebViewJavascriptBridge) {
        return;
    }

    // 进行url scheme传值的iframe
    var messagingIframe;
    // 发送消息队列
    var sendMessageQueue = [];
    // 接收消息队列
    var receiveMessageQueue = [];
    // 本地注册的方法集合，原生只能调用本地注册的方法，否则会提示错误
    var messageHandlers = {};

    // JsBridge协议定义的名称
    var JSBRIDGE_PROTOCOL_SCHEME = 'bridge://';
    var JSBRIDGE_RETURN_DATA = JSBRIDGE_PROTOCOL_SCHEME + 'return/';
    var JSBRIDGE_FETCH_QUEUE = JSBRIDGE_RETURN_DATA + '_fetchQueue/';
    var QUEUE_HAS_MESSAGE = '__QUEUE_MESSAGE__/';
    var UNDERLINE = '_';
    // 定义的回调函数集合，在原生调用完对应的方法后，会执行对应的回调函数id
    var responseCallbacks = {};
    // 唯一ID，用来确保每一个回调函数的唯一性
    var uniqueId = 1;

    function _createQueueReadyIframe(doc) {
        // 创建隐藏iframe过程
        messagingIframe = doc.createElement('iframe');
        messagingIframe.style.display = 'none';
        doc.documentElement.appendChild(messagingIframe);
    }

    // set default messageHandler
    function init(messageHandler) {
        if (WebViewJavascriptBridge._messageHandler) {
            throw new Error('WebViewJavascriptBridge.init called twice.');
        }
        WebViewJavascriptBridge._messageHandler = messageHandler;
        var receivedMessages = receiveMessageQueue;
        receiveMessageQueue = null;
        for (var i = 0; i < receivedMessages.length; i++) {
            _dispatchMessageFromNative(receivedMessages[i]);
        }
    }

    // 注册本地JS方法，注册后Native可通过JSBridge调用，调用后会将方法注册到本地变量messageHandlers中
    function registerHandler(handlerName, handler) {
        messageHandlers[handlerName] = handler;
    }

    // 调用原生开放的API，调用后实际上还是本地通过url scheme触发，调用时会将回调id存放到本地变量responseCallbacks中
    function callHandler(handlerName, data, responseCallback) {
        _doSend({
            handlerName: handlerName,
            data: data
        }, responseCallback);
    }

    function send(data, responseCallback) {
        _doSend({
            data: data
        }, responseCallback);
    }

    // sendMessage add message， 触发native处理 sendMessage
    function _doSend(message, responseCallback) {
        // 判断是否有回调函数，如果有则生成一个回调函数id，并将id和对应回调添加进入回调函数集合responseCallbacks中
        if (responseCallback) {
            var callbackId = 'callback_' + (uniqueId++) + UNDERLINE + new Date().getTime();
            responseCallbacks[callbackId] = responseCallback;
            message.callbackId = callbackId;
        }
        // 使用sendMessageQueue队列把消息存起来，并且改变Iframe的src，提醒native端来取消息
        sendMessageQueue.push(message);
        // 触发scheme
        messagingIframe.src = JSBRIDGE_PROTOCOL_SCHEME + QUEUE_HAS_MESSAGE;
    }

    // 提供给native调用，该函数作用:获取sendMessageQueue返回给native，由于android不能直接获取返回的内容，所以使用url shouldOverrideUrlLoading 的方式返回内容
    function _fetchQueue() {
        var messageQueueString = JSON.stringify(sendMessageQueue);
        sendMessageQueue = [];
        // android can't read directly the return data, so we can reload iframe src to communicate with java
        // 将js传递的参数拼接入Url中，传递到native，而相应的shouldOverrideUrlLoading()中调用handlerReturnData()这个方法
        // 注意：正常来说是可以通过window.location.href达到发起网络请求的效果的，但是有一个很严重的问题，就是如果连续多次修改window.location.href的值，在Native层只能接收到最后一次请求，前面的请求都会被忽略掉。所以JS端发起网络请求的时候，需要使用iframe，这样就可以避免这个问题。
        messagingIframe.src = JSBRIDGE_FETCH_QUEUE + encodeURIComponent(messageQueueString);
    }

    // 提供给native使用
    function _dispatchMessageFromNative(messageJSON) {
        setTimeout(function() {
            var message = JSON.parse(messageJSON);
            var responseCallback;
            // java call finished, now need to call js callback function
            // 假如存在responseId，则回调完成时删除responseId
            if (message.responseId) {
                responseCallback = responseCallbacks[message.responseId];
                if (!responseCallback) {
                    return;
                }
                responseCallback(message.responseData);
                delete responseCallbacks[message.responseId];
            } else {
                // 直接发送
                if (message.callbackId) {
                    var callbackResponseId = message.callbackId;
                    responseCallback = function(responseData) {
                        // js回调给java
                        _doSend({
                            responseId: callbackResponseId,
                            responseData: responseData
                        });
                    };
                }
                var handler = WebViewJavascriptBridge._messageHandler;
                if (message.handlerName) {
                    // 通过队列拿到handler
                    handler = messageHandlers[message.handlerName];
                }
                // 查找指定handler
                try {
                    handler(message.data, responseCallback);
                } catch (exception) {
                    if (typeof console != 'undefined') {
                        console.log("WebViewJavascriptBridge: WARNING: javascript handler threw.", message, exception);
                    }
                }
            }
        });
    }

    // 提供给native调用，receiveMessageQueue在会在页面加载完后赋值为null
    function _handleMessageFromNative(messageJSON) {
        console.log(messageJSON);
        if (receiveMessageQueue) {
            receiveMessageQueue.push(messageJSON);
        } else {
            // 处理来自Java层的主动调用
            _dispatchMessageFromNative(messageJSON);
        }
    }

    var WebViewJavascriptBridge = window.WebViewJavascriptBridge = {
        init: init,
        send: send,
        registerHandler: registerHandler,
        callHandler: callHandler,
        _fetchQueue: _fetchQueue,
        _handleMessageFromNative: _handleMessageFromNative
    };

    var doc = document;
    _createQueueReadyIframe(doc);
    var readyEvent = doc.createEvent('Events');
    readyEvent.initEvent('WebViewJavascriptBridgeReady');
    readyEvent.bridge = WebViewJavascriptBridge;
    doc.dispatchEvent(readyEvent);
})();