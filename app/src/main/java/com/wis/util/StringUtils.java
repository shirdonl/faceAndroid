package com.wis.util;

/**
 * Created by ybbz on 16/9/8.
 */
public class StringUtils {

    // 使用java正则表达式去掉多余的.与0
    public static String subZeroAndDot(String s) {
        if (s.indexOf(".") > 0) {
            s = s.replaceAll("0+?$", "");//去掉多余的0
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉
        }
        return s;
    }

    // 去掉小数部分
    public static int doubleNoTail(double lengthnum) {
        //将参数转为字符串
        String str = String.valueOf(lengthnum);
        //获得小数点位置
        int index = str.indexOf(".");
        //整数部分
        String intsub = str.substring(0, index);
        //小数部分
        //String decisub = str.substring(index + 1, str.length());
        return Integer.parseInt(intsub);
    }
}
