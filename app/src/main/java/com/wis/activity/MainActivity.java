package com.wis.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.wis.R;
import com.wis.application.WisApplication;
import com.wis.face.WisUtil;

import java.util.Date;

public class MainActivity extends Activity implements View.OnClickListener {

    private WisApplication application;
    private LinearLayout compareLayout, detectLayout, manageLayout;
    // 手机按钮监听
    private long preTime;

    private void test()
    {
        Bitmap bmp = BitmapFactory.decodeFile("/sdcard/wis/images/1.jpg");
        byte[] rgbData = WisUtil.getRGBByBitmap(bmp);
        //detect face return face rect(x,y,width,height) in picture
        long startTime = System.nanoTime();
        int[] ret = application.getWisMobile().detectFace(rgbData, bmp.getWidth(),
                bmp.getHeight(), bmp.getWidth() * 3);
        long consumingTime = System.nanoTime() - startTime;
        Log.i("wisMobile", "detect time  " + consumingTime/1000);
        Log.i("wisMobile", "detectFace size=" + ret[0] + ",rect(x,y,width,height) = " + ret[1] + "," + ret[2] + "," + ret[3] + "," + ret[4]);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        Log.i("MainActivity", "onCreate");

        //加载模型
        application = (WisApplication) getApplication();
        application.loadWisMobile();

//        for (int i = 0; i < 10000000; i++){
//            Log.d("wisMobile","------------->"+i);
//            test();
//        }

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                //加载模型
//                application = (WisApplication) getApplication();
//                application.loadWisMobile();
//            }
//        }).start();
    }

    private void initView() {
        detectLayout = (LinearLayout) findViewById(R.id.detect_layout);
        detectLayout.setOnClickListener(this);
        compareLayout = (LinearLayout) findViewById(R.id.compare_layout);
        compareLayout.setOnClickListener(this);
        manageLayout = (LinearLayout) findViewById(R.id.manage_layout);
        manageLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.detect_layout:

                intent = new Intent(MainActivity.this, DetectActivity.class);
                break;
            case R.id.compare_layout:
                intent = new Intent(MainActivity.this, CompareActivity.class);
                break;
            case R.id.manage_layout:
                intent = new Intent(MainActivity.this, ManageActivity.class);
                break;
            default:
                break;
        }
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 截获后退键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long currentTime = new Date().getTime();
            // 如果时间间隔大于2秒, 不处理
            if ((currentTime - preTime) > 2 * 1000) {
                // 显示消息
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                // 更新时间
                preTime = currentTime;
                // 截获事件,不再处理
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

}
