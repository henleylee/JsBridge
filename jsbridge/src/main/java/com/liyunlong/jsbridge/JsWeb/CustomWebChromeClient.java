package com.liyunlong.jsbridge.JsWeb;

import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.liyunlong.jsbridge.view.NumberProgressBar;

/**
 * 带进度条的ChromeClient
 *
 * @author liyunlong
 * @date 2017/7/10 10:39
 */
public class CustomWebChromeClient extends WebChromeClient {

    private NumberProgressBar mProgressBar;
    private final static int DEFAULT_PROGRESS = 95;

    public CustomWebChromeClient(NumberProgressBar progressBar) {
        this.mProgressBar = progressBar;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        if (newProgress >= DEFAULT_PROGRESS) {
            mProgressBar.setVisibility(View.GONE);
        } else {
            if (mProgressBar.getVisibility() == View.GONE) {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            mProgressBar.setProgress(newProgress);
        }
        super.onProgressChanged(view, newProgress);
    }

}
