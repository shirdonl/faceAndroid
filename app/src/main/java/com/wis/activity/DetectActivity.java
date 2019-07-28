package com.wis.activity;

/**
 * Created by ybbz on 16/7/28.
 */

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.wis.R;
import com.wis.application.WisApplication;
import com.wis.bean.Person;
import com.wis.db.DBManager;
import com.wis.util.ImageUtils;
import com.wis.face.WisUtil;
import com.wis.view.CameraPreview;
import com.wis.view.DrawImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class DetectActivity extends Activity {

    private WisApplication application;

    private ImageView detectImageView, similarImageView;
    private TextView nameView, resultView;
    private FrameLayout previewLayout;
    private ActionBar actionBar;

    private Camera mCamera;
    private CameraPreview mPreview;
    public static final int FRONT = 1;
    public static final int BACK = 2;
    private int currentCameraType = -1;

    private Handler handler;
    private Runnable runnable;

    private int counter = 1;
    private Bitmap globalBitmap;
    private int globalWidth, globalHeight = 0;
    private final static float DIGREE_90 = 90;
    private final static float DIGREE_270 = 270;

    private DrawImageView drawImageView;

    private List<Person> persons;
    private DBManager dbManager;

    private String globalMaxName = "";
    private float globalMaxScore = 0;
    private byte[] globalMaxImage = null;


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
        setContentView(R.layout.activity_detect);
        Log.i("DetectActivity", "onCreate");

        if (!checkCamera()) {
            finish();
        }
        application = (WisApplication) getApplication();
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        initView();
        initList();
        try {
            mCamera = openCamera(FRONT);
        } catch (Exception e) {
            e.printStackTrace();
        }


        mPreview = new CameraPreview(this, mCamera);
        previewLayout.addView(mPreview);
        drawImageView = new DrawImageView(DetectActivity.this, null);
        drawImageView.setParam(1, 1, 10, 10);
        previewLayout.addView(drawImageView);

       // mCamera.startPreview();
//        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
//            @Override
//            public void onPreviewFrame(byte[] data, Camera camera) {
//                //test();
//                Log.i("wisMobile","onPreviewFrame");
//                Bitmap bmp = decodeToBitMap(data,camera);
//                byte[] rgbData = WisUtil.getRGBByBitmap(bmp);
//                //detect face return face rect(x,y,width,height) in picture
//                long startTime = System.nanoTime();
//                int[] ret = application.getWisMobile().detectFace(rgbData, bmp.getWidth(),
//                        bmp.getHeight(), bmp.getWidth() * 3);
//                long consumingTime = System.nanoTime() - startTime;
//                Log.i("wisMobile", "detect time  " + consumingTime/1000);
//                Log.i("wisMobile", "detectFace size=" + ret[0] + ",rect(x,y,width,height) = " + ret[1] + "," + ret[2] + "," + ret[3] + "," + ret[4]);
////                //人脸标记
//                int size = ret[0];
//                if (size > 0) {
//                    int ret_x = ret[1];
//                    int ret_y = ret[2];
//                    int ret_width = ret[3];
//                    int ret_height = ret[4];
//                    Log.e("wisMobile", "detectFace size=" + size + ",rect(x,y,width,height) = " + ret_x + "," + ret_y + "," + ret_width + "," + ret_height);
////                    //drawImageView.setParam(detectRetLeft(ret_x), detectRetTop(ret_y), detectRetRight(ret_x, ret_width), detectRetBottom(ret_y, ret_height));
////                    //draw face rect
//                    Canvas _canvas = new Canvas(bmp);
//                    Paint _paint = new Paint();
//                    _paint.setColor(Color.GREEN);
//                    _paint.setStrokeWidth((float) 3.0);              //线宽
//                    _paint.setStyle(Paint.Style.STROKE);             //空心效果
//                    _canvas.drawRect(ret_x, ret_y, ret_x + ret_width, ret_y + ret_height, _paint);
//                }
//               //不用多说吧
//                bmp.recycle();
//                bmp = null;
//            }
//        });

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                //获取相机预览图
                takeSnapPhoto();
                if (globalBitmap != null) {
                    detectImageView.setImageBitmap(globalBitmap);
                    //人脸检测
                    //get rgb data buffer from bitmap
                    byte[] rgbData = WisUtil.getRGBByBitmap(globalBitmap);
                    //detect face return face rect(x,y,width,height) in picture
                    long startTime = System.nanoTime();
                    int[] ret = application.getWisMobile().detectFace(rgbData, globalBitmap.getWidth(), globalBitmap.getHeight(), globalBitmap.getWidth() * 3);
                    long consumingTime = System.nanoTime() - startTime;
                    Log.i("wisMobile", "detect time  " + consumingTime/1000);
                    Log.i("wisMobile", "detectFace size=" + ret[0] + ",rect(x,y,width,height) = " + ret[1] + "," + ret[2] + "," + ret[3] + "," + ret[4]);
                    //人脸标记
                    int size = ret[0];
                    if (size > 0) {
                        int ret_x = ret[1];
                        int ret_y = ret[2];
                        int ret_width = ret[3];
                        int ret_height = ret[4];
                        Log.e("wisMobile", "detectFace size=" + size + ",rect(x,y,width,height) = " + ret_x + "," + ret_y + "," + ret_width + "," + ret_height);
                        drawImageView.setParam(detectRetLeft(ret_x), detectRetTop(ret_y), detectRetRight(ret_x, ret_width), detectRetBottom(ret_y, ret_height));
                        //draw face rect
                        Canvas _canvas = new Canvas(globalBitmap);
                        Paint _paint = new Paint();
                        _paint.setColor(Color.GREEN);
                        _paint.setStrokeWidth((float) 3.0);              //线宽
                        _paint.setStyle(Paint.Style.STROKE);             //空心效果
                        _canvas.drawRect(ret_x,ret_y, ret_x + ret_width, ret_y + ret_height, _paint);
                        //相似度计算
                        int faceRect[] = {ret_x, ret_y, ret_width, ret_height};
                        startTime = System.nanoTime();
                        float[] fea = application.getWisMobile().extractFeature(rgbData, globalBitmap.getWidth(), globalBitmap.getHeight(), globalBitmap.getWidth() * 3, faceRect);
                        consumingTime = System.nanoTime() - startTime;
                        Log.i("wisMobile", "extractFeature time  " + consumingTime/1000);
                        similarPerson(fea);
                        //更新界面
                        if (globalMaxScore >= 0.55) {
                            similarImageView.setImageBitmap(ImageUtils.BytesToBitmap(globalMaxImage));
                            nameView.setText("姓名：" + globalMaxName);
                            resultView.setText("相似度：" + globalMaxScore + "\n" + "计数器：" + counter);
                        } else {
                            similarImageView.setImageBitmap(null);
                            nameView.setText("无匹配");
                            resultView.setText("无匹配结果" + "\n" + "计数器：" + counter);
                        }
                    } else {
                        similarImageView.setImageBitmap(null);
                        nameView.setText("无匹配");
                        resultView.setText("无匹配结果" + "\n" + "计数器：" + counter);
                    }
                }
                handler.postDelayed(this, 100);
                counter++;
            }
        };
        handler.postDelayed(runnable, 100);//每两秒执行一次
    }

    private void initView() {
        detectImageView = (ImageView) findViewById(R.id.detect_image);
        similarImageView = (ImageView) findViewById(R.id.similar_image);
        nameView = (TextView) findViewById(R.id.name);
        resultView = (TextView) findViewById(R.id.result);
        previewLayout = (FrameLayout) findViewById(R.id.camera_preview);
    }

    private void initList() {
        dbManager = new DBManager(this);
        persons = dbManager.query();
    }

    private float[] StringToFloatArray(String feaStr) {
        String[] strArray = feaStr.split(",");
        float[] floatArray = new float[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            floatArray[i] = Float.parseFloat(strArray[i]);
        }
        return floatArray;
    }

    private void similarPerson(float[] feature) {
        globalMaxScore = 0;
        for (int i = 0; i < persons.size(); i++) {
            Person person = persons.get(i);
            long startTime = System.nanoTime();
            float score = application.getWisMobile().compare2Feature(feature, StringToFloatArray(person.feature));
            long consumingTime = System.nanoTime() - startTime;
            Log.d("wisMobile", "compare2Feature time  " + consumingTime/1000);
            if (score >= 0.5 && score > globalMaxScore) {
                globalMaxScore = score;
                globalMaxName = person.name;
                globalMaxImage = person.image;
            }
        }
    }

