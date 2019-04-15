package com.henley.jsbridge;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * JsBridge辅助类
 *
 * @author Henley
 * @date 2017/7/10 10:41
 */
final class JsBridgeHelper {

    static final String JSBRIDGE_PROTOCOL_SCHEME = "bridge://";
    static final String JSBRIDGE_RETURN_DATA = JSBRIDGE_PROTOCOL_SCHEME + "return/";//格式为 bridge://return/{function}/returnContent
    static final String JSBRIDGE_FETCH_QUEUE = JSBRIDGE_RETURN_DATA + "_fetchQueue/";
    static final String TO_LOAD_JS = "WebViewJavascriptBridge.js";
    static final String JS_HANDLE_MESSAGE_FROM_JAVA = "javascript:WebViewJavascriptBridge._handleMessageFromNative('%s');";
    static final String JS_FETCH_QUEUE_FROM_JAVA = "javascript:WebViewJavascriptBridge._fetchQueue();";
    private static final String EMPTY_CHAR = "";
    private static final String SPLIT_MARK = "/";
    private static final String UNDERLINE = "_";
    private static final String JAVASCRIPT = "javascript:";
    private static final String CALLBACK_ID_FORMAT = "JAVA_CALLBACK_%s";

    /**
     * 从JS返回的Url中获取Data
     *
     * @param jsUrl Url
     */
    static String getDataFromReturnUrl(String jsUrl) {
        // url = bridge://return/_fetchQueue/message
        if (jsUrl.startsWith(JSBRIDGE_FETCH_QUEUE)) {
            return jsUrl.replace(JSBRIDGE_FETCH_QUEUE, EMPTY_CHAR);
        }
        String temp = jsUrl.replace(JSBRIDGE_RETURN_DATA, EMPTY_CHAR);
        String[] functionAndData = temp.split(SPLIT_MARK);
        if (functionAndData.length >= 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < functionAndData.length; i++) {
                sb.append(functionAndData[i]);
            }
            return sb.toString();
        }
        return null;
    }

    /**
     * 从JS返回的Url中获取方法名称
     *
     * @param jsUrl Url
     */
    static String getFunctionFromReturnUrl(String jsUrl) {
        String temp = jsUrl.replace(JSBRIDGE_RETURN_DATA, EMPTY_CHAR);
        String[] functionAndData = temp.split(SPLIT_MARK);
        if (functionAndData.length >= 1) {
            return functionAndData[0];
        }
        return null;
    }


    /**
     * 从Url中加载JS(JS文件将注入为第一个Script引用)
     *
     * @param view
     * @param url
     */
    public static void webViewLoadJs(WebView view, String url) {
        String js = "var newscript = document.createElement(\"script\");";
        js += "newscript.src=\"" + url + "\";";
        js += "document.scripts[0].parentNode.insertBefore(newscript,document.scripts[0]);";
        view.loadUrl(JAVASCRIPT + js);
    }

    /**
     * 注入本地Javascript
     *
     * @param webView       WebView对象
     * @param assetFilePath Javascript文件路径
     */
    static void webViewLoadLocalJs(WebView webView, String assetFilePath) {
        String jsContent = assetFile2Str(webView.getContext(), assetFilePath);
        webView.loadUrl(JAVASCRIPT + jsContent);
    }

    /**
     * 将Assets文件转换为字符串
     *
     * @param context  上下文
     * @param fileName 文件名
     */
    private static String assetFile2Str(Context context, String fileName) {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = context.getAssets().open(fileName);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder sb = new StringBuilder();
            do {
                line = bufferedReader.readLine();
                if (line != null && !line.matches("^\\s*\\/\\/.*")) {
                    sb.append(line);
                }
            } while (line != null);
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 生产Callback的唯一标识
     */
    static String generateCallbackId(long uniqueId) {
        return String.format(CALLBACK_ID_FORMAT, uniqueId + UNDERLINE + SystemClock.currentThreadTimeMillis());
    }

    /**
     * 从JS返回的Url中获取函数名
     *
     * @param jsUrl Url
     */
    static String parseFunctionName(String jsUrl) {
        return jsUrl.replace("javascript:WebViewJavascriptBridge.", "").replaceAll("\\(.*\\);", "");
    }

    static String decode(String content) {
        if (TextUtils.isEmpty(content)) {
            return "";
        }
        try {
            return URLDecoder.decode(content, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return URLDecoder.decode(content);
        }
    }

}
