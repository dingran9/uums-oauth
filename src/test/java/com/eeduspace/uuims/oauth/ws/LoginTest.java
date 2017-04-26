package com.eeduspace.uuims.oauth.ws;


import com.eeduspace.uuims.oauth.BaseTest;
import com.eeduspace.uuims.oauth.model.BaseModel;
import com.eeduspace.uuims.oauth.util.UIDGenerator;
import com.google.gson.Gson;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Author: dingran
 * Date: 2015/10/28
 * Description:
 */
public class LoginTest extends BaseTest {
    private final Logger logger = LoggerFactory.getLogger(LoginTest.class);


    private Gson gson=new Gson();
    @Test
    public void testLogin(){
        WebClient webClient = null;
        String response = null;
        BaseModel baseModel=new BaseModel();
        baseModel.setAction("login");
        baseModel.setPhone("13311335930");
        baseModel.setPassword("47ec2dd791e31e2ef2076caf64ed9b3d");

/*        try {
            webClient = WebClient.create("http://127.0.0.1:8080/oauth");
            response = webClient.path("/authorization")
                    .type(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON).post(gson.toJson(baseModel),String.class);
        } finally {
            if (webClient != null) {
                webClient.close();
            }
        }*/
        logger.info("response:{}",response);
    }


    @Test
    public void testU(){
        String uuid= UIDGenerator.getUUID();
//        String password=Digest.md5Digest("test123456");
//        String accessKeyId="VE"+ Digest.md5Digest16("13311335930" + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
//        String secretKey= Digest.md5Digest("13311335930" + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6) + accessKeyId);
        logger.debug("-------------------------------->"+uuid);
//        logger.debug("-------------------------------->"+accessKeyId);
//        logger.debug("-------------------------------->"+secretKey);
//        logger.debug("-------------------------------->"+password);


    }


    @Test
    public void send2(){
        StringBuffer sb = new StringBuffer("http://m.5c.com.cn/api/send/?"); // 创建StringBuffer对象用来操作字符串
        String  code = null;
        try {
            code = "123456";
            sb.append("apikey=2eee080d1703f1d404de7f289b94c27f");  // APIKEY
            sb.append("&username=grts"); //用户名
            sb.append("&password=dxpt1756314"); // 向StringBuffer追加密码
            sb.append("&mobile="+"13311335930"); // 向StringBuffer追加手机号码
            sb.append("&content="+ URLEncoder.encode(code + "【好学生】", "GBK"));// 向StringBuffer追加消息内容转URL标准码

            URL url = new URL(sb.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();// 打开url连接
            connection.setRequestMethod("POST");// 设置url请求方式 ‘get’ 或者 ‘post’
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream())); // 发送
            logger.debug("result--->"+in.readLine()); // 输出结果

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
