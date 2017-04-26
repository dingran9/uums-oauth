package com.eeduspace.uuims.oauth.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Author: dingran
 * Date: 2015/10/21
 * Description:格式化工具类(把对象格式化成字符串)
 */
public class FormatUtils {
    /**
     * 格式化时间
     *
     * @param date     时间参数
     * @param pattern  格式模板
     * @param timeZone 时区
     * @return 格式化后的输出文本
     */
    public static String formatDate(Object date, String pattern, TimeZone timeZone) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        if (timeZone != null) {
            format.setTimeZone(timeZone);
        }
        return format.format(date);
    }

    /**
     * 格式化数字
     *
     * @param num        数字参数
     * @param mode       取舍模式
     * @param pattern    格式模板
     * @param multiplier 乘数(如果要转换为百分比或者千分比)
     * @return 格式化后的输出文本
     */
    public static String formatNumber(Object num, String pattern, RoundingMode mode, int multiplier) {
        DecimalFormat format = new DecimalFormat(pattern);
        format.setRoundingMode(mode);
        format.setMultiplier(multiplier);
        return format.format(num);
    }


    /**
     * 格式化手机号以便显示
     *
     * @param cellphoneNo
     * @return
     */
    public static String formatCellphone(String cellphoneNo) {
        if (cellphoneNo == null || cellphoneNo.trim().length() != 11) {
            return cellphoneNo;
        }
        return new StringBuilder().append(cellphoneNo.substring(0, 3))
                .append("****").append(cellphoneNo
                        .substring(cellphoneNo.length() - 4)).toString();
    }

    /**
     * 格式化邮箱地址便于显示
     *
     * @param email
     * @return
     */
    public static String formatEmail(String email) {
        return email;
    }
}
