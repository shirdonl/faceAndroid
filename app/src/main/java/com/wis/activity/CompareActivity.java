package com.wis.activity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wis.R;
import com.wis.application.WisApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;

@SuppressLint("SdCardPath")
public class CompareActivity extends Activity implements View.OnClickListener {

    private WisApplication application;

    private Button leftLocalBtn, leftTakeBtn, rightLocalBtn, rightTakeBtn, compareBtn;
    private ImageView leftImageView, rightImageView;
    private TextView resultView;
    private ProgressBar progressBar;
    private ActionBar actionBar;

    private File tempFile = new File(Environment.getExternalStorageDirectory(),
            getPhotoFileName());

    private int photoDirection;
    private static final int PHOTO_LEFT = 1;// 左侧
    private static final int PHOTO_RIGHT = 2;// 右侧

    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果

    private Handler mHandler;
    private final static int COMPARE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);
        application = (WisApplication) getApplication();
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        initView();
        // 比对，更新UI
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case COMPARE:
                        Log.i("face", "score= start");
                        String fileName1 = "/sdcard/wis/images/1.jpg";
                        String fileName2 = "/sdcard/wis/images/2.jpg";
                        //compare 2 faces in 2 picture
                        Log.e("compare","1");
                        float score = application.getWisMobile().calculate2ImageSimilarity(fileName1, fileName2);
                        Log.e("compare","2");
                        Log.i("face", "score=" + score);

                        progressBar.setVisibility(View.GONE);
                        resultView.setText("比对结果：" + (score * 100) + "%");
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void initView() {
        leftImageView = (ImageView) findViewById(R.id.left_img);
        rightImageView = (ImageView) findViewById(R.id.right_img);

        leftLocalBtn = (Button) findViewById(R.id.left_local);
        leftTakeBtn = (Button) findViewById(R.id.left_take);
        rightLocalBtn = (Button) findViewById(R.id.right_local);
        rightTakeBtn = (Button) findViewById(R.id.right_take);

        leftLocalBtn.setOnClickListener(this);
        leftTakeBtn.setOnClickListener(this);
        rightLocalBtn.setOnClickListener(this);
        rightTakeBtn.setOnClickListener(this);

        compareBtn = (Button) findViewById(R.id.compare);
        compareBtn.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progress);
        resultView = (TextView) findViewById(R.id.result);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_local:
                photoDirection = PHOTO_LEFT;
                // 本地图片
                Intent leftLocalIntent = new Intent(Intent.ACTION_PICK, null);
                leftLocalIntent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(leftLocalIntent, PHOTO_REQUEST_GALLERY);
                break;
            case R.id.left_take:
                photoDirection = PHOTO_LEFT;
                // 调用系统的拍照功能
                Intent leftTakeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 指定调用相机拍照后照片的储存路径
                leftTakeIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(tempFile));
                startActivityForResult(leftTakeIntent, PHOTO_REQUEST_TAKEPHOTO);
                break;
            case R.id.right_local:
                photoDirection = PHOTO_RIGHT;
                // 本地图片
                Intent rightLocalIntent = new Intent(Intent.ACTION_PICK, null);
                rightLocalIntent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(rightLocalIntent, PHOTO_REQUEST_GALLERY);
                break;
            case R.id.right_take:
                photoDirection = PHOTO_RIGHT;
                // 调用系统的拍照功能
                Intent rightTakeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 指定调用相机拍照后照片的储存路径
                rightTakeIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(tempFile));
                startActivityForResult(rightTakeIntent, PHOTO_REQUEST_TAKEPHOTO);
                break;
            case R.id.compare:
                resultView.setText("");
                progressBar.setVisibility(View.VISIBLE);

                Message msg = new Message();
                msg.what = COMPARE;
                mHandler.sendMessage(msg);

                /*
                //test
                Bitmap bmp = BitmapFactory.decodeFile(fileName1);
                //get rgb data buffer from bitmap
                byte[] rgbData = WisUtil.getRGBByBitmap(bmp);
                //detect face return face rect(x,y,width,height) in picture
                int[] ret = wisMobile.detectFace(rgbData,bmp.getWidth(),bmp.getHeight(),bmp.getWidth()*3);
                int faceNum = ret[0];
                Log.i("wisMobile","detectFace size="+ret[0]+ ",rect(x,y,width,height) = "+ret[1] + ","+ret[2]+ ","+ret[3]+ ","+ret[4]);
                int faceRect[] = {ret[1],ret[2],ret[3],ret[4]};
                float[] fea = wisMobile.extractFeature(rgbData,bmp.getWidth(),bmp.getHeight(),bmp.getWidth()*3,faceRect);

                Bitmap bmp2 = BitmapFactory.decodeFile(fileName2);
                byte[] rgbData2 = WisUtil.getRGBByBitmap(bmp2);
                int[] ret2 = wisMobile.detectFace(rgbData2,bmp2.getWidth(),bmp2.getHeight(),bmp2.getWidth()*3);
                Log.i("wisMobile","detectFace size="+ret2[0]+ ",rect(x,y,width,height) = "+ret2[1] + ","+ret2[2]+ ","+ret2[3]+ ","+ret2[4]);
                int faceRect2[] = {ret2[1],ret2[2],ret2[3],ret2[4]};
                float[] fea2 = wisMobile.extractFeature(rgbData2,bmp2.getWidth(),bmp2.getHeight(),bmp2.getWidth()*3,faceRect2);

                //calculate two face feature similarity(0,1)
                score = wisMobile.compare2Feature(fea,fea2);
                Log.i("wisMobile","Face score="+score);
                */

                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PHOTO_REQUEST_TAKEPHOTO:
                startPhotoZoom(Uri.fromFile(tempFile), 150);
                break;
            case PHOTO_REQUEST_GALLERY:
                if (data != null)
                    startPhotoZoom(data.getData(), 150);
                break;
            case PHOTO_REQUEST_CUT:
                if (data != null)
                    setPicToView(data);
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startPhotoZoom(Uri uri, int size) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "false");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", size);
        intent.putExtra("outputY", size);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    // 将进行剪裁后的图片显示到UI界面上
    @SuppressWarnings("deprecation")
    private void setPicToView(Intent picdata) {
        Bundle bundle = picdata.getExtras();
        if (bundle != null) {
            Bitmap photo = bundle.getParcelable("data");
            // 更新UI
            Drawable drawable = new BitmapDrawable(photo);
            switch (photoDirection) {
                case PHOTO_LEFT:
                    leftImageView.setBackgroundDrawable(drawable);
                    break;
                case PHOTO_RIGHT:
                    rightImageView.setBackgroundDrawable(drawable);
                    break;
            }
            // 保存Bitmap到本地
            saveBitmap(photo);
        }
    }

    private void saveBitmap(Bitmap bmp) {
        File file = new File("/sdcard/wis/images");
        if (!file.exists()) {
            file.mkdir();
        }
        switch (photoDirection) {
            case PHOTO_LEFT:
                file = new File("/sdcard/wis/images/1.jpg");
                break;
            case PHOTO_RIGHT:
                file = new File("/sdcard/wis/images/2.jpg");
                break;
        }

        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPhotoFileName() {
        return "temp.jpg";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
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
