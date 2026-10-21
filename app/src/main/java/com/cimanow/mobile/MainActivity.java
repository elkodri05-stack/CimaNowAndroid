package com.cimanow.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
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
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

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
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog,
                                          boolean isUserGesture, Message resultMsg) {
                // Send pop-up/new-window links to the phone's external browser.
                final WebView popupWebView = new WebView(MainActivity.this);
                popupWebView.getSettings().setJavaScriptEnabled(true);
                popupWebView.setWebViewClient(new WebViewClient() {
                    private boolean opened = false;

                    private void openExternal(Uri uri) {
                        if (opened) return;
                        String scheme = uri.getScheme();
                        if (!("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))) return;
                        opened = true;
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        } catch (Exception ignored) {
                            // No compatible external browser available.
                        } finally {
                            popupWebView.postDelayed(() -> popupWebView.destroy(), 300);
                        }
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        openExternal(request.getUrl());
                        return true;
                    }

                    @SuppressWarnings("deprecation")
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        openExternal(Uri.parse(url));
                        return true;
                    }

                    @Override
                    public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                        openExternal(Uri.parse(url));
                    }
                });

                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(popupWebView);
                resultMsg.sendToTarget();
                return true;
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