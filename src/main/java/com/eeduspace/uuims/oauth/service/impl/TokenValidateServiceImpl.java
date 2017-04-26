package com.eeduspace.uuims.oauth.service.impl;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.eeduspace.uuims.oauth.model.TokenModel;
import com.eeduspace.uuims.oauth.redis.RedisClientTemplate;
import com.eeduspace.uuims.oauth.service.TokenValidateService;
import com.eeduspace.uuims.oauth.ws.TokenWs;
import com.eeduspace.uuims.rescode.ParamName;
import com.eeduspace.uuims.rescode.ResponseCode;
import com.google.gson.Gson;

/**登录token验证实现
 * @author songwei
 * Date 2016-03-11
 */
@Service
public class TokenValidateServiceImpl implements TokenValidateService {
	
	  private final Logger logger = LoggerFactory.getLogger(TokenWs.class);
	  private Gson gson=new Gson();
	  @Inject
	  private RedisClientTemplate redisClientTemplate;
	
	@Override
	public TokenModel tokenValidate(String requestId,String token,TokenModel tokenM) {
		TokenModel tokenModel = new TokenModel();
		if(tokenM==null || StringUtils.isBlank(tokenM.getOpenId())){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.OPENID.toString());
            return tokenModel;
        }
        if (StringUtils.isBlank(token)) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.TOKEN.toString());
            return tokenModel;
        }
        String token_ = redisClientTemplate.get(token);
        if(StringUtils.isBlank(token_)){
            logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_TOKENTIMEOUT.toString());
            return tokenModel;
        }
        tokenModel=gson.fromJson(token_, TokenModel.class);
        if(tokenModel==null || StringUtils.isBlank(tokenModel.getOpenId())){
            logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_TOKENFAILURE.toString());
            return tokenModel;
        }
        if(!tokenM.getOpenId().equals(tokenModel.getOpenId())){
            logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_TOKENFAILURE.toString()+ParamName.OPENID.toString());
            return tokenModel;
        }
        
        return tokenModel;
	}
	
	

}
