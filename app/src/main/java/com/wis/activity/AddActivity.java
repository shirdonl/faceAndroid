package com.wis.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.wis.R;
import com.wis.application.WisApplication;
import com.wis.bean.Person;
import com.wis.db.DBManager;
import com.wis.util.ImageUtils;
import com.wis.face.WisUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddActivity extends Activity implements View.OnClickListener {

    private WisApplication application;

    private Button leftLocalBtn, leftTakeBtn, addBtn;
    private ImageView leftImageView;
    private EditText nameView;
    private ActionBar actionBar;

    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果

    private Bitmap photo;
    private File tempFile = new File(Environment.getExternalStorageDirectory(), "temp.jpg");

    private DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        application = (WisApplication) getApplication();
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        initView();
        //初始化DBManager
        dbManager = new DBManager(this);
    }

    private void initView() {
        leftImageView = (ImageView) findViewById(R.id.left_img);

        leftLocalBtn = (Button) findViewById(R.id.left_local);
        leftTakeBtn = (Button) findViewById(R.id.left_take);
        leftLocalBtn.setOnClickListener(this);
        leftTakeBtn.setOnClickListener(this);

        nameView = (EditText) findViewById(R.id.name);

        addBtn = (Button) findViewById(R.id.add);
        addBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_local:
                // 本地图片
                Intent leftLocalIntent = new Intent(Intent.ACTION_PICK, null);
                leftLocalIntent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(leftLocalIntent, PHOTO_REQUEST_GALLERY);
                break;
            case R.id.left_take:
                // 调用系统的拍照功能
                Intent leftTakeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 指定调用相机拍照后照片的储存路径
                leftTakeIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(tempFile));
                startActivityForResult(leftTakeIntent, PHOTO_REQUEST_TAKEPHOTO);
                break;
            case R.id.add:
                // 保存图片到SD卡
                String name = nameView.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(AddActivity.this, "姓名为空", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (photo == null) {
                    Toast.makeText(AddActivity.this, "图片为空", Toast.LENGTH_SHORT).show();
                    break;
                }
                saveBitmap(photo, name);

                // 提取图片特征
                //get rgb data buffer from bitmap
                byte[] rgbData = WisUtil.getRGBByBitmap(photo);
                //detect face return face rect(x,y,width,height) in picture
                int[] ret = application.getWisMobile().detectFace(rgbData, photo.getWidth(), photo.getHeight(), photo.getWidth() * 3);
                Log.i("wisMobile", "detectFace size=" + ret[0] + ",rect(x,y,width,height) = " + ret[1] + "," + ret[2] + "," + ret[3] + "," + ret[4]);
                int faceRect[] = {ret[1], ret[2], ret[3], ret[4]};
                float[] fea = application.getWisMobile().extractFeature(rgbData, photo.getWidth(), photo.getHeight(), photo.getWidth() * 3, faceRect);
                Log.i("wisMobile", "extractFeature " + fea);

                //特征数组转化为String
                String feaStr = "";
                for (int i = 0; i < fea.length; i++) {
                    if (i == fea.length - 1) {
                        feaStr = feaStr + fea[i];
                    } else {
                        feaStr = feaStr + fea[i] + ",";
                    }
                }

                // 添加到数据库，并返回人脸管理列表
                Person person = new Person();
                person.name = name;
                person.image = ImageUtils.BitmapToBytes(photo);
                person.feature = feaStr;
                dbManager.addPerson(person);
                // 跳转
                Intent intent = new Intent(AddActivity.this, ManageActivity.class);
                startActivity(intent);
                finish();
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
            photo = bundle.getParcelable("data");
            // 更新UI
            Drawable drawable = new BitmapDrawable(photo);
            leftImageView.setBackgroundDrawable(drawable);
        }
    }

    private void saveBitmap(Bitmap bmp, String name) {
        File file = new File("/sdcard/wis/images/manage");
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File("/sdcard/wis/images/manage/" + name + ".jpg");
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onDestroy();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbManager.closeDB();
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

}
