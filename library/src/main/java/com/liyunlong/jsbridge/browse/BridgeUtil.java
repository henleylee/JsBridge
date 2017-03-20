package com.liyunlong.jsbridge.browse;

import android.content.Context;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BridgeUtil {

    static final String CUSTOM_PROTOCOL_SCHEME = "bridge://";
    static final String CUSTOM_RETURN_DATA = CUSTOM_PROTOCOL_SCHEME + "return/";//格式为   bridge://return/{function}/returncontent
    static final String CUSTOM_FETCH_QUEUE = CUSTOM_RETURN_DATA + "_fetchQueue/";
    static final String TO_LOAD_JS = "WebViewJavascriptBridge.js";
    static final String EMPTY = "";
    static final String SPLIT_MARK = "/";
    static final String UNDERLINE = "_";
    static final String JAVASCRIPT = "javascript:";
    static final String CALLBACK_ID_FORMAT = "JAVA_CALLBACK_%s";
    static final String JS_HANDLE_MESSAGE_FROM_JAVA = "javascript:WebViewJavascriptBridge._handleMessageFromNative('%s');";
    static final String JS_FETCH_QUEUE_FROM_JAVA = "javascript:WebViewJavascriptBridge._fetchQueue();";

    public static String parseFunctionName(String jsUrl) {
        return jsUrl.replace("javascript:WebViewJavascriptBridge.", "").replaceAll("\\(.*\\);", "");
    }

    public static String getDataFromReturnUrl(String url) {
        if (url.startsWith(CUSTOM_FETCH_QUEUE)) {
            return url.replace(CUSTOM_FETCH_QUEUE, EMPTY);
        }
        String temp = url.replace(CUSTOM_RETURN_DATA, EMPTY);
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
     * 根据Url获取方法名称
     *
     * @param url Url
     */
    public static String getFunctionFromReturnUrl(String url) {
        String temp = url.replace(CUSTOM_RETURN_DATA, EMPTY);
        String[] functionAndData = temp.split(SPLIT_MARK);
        if (functionAndData.length >= 1) {
            return functionAndData[0];
        }
        return null;
    }


    /**
     * js 文件将注入为第一个script引用
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
     * @param webView WebView对象
     * @param path    Javascript文件路径
     */
    public static void webViewLoadLocalJs(WebView webView, String path) {
        String jsContent = assetFile2Str(webView.getContext(), path);
        webView.loadUrl(JAVASCRIPT + jsContent);
    }

    /**
     * 将assets文件转换为字符串
     *
     * @param context  上下文
     * @param fileName 文件名
     */
    private static String assetFile2Str(Context context, String fileName) {
        InputStream in = null;
        BufferedReader bufferedReader = null;
        try {
            in = context.getAssets().open(fileName);
            bufferedReader = new BufferedReader(new InputStreamReader(in));
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
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
