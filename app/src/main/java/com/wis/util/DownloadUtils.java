package com.wis.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Environment;
import android.util.Log;

/**
 * Created by ybbz on 16/9/7.
 */

public class DownloadUtils {
    /**
     * 连接url
     */
    private String urlstr;
    /**
     * sd卡目录路径
     */
    private String sdcard;
    /**
     * http连接管理类
     */
    private HttpURLConnection urlcon;

    public DownloadUtils(String url) {
        this.urlstr = url;
        //获取设备sd卡目录
        this.sdcard = Environment.getExternalStorageDirectory() + "/";
        urlcon = getConnection();
    }

    /*
     * 读取网络文本
     */
    public String downloadAsString() {
        StringBuilder sb = new StringBuilder();
        String temp = null;
        try {
            InputStream is = urlcon.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /*
     * 获取http连接处理类HttpURLConnection
     */
    private HttpURLConnection getConnection() {
        URL url;
        HttpURLConnection urlcon = null;
        try {
            url = new URL(urlstr);
            urlcon = (HttpURLConnection) url.openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return urlcon;
    }

    /*
     * 获取连接文件长度。
     */
    public int getLength() {
        return urlcon.getContentLength();
    }

    public int downloadToSD(String strOutFileName, downhandler handler) throws IOException {
        Log.i("wisface", "start copy file " + strOutFileName);
        String tmpFile = "/sdcard/wis/" + strOutFileName;
        File f = new File(tmpFile);
        if (f.exists()) {
            Log.i("wisface", "file exists " + strOutFileName);
            return 0;
        }
        InputStream myInput;
        java.io.OutputStream myOutput = new FileOutputStream("/sdcard/wis/" + strOutFileName);
        myInput = urlcon.getInputStream();
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
            //同步更新数据
            handler.setSize(buffer.length);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
        Log.i("wisface", "end copy file " + strOutFileName);
        return 1;
    }

    /*
     * 写文件到sd卡 demo
     * 前提需要设置模拟器sd卡容量，否则会引发EACCES异常
     * 先创建文件夹，在创建文件
     */
    public int down2sd(String dir, String filename, downhandler handler) {
        StringBuilder sb = new StringBuilder(sdcard).append(dir);
        File file = new File(sb.toString());
        if (!file.exists()) {
            file.mkdirs();
            //创建文件夹
            Log.d("log", sb.toString());
        }
        //获取文件全名
        sb.append(filename);
        file = new File(sb.toString());

        FileOutputStream fos = null;
        try {
            InputStream is = urlcon.getInputStream();
            //创建文件
            file.createNewFile();
            fos = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            while ((is.read(buf)) != -1) {
                fos.write(buf);
                //同步更新数据
                handler.setSize(buf.length);
            }
            is.close();
        } catch (Exception e) {
            return 0;
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 1;
    }

    /*
     * 内部回调接口类
     */
    public abstract class downhandler {
        public abstract void setSize(int size);
    }
}
