package com.eeduspace.uuims.oauth.util;

import java.util.Date;
import java.util.UUID;

/**
 * Author: dingran
 * Date: 2015/10/21
 * Description:生成唯一序号的辅助工具类
 */
public class UIDGenerator {

    private static final int[] DEFAULT_CONFOUNDER = {3, 6, 7, 1, 8, 9, 5, 2};

    /**
     * 获取UUID
     *
     * @return
     */
    public static String getShortUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 获取UUID
     *
     * @return
     */
    public static String getUUID() {
        return UUID.randomUUID().toString();
    }


    /**
     * 把一个输入整数置换为另一个数
     *
     * @param num
     * @return
     */
    private static long confuse(long num, int[] confounder) {
        String tempStr = num + "";
        int length = confounder.length;
        int numLength = tempStr.length();

        //检查输入数值是否过大
        if (length < numLength) {
            throw new RuntimeException("confounder length must greater then number length, " + length + " : " + numLength);
        }

        String output = "";
        char[] input = tempStr.toCharArray();

        int confounderIndex = Integer.parseInt(input[input.length - 1] + "") % 8;
        int paddingLength = length - numLength;

        for (int i = 0; i < paddingLength; i++) {
            confounderIndex = (confounderIndex + 1) % 8;
            output = output + (confounder[confounderIndex] % 10);
        }

        for (int i = 0; i < numLength; i++) {
            confounderIndex = (confounderIndex + 1) % 8;
            output = output + ((Integer.parseInt(input[i] + "") + confounder[confounderIndex]) % 10);
        }
        try {
            return Long.parseLong(output);
        } catch (Exception e) {
            throw new RuntimeException("confuse number overflow : " + output);
        }
    }

    public static String generateSysUID(String sysFlag) {
        if (sysFlag == null || sysFlag.length() != 2) {
            throw new RuntimeException("sysFlag must be 2 length");
        }
        return sysFlag + getShortUUID();
    }

    /**
     * 生成业务流水号
     *
     * @param sequence 序列号
     * @param bizFlag  业务标识
     * @return
     */
    public static String generateBizUID(long sequence, String bizFlag) {
        return generateBizUID(sequence, bizFlag, new Date());
    }

    /**
     * 生成业务流水号
     *
     * @param sequence 序列号
     * @param bizFlag  业务标识
     * @param date     业务发生时间
     * @return
     */
    public static String generateBizUID(long sequence, String bizFlag, Date date) {
        if (bizFlag == null || bizFlag.length() != 2) {
            throw new RuntimeException("bizFlag must be 2 length");
        }
        String dateStr = FormatUtils.formatDate(date, "yyMMdd", null);
        return bizFlag + "A" + dateStr + confuse(sequence, DEFAULT_CONFOUNDER);
    }
}