//    private Camera openFrontCamera() {
//        int cameraCount = 0;
//        Camera cam = null;
//
//        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//        cameraCount = Camera.getNumberOfCameras(); // get cameras number
//
//        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
//            Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
//            // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
//            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                try {
//                    cam = Camera.open(camIdx);
//                } catch (RuntimeException e) {
//                    Log.e("DetectActivity", "Camera failed to open: " + e.getLocalizedMessage());
//                }
//            }
//        }
//        return cam;
//    }

    private boolean checkCamera() {
        return DetectActivity.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private Camera openCamera(int type) {
        int frontIndex = -1;
        int backIndex = -1;
        int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int cameraIndex = 0; cameraIndex < cameraCount; cameraIndex++) {
            Camera.getCameraInfo(cameraIndex, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                frontIndex = cameraIndex;
            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                backIndex = cameraIndex;
            }
        }

        currentCameraType = type;
        Camera mCameraDevice = null;
        if (type == FRONT && frontIndex != -1) {
            mCameraDevice = Camera.open(frontIndex);
            Camera.Parameters mParameters = mCameraDevice.getParameters();
            List<Camera.Size> size = mParameters.getSupportedPictureSizes();
            for(Camera.Size s : size){
                Log.i("wisMobile", " capture size  width= " + s.width + ", height="+s.height);
            }
            mParameters.setPreviewSize(640,480);
            mParameters.setPictureSize(640,480);
            mCameraDevice.setParameters(mParameters);

        } else if (type == BACK && backIndex != -1) {
            mCameraDevice = Camera.open(backIndex);
        }
        return mCameraDevice;
    }

    private void changeCamera() throws IOException {

        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;

        if (currentCameraType == FRONT) {
            Log.i("DetectActivity", "BACK");
            mCamera = openCamera(BACK);
        } else if (currentCameraType == BACK) {
            Log.i("DetectActivity", "FRONT");
            mCamera = openCamera(FRONT);
        }
        try {
            mCamera.setPreviewDisplay(mPreview.getHolder());
            mCamera.setDisplayOrientation(getPreviewDegree(DetectActivity.this));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Camera.Parameters parameters = mCamera.getParameters(); // 获取各项参数
        parameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
        parameters.setJpegQuality(50); // 设置照片质量
        mCamera.startPreview();
    }

    /**
     * http://stackoverflow.com/questions/17682345/how-to-get-android-camera-preview-data
     * http://stackoverflow.com/questions/1893072/getting-frames-from-video-image-in-android
     */
    private void takeSnapPhoto() {
        Log.i("DetectActivity", "takeSnapPhoto");
        mCamera.setOneShotPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                globalBitmap = decodeToBitMap(data,camera);
                Log.i("DetectActivity", "globalBitmap update");
            }
        });
    }


    public Bitmap decodeToBitMap(byte[] data, Camera _camera) {
        Camera.Size size = mCamera.getParameters().getPreviewSize();
        Bitmap bmp = null;
        try {
            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
            if (image != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, size.width, size.height),80, stream);
                bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                stream.close();
                Matrix matrix = new Matrix();
                switch (currentCameraType) {
                    case FRONT://前
                        matrix.preRotate(DIGREE_270);
                        break;
                    case BACK://后
                        matrix.preRotate(DIGREE_90);
                        break;
                }
                Log.i("DetectActivity", "DecodeToBitmap");
                bmp  = Bitmap.createBitmap(bmp, 0, 0, size.width, size.height, matrix, true);
            }
        } catch (Exception ex) {
            Log.e("Sys", "Error:" + ex.getMessage());
        }
        return bmp;
    }
    //这个方法存在内存泄漏,导致程序内存溢出,可以pass掉了.
     public Allocation renderScriptNV21ToRGBA888(Context context, int width, int height, byte[] nv21) {
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

        Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
        Allocation in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

        Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
        Allocation out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
        in.copyFrom(nv21);
        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        in.destroy();
        return out;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.camera_switch:
                //切换摄像头
                Toast.makeText(DetectActivity.this, R.string.camera_switch, Toast.LENGTH_SHORT).show();
                try {
                    changeCamera();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 用于根据手机方向获得相机预览画面旋转的角度
     *
     * @param activity
     * @return
     */
    public int getPreviewDegree(Activity activity) {
        // 获得手机的方向
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        // 根据手机的方向计算相机预览画面应该选择的角度
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }

    private int detectRetLeft(int x) {
        int result;
        if (x < 0) {
            result = 0;
        } else if (x > globalWidth) {
            result = globalWidth;
        } else {
            result = x;
        }
        return result;
    }

    private int detectRetTop(int y) {
        int result;
        if (y < 0) {
            result = 0;
        } else if (y > globalHeight) {
            result = globalHeight;
        } else {
            result = y;
        }
        return result;
    }

    private int detectRetRight(int x, int w) {
        int result;
        if (x + w < 0) {
            result = 0;
        } else if (x + w > globalWidth) {
            result = globalWidth;
        } else {
            result = x + w;
        }
        return result;
    }

    private int detectRetBottom(int y, int h) {
        int result;
        if (y + h < 0) {
            result = 0;
        } else if (y + h > globalHeight) {
            result = globalHeight;
        } else {
            result = y + h;
        }
        return result;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onDestroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("DetectActivity", "onDestroy");
        handler.removeCallbacks(runnable);
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