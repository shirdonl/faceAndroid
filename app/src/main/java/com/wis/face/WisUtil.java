package com.wis.face;
import android.graphics.Bitmap;
/**
 * Created by wis on 16-8-15.
 */
public class WisUtil {


    /*
     * 获取位图的RGB数据
     */
    public static byte[]  getRGBByBitmap(Bitmap bitmap)
    {
        if (bitmap == null)
        {
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int size = width * height;

        int pixels[] = new int[size];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        byte[]data = convertColorToByte(pixels);

        return data;
    }

    /*
     * 像素数组转化为RGB数组
     */
    public static byte[] convertColorToByte(int color[])
    {
        if (color == null)
        {
            return null;
        }

        byte[] data = new byte[color.length * 3];
        for(int i = 0; i < color.length; i++)
        {
            data[i * 3] = (byte) (color[i] >> 16 & 0xff);
            data[i * 3 + 1] = (byte) (color[i] >> 8 & 0xff);
            data[i * 3 + 2] =  (byte) (color[i] & 0xff);
        }

        return data;

    }
}


