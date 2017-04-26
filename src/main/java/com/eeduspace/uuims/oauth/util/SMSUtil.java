package com.eeduspace.uuims.oauth.util;


import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;


@Component("SMSUtil")
public class SMSUtil {
    protected final Logger logger = LoggerFactory.getLogger(SMSUtil.class);

    @Value("${uuims.sms.url}")
    private String sms_url;
//    @Value("${uuims.sms.password}")
//    private String sms_password;
//    @Value("${uuims.sms.openId}")
//    private String sms_openId;
   
    
    
    
    
    public static void main(String[] args) {
		SMSUtil util = new SMSUtil();
		String send = util.send("13520465210", "reset_password",null,null);
		System.out.println(send);
	}
    
    
   
    public String send(String telephone, String type,String sms_password, String sms_openId) {

//    	String sms_url = "http://182.92.1.35/uutool/sms/verificationCode?";
//    	String sms_password = "47ec2dd791e31e2ef2076caf64ed9b3d";
//    	String sms_openId   = "c8a856f8-a5af-4ef7-b850-a76c979265c7";
        String code = "";
        logger.debug("sms_url:{},sms_password:{},sms_openId:{},type:{},telephone:{}",sms_url,sms_password,sms_openId,type,telephone);
        if (StringUtils.isNotBlank(sms_url)
                && StringUtils.isNotBlank(sms_password)
                && StringUtils.isNotBlank(sms_openId)
                && StringUtils.isNotBlank(type)) {

            String paramter = "phone=" + telephone + "&requestId=''&password="
                    + sms_password + "&openId=" + sms_openId
                    + "&sendType="+type;
            logger.debug(paramter);
            // 发送短信
            String result = postRequest(sms_url, paramter);
            // result="{"message":"Success","result":{"code":"482664"},"code":"Success","requestId":"sdfsds2343546546ffsdfw","httpCode":"200"}";

            logger.debug("sms client result:{}",result);
            // 解析返回结果 获取验证码code值
            if (StringUtils.isNotBlank(result)) {
                JsonNode jsonNode = Json.parse(result);
                // 获取 code
                JsonNode subJsonNode = jsonNode.findValue("result");
                if (subJsonNode != null) {
                    JsonNode codeJsonNode = subJsonNode.findValue("code");
                    if (codeJsonNode != null) {
                        code = codeJsonNode.textValue();
                    }
                }

            }

        }

        return code;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url
     *            发送请求的 URL
     * @param paramter
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */

    public String postRequest(String url, String paramter) {

        // url: http://218.240.38.108/uutool/sms/verificationCode?
        // paramter:
        // phone=value1&requestId=sdfsds2343546546ffsdfw&password=e10adc3949ba59abbe56e057f20f883e&openId:d4ae37ce-24a8-4763-82b5-51b261a416c8

        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(paramter);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
}
