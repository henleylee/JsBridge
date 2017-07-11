# JsBridge-master —— Web与Native交互
JSBridge利用WebViewJavascriptBridge实现Javascript和Native的交互

## 1. 背景 ##
首先介绍一下采用webview开发和采用原生开发的客户端的优缺点。
#### 1.1 使用webview： ####
    优点：便于敏捷开发、便于维护和可以热修复和定制
    缺点：UI没原生的美观

#### 1.2 使用原生开发： ####
    优点：当然是可以方便使用原生UI
    缺点：无法热修复等

## 2. 原理 ##
JSBridge是Native代码与JS代码的通信桥梁。目前的一种统一方案是:H5触发url scheme->Native捕获url scheme->原生分析执行->原生调用H5。如下图
![](/screenshots/JSBridge原理.png)

## 3. 准备工作 ##
#### 3.1 传送的消息结构见Message类： ####
```java
    private String data;            //data of message
    private String callbackId;      //callbackId
    private String responseId;      //responseId
    private String handlerName;     //name of handler
    private String responseData;    //responseData
```

#### 3.2 工具类：JsBridgeHelper： ####
从JS返回的Url中获取函数名
```java
    static String parseFunctionName(String jsUrl)
```

从JS返回的Url中获取Data
```java
    static String getDataFromReturnUrl(String jsUrl)
```

从JS返回的Url中获取方法名称
```java
    static String getFunctionFromReturnUrl(String jsUrl)
```

生产Callback的唯一标识
```java
    static String generateCallbackId(long uniqueId)
```

从Url中加载JS(JS文件将注入为第一个Script引用)
```java
    static void webViewLoadJs(WebView view, String url)
```

注入本地Javascript
```java
    static void webViewLoadLocalJs(WebView webView, String assetFilePath)

将Assets文件转换为字符串
```java
    static void assetFile2Str(Context context, String fileName)
```
## 4. 初始化 ##
#### 4.1 Native端的初始化： ####
#### 设置默认处理程序，用于处理由JavaScript发送的没有指定处理程序名称的消息： ####
```java
    webView.setDefaultHandler(new BridgeHandler() {
        @Override
        public void handler(String data, Callback callback) {
            // 处理消息内容
            if (callback != null) {
                callback.onCallback("通知JS结果");
            }
        }
    });
```

#### 注册处理程序，以供JavaScript调用： ####
```java
    webView.registerHandler(handlerName, new JsHandler() {
        @Override
        public void onHandler(String handlerName, String data, Callback callback) {
            // 处理消息内容
            if (callback != null) {
                callback.onCallback("通知JS结果");
            }
        }
    });
```

#### Native调用JavaScript注册的处理程序： ####
```java
    webView.callHandler(handlerName, javaData, new JavaCallHandler() {
        @Override
        public void onHandler(String handlerName, String jsResponseData) {
            // 处理消息内容
        }
    });
```

#### Native发送消息给JavaScript： ####
```java
    webView.send(javaData, new Callback() {
        @Override
        public void onCallback(String data) {
            // 处理消息内容
        }
    });
```

#### 4.2 Web端的初始化： ####
#### 初始化函数，注册处理程序，以供Native调用(第一次连接时调用)： ####
```java
    connectWebViewJavascriptBridge(function(bridge) {
        bridge.init(function(message, responseCallback) {
            // 处理消息内容
            responseCallback('通知Java结果');
        });

        bridge.registerHandler(handlerName, function(data, responseCallback) {
            // 处理消息内容
            responseCallback('通知Java结果');
        });
    })
```

#### 注册处理程序，以供Native调用： ####
```java
    window.WebViewJavascriptBridge.callHandler(
        handlerName
        , JsData
        , function(responseData) {
            // 处理消息内容
        }
    );
