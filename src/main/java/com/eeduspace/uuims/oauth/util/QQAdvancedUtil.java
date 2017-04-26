package com.eeduspace.uuims.oauth.util;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eeduspace.uuims.thirdparty.model.Oauth2Token;
import com.eeduspace.uuims.thirdparty.model.SNSUserInfo;

/**
 * Author: dingran Date: 2015/11/11 Description:高级接口工具类
 */
public class QQAdvancedUtil {
	private static final Logger logger = LoggerFactory
			.getLogger(QQAdvancedUtil.class);
	private static  String getUserInfoURL="https://graph.qq.com/user/get_user_info";
	private static String accessTokenURL="https://graph.qq.com/oauth2.0/token";
	private static String getOpenIDURL="https://graph.qq.com/oauth2.0/me";

	/**
	 * 获取网页授权凭证
	 * uuims.qq.getUserInfoURL=https://graph.qq.com/user/get_user_info
uuims.qq.accessTokenURL=https://graph.qq.com/oauth2.0/token
uuims.qq.getOpenIDURL=https://graph.qq.com/oauth2.0/me
	 * @param appId
	 *            公众账号的唯一标识
	 * @param appSecret
	 *            公众账号的密钥
	 * @param code
	 * @return WeixinAouth2Token
	 */
	public static  Oauth2Token getOauth2AccessToken(String appId,
			String appSecret, String code, String uri, String equipmentType) {
		Oauth2Token wat = null;
		// 拼接请求地址 "https://graph.qq.com/oauth2.0/token" 
		String requestUrl = accessTokenURL+"?client_id=APPID&client_secret=SECRET&code=CODE&grant_type=authorization_code&redirect_uri=URI";
		requestUrl = requestUrl.replace("APPID", appId);
		requestUrl = requestUrl.replace("SECRET", appSecret);
		requestUrl = requestUrl.replace("CODE", code);
		requestUrl = requestUrl.replace("URI", uri);
		// 获取网页授权
		String resultInfo = CommonUtil.httpsRequestForQQ(requestUrl, "GET",
				null);
		String[] infos = resultInfo.split("&");

		if (infos.length > 0) {
			try {
				wat = new Oauth2Token();
				wat.setAccessToken(infos[0].split("=")[1]);// access_token
				wat.setExpiresIn(Integer.valueOf(infos[1].split("=")[1]));// expires_in
				wat.setRefreshToken(infos[2].split("=")[1]);// refresh_token

				// 获取用户openId getOpenIDURL
				String getOpenIdUrl =getOpenIDURL +"?access_token="
						+ infos[0].split("=")[1];
				String openIdInfo = CommonUtil.httpsRequestForQQ(
						getOpenIdUrl, "GET", null);
				JSONObject getOpenIdObject=JSONObject.fromObject(openIdInfo.substring(openIdInfo.indexOf("(")+1,openIdInfo.indexOf(")")));
				wat.setOpenId(getOpenIdObject.getString("openid"));
			} catch (Exception e) {
				wat = null;
				logger.error("获取网页授权凭证失败:" + resultInfo);
			}
		}
		return wat;
	}

	/**
	 * 刷新网页授权凭证
	 * 
	 * @param appId
	 *            公众账号的唯一标识
	 * @param refreshToken
	 * @return WeixinAouth2Token
	 */
	public  static Oauth2Token refreshOauth2AccessToken(String appId,
			String secretKey, String refreshToken, String equipmentType) {
		Oauth2Token wat = null;
		// 拼接请求地址
		String requestUrl = accessTokenURL+"?client_id=APPID&client_secret=SECRET&grant_type=refresh_token&refresh_token=REFRESH_TOKEN";
		requestUrl = requestUrl.replace("APPID", appId);
		requestUrl = requestUrl.replace("SECRET", secretKey);
		requestUrl = requestUrl.replace("REFRESH_TOKEN", refreshToken);
		// 刷新网页授权凭证
		String resultInfo = CommonUtil.httpsRequestForQQ(requestUrl, "GET",
				null);
		String[] infos = resultInfo.split("&");

		if (infos.length > 0) {
			try {
				wat = new Oauth2Token();
				wat.setAccessToken(infos[0].split("=")[1]);// access_token
				wat.setExpiresIn(Integer.valueOf(infos[1].split("=")[1]));// expires_in
				wat.setRefreshToken(infos[2].split("=")[1]);// refresh_token

				// 获取用户openId
				String getOpenIdUrl = getOpenIDURL+"?access_token="
						+ infos[0].split("=")[1];
				String openIdInfo = CommonUtil.httpsRequestForQQ(
						getOpenIdUrl, "GET", null);
				JSONObject getOpenIdObject=JSONObject.fromObject(openIdInfo.substring(openIdInfo.indexOf("(")+1,openIdInfo.indexOf(")")));
				wat.setOpenId(getOpenIdObject.getString("openid"));
			} catch (Exception e) {
				wat = null;
				logger.error("获取网页授权凭证失败:" + resultInfo);
			}
		}
		return wat;
	}

	/**
	 * 通过网页授权获取用户信息
	 * 
	 * @param accessToken
	 *            网页授权接口调用凭证
	 * @param openId
	 *            用户标识
	 * @return SNSUserInfo
	 */
	@SuppressWarnings({ "deprecation", "unchecked" })
	public static  SNSUserInfo getSNSUserInfo(String accessToken, String openId,
			String appid, String equipmentType) {
		SNSUserInfo snsUserInfo = null;
		// 拼接请求地址  ""
		String requestUrl = getUserInfoURL+"?access_token="+accessToken+"&oauth_consumer_key="+appid+"&openid="+openId;
		// 通过网页授权获取用户信息
		JSONObject jsonObject = CommonUtil
				.httpsRequest(requestUrl, "GET", null);

		if (null != jsonObject) {
			try {
				snsUserInfo = new SNSUserInfo();
				// 用户的标识
				snsUserInfo.setThridOpenId(openId);
				// 昵称
				snsUserInfo.setNickname(jsonObject.getString("nickname"));
				// 性别（1是男性，2是女性，0是未知）
				if(jsonObject.get("gender").equals("男")){
					snsUserInfo.setSex(1);
				}
				else if(jsonObject.get("gender").equals("女")){
					snsUserInfo.setSex(2);
				}else{
					snsUserInfo.setSex(0);
				}
				// 用户所在国家
				// snsUserInfo.setCountry(jsonObject.getString("country"));
				// // 用户所在省份
				// snsUserInfo.setProvince(jsonObject.getString("province"));
				// // 用户所在城市
				// snsUserInfo.setCity(jsonObject.getString("city"));
				// // 用户头像
				 snsUserInfo.setHeadImgUrl(jsonObject.getString("figureurl_qq_1"));
				// // 用户特权信息
				// snsUserInfo.setPrivilegeList(JSONArray.toList(jsonObject.getJSONArray("privilege"),
				// List.class));
				// AK
				snsUserInfo.setAccessToken(accessToken);
			} catch (Exception e) {
				snsUserInfo = null;
				logger.error("获取用户信息失败  errcode:"
						+ jsonObject.getInt("ret") + " errmsg:"
						+ jsonObject.getString("msg"));
			}
		}
		return snsUserInfo;
	}
}
