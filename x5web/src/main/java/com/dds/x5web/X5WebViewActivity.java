package com.dds.x5web;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.tencent.smtt.utils.TbsLog;

import java.net.MalformedURLException;
import java.net.URL;

public class X5WebViewActivity extends AppCompatActivity {
    private X5WebView mWebView;
    private ViewGroup mViewParent;

    private String mHomeUrl = "";
    private ProgressBar progressBar = null;
    private ValueCallback<Uri> uploadFile;
    private URL mIntentUrl;
    private String title = "";
    private boolean isShare;
    private boolean isToolbar;


    public static void openActivity(Activity activity, String url) {
        openActivity(activity, url, false, true);
    }


    // isShare 是否显示分享按钮  isToolBar 是否显示toolbar
    public static void openActivity(Activity activity, String url, boolean isShare, boolean isToolbar) {
        Intent intent = new Intent(activity, X5WebViewActivity.class);
        intent.setData(Uri.parse(url));
        intent.putExtra("isShare", isShare);
        intent.putExtra("isToolbar", isToolbar);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        Intent intent = getIntent();
        if (intent != null) {
            try {
                mIntentUrl = new URL(intent.getData().toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();

            } catch (Exception e) {
                e.printStackTrace();
            }
            isShare = intent.getBooleanExtra("isShare", false);
            isToolbar = intent.getBooleanExtra("isToolbar", false);
        }
        //設置硬件加速模式
        try {
            if (Integer.parseInt(android.os.Build.VERSION.SDK) >= 11) {
                getWindow().setFlags(
                        android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                        android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!isToolbar) {
            setTheme(R.style.X5_AppTheme_NoActionBar_Black);
        }
        setContentView(R.layout.activity_x5_web_view);
        mViewParent = findViewById(R.id.webView1);
        mTestHandler.sendEmptyMessageDelayed(MSG_INIT_UI, 10);


        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView textView = findViewById(R.id.btn_toolbar);
        if (isShare) {
            textView.setBackgroundResource(R.drawable.x5_ic_share_black_24dp);
        }
        if (!isToolbar) {
            toolbar.setVisibility(View.GONE);
        }
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                X5WebViewActivity.this.finish();
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentShare = new Intent(Intent.ACTION_SEND);
                intentShare.setType("text/plain");
                intentShare.putExtra(Intent.EXTRA_SUBJECT, title);
                intentShare.putExtra(Intent.EXTRA_TEXT, mIntentUrl);
                intentShare.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(
                        intentShare, "share"));
            }
        });
    }

    private void initProgressBar() {
        progressBar = findViewById(R.id.progressBar1);
        progressBar.setMax(100);

    }

    private void init() {
        mWebView = new X5WebView(this, null);
        CoordinatorLayout.LayoutParams lp = new CoordinatorLayout.LayoutParams(-1, -1);
        lp.setBehavior(new AppBarLayout.ScrollingViewBehavior());
        mWebView.setLayoutParams(lp);
        mViewParent.addView(mWebView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        initProgressBar();
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 同程旅游国际用车
                getSupportActionBar().setTitle(view.getTitle());
                String javascript =
                        "javascript:function hideTitle() {"
                                + "var divNav = document.getElementsByClassName('fed-navbar')[0];"
                                + "divNav.style.display = 'none';"
                                + "}";

                view.loadUrl(javascript);
                //加载Java对象 InJavaScriptLocalObj 里的getSource方法，返回标题 "用车"
                view.loadUrl("javascript:window.java_obj.getSource(" +
                        "document.getElementsByClassName('fed-navbar-title')[0].innerHTML);");

                // 加载hideTitle方法
                view.loadUrl("javascript:hideTitle();");
            }

            @Override
            public void onReceivedSslError(WebView webView, final SslErrorHandler handler, SslError sslError) {
                AlertDialog.Builder builder = new AlertDialog.Builder(X5WebViewActivity.this);
                builder.setTitle("证书提示");
                builder.setMessage("您访问的是含有证书的私密网站，是否允许继续访问？拒绝将不显示关于网页的任何内容");
                AlertDialog.OnClickListener dialogButtonOnClickListener = new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int clickedButton) {
                        if (DialogInterface.BUTTON_POSITIVE == clickedButton) {
                            handler.proceed();
                        } else if (DialogInterface.BUTTON_NEGATIVE == clickedButton) {
                            handler.cancel();
                        }
                    }
                };
                builder.setPositiveButton("允许", dialogButtonOnClickListener);
                builder.setNegativeButton("拒绝", dialogButtonOnClickListener);
                builder.show();
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsConfirm(WebView arg0, String arg1, String arg2,
                                       JsResult arg3) {
                return super.onJsConfirm(arg0, arg1, arg2, arg3);
            }

            View myVideoView;
            View myNormalView;
            IX5WebChromeClient.CustomViewCallback callback;

            /**
             * 全屏播放配置
             */
            @Override
            public void onShowCustomView(View view,
                                         IX5WebChromeClient.CustomViewCallback customViewCallback) {
                FrameLayout normalView = findViewById(R.id.web_filechooser);
                ViewGroup viewGroup = (ViewGroup) normalView.getParent();
                viewGroup.removeView(normalView);
                viewGroup.addView(view);
                myVideoView = view;
                myNormalView = normalView;
                callback = customViewCallback;
            }

            @Override
            public void onHideCustomView() {
                if (callback != null) {
                    callback.onCustomViewHidden();
                    callback = null;
                }
                if (myVideoView != null) {
                    ViewGroup viewGroup = (ViewGroup) myVideoView.getParent();
                    viewGroup.removeView(myVideoView);
                    viewGroup.addView(myNormalView);
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String arg1, String message,
                                     final JsResult result) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("")
                        .setMessage(message)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                result.confirm();
                            }
                        })
                        .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                            }
                        });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        result.cancel();
                    }
                });

                // 防止重复点击
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        return false;
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }

            @Override
            public void onProgressChanged(WebView webView, int newProgress) {
                super.onProgressChanged(webView, newProgress);
                // 这里将textView换成你的progress来设置进度
                if (newProgress == 0) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                progressBar.setProgress(newProgress);
                progressBar.postInvalidate();
                if (newProgress >= 80) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedTitle(WebView webView, String title) {
                super.onReceivedTitle(webView, title);
                setTitle(title);
            }
        });

        WebSettings webSetting = mWebView.getSettings();
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(false);
        // webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        // webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setJavaScriptEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setAppCachePath(this.getDir("appcache", 0).getPath());
        webSetting.setDatabasePath(this.getDir("databases", 0).getPath());
        webSetting.setGeolocationDatabasePath(this.getDir("geolocation", 0)
                .getPath());
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);

        // 将网页中的title隐藏
        mWebView.addJavascriptInterface(new InJavaScriptLocalObj(), "java_obj");

        // webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // webSetting.setPreFectch(true);
        long time = System.currentTimeMillis();
        if (mIntentUrl == null) {
            mWebView.loadUrl(mHomeUrl);
        } else {
            mWebView.loadUrl(mIntentUrl.toString());
        }
        TbsLog.d("time-cost", "cost time: "
                + (System.currentTimeMillis() - time));
        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().sync();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView != null && mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            } else
                return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0:
                    if (null != uploadFile) {
                        Uri result = data == null || resultCode != RESULT_OK ? null
                                : data.getData();
                        uploadFile.onReceiveValue(result);
                        uploadFile = null;
                    }
                    break;
                default:
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (null != uploadFile) {
                uploadFile.onReceiveValue(null);
                uploadFile = null;
            }

        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent == null || mWebView == null || intent.getData() == null)
            return;
        mWebView.loadUrl(intent.getData().toString());
    }

    @Override
    protected void onDestroy() {
        if (mTestHandler != null)
            mTestHandler.removeCallbacksAndMessages(null);
        if (mWebView != null)
            mWebView.destroy();
        super.onDestroy();
    }

    public static final int MSG_INIT_UI = 1;
    @SuppressLint("HandlerLeak")
    private Handler mTestHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT_UI:
                    init();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private class InJavaScriptLocalObj {
        @JavascriptInterface
        public void getSource(String title) {
            //拿到返回的"用车"标题
            getSupportActionBar().setTitle(title);
        }
    }

}
