package com.dds.x5web;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;

public class XFileReaderActivity extends AppCompatActivity implements TbsReaderView.ReaderCallback {
    private FrameLayout x_root;
    private TbsReaderView mTbsReaderView;


    private String filePath;

    // isShare 是否显示分享按钮  isToolBar 是否显示toolbar
    public static void openActivity(Activity activity, String filePath) {
        Intent intent = new Intent(activity, XFileReaderActivity.class);
        intent.putExtra("filePath", filePath);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xfile_reader);
        initView();
        initData();


    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        initData();
        super.onNewIntent(intent);
    }

    private void initView() {
        x_root = findViewById(R.id.x_root);
        mTbsReaderView = new TbsReaderView(XFileReaderActivity.this, this);
        CoordinatorLayout.LayoutParams lp = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.setBehavior(new AppBarLayout.ScrollingViewBehavior());
        x_root.addView(mTbsReaderView, lp);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XFileReaderActivity.this.finish();
            }
        });


    }

    private void initData() {
        Intent intent = getIntent();
        filePath = intent.getStringExtra("filePath");
        File file = new File(getFilesDir(), "TbsReaderTemp");
        if (!file.exists()) {
            file.mkdirs();
        }
        Bundle localBundle = new Bundle();
        localBundle.putString("filePath", filePath);
        localBundle.putString("tempPath", file.getAbsolutePath());
        boolean bool = this.mTbsReaderView.preOpen(getFileType(filePath), false);
        if (bool) {
            this.mTbsReaderView.openFile(localBundle);
        }
    }

    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {

    }

    private String getFileType(String paramString) {
        String str = "";
        if (TextUtils.isEmpty(paramString)) {
            return str;
        }
        int i = paramString.lastIndexOf('.');
        if (i <= -1) {
            return str;
        }
        str = paramString.substring(i + 1);
        return str;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mTbsReaderView != null) {
            mTbsReaderView.onStop();
        }
    }
}
