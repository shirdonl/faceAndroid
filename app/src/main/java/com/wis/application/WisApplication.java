package com.wis.application;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;
import com.wis.face.WisMobile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ybbz on 16/9/1.
 */
public class WisApplication extends Application {

    private static final String TAG = "WisApplication";

    private static final int ASSETS_SUFFIX_BEGIN = 0;
    private static final int ASSETS_SUFFIX_END = 9;

    //private CaffeMobile caffeMobile;
    private WisMobile wisMobile;
    File sdcard = Environment.getExternalStorageDirectory();
    String modelDir = "/sdcard/wis";

    static {
        System.loadLibrary("wis_engine_jni");

        File destDir = new File("/sdcard/wis");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Create");
        //initCopyFiles();
        //initWisMobile();

        //umeng
        MobclickAgent.setScenarioType(getApplicationContext(), MobclickAgent.EScenarioType.E_UM_NORMAL);
    }

    private void initWisMobile() {
        wisMobile = new WisMobile();
       // wisMobile.setNumThreads(2);
        wisMobile.loadModel(modelDir);
    }

    public void loadWisMobile() {
        Log.i("WisApplication", "loadWisMobile");
        wisMobile = new WisMobile();
        wisMobile.loadModel(modelDir);
    }

    private void initCopyFiles() {
        try {
            Log.i("wisface", "start copy files----------> ");
            copyBigDataBase();

            copyBigDataToSD("file1-proto");
            copyBigDataToSD("file2-model");
            copyBigDataToSD("fdetector_model.dat");

            Log.i("wisface", "end start copy files----------> ");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void copyBigDataBase() throws IOException {
        InputStream myInput;
        String outFileName = "/sdcard/wis/model_small.xml.gz";
        Log.i("wisface", "start copy file " + outFileName);
        File f = new File(outFileName);
        if (f.exists()) {
            Log.i("wisface", "file exists " + outFileName);
            return;
        }
        OutputStream myOutput = new FileOutputStream(outFileName);
        for (int i = ASSETS_SUFFIX_BEGIN; i < ASSETS_SUFFIX_END + 1; i++) {
            String filename = "wis_alignment_0" + i;
            Log.i("wisface", "start copy files--- " + filename);
            myInput = this.getAssets().open(filename);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();
            myInput.close();
        }
        myOutput.close();
        Log.i("wisface", "end copy file " + outFileName);
    }

    private void copyBigDataToSD(String strOutFileName) throws IOException {
        Log.i("wisface", "start copy file " + strOutFileName);
        String tmpFile = "/sdcard/wis/" + strOutFileName;
        File f = new File(tmpFile);
        if (f.exists()) {
            Log.i("wisface", "file exists " + strOutFileName);
            return;
        }
        InputStream myInput;
        java.io.OutputStream myOutput = new FileOutputStream("/sdcard/wis/" + strOutFileName);
        myInput = this.getAssets().open(strOutFileName);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
        Log.i("wisface", "end copy file " + strOutFileName);
    }

    public WisMobile getWisMobile() {
        return wisMobile;
    }

}