```

## 5. Native调用Javascript ##
原理：是通过WebView的webview.loadUrl("javascript:WebViewJavascriptBridge._handleMessageFromNative('%s');" );调用时序图如下图：
![](/screenshots/Native调用Javascript.png)
参照时序图基本上就了解了Android端调用JavaSript的流程。这里补充说一下JS的_dispatchMessageFromNative()函数中调用的handler的名字“functionInJs”是客户端、web前端提前约定好的。而最后调用的_doSend()就是Javasript回调给Java的了。
Native通过WebViewJavascriptBridge调用H5的JS方法或者通知H5进行回调：
```java
    String javascriptCommand = String.format("javascript:WebViewJavascriptBridge._handleMessageFromNative('%s');", messageJson);
    if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
        mWebView.loadUrl(javascriptCommand); // 调用WebViewJavascriptBridge._handleMessageFromNative(messageJson)这个JS方法
    }
```
如上，实际上是通过WebViewJavascriptBridge的_handleMessageFromNative()方法传递数据给H5。其中的messageJSON数据格式根据两种不同的类型，分别为：
    (1)Native通知H5页面进行回调
    (2)Native主动调用H5方法

## 6. Javascript调用Native ##
原理：是通过WebViewJavascriptBridge的callHandler(handlerName, data, responseCallback)方法来调用原生API，调用时序图如下图：
![](/screenshots/Javascript调用Native.png)
参照时序图，大致了解了调用过程。实现原理的思想也比较简单，利用JS的iFrame(不显示)的src动态变化，触发Java层webClient的shouldOverrideUrlLoading，然后让本地去调用Javasript。
在执行callHandler时，内部经历了以下步骤:
    (1)判断是否有回调函数，如果有，生成一个回调函数id，并将id和对应回调添加进入回调函数集合responseCallbacks中
    (2)通过特定的参数转换方法，将传入的数据，方法名一起，拼接成一个url scheme
    (3)使用内部早就创建好的一个隐藏iframe来触发scheme
    (4)原生捕获到这个scheme后会进行分析，而相应的shouldOverrideUrlLoading()中调用handlerReturnData()这个方法
    注意：正常来说是可以通过window.location.href达到发起网络请求的效果的，但是有一个很严重的问题，就是如果我们连续多次修改window.location.href的值，在Native层只能接收到最后一次请求，前面的请求都会被忽略掉。所以JS端发起网络请求的时候，需要使用iframe，这样就可以避免这个问题。

## 7. Native如何得知API被调用 ##
在Android中(WebViewClient里)，通过shouldoverrideurlloading()方法可以捕获到url scheme的触发
```java
    public boolean shouldOverrideUrlLoading(WebView view, String url){
	    //读取到url后自行进行分析处理

	    //如果返回false，则WebView处理链接url，如果返回true，代表WebView根据程序来执行url
	    return true;
    }
```
另外，Android中也可以不通过iframe.src来触发scheme，Android中可以通过window.prompt(uri, "");来触发scheme，然后Native中通过重写WebViewClient的onJsPrompt来获取Uri。

## 8. 分析Url-参数和回调的格式 ##
```java
    if (url.startsWith(JsBridgeHelper.JSBRIDGE_RETURN_DATA)) { // 判断是否是返回数据
        helper.handlerReturnData(url); // 处理返回数据
        return true;
    } else if (url.startsWith(JsBridgeHelper.JSBRIDGE_PROTOCOL_SCHEME)) { // 刷新消息队列
        helper.flushMessageQueue();
        return true;
    }
```
Native接收到Url后，可以按照这种格式将回调参数id、API名、参数提取出来，然后按如下步骤进行
    (1)根据API名，在本地找寻对应的API方法,并且记录该方法执行完后的回调函数id
    (2)根据提取出来的参数，根据定义好的参数进行转化(如果是JSON格式需要手动转换，如果是String格式直接可以使用)
    (3)原生本地执行对应的API功能方法
    (4)功能执行完毕后，找到这次API调用对应的回调函数id，然后连同需要传递的参数信息，组装成一个JSON格式的参数,回调的JSON格式为:{responseId:回调id,responseData:回调数据}
        responseId String型 Web页面中对应需要执行的回调函数的id，在Web中生成url scheme时就已经产生
        responseData JSON型 Native需要传递给Web的回调数据，是一个JSON格式: {code:(整型,调用是否成功,1成功,0失败),result:具体需要传递的结果信息,可以为任意类型,msg:一些其它信息,如调用错误时的错误信息}
    (5)通过JSBridge通知Web页面回调
```java
        JSBridge._handleMessageFromNative(messageJSON);	// 将回调信息传给H5
```



