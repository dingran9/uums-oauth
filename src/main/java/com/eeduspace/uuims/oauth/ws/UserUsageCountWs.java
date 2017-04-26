package com.eeduspace.uuims.oauth.ws;

import com.eeduspace.uuims.comm.util.base.DateUtils;
import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.model.UserUsageCountModel;
import com.eeduspace.uuims.oauth.model.UserUsageTotalCountModel;
import com.eeduspace.uuims.oauth.persist.enumeration.DateTypeEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.UserUsageCountEnum.CountType;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import com.eeduspace.uuims.oauth.persist.po.UserUsageCountPo;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.response.UserCountRegisterAndActiveResponse;
import com.eeduspace.uuims.oauth.response.UserUsageCountResponse;
import com.eeduspace.uuims.oauth.service.ManagerLogService;
import com.eeduspace.uuims.oauth.service.ProductService;
import com.eeduspace.uuims.oauth.service.UserLogService;
import com.eeduspace.uuims.oauth.service.UserUsageCountService;
import com.eeduspace.uuims.rescode.ResourceName;
import com.eeduspace.uuims.rescode.ResponseCode;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 用户使用情况统计服务
 * @author zhuchaowei
 * 2016年3月21日
 * Description
 */
@Component
@Path(value = "/user_usage_count")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@CrossOriginResourceSharing(allowAllOrigins = true)
public class UserUsageCountWs extends BaseWs{
	@Inject
	private UserLogService userLogService;
	@Inject
	private ManagerLogService managerLogService;
	@Inject
	private UserUsageCountService userUsageCountService;
	@Inject
	private ProductService productService;
    private final Logger logger = LoggerFactory.getLogger(UserUsageCountWs.class);
	private Gson gson = new Gson();
	@Override
	public Response dispatch(String action, String token, String requestBody,
			HttpServletRequest httpServletRequest, ManagerPo managerPo,
			UserPo userPo) {
		UserUsageCountModel usageCountModel=gson.fromJson(requestBody, UserUsageCountModel.class);
		switch (ActionName.toEnum(action)) {
		case USERACTIVECOUNT:
			return userActiveCouont(usageCountModel, managerPo);
		case USERREGISTERCOUNT:
			return userRegisterCount(usageCountModel, managerPo);
		case USERREGISTERTOTALCOUNT:
			return userRegisterCountTotalCount(usageCountModel, managerPo);
		case USERACTIVETOTALCOUNT:
			return userAcitveCountTotalCount(usageCountModel, managerPo);
	    case TODAYUSERACTIVECOUNT:
	    	return todayUserActiveCount(usageCountModel, managerPo);
	    case TODAYUSERREGISTERCOUNT:
	    	return todayUserRegisterCount(usageCountModel, managerPo);
	    case TODAYUSERREGISTERCOUNTBYPRODUCTID:
	    	return todayUserRegisterByProduct(usageCountModel, managerPo);
	    case TODAYUSERACTIVECOUNTBYPRODUCTID:
	    	return todayUserActiveByProduct(usageCountModel, managerPo);
	    case USERCOUNTBYPRODUCT:
	    	return userCountByProductId(usageCountModel, managerPo);
	    default :
	    	return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.UNKNOWN_OPERATION.toString(), "USER_USAGE_COUNT"))).build();
		}
	}
	/**
	 * 用户注册量历史统计
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月22日 上午10:52:50
	 * @param managerPo 用户使用情况统计实体
	 * @param usageCountModel 管理员实体
	 * @return
	 */
	public Response userRegisterCount(UserUsageCountModel usageCountModel,ManagerPo managerPo){
		BaseResponse baseResponse=new BaseResponse(requestId);
		UserCountRegisterAndActiveResponse response=new UserCountRegisterAndActiveResponse();
		Date nowDate=new Date();
		Date startDate=null;
		if(StringUtils.isBlank(usageCountModel.getQueryDateType())){
             logger.error("userRegisterCount ExceptionrequestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ "QUERYDATETYPE");
             return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), "QUERYDATETYPE"))).build();
        }
		if(usageCountModel.getQueryDateType().equals(DateTypeEnum.TODAY.toString())){
			logger.error("userRegisterCount ExceptionrequestId："+"requestId,"+ResponseCode.PARAMETER_INVALID.toString()+"."+ "QUERYDATETYPE");
			return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), "QUERYDATETYPE"))).build();
		}
		try {
			Long productIdLong=managerPo.getProductPo()==null?null:managerPo.getProductPo().getId();
			if(usageCountModel.isCountAll()){
					productIdLong=null;
			}
			if(!StringUtils.isBlank(usageCountModel.getProductUUID())){
				productIdLong=productService.findByUuid(usageCountModel.getProductUUID())==null?null:productService.findByUuid(usageCountModel.getProductUUID()).getId();
			}
			startDate=DateUtils.addDay(nowDate, -DateTypeEnum.toEnumValue(usageCountModel.getQueryDateType()));
			startDate.setHours(0);
			startDate.setMinutes(0);
			startDate.setSeconds(0);
			if(DateTypeEnum.toEnumValue(usageCountModel.getQueryDateType())>30){
				usageCountModel.setUsageCountModels(getUserRegisterData(startDate,nowDate,productIdLong,"group_by_month"));
			}else{
				usageCountModel.setUsageCountModels(getUserRegisterData(startDate,nowDate,productIdLong,"group_by_day"));
			}
			response.setUsageCountModel(usageCountModel);
			baseResponse.setResult(response);
			return Response.ok(gson.toJson(baseResponse)).build();
		} catch (Exception e) {
			 logger.error("requestId：{},userActiveCouont Exception：", requestId, e);
	         return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER_USAGE_COUNT.toString()))).build();
		}
	}
	/**
	 * 用户活跃度历史统计
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月22日 上午10:54:26
	 * @param usageCountModel  用户使用情况统计实体
	 * @param managerPo 管理员实体
	 * @return
	 */
	public Response userActiveCouont(UserUsageCountModel usageCountModel,ManagerPo managerPo){
		BaseResponse baseResponse=new BaseResponse(requestId);
		UserCountRegisterAndActiveResponse response=new UserCountRegisterAndActiveResponse();
		if(StringUtils.isBlank(usageCountModel.getQueryDateType())){
			logger.error("userActiveCouont ExceptionrequestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ "QUERYDATETYPE");
			return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), "QUERYDATETYPE"))).build();
		}
		if(usageCountModel.getQueryDateType().equals(DateTypeEnum.TODAY.toString())){
			logger.error("userActiveCouont ExceptionrequestId："+"requestId,"+ResponseCode.PARAMETER_INVALID.toString()+"."+ "QUERYDATETYPE");
			return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), "QUERYDATETYPE"))).build();
		}
		try {
			Date nowDate=new Date();
			Date startDate=null;
			Long productIdLong=managerPo.getProductPo()==null?null:managerPo.getProductPo().getId();
			if(usageCountModel.isCountAll()){
				productIdLong=null;
			}
			if(!StringUtils.isBlank(usageCountModel.getProductUUID())){
				productIdLong=productService.findByUuid(usageCountModel.getProductUUID())==null?null:productService.findByUuid(usageCountModel.getProductUUID()).getId();
			}
			//获取，当前时间之前 -天数
			startDate=DateUtils.addDay(nowDate, -DateTypeEnum.toEnumValue(usageCountModel.getQueryDateType()));
			startDate.setHours(0);
			startDate.setMinutes(0);
			startDate.setSeconds(0);
			if(DateTypeEnum.toEnumValue(usageCountModel.getQueryDateType())>30){
				usageCountModel.setUsageCountModels(getUserActiveData(startDate,nowDate,productIdLong,"group_by_month"));
			}else{
				usageCountModel.setUsageCountModels(getUserActiveData(startDate,nowDate,productIdLong,"group_by_day"));
			}
			response.setUsageCountModel(usageCountModel);
			baseResponse.setResult(response);
			return Response.ok(gson.toJson(baseResponse)).build();
		} catch (Exception e) {
			 logger.error("requestId：{},userActiveCouont Exception：", requestId, e);
	         return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER_USAGE_COUNT.toString()))).build();
		}
	}
	
	/**
	 * 统计当天活跃用户的数据
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月22日 上午11:22:44
	 * @param usageCountModel
	 * @param managerPo
	 * @return
	 */
	public Response todayUserActiveCount(UserUsageCountModel usageCountModel,ManagerPo managerPo){
		 BaseResponse baseResponse=new BaseResponse(requestId);
		 UserUsageCountResponse usageCountRespon=new UserUsageCountResponse();
		 List<Object> userLogPos=new ArrayList<>();
		 List<UserUsageCountModel> usageTotalCountModels=new ArrayList<>();
		 Long productId=null;
		 try {
			 if(!usageCountModel.isCountAll()){
				 productId=managerPo.getProductPo()==null?null:managerPo.getProductPo().getId();
			 }
			 if(!StringUtils.isBlank(usageCountModel.getProductUUID())){
				 productId=productService.findByUuid(usageCountModel.getProductUUID())==null?null:productService.findByUuid(usageCountModel.getProductUUID()).getId();
			 }
			 userLogPos=userLogService.findUserActiveGroupByHour(productId);
			 for (int i = 0; i < userLogPos.size(); i++) {
	    		  Object[] object=(Object[]) userLogPos.get(i);
	    		  String dateString=(String) object[0];
	    		  BigInteger total =  (BigInteger) object[1];
	    		  BigInteger totalProductId = (BigInteger) object[2];
	    		  UserUsageCountModel countModel=new UserUsageCountModel();
	    		  countModel.setProductId(Long.valueOf(totalProductId.intValue()));
	    		  countModel.setCountTotal(Long.valueOf(total.intValue()));
	    		  countModel.setProductName(productService.findOne(Long.valueOf(totalProductId.intValue()))==null?"---":productService.findOne(Long.valueOf(totalProductId.intValue())).getName());
	    		  countModel.setCreateDate(DateUtils.parseDate(dateString,"yyyy-MM-dd HH"));
	    		  usageTotalCountModels.add(countModel);
			}
			 usageCountModel.setUsageCountModels(usageTotalCountModels);
			 usageCountRespon.setUsageCountModel(usageCountModel);
			 baseResponse.setResult(usageCountRespon);
			 return Response.ok(gson.toJson(baseResponse)).build();
		 } catch (Exception e) {
			 logger.error("requestId：{},todayUserActiveCount Exception：", requestId, e);
	         return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER_USAGE_COUNT.toString()))).build();
		}
	}
	
	/**
	 * 统计当天注册用户的数据
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月22日 上午11:22:44
	 * @param usageCountModel
	 * @param managerPo
	 * @return
	 */
	public Response todayUserRegisterCount(UserUsageCountModel usageCountModel,ManagerPo managerPo){
		 BaseResponse baseResponse=new BaseResponse(requestId);
		 UserUsageCountResponse usageCountRespon=new UserUsageCountResponse();
		 Long productIdLong=null;
		 if (!usageCountModel.isCountAll()) {
			 productIdLong=managerPo.getProductPo()==null?null:managerPo.getProductPo().getId();
		 }
		 if(!StringUtils.isBlank(usageCountModel.getProductUUID())){
			 productIdLong=productService.findByUuid(usageCountModel.getProductUUID())==null?null:productService.findByUuid(usageCountModel.getProductUUID()).getId();
		 }
		try {
			List<UserUsageCountModel> usageTotalCountModels=new ArrayList<>();
			List<Object> managerLogPos = managerLogService.findUserRegisterGroupByHour(productIdLong);
			for (int i = 0; i < managerLogPos.size(); i++) {
	    		  Object[] object=(Object[]) managerLogPos.get(i);
	    		  String dateString=(String) object[0];
	    		  BigInteger total =  (BigInteger) object[1];
	    		  BigInteger totalProductId = (BigInteger) object[2];
	    		  UserUsageCountModel countModel=new UserUsageCountModel();
	    		  countModel.setProductId(Long.valueOf(totalProductId.intValue()));
	    		  countModel.setCountTotal(Long.valueOf(total.intValue()));
	    		  countModel.setProductName(productService.findOne(Long.valueOf(totalProductId.intValue()))==null?"---":productService.findOne(Long.valueOf(totalProductId.intValue())).getName());
	    		  countModel.setCreateDate(DateUtils.parseDate(dateString,"yyyy-MM-dd HH"));
	    		  usageTotalCountModels.add(countModel);
			}
			 usageCountModel.setUsageCountModels(usageTotalCountModels);
			 usageCountRespon.setUsageCountModel(usageCountModel);
			 baseResponse.setResult(usageCountRespon);
			 return Response.ok(gson.toJson(baseResponse)).build();
		 } catch (Exception e) {
			 logger.error("requestId：{},todayUserRegisterCount Exception：", requestId, e);
	         return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER_USAGE_COUNT.toString()))).build();
		}
	}
	/**
	 * 统计当天用户注册量根据产品分组
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月29日 下午4:20:44
	 * @param usageCountModel
	 * @param managerPo
	 * @return
	 */
	public Response todayUserRegisterByProduct(UserUsageCountModel usageCountModel,ManagerPo managerPo){
		 BaseResponse baseResponse=new BaseResponse(requestId);
		 UserUsageCountResponse usageCountRespon=new UserUsageCountResponse();
		 Long productIdLong=null;
		 if (!usageCountModel.isCountAll()) {
			 productIdLong=managerPo.getProductPo()==null?null:managerPo.getProductPo().getId();
		 }
		try {
			 List<UserUsageCountModel> usageTotalCountModels=getUserTodayRegisterByProduct(productIdLong);
			 usageCountModel.setUsageCountModels(usageTotalCountModels);
			 usageCountRespon.setUsageCountModel(usageCountModel);
			 baseResponse.setResult(usageCountRespon);
			 return Response.ok(gson.toJson(baseResponse)).build();
		 } catch (Exception e) {
			 logger.error("requestId：{},todayUserRegisterByProduct Exception：", requestId, e);
	         return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER_USAGE_COUNT.toString()))).build();
		}
	}
	
	/**
	 * 统计当天活跃量 根据产品分组
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月29日 下午4:29:28
	 * @param usageCountModel
	 * @param managerPo
	 * @return
	 */
	public Response todayUserActiveByProduct(UserUsageCountModel usageCountModel,ManagerPo managerPo){
		 BaseResponse baseResponse=new BaseResponse(requestId);
		 UserUsageCountResponse usageCountRespon=new UserUsageCountResponse();
		 Long productIdLong=null;
		 if (!usageCountModel.isCountAll()) {
			 productIdLong=managerPo.getProductPo()==null?null:managerPo.getProductPo().getId();
		 }
		try {
			 List<UserUsageCountModel> usageTotalCountModels=getUserTodayActiveByProduct(productIdLong);
			 usageCountModel.setUsageCountModels(usageTotalCountModels);
			 usageCountRespon.setUsageCountModel(usageCountModel);
			 baseResponse.setResult(usageCountRespon);
			 return Response.ok(gson.toJson(baseResponse)).build();
		 } catch (Exception e) {
			 logger.error("requestId：{},todayUserRegisterByProduct Exception：", requestId, e);
	         return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER_USAGE_COUNT.toString()))).build();
		}
	}
	/**
	 * 用户注册统计数据汇总
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月22日 上午11:23:08
	 * @param usageCountModel
	 * @param managerPo
	 * @return
	 */
	public Response userRegisterCountTotalCount(UserUsageCountModel usageCountModel,ManagerPo managerPo){
		 BaseResponse baseResponse=new BaseResponse(requestId);
		 UserCountRegisterAndActiveResponse response=new UserCountRegisterAndActiveResponse();
		 try {
			 List<String> queryTypeList=usageCountModel.getQueryTypeList();
			 if(queryTypeList==null || queryTypeList.size()==0){
				 queryTypeList.add(DateTypeEnum.TODAY.toString());
				 queryTypeList.add(DateTypeEnum.THREE_DAY.toString());
				 queryTypeList.add(DateTypeEnum.SEVEN_DAY.toString());
				 queryTypeList.add(DateTypeEnum.ONE_MONTH.toString());
				 queryTypeList.add(DateTypeEnum.THREE_MONTH.toString());
				 queryTypeList.add(DateTypeEnum.SIX_MONTH.toString());
				 queryTypeList.add(DateTypeEnum.ONE_YEAR.toString());
			 }
			 List<String> tempList=new ArrayList<>();
			 Iterator<String> it=queryTypeList.iterator();
			 while (it.hasNext()) {
				String string=it.next();
				if(!tempList.contains(string)){
					tempList.add(string);
				}
			 }
			 List<UserUsageTotalCountModel> lists=new ArrayList<>();
			 Date nowDate=new Date();
			 Date startDate=null;
			 Long productIdLong=managerPo.getProductPo()==null?null:managerPo.getProductPo().getId();
			 if(usageCountModel.isCountAll()){
					productIdLong=null;
			 }
			 if(!StringUtils.isBlank(usageCountModel.getProductUUID())){
					productIdLong=productService.findByUuid(usageCountModel.getProductUUID())==null?null:productService.findByUuid(usageCountModel.getProductUUID()).getId();
			 }
			 for (String string : tempList) {
				UserUsageTotalCountModel userUsageTotalCountModel=new UserUsageTotalCountModel();
				if (string.equals(DateTypeEnum.TODAY.toString())) {
					userUsageTotalCountModel.setQueryType(string);
					userUsageTotalCountModel.setTotal(Long.valueOf(managerLogService.findAllTodayRegister(productIdLong).size()));
				}else{
					userUsageTotalCountModel.setQueryType(string);	
					startDate=DateUtils.addDay(nowDate, -DateTypeEnum.toEnumValue(string));
					startDate.setHours(0);
					startDate.setMinutes(0);
					startDate.setSeconds(0);
					userUsageTotalCountModel.setTotal(getUserRegisterTotalCount(startDate, nowDate, productIdLong));
				}
				lists.add(userUsageTotalCountModel);
			}
			 usageCountModel.setUsageTotalCountModels(lists);
			 response.setUsageCountModel(usageCountModel);
			 baseResponse.setResult(response);
			 return Response.ok(gson.toJson(baseResponse)).build();
		 } catch (Exception e) {
			 logger.error("requestId：{},userRegisterCountTotalCount Exception：", requestId, e);
	         return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER_USAGE_COUNT.toString()))).build();
		}
	}
	
	/**
	 * 用户活跃统计数据汇总
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月22日 上午11:23:08
	 * @param usageCountModel
	 * @param managerPo
	 * @return
	 */
	public Response userAcitveCountTotalCount(UserUsageCountModel usageCountModel,ManagerPo managerPo){
		 BaseResponse baseResponse=new BaseResponse(requestId);
		 UserCountRegisterAndActiveResponse response=new UserCountRegisterAndActiveResponse();
		 try {
			 List<String> queryTypeList=usageCountModel.getQueryTypeList();
			 if(queryTypeList==null || queryTypeList.size()==0){
				 queryTypeList.add(DateTypeEnum.TODAY.toString());
				 queryTypeList.add(DateTypeEnum.THREE_DAY.toString());
				 queryTypeList.add(DateTypeEnum.SEVEN_DAY.toString());
				 queryTypeList.add(DateTypeEnum.ONE_MONTH.toString());
				 queryTypeList.add(DateTypeEnum.THREE_MONTH.toString());
				 queryTypeList.add(DateTypeEnum.SIX_MONTH.toString());
				 queryTypeList.add(DateTypeEnum.ONE_YEAR.toString());
			 }
			 List<String> tempList=new ArrayList<>();
			 Iterator<String> it=queryTypeList.iterator();
			 while (it.hasNext()) {
				String string=it.next();
				if(!tempList.contains(string)){
					tempList.add(string);
				}
			 }
			 List<UserUsageTotalCountModel> lists=new ArrayList<>();
			 Date nowDate=new Date();
			 Date startDate=null;
			 Long productIdLong=managerPo.getProductPo()==null?null:managerPo.getProductPo().getId();
			 if(usageCountModel.isCountAll()){
					productIdLong=null;
			 }
			 if(!StringUtils.isBlank(usageCountModel.getProductUUID()))
			 {
				 productIdLong=productService.findByUuid(usageCountModel.getProductUUID())==null?null:productService.findByUuid(usageCountModel.getProductUUID()).getId();
			 }
			 for (String string : tempList) {
				UserUsageTotalCountModel userUsageTotalCountModel=new UserUsageTotalCountModel();
				if (string.equals("TODAY")) {
					userUsageTotalCountModel.setQueryType(string);
					userUsageTotalCountModel.setTotal(Long.valueOf(userLogService.findTodayAllData(productIdLong).size()));
				}else{
					userUsageTotalCountModel.setQueryType(string);
					startDate=DateUtils.addDay(nowDate, -DateTypeEnum.toEnumValue(string));
					startDate.setHours(0);
					startDate.setMinutes(0);
					startDate.setSeconds(0);
					userUsageTotalCountModel.setTotal(getUserActiveTotalCount(startDate, nowDate, productIdLong));
				}
				lists.add(userUsageTotalCountModel);
			}
			 usageCountModel.setUsageTotalCountModels(lists);
			 response.setUsageCountModel(usageCountModel);
			 baseResponse.setResult(response);
			 return Response.ok(gson.toJson(baseResponse)).build();
		 } catch (Exception e) {
			 logger.error("requestId：{},userAcitveCountTotalCount Exception：", requestId, e);
	         return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER_USAGE_COUNT.toString()))).build();
		}
	}
	/**
	 * 按产品分组统计活跃度和注册量
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月29日 下午4:41:51
	 * @param usageCountModel
	 * @param managerPo
	 * @return
	 */
	public Response userCountByProductId(UserUsageCountModel usageCountModel,ManagerPo managerPo){
		 BaseResponse baseResponse=new BaseResponse(requestId);
		 UserUsageCountResponse usageCountRespon=new UserUsageCountResponse();
		 Long productIdLong=null;
		 
		 if (!usageCountModel.isCountAll()) {
			 productIdLong=managerPo.getProductPo()==null?null:managerPo.getProductPo().getId();
		 }
		 if(StringUtils.isBlank(usageCountModel.getCountType())){
			 logger.error("userCountByProductId ExceptionrequestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ "COUNTTYPE");
			 return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), "COUNTTYPE"))).build();
		 }
		 if(StringUtils.isBlank(usageCountModel.getQueryDateType())){
			 logger.error("userCountByProductId ExceptionrequestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ "QueryDateType");
			 return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), "QueryDateType"))).build();
		 }
		 if(usageCountModel.getQueryDateType().equals(DateTypeEnum.TODAY.toString())){
			 logger.error("userCountByProductId ExceptionrequestId："+"requestId,"+ResponseCode.PARAMETER_INVALID.toString()+"."+ "QueryDateType");
			 return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), "QueryDateType"))).build();
		 }
		 CountType countType=null;
		 if(usageCountModel.getCountType().equals(CountType.USERACTIVE.toString())){
			 countType=CountType.USERACTIVE;
		 }
		 else if(usageCountModel.getCountType().equals(CountType.USERREGISTER.toString())){
			 countType=CountType.USERREGISTER;
		 }else{
			 logger.error("userCountByProductId ExceptionrequestId："+"requestId,"+ResponseCode.PARAMETER_INVALID.toString()+"."+ "countType");
			 return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), "countType"))).build();
		 }
		 Date startDate=null;
		 Date nowDate=new Date();
		 startDate=DateUtils.addDay(nowDate, -DateTypeEnum.toEnumValue(usageCountModel.getQueryDateType()));
		 startDate.setHours(0);
		 startDate.setMinutes(0);
		 startDate.setSeconds(0);
		try {
			 List<UserUsageCountModel> usageTotalCountModels=getUserUsageDataGroupByProductId(startDate, nowDate, countType,productIdLong);
			 usageCountModel.setUsageCountModels(usageTotalCountModels);
			 usageCountRespon.setUsageCountModel(usageCountModel);
			 baseResponse.setResult(usageCountRespon);
			 return Response.ok(gson.toJson(baseResponse)).build();
		 } catch (Exception e) {
			 logger.error("requestId：{},todayUserRegisterByProduct Exception：", requestId, e);
	         return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER_USAGE_COUNT.toString()))).build();
		}
	}
	/**
	 * 获取注册数据
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月23日 上午9:41:42
	 * @param startDate
	 * @param endDate
	 * @param countType
	 * @return
	 * @throws ParseException 
	 */
	public List<UserUsageCountModel> getUserRegisterData(Date startDate,Date endDate,Long productId,String groupType) throws ParseException{
		List<Object> usageCountPos=new ArrayList<>();
		usageCountPos=userUsageCountService.findGroupData(startDate, endDate, productId, CountType.USERREGISTER, groupType);
		List<UserUsageCountModel> usageCountModels=new ArrayList<>();
		String sdf="yyyy-MM-dd";
		if(groupType.equals("group_by_month")){
			sdf="yyyy-MM";
		}
		for (int i = 0; i < usageCountPos.size(); i++) {
    		  Object[] object=(Object[]) usageCountPos.get(i);
    		  String dateString=(String) object[0];
    		  BigDecimal total =  (BigDecimal) object[1];
    		  Integer totalProductId = (Integer) object[2];
    		  UserUsageCountModel countModel=new UserUsageCountModel();
    		  countModel.setProductId(Long.valueOf(totalProductId));
    		  countModel.setCountTotal(Long.valueOf(total.intValue()));
    		  countModel.setCreateDate(DateUtils.parseDate(dateString,sdf));
    		  countModel.setProductName(productService.findOne(Long.valueOf(totalProductId.intValue()))==null?"---":productService.findOne(Long.valueOf(totalProductId.intValue())).getName());
    		  usageCountModels.add(countModel);
		}
		return usageCountModels;
	}
	/**
	 * 获取活跃数据
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月23日 上午9:55:06
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws ParseException 
	 */
	public List<UserUsageCountModel> getUserActiveData(Date startDate,Date endDate,Long productId,String groupType) throws ParseException{
		List<Object> usageCountPos=new ArrayList<>();
		usageCountPos=userUsageCountService.findGroupData(startDate, endDate, productId, CountType.USERACTIVE, groupType);
		List<UserUsageCountModel> usageCountModels=new ArrayList<>();
		String sdf="yyyy-MM-dd";
		if(groupType.equals("group_by_month")){
			sdf="yyyy-MM";
		}
		for (int i = 0; i < usageCountPos.size(); i++) {
    		  Object[] object=(Object[]) usageCountPos.get(i);
    		  String dateString=(String) object[0];
    		  BigDecimal total =  (BigDecimal) object[1];
    		  Integer totalProductId = (Integer) object[2];
    		  UserUsageCountModel countModel=new UserUsageCountModel();
    		  countModel.setProductId(Long.valueOf(totalProductId));
    		  countModel.setCountTotal(Long.valueOf(total.intValue()));
    		  countModel.setCreateDate(DateUtils.parseDate(dateString,sdf));
    		  countModel.setProductName(productService.findOne(Long.valueOf(totalProductId.intValue()))==null?"---":productService.findOne(Long.valueOf(totalProductId.intValue())).getName());
    		  usageCountModels.add(countModel);
		}
		return usageCountModels;
	}
	/**
	 * 获取注册数量
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月23日 上午11:34:54
	 * @param startDate
	 * @param endDate
	 * @param productId
	 * @return
	 */
	public Long getUserRegisterTotalCount(Date startDate,Date endDate,Long productId){
		List<UserUsageCountPo> usageCountPos=new ArrayList<>();
		Long returnLong=0l;
		if(productId!=null){
			usageCountPos=userUsageCountService.findAllByProductId(startDate, endDate, productId, CountType.USERREGISTER);
		}else{
			usageCountPos=userUsageCountService.findAll(startDate, endDate, CountType.USERREGISTER);
		}
		for (UserUsageCountPo userUsageCountPo : usageCountPos) {
			returnLong+=userUsageCountPo.getCountTotal();
		}
		return returnLong;
	}
	/**
	 * 获取活跃数量
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月23日 上午11:34:54
	 * @param startDate
	 * @param endDate
	 * @param productId
	 * @return
	 */
	public Long getUserActiveTotalCount(Date startDate,Date endDate,Long productId){
		List<UserUsageCountPo> usageCountPos=new ArrayList<>();
		Long returnLong=0l;
		if(productId!=null){
			usageCountPos=userUsageCountService.findAllByProductId(startDate, endDate, productId, CountType.USERACTIVE);
		}else{
			usageCountPos=userUsageCountService.findAll(startDate, endDate, CountType.USERACTIVE);
		}
		for (UserUsageCountPo userUsageCountPo : usageCountPos) {
			returnLong+=userUsageCountPo.getCountTotal();
		}
		return returnLong;
	}
	/**
	 * 根据产品ID分组获取统计数据
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月29日 下午2:59:09
	 * @param startDate
	 * @param endDate
	 * @param countType
	 * @return
	 */
	public List<UserUsageCountModel> getUserUsageDataGroupByProductId(Date startDate,Date endDate,CountType countType,Long productId){
		List<Object> usageCountPos=new ArrayList<>();
		usageCountPos=userUsageCountService.findGroupByProductId(startDate, endDate, countType,productId);
		List<UserUsageCountModel> usageCountModels=new ArrayList<>();
		for (int i = 0; i < usageCountPos.size(); i++) {
    		  Object[] object=(Object[]) usageCountPos.get(i);
    		  BigDecimal total =  (BigDecimal) object[0];
    		  String productName = (String) object[1];
    		  Integer pId=(Integer) object[2];
    		  UserUsageCountModel usageCountModel=new UserUsageCountModel();
    		  usageCountModel.setProductName(productName);
    		  usageCountModel.setProductId(Long.valueOf(pId.intValue()));
    		  usageCountModel.setCountTotal(Long.valueOf(total.longValue()));
    		  usageCountModels.add(usageCountModel);
		}
		return usageCountModels;
	}
	/**
	 * 获取注册量根据产品分组
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月29日 下午3:46:01
	 * @return
	 */
	public List<UserUsageCountModel> getUserTodayRegisterByProduct(Long productId){
		List<Object> usageCountPos=new ArrayList<>();
		usageCountPos=managerLogService.findUserRegisterGroupByProductId(productId);
		List<UserUsageCountModel> usageCountModels=new ArrayList<>();
		for (int i = 0; i < usageCountPos.size(); i++) {
  		  Object[] object=(Object[]) usageCountPos.get(i);
  		  BigInteger total =  (BigInteger) object[0];
  		  BigInteger pId= (BigInteger) object[1];
  		  UserUsageCountModel usageCountModel=new UserUsageCountModel();
  		  usageCountModel.setProductName(productService.findOne(Long.valueOf(pId.intValue()))==null?"---":productService.findOne(Long.valueOf(pId.intValue())).getName());
		  usageCountModel.setProductId(Long.valueOf(pId.intValue()));
		  usageCountModel.setCountTotal(Long.valueOf(total.intValue()));
		  usageCountModels.add(usageCountModel);
		}
		return usageCountModels;
	}
	/**
	 * 活跃量统计 根据产品分组
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月29日 下午4:04:13
	 * @return
	 */
	public List<UserUsageCountModel> getUserTodayActiveByProduct(Long productIdLong){
		List<Object> usageCountPos=new ArrayList<>();
		usageCountPos=userLogService.findUserActeiceGroupByProductId(productIdLong);
		List<UserUsageCountModel> usageCountModels=new ArrayList<>();
		for (int i = 0; i < usageCountPos.size(); i++) {
  		  Object[] object=(Object[]) usageCountPos.get(i);
  		  BigInteger total =  (BigInteger) object[0];
  		  BigInteger productId= (BigInteger) object[1];
  		  UserUsageCountModel usageCountModel=new UserUsageCountModel();
  		  usageCountModel.setProductName(productService.findOne(Long.valueOf(productId.intValue()))==null?"---":productService.findOne(Long.valueOf(productId.intValue())).getName());
		  usageCountModel.setProductId(Long.valueOf(productId.intValue()));
		  usageCountModel.setCountTotal(Long.valueOf(total.intValue()));
		  usageCountModels.add(usageCountModel);
		}
		return usageCountModels;
	}
}
