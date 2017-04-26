package com.eeduspace.uuims.oauth.ws;


import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;



/**
 * @author zengzhe
 * @parame token
 * 
 * DATA:2016-4-26 5:44:34
 * 定义一个cxf拦截器拦截所有url
 */
    

	public class MyInterceptor extends AbstractPhaseInterceptor<Message>{
		private final Logger logger = LoggerFactory.getLogger(MyInterceptor.class);
		public MyInterceptor(String phase) {
			super(phase);
		}
		// 指定该拦截器在哪个阶段被激发  RECEIVE只对in有效   send对out有效   PRE_STREAM触发点在流关闭之前  
		public MyInterceptor() {  
	        super(Phase.RECEIVE);  
	    }  
		@Override
		public void handleMessage(Message message) throws Fault {
			// TODO Auto-generated method stub
			logger.debug("开始执行拦截器");
			logger.debug("开始执行拦截器:REQUEST_URI="+message.get(message.REQUEST_URI));
			logger.debug("开始执行拦截器:REQUEST_URL="+message.get(message.REQUEST_URL));
			logger.debug("开始执行拦截器:QUERY_STRING="+message.get(message.QUERY_STRING));

            if("_wadl".equals(message.get(message.QUERY_STRING))){
//                return;
              //  throw new Fault(new TokenException("SOAP消息头格式不对哦！"));
            }
			String reqParams=null;

	        if(message.get(message.HTTP_REQUEST_METHOD).equals("GET")){//采用GET方式请求  
	            reqParams=(String) message.get(message.QUERY_STRING);  
	            message.remove(message.QUERY_STRING);  
	            reqParams=this.getParams(this.getParamsMap(reqParams));  
	            message.put(message.QUERY_STRING,reqParams);  
	              
	        }else if(message.get(message.HTTP_REQUEST_METHOD).equals("POST")){//采用POST方式请求  
	            try {  
	                InputStream is = message.getContent(InputStream.class);  
	                reqParams=this.getParams(this.getParamsMap(is.toString()));  
	                   if (is != null)  
	                       message.setContent(InputStream.class, new ByteArrayInputStream(reqParams.getBytes()));  
	               } catch (Exception e) {  
	                   logger.error("MyInterceptor异常",e);  
	               }  
	        }  
	        logger.info("请求的参数："+reqParams); 
	        
	        //TODO  处理参数的token去验证
	       /* Map<String, String> map = this.getParamsMap(reqParams);
	        String tokenValue = map.get("token");
	        logger.info("tokende参数值："+tokenValue); */
		}
		
	
		
		private Map<String,String> getParamsMap(String strParams){  
		        if(strParams==null||strParams.trim().length()<=0){  
		            return null;  
		        }  
		        Map<String,String> map =new HashMap<String,String>();  
		        String[] params=strParams.split("&");
                logger.debug(params.toString());
                if(params.length>0){
                    for(int i=0;i<params.length;i++){
                        String[] arr=params[i].split("=");
                        if(arr.length>1) {
                            map.put(arr[0], arr[1]);
                        }
                    }
                    return map;
                }
                return null;

		    }  
		      
		    private String getParams(Map<String,String> map){  
		        if(map==null||map.size()==0){  
		            return null;  
		        }  
		        StringBuffer sb=new StringBuffer();  
		        Iterator<String> it =map.keySet().iterator();  
		        while(it.hasNext()){  
		            String key=it.next();  
		            String value =map.get(key);  
		            /*这里可以对客户端上送过来的输入参数进行特殊处理。如密文解密；对数据进行验证等等。。。 
		            if(key.equals("content")){ 
		                value.replace("%3D", "="); 
		                value = DesEncrypt.convertPwd(value, "DES"); 
		            }*/  
		            if(sb.length()<=0){  
		                sb.append(key+"="+value);  
		            }else{  
		                sb.append("&"+key+"="+value);  
		            }  
		        }  
		        return sb.toString();  
		    }  

}
