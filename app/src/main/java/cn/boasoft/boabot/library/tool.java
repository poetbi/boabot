package cn.boasoft.boabot.library;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class tool {
    private static final char[] hexCode = "0123456789abcdef".toCharArray();

    public static String _error(int code){
        String[] arr = {
                "成功", //0
                "APP未安装", //1
                "运行对象未找到", //2
                "目标未找到", //3
                "执行失败", //4
                "获取网络内容失败", //5
                "坐标点击失败", //6
                "坐标操作失败", //7
                "请开启boa机器人", //8
                "任务终止" //9
        };

        if(code < 0){
            return "未知";
        }else{
            return arr[code];
        }
    }

    public static String _socket(int code){
        String[] arr = {"未连接", "已连接", "已认证", "通信中", "已关闭"};
        return arr[code];
    }

    public static long str2time(String str, String format) {
        if(format == null || format.equals("")){
            format = "yyyyMMdd H:m:s";
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
            return sdf.parse(str).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String time2str(String format, long time){
        if(time <= 0){
            time = System.currentTimeMillis();
        }

        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(date);
    }

    public static int indexof(String str, String[] arr){
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(str)) {
                return i;
            }
        }
        return 0;
    }

    public static String md5(String input) throws NoSuchAlgorithmException {
        byte[] bytes = MessageDigest.getInstance("MD5").digest(input.getBytes());
        return printHexBinary(bytes);
    }

    private static String printHexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }
}