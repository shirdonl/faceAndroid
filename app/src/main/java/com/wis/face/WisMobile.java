package com.wis.face;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Created by wis on 16-7-26.
 */
public class WisMobile {



    /**
     * 两张人脸照片进行比对,返回相似度(0~1)之间, 如果大于0.5就代表很相似,如果超过0.9表示两个照片属于同一个人的概率极大
     * @return
     */
    public float calculate2ImageSimilarity(String imgFile1,String imgFile2){
        //请自行判断两个文件的存在性,sdk内部不做文件是否存在判断
        Bitmap bmp1 = BitmapFactory.decodeFile(imgFile1);
        Bitmap bmp2 = BitmapFactory.decodeFile(imgFile2);
        float[] fea1 = detectFace(bmp1);
        if (fea1 == null) return 0;
        float[] fea2 = detectFace(bmp2);
        if (fea2 == null) return 0;
        float score = compare2Feature(fea1,fea2);
        Log.i("wisMobile", "score   " + score);
        return score;
    }

    public float[] detectFace( Bitmap bmp){
        byte[] rgbData = WisUtil.getRGBByBitmap(bmp);
        //detect face return face rect(x,y,width,height) in picture
        long startTime;
        int[] ret = null;
        long consumingTime;
        float[] fea = null;
        // for(int i=0; i < 1000000000; i++) {

        //Log.i("wisMobile","times=>"+i);
        startTime = System.nanoTime();
        ret = detectFace(rgbData, bmp.getWidth(), bmp.getHeight(), bmp.getWidth() * 3);
        consumingTime = System.nanoTime() - startTime;
        Log.i("wisMobile", "detect time  " + consumingTime / 1000);
        Log.i("wisMobile", "detectFace num =" + ret[0] + ",rect(x,y,width,height) = " + ret[1] + "," + ret[2] + "," + ret[3] + "," + ret[4]);

        if(ret[0] < 1){
            return null;
        }
        startTime = System.nanoTime();
        int faceRect[] = {ret[1], ret[2], ret[3], ret[4]};
        fea = extractFeature(rgbData, bmp.getWidth(),bmp.getHeight(), bmp.getWidth() * 3, faceRect);
        consumingTime = System.nanoTime() - startTime;
        Log.i("wisMobile", "extractFeature time  " + consumingTime/1000);
        // }
        return fea;
    }



    public native int loadModel(String modelDir);  // required

    //face
    public native float compare2Image(String imgFile1, String imgFile2);

    //face detect
    public native int[] detectFace(byte[] rgb32, int width, int height, int widthStep);

    //extract face feature
    public native float[] extractFeature(byte[] rgb32, int width, int height, int widthStep, int[] faceRect);

    //calculate two face feature similarity
    public native float compare2Feature(float[] fea1, float[] fea2);
}
