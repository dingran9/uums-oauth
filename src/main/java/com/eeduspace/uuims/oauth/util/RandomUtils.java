package com.eeduspace.uuims.oauth.util;

import java.util.Random;

/**
 * Author: dingran
 * Date: 2015/10/21
 * Description:随机数工具类
 */
public class RandomUtils {

    /**
     * 生成随机 6位字符串
     *
     * @param len
     * @return
     */
    public static String getRandom(int len) {
        len = len == 0 ? 6 : len;
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        int maxPos = chars.length;
        String string = "";
        for (int i = 0; i < len; i++) {
            Random random = new Random();
            string += chars[random.nextInt(maxPos)];
        }
        return string;
    }
}
