package com.eeduspace.uuims.oauth.util;


import com.eeduspace.uuims.thirdparty.model.Oauth2Token;
import com.eeduspace.uuims.thirdparty.model.SNSUserInfo;
import com.eeduspace.uuims.thirdparty.model.Ticket;
import com.eeduspace.uuims.thirdparty.model.Token;
import com.google.gson.Gson;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.cxf.jaxrs.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;


/**
 * Author: dingran
 * Date: 2015/11/11
 * Description:高级接口工具类
 */
public class WXAdvancedUtil {

    private static Gson gson=new Gson();
    private static final Logger logger = LoggerFactory.getLogger(WXAdvancedUtil.class);
    private static String ACCESS_TOKEN="https://api.weixin.qq.com/sns/oauth2/access_token";
    private static String REFRESH_TOKEN="https://api.weixin.qq.com/sns/oauth2/refresh_token";
    private static String TOKEN="https://api.weixin.qq.com/cgi-bin/token";
    private static String GETTICKET="https://api.weixin.qq.com/cgi-bin/ticket/getticket";
    private static String UNIFIEDORDER="https://api.mch.weixin.qq.com/pay/unifiedorder";
    private static String USERINFO="https://api.weixin.qq.com/sns/userinfo";
    /**
     * 获取网页授权凭证
     *
     * @param appId 公众账号的唯一标识
     * @param appSecret 公众账号的密钥
     * @param code
     * @return WeixinAouth2Token
     */
    public static Oauth2Token getOauth2AccessToken(String appId, String appSecret, String code) throws IOException {
        Oauth2Token wat = null;
        // 拼接请求地址
        String requestUrl = ACCESS_TOKEN+"?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
        requestUrl = requestUrl.replace("APPID", appId);
        requestUrl = requestUrl.replace("SECRET", appSecret);
        requestUrl = requestUrl.replace("CODE", code);
        // 获取网页授权凭证
  /*      String response = HTTPClientUtils.httpPostRequestJson(requestUrl,"");

        gson.fromJson(response, JSONObject.class);
        logger.debug("--->response:{}",response);*/
        JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "GET", null);
        if (null != jsonObject) {
            try {
                wat = new Oauth2Token();
                wat.setAccessToken(jsonObject.getString("access_token"));
                wat.setExpiresIn(jsonObject.getInt("expires_in"));
                wat.setRefreshToken(jsonObject.getString("refresh_token"));
                wat.setOpenId(jsonObject.getString("openid"));
                wat.setScope(jsonObject.getString("scope"));
            } catch (Exception e) {
                wat = null;
                int errorCode = jsonObject.getInt("errcode");
                String errorMsg = jsonObject.getString("errmsg");
                logger.error("获取网页授权凭证失败 errcode:"+errorCode+" errmsg:"+ errorMsg);
            }
        }
        return wat;
    }

    /**
     * 刷新网页授权凭证
     *
     * @param appId 公众账号的唯一标识
     * @param refreshToken
     * @return WeixinAouth2Token
     */
    public static Oauth2Token refreshOauth2AccessToken(String appId, String refreshToken) {
        Oauth2Token wat = null;
        // 拼接请求地址
        String requestUrl = REFRESH_TOKEN+"?appid=APPID&grant_type=refresh_token&refresh_token=REFRESH_TOKEN";
        requestUrl = requestUrl.replace("APPID", appId);
        requestUrl = requestUrl.replace("REFRESH_TOKEN", refreshToken);
        // 刷新网页授权凭证
        JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "GET", null);
        if (null != jsonObject) {
            try {
                wat = new Oauth2Token();
                wat.setAccessToken(jsonObject.getString("access_token"));
                wat.setExpiresIn(jsonObject.getInt("expires_in"));
                wat.setRefreshToken(jsonObject.getString("refresh_token"));
                wat.setOpenId(jsonObject.getString("openid"));
                wat.setScope(jsonObject.getString("scope"));
            } catch (Exception e) {
                wat = null;
                int errorCode = jsonObject.getInt("errcode");
                String errorMsg = jsonObject.getString("errmsg");
                logger.error("刷新网页授权凭证失败 errcode:"+errorCode+" errmsg:"+ errorMsg);
            }
        }
        return wat;
    }

    /**
     * Description: 获取access_token,access_token是公众号的全局唯一票据，公众号调用各接口时都需使用access_token。
     * @Version 1.0 2015-10-9 上午11:05:05 王斌(wangb@unimlink.com) 创建
     * @param appId
     * @param appSecret
     * @return
     */
    public static Token getAccessToken(String appId, String appSecret) {
        Token token = null;
        // 拼接请求地址
        String requestUrl = TOKEN+"?grant_type=client_credential&appid=APPID&secret=APPSECRET";
        requestUrl = requestUrl.replace("APPID", appId);
        requestUrl = requestUrl.replace("APPSECRET", appSecret);
        // 刷新网页授权凭证
        JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "GET", null);
        if (null != jsonObject) {
            try {
                token = new Token();
                token.setAccessToken(jsonObject.getString("access_token"));
                token.setExpiresIn(jsonObject.getInt("expires_in"));
                token.setCreateDate(new Date());
            } catch (Exception e) {
                token = null;
                logger.error("获取token失败 errcode:"+jsonObject.getInt("errcode")+" errmsg:"+jsonObject.getString("errmsg") ,e);
            }
        }
        return token;
    }

    /**
     * Description: JS-SDK使用权限签名
     * @Version 1.0 2015-10-9 上午11:18:50 王斌(wangb@unimlink.com) 创建
     * @param access_token
     * @return
     */
    public static Ticket getTicket(String access_token) {
        Ticket ticket = null;
        // 拼接请求地址
        String requestUrl = GETTICKET+"?access_token=ACCESS_TOKEN&type=jsapi";
        requestUrl = requestUrl.replace("ACCESS_TOKEN", access_token);
        // 刷新网页授权凭证
        JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "GET", null);
        if (null != jsonObject) {
            try {
                ticket = new Ticket();
                ticket.setErrcode(jsonObject.getInt("errcode"));
                ticket.setErrmsg(jsonObject.getString("errmsg"));
                ticket.setTicket(jsonObject.getString("ticket"));
                ticket.setExpiresIn(jsonObject.getInt("expires_in"));
                ticket.setCreateDate(new Date());
            } catch (Exception e) {
                ticket = null;
                int errorCode = jsonObject.getInt("errcode");
                String errorMsg = jsonObject.getString("errmsg");
                logger.error(" 获取access_token失败 errcode:"+errorCode+" errmsg:"+ errorMsg);
            }
        }
        return ticket;
    }

    /**
     * Description: 统一下单
     * @Version 1.0 2015-10-9 上午11:49:57 王斌(wangb@unimlink.com) 创建
     * @param requestUrl
     * @return
     */
    public static String unifiedOrder(String requestUrl) {
        String str = "";
        try {
            // 统一下单接口提交 xml格式
            URL orderUrl = new URL(UNIFIEDORDER);
            HttpURLConnection conn = (HttpURLConnection) orderUrl.openConnection();
            conn.setConnectTimeout(30000); // 设置连接主机超时（单位：毫秒)
            conn.setReadTimeout(30000); // 设置从主机读取数据超时（单位：毫秒)
            conn.setDoOutput(true); // post请求参数要放在http正文内，顾设置成true，默认是false
            conn.setDoInput(true); // 设置是否从httpUrlConnection读入，默认情况下是true
            conn.setUseCaches(false); // Post 请求不能使用缓存

            // 设定传送的内容类型是可序列化的java对象(如果不设此项,在传送序列化对象时,当WEB服务默认的不是这种类型时可能抛java.io.EOFException)
            conn.setRequestProperty("Content-Type" , "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");// 设定请求的方法为"POST"，默认是GET
            conn.setRequestProperty("Content-Length", requestUrl.length() + "");
            String encode = "utf-8";
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), encode);
            out.write(requestUrl.toString());
            out.flush();
            out.close();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }
            // 获取响应内容体
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line = "";
            StringBuffer strBuf = new StringBuffer();
            while ((line = in.readLine()) != null) {
                strBuf.append(line).append("\n");
            }
            in.close();
            return strBuf.toString().trim();
        }catch (Exception e) {
            logger.error("unifiedOrder Exception:" ,e);
        }
        return str;
    }

    /**
     * 通过网页授权获取用户信息
     *
     * @param accessToken 网页授权接口调用凭证
     * @param openId 用户标识
     * @return SNSUserInfo
     */
    @SuppressWarnings( { "deprecation", "unchecked" })
    public static SNSUserInfo getSNSUserInfo(String accessToken, String openId) throws IOException {
        SNSUserInfo snsUserInfo = null;
        // 拼接请求地址
        String requestUrl = USERINFO+"?access_token=ACCESS_TOKEN&openid=OPENID";
        requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken).replace("OPENID", openId);
        // 通过网页授权获取用户信息
