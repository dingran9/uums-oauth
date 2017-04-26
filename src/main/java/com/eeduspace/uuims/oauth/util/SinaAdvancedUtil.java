package com.eeduspace.uuims.oauth.util;

import com.eeduspace.uuims.thirdparty.model.Oauth2Token;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: dingran
 * Date: 2015/11/23
 * Description:
 */
public class SinaAdvancedUtil {

    private static final Logger logger = LoggerFactory.getLogger(SinaAdvancedUtil.class);
    /**
     * default 	默认的授权页面，适用于web浏览器。
     * mobile 	移动终端的授权页面，适用于支持html5的手机。注：使用此版授权页请用 https://open.weibo.cn/oauth2/authorize 授权接口
     * wap 	wap版授权页面，适用于非智能手机。
     * client 	客户端版本授权页面，适用于PC桌面应用。
     * apponweibo 	默认的站内应用授权页，授权后不返回access_token，只刷新站内应用父框架。
     */
    private static final String[] display={"default","mobile","wap","client","apponweibo"};

    /**
     * 获取网页授权凭证
     *
     * @param appId 公众账号的唯一标识
     * @param appSecret 公众账号的密钥
     * @param code
     * @return WeixinAouth2Token
     */
    public static Oauth2Token getOauth2AccessToken(String appId, String appSecret, String code,String uri) {
        Oauth2Token wat = null;
        // 拼接请求地址
        String requestUrl = "https://api.weibo.com/oauth2/access_token?client_id=APPID&client_secret=SECRET&code=CODE&grant_type=authorization_code&redirect_uri=URI";
        requestUrl = requestUrl.replace("APPID", appId);
        requestUrl = requestUrl.replace("SECRET", appSecret);
        requestUrl = requestUrl.replace("CODE", code);
        requestUrl = requestUrl.replace("URI", uri);
        // 获取网页授权
        JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "GET", null);
        if (null != jsonObject) {
            try {
                wat = new Oauth2Token();
                wat.setAccessToken(jsonObject.getString("access_token"));
                wat.setExpiresIn(jsonObject.getInt("expires_in"));
                wat.setOpenId(jsonObject.getString("uid"));

                //获取用户openId
                String getOpenIdUrl = "https://api.weibo.com/oauth2/get_token_info?access_token="+jsonObject.getString("access_token");
                JSONObject getOpenIdObject = CommonUtil.httpsRequest(getOpenIdUrl, "POST", null);
                wat.setOpenId(getOpenIdObject.getString("uid"));
                wat.setAppkey(getOpenIdObject.getString("appkey"));
                wat.setScope(getOpenIdObject.getString("scope"));
                wat.setCreateAt(getOpenIdObject.getString("create_at"));
                wat.setExpiresIn(Integer.parseInt(getOpenIdObject.getString("expire_in")));

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
     * 授权回收地址
     *
     * @param token token
     * @return
     */
    public static Oauth2Token revokeOauth2(String token) {
        Oauth2Token wat = null;
        // 拼接请求地址
        String requestUrl = "https://api.weibo.com/oauth2/revokeoauth2?access_token="+token;
        // 刷新网页授权凭证
        JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "GET", null);
        if (null != jsonObject) {
            try {
                wat = new Oauth2Token();
                wat.setResult(jsonObject.getString("result"));
            } catch (Exception e) {
                wat = null;
                int errorCode = jsonObject.getInt("errcode");
                String errorMsg = jsonObject.getString("errmsg");
                logger.error("授权回收地址失败 errcode:"+errorCode+" errmsg:"+ errorMsg);
            }
        }
        return wat;
    }

}
