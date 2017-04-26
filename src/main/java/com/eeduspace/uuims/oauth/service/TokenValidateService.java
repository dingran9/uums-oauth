package com.eeduspace.uuims.oauth.service;

import com.eeduspace.uuims.oauth.model.TokenModel;

/**
 * 单点登录token验证接口
 * @author songwei
 *	Date 2016-03-11
 */
public interface TokenValidateService {
	
	/**
	 * 验证token的有效性
	 * @param requestId
	 * @param token
	 * @param tokenM
	 * @return
	 */
	public TokenModel tokenValidate(String requestId,String token,TokenModel tokenM);
}