/*        String response = HTTPClientUtils.httpPostRequestJson(requestUrl,"");
        logger.debug("--->response:{}",response);*/
        JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "GET", null);

        if (null != jsonObject) {
            try {
                snsUserInfo = new SNSUserInfo();
                // 用户的标识
                snsUserInfo.setThridOpenId(jsonObject.getString("openid"));
                // 昵称
                snsUserInfo.setNickname(jsonObject.getString("nickname"));
                // 性别（1是男性，2是女性，0是未知）
                snsUserInfo.setSex(jsonObject.getInt("sex"));
                // 用户所在国家
                snsUserInfo.setCountry(jsonObject.getString("country"));
                // 用户所在省份
                snsUserInfo.setProvince(jsonObject.getString("province"));
                // 用户所在城市
                snsUserInfo.setCity(jsonObject.getString("city"));
                // 用户头像
                snsUserInfo.setHeadImgUrl(jsonObject.getString("headimgurl"));
                // 用户特权信息
                snsUserInfo.setPrivilegeList(JSONArray.toList(jsonObject.getJSONArray("privilege"), List.class));
                //AK
                snsUserInfo.setAccessToken(accessToken);
            } catch (Exception e) {
                snsUserInfo = null;
                logger.error("获取用户信息失败  errcode:"+jsonObject.getInt("errcode")+" errmsg:"+ jsonObject.getString("errmsg"));
            }
        }
        return snsUserInfo;
    }

    public static void main(String args[]) {
        // 获取接口访问凭证
        String accessToken = getAccessToken("APPID", "APPSECRET").getAccessToken();
    }
}
