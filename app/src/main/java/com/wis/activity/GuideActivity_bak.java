package com.wis.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wis.R;

import java.util.Timer;
import java.util.TimerTask;


public class GuideActivity_bak extends Activity {

    private ProgressBar progressBar;
    private TextView nameView;
    private Handler mHandler;

    private TimerTask task1, task2 = null;
    private Timer timer1, timer2 = null;
    private static final int DELAY_TIME = 1000;
    private boolean timeTag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_guide);
        getActionBar().hide();
        initView();
        initTimer();
    }


    private void initView() {
        // nameView = (TextView) findViewById(R.id.app_name);
        // progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    private void initTimer() {
        final Intent intent = new Intent(GuideActivity_bak.this, MainActivity.class);
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0x01:
                        if (timeTag) {
                            alertDialog();
                        }
                        break;
                    case 0x02:
                        timeTag = true;
                        break;
                    case 0x03:
                        startActivity(intent);
                        finish();
                    default:
                        break;
                }
            }
        };

        timer1 = new Timer();
        task1 = new TimerTask() {
            @Override
            public void run() {
                Message msg = mHandler.obtainMessage();
                msg.what = 0x01;
                msg.sendToTarget();
            }
        };
        timer1.scheduleAtFixedRate(task1, DELAY_TIME, DELAY_TIME);

        nameView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        timer2 = new Timer();
        task2 = new TimerTask() {
            @Override
            public void run() {
                Message msg = mHandler.obtainMessage();
                msg.what = 0x02;
                msg.sendToTarget();
            }
        };
        timer2.schedule(task2, DELAY_TIME);
    }

    private void alertDialog() {
        new AlertDialog.Builder(GuideActivity_bak.this)
                .setTitle("提示")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("确定删除该记录？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 下载模型文件
                        Toast.makeText(GuideActivity_bak.this, "开始下载模型文件", Toast.LENGTH_SHORT).show();
                        Message msg = mHandler.obtainMessage();
                        msg.what = 0x03;
                        msg.sendToTarget();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 退出app
                        finish();
                    }
                }).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer1.cancel();
        timer2.cancel();
    }

}
