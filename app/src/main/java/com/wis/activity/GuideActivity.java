package com.wis.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.panwrona.downloadprogressbar.library.DownloadProgressBar;
import com.umeng.analytics.MobclickAgent;
import com.wis.R;
import com.wis.util.DownloadUtils;
import com.wis.util.NetUtils;
import com.wis.util.StringUtils;

import java.io.File;
import java.text.DecimalFormat;


public class GuideActivity extends Activity {

    private int val = 0;
    private int curVal = 0;
    private int status = 0;
    private Handler handler;
    // private static final String MODEL_FILE = "1.txt";
    //private static final String DOWNLOAD_URL = "http://home.face.ac.cn/1.txt";
    private static final String MODEL_FILE = "file2-model";
    private static final String DOWNLOAD_URL = "http://home.face.ac.cn/file2-model";
    private DownloadProgressBar downloadProgressView;
    private TextView successTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_guide);
        //getActionBar().hide();

        //if (checkModelFile(MODEL_FILE))
        {
            // 跳转到主页
            Intent intent = new Intent(GuideActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
       // else {
          // initDownloadProgressBar();
        //}

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0x01:
                        //主线程ui更新text值
                        //text.setText(msg.obj.toString());
                        val = msg.arg1;
                        Log.e("handleMessage:val", String.valueOf(val));
                        downloadProgressView.setProgress(val);
                        break;
                    case 0x02:
                        if (val == 100 && status == 1) {
                            // 下载完成，跳转到主页
                            Log.e("handleMessage", "MainActivity");
                            Intent intent = new Intent(GuideActivity.this, MainActivity.class);
                            startActivity(intent);
                            onDestroy();
                        }
                    default:
                        break;
                }
            }
        };

    }

    private void initDownloadProgressBar() {
        downloadProgressView = (DownloadProgressBar) findViewById(R.id.dpv3);
        successTextView = (TextView) findViewById(R.id.success_text_view);
        successTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //val = val + 10;
                //downloadProgressView.setProgress(val);
                Log.e("successTextView", "onClick");
            }
        });

        downloadProgressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetUtils.isNetworkAvailable(GuideActivity.this)) {
                    downloadProgressView.playManualProgressAnimation();
                    successTextView.setText("正在下载……");
                    // 检查网络是否连接
                    // TODO
                    // 新线程下载
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //新建一个下载
                            DownloadUtils load = new DownloadUtils(DOWNLOAD_URL);
                            final int len = load.getLength();
                            Log.e("len", String.valueOf(len));

                            /**
                             * 下载文件到sd卡，虚拟设备必须要开始设置sd卡容量
                             * downhandler是Download的内部类，作为回调接口实时显示下载数据
                             */
                            try {
                                status = load.downloadToSD(MODEL_FILE, load.new downhandler() {
                                    @Override
                                    public void setSize(int size) {
                                        Log.e("size", String.valueOf(size));
                                        // 传递消息
                                        Message msg = handler.obtainMessage();
                                        msg.what = 0x01;
                                        // 处理进度条数据
                                        if (len <= size) {
                                            Log.e("s1", "s1");
                                            msg.arg1 = 100;
                                        } else {
                                            curVal += size;
                                            Log.e("curVal", String.valueOf(curVal));
                                            if (curVal >= len) {
                                                Log.e("s2", "s2");
                                                msg.arg1 = 100;
                                            } else {
                                                Log.e("s3", "s3");
                                                double num = (float) curVal / len;
                                                double num_percent = num * 100;
                                                if (num_percent <= 10) {
                                                    msg.arg1 = 10;
                                                } else {
                                                    int percent = StringUtils.doubleNoTail(num_percent);
                                                    Log.e("percent", percent + "");
                                                    msg.arg1 = percent;
                                                }

                                            }
                                        }
                                        msg.sendToTarget();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Log.e("status", Integer.toString(status));
                            if (status == 1) {
                                Log.i("msg:0x02", "sendToTarget");
                                Message msg = handler.obtainMessage();
                                msg.what = 0x02;
                                msg.sendToTarget();
                            } else {
                                Log.i("GuideActivity", "下载文件失败");
                                successTextView.setText("下载文件失败，请重新下载");
                                if (deleteFile(MODEL_FILE)) {
                                    Log.i("GuideActivity", "下载缓存已删除，请重新下载");
                                } else {
                                    Log.i("GuideActivity", "下载缓存删除失败，请手动删除");
                                }
                            }
                        }
                    }).start();
                } else {
                    Toast.makeText(GuideActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();
                }
            }
        });
        downloadProgressView.setOnProgressUpdateListener(new DownloadProgressBar.OnProgressUpdateListener() {
            @Override
            public void onProgressUpdate(float currentPlayTime) {
                Log.i("GuideActivity", "onProgressUpdate");
            }

            @Override
            public void onAnimationStarted() {
                Log.i("GuideActivity", "onAnimationStarted");
                downloadProgressView.setEnabled(false);
            }

            @Override
            public void onAnimationEnded() {
                Log.i("GuideActivity", "onAnimationEnded");
                // val = 0;
                // successTextView.setText("Click to download");
                // downloadProgressView.setEnabled(true);
            }

            @Override
            public void onAnimationSuccess() {
                Log.i("GuideActivity", "onAnimationSuccess");
                successTextView.setText("下载完成，正在启动……");
            }

            @Override
            public void onAnimationError() {
                Log.i("GuideActivity", "onAnimationError");
                successTextView.setText("下载出现错误!");
            }

            @Override
            public void onManualProgressStarted() {
                Log.i("GuideActivity", "onManualProgressStarted");
            }

            @Override
            public void onManualProgressEnded() {
                Log.i("GuideActivity", "onManualProgressEnded");
            }
        });
    }

    private boolean checkModelFile(String modelFileName) {
        Log.i("GuideActivity", "start check model file " + modelFileName);
        String tmpFile = "/sdcard/wis/" + modelFileName;
        File f = new File(tmpFile);
        if (f.exists()) {
            Log.i("GuideActivity", "model file exist.");
            return true;
        }
        Log.i("GuideActivity", "model file does not exist.");
        return false;
    }

    public boolean deleteFile(String modelFileName) {
        Log.i("GuideActivity", "delete check model file " + modelFileName);
        String tmpFile = "/sdcard/wis/" + modelFileName;
        File file = new File(tmpFile);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

//    private void alertDialog() {
//        new AlertDialog.Builder(GuideActivity.this)
//                .setTitle("下载提示")
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .setMessage("使用本App需要提前下载模型文件，确定下载？")
//                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // 下载模型文件
//                        Toast.makeText(GuideActivity.this, "开始下载模型文件", Toast.LENGTH_SHORT).show();
//                        // 开启下载进度条
//
//                        // 下载完成，跳转到主页
//                        Intent intent = new Intent(GuideActivity.this, MainActivity.class);
//                        startActivity(intent);
//                        finish();
//                    }
//                })
//                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // 退出app
//                        Toast.makeText(GuideActivity.this, "取消下载", Toast.LENGTH_SHORT).show();
//                        finish();
//                    }
//                }).show();
//    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("GuideActivity", "onDestroy");
    }

}
