package com.eeduspace.uuims.oauth.ws;


import com.eeduspace.uuims.comm.util.HTTPClientUtils;
import com.eeduspace.uuims.comm.util.base.encrypt.Digest;
import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.model.BaseModel;
import com.eeduspace.uuims.oauth.model.UserModel;
import com.eeduspace.uuims.oauth.persist.dao.UserDao;
import com.eeduspace.uuims.oauth.persist.model.SmsModel;
import com.eeduspace.uuims.oauth.service.UserService;
import com.google.gson.Gson;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Author: dingran
 * Date: 2015/11/2
 * Description:
 */
public class UserWsTest {

    private final Logger logger = LoggerFactory.getLogger(LoginTest.class);


    private Gson gson=new Gson();


    @Inject
    private UserDao userDao;
    @Inject
    private UserService userService;

   // @Test
    public void test() {
        WebClient webClient = null;
        String response = null;

        String ak = "CU0A98DD41233D9D8A";
        String sk = "de1eaa18995b0f841fd0c28258ea7140";
        String token="TK0995DB6331E0E916";
        UserModel userModel=new UserModel();
        userModel.setAction(ActionName.EDIT_PASSWORD.toString());
        userModel.setToken(token);
        userModel.setAccessKey(ak);
        userModel.setSecretKey(sk);

        String url = "http://127.0.0.1:8080/oauth/user"+getParamWithToken(ak,sk,token,gson.toJson(userModel));
        try {
            response= HTTPClientUtils.httpPostRequestJson(url, gson.toJson(userModel));
            logger.debug("response:{}",response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 拼接token及其他参数

     * @return
     */
    public String getParamWithToken(String ak,String sk,String token, String postBody) {
        //  String password = userModel.getPassword();//Digest.md5Digest(userPo.getPassword());
        BaseModel baseModel = gson.fromJson(postBody, BaseModel.class);
        String action=baseModel.getAction();
        String body_md5hex = Digest.md5Digest(postBody);
        String timestamp = System.currentTimeMillis() + "";
        //token串：用户名称+用户密码+时间戳+用户key+请求body的md5加密串 的 md5加密串
        String _signature="";
        try{
           _signature= Digest.getSignature((ak+"\n"+action+"\n"+timestamp+"\n"+body_md5hex).getBytes(),sk.getBytes());
            // _token = Digest.getSignature((userModel.getName()+"\n"+timestamp+"\n"+body_md5hex).getBytes(), password.getBytes());
        }catch (Exception e){
            logger.debug("router post Exception:{}", e);
        }
        //String  _token = Digest.md5Digest(userModel.getName() + password + timestamp + key + body_md5hex);
        //参数：用户名称、用户密码、时间戳、用户key、请求body的md5加密串、token串
        logger.debug("requestId：{},accessKey：{},action：{},timestamp:{},bodyMD5:{},signature:{}, requestBody:{}, request:{}"
                ,"", ak, action, timestamp, body_md5hex, _signature, postBody ,"");
        String param = "?accessKey=" + ak+ "&action=" + action + "&timestamp=" + timestamp
                + "&bodyMD5=" + body_md5hex + "&signature=" + _signature+"&token="+token;        
        return param;
    }

    @Test
    public void test1() {

        String ak = "VE348EDD7D0FB3EF0E";
        String sk = "17f5fa3ea232522d930356612b5b50f6";
        String token="TK14FC3488544507A8";
        SmsModel sms=new SmsModel();
        sms.setAction(ActionName.SEND_SMS.toString());
        sms.setToken(token);
        sms.setAccessKey(ak);
        sms.setSecretKey(sk);
        sms.setPhone("13520465210");

      String url = "http://127.0.0.1:8080/uuims-oauth-ws/oauth/user/restPwd"+getParamWithToken(ak,sk,token,gson.toJson(sms));
        try {
            String response = HTTPClientUtils.httpPostRequestJson(url, gson.toJson(sms));
            logger.debug("response:{}",response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void test2() {

        String ak = "VE348EDD7D0FB3EF0E";
        String sk = "17f5fa3ea232522d930356612b5b50f6";
        String token="TK14FC3488544507A8";
        SmsModel sms=new SmsModel();
        sms.setAction(ActionName.VALIDATE_CODE.toString());
        sms.setToken(token);
        sms.setAccessKey(ak);
        sms.setSecretKey(sk);
        sms.setPhone("13520465210");
        sms.setCode("590558");
        sms.setTicket("lcc2zQ");
      String url = "http://127.0.0.1:8080/uuims-oauth-ws/oauth/user/restPwd"+getParamWithToken(ak,sk,token,gson.toJson(sms));
        try {
            String response = HTTPClientUtils.httpPostRequestJson(url, gson.toJson(sms));
            logger.debug("response:{}",response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void test3() {

        String ak = "VE348EDD7D0FB3EF0E";
        String sk = "17f5fa3ea232522d930356612b5b50f6";
        String token="TK14FC3488544507A8";
        SmsModel sms=new SmsModel();
        sms.setAction(ActionName.RESET_PASSWORD.toString());
        sms.setToken(token);
        sms.setAccessKey(ak);
        sms.setSecretKey(sk);
        sms.setPhone("13520465210");
        sms.setTicket("lcc2zQ");
        sms.setPassword("123");

      String url = "http://127.0.0.1:8080/uuims-oauth-ws/oauth/user/restPwd"+getParamWithToken(ak,sk,token,gson.toJson(sms));
        try {
            String response = HTTPClientUtils.httpPostRequestJson(url, gson.toJson(sms));
            logger.debug("response:{}",response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
    	UserWsTest wsTest = new UserWsTest();
    	wsTest.test1();
    	//wsTest.test2();
    	//wsTest.test3();
	}
}
   

