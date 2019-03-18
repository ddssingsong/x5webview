package com.dds.x5webview;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.dds.x5web.X5WebViewActivity;
import com.dds.x5web.XFileReaderActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        X5WebViewActivity.openActivity(this, "http://www.baidu.com");

    }

    public void onClick1(View view) {
        XFileReaderActivity.openActivity(this, Environment.getExternalStorageDirectory() + "/test.pdf");
    }
}
