package com.cimanow.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.net.Uri;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.os.Message;

public class MainActivity extends Activity {
    private WebView webView;
    private static final String HOME_URL = "https://cimanow.cc/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(request.getUrl());
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(Uri.parse(url));
            }

            private boolean handleUrl(Uri uri) {
                String scheme = uri.getScheme();
                return !("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme));
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Hide only the site's anti-pop-up/anti-adblock warning overlay.
                // Popup blocking remains enabled; video and ordinary page content are untouched.
                String script = "(function(){"
                    + "function hideWarning(){"
                    + "var els=document.querySelectorAll('body *');"
                    + "for(var i=0;i<els.length;i++){"
                    + "var e=els[i]; if(!e||!e.innerText) continue;"
                    + "var t=e.innerText;"
                    + "if(t.indexOf('يرجى إيقاف مانع الإعلانات')!==-1 || "
                    + "t.indexOf('دعمك يساعدنا في تقديم المحتوى')!==-1){"
                    + "var p=e;"
                    + "for(var j=0;j<5 && p && p!==document.body;j++){"
                    + "var s=window.getComputedStyle(p);"
                    + "if(s.position==='fixed' || s.position==='absolute' || "
                    + "p.getAttribute('role')==='dialog' || p.getAttribute('aria-modal')==='true'){"
                    + "p.style.setProperty('display','none','important');"
                    + "p.style.setProperty('visibility','hidden','important');"
                    + "break;"
                    + "}"
                    + "p=p.parentElement;"
                    + "}"
                    + "}"
                    + "}"
                    + "}"
                    + "hideWarning();"
                    + "new MutationObserver(hideWarning).observe(document.documentElement,{childList:true,subtree:true});"
                    + "})();";
                view.evaluateJavascript(script, null);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog,
                                          boolean isUserGesture, Message resultMsg) {
                // Block new WebView windows, commonly used for pop-up ads.
                return false;
            }
        });

        if (savedInstanceState != null) webView.restoreState(savedInstanceState);
        else webView.loadUrl(HOME_URL);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}