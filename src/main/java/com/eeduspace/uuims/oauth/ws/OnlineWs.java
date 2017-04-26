package com.eeduspace.uuims.oauth.ws;

import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.model.OnlineUserAllModel;
import com.eeduspace.uuims.oauth.model.OnlineUserModel;
import com.eeduspace.uuims.oauth.model.UserModel;
import com.eeduspace.uuims.oauth.persist.enumeration.RoleEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum.EquipmentType;
import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum.OnlineSourceType;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.ProductPo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import com.eeduspace.uuims.oauth.redis.RedisClientTemplate;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.response.OnlineUserRespone;
import com.eeduspace.uuims.oauth.service.ProductService;
import com.eeduspace.uuims.oauth.service.UserService;
import com.eeduspace.uuims.oauth.util.PageUtil;
import com.eeduspace.uuims.rescode.ParamName;
import com.eeduspace.uuims.rescode.ResourceName;
import com.eeduspace.uuims.rescode.ResponseCode;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.*;



/**在线用户查询
 * @author songwei
 *	Date 2016-03-23
 */
@Component
@Path(value = "/online")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@CrossOriginResourceSharing(allowAllOrigins = true)
public class OnlineWs extends BaseWs {

	private final Logger logger = LoggerFactory.getLogger(OnlineWs.class);
	private Gson gson = new Gson();

	@Inject
	private RedisClientTemplate redisClientTemplate;
	@Inject
	private UserService userService;
	@Inject
	private ProductService productService;
	@Inject
	private AuthConverter authConverter;

	@Value("${online.pidKey}")
	private String onlinePidKey;
	@Value("${online.uidKey}")
	private String onlineUidKey;
	@Value("${online.xing}")
	private String onlineXing;

	@Override
	public Response dispatch(String action, String token, String requestBody,HttpServletRequest httpServletRequest, ManagerPo managerPo,UserPo userPo) {
		OnlineUserModel onlineUserModel = gson.fromJson(requestBody, OnlineUserModel.class);
		switch (ActionName.toEnum(action)) { 
		case ONLINE_USER_SIZE:
			return getUserCount(onlineUserModel,userPo, managerPo);
		case ONLINE_USER_LIST:
			return getUserList(onlineUserModel,userPo, managerPo);
		case ONLINE_USER_SIZE_ALL:
			return getUserCountAll(onlineUserModel,userPo, managerPo);
		default:
			return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.ONLINEUSER.toString()))).build();
		}
	}

	/**在线用户数量查询
	 * @author songwei
	 * Date 2016-03-24
	 */
	private Response getUserCount(OnlineUserModel onlineUserModel , UserPo userPo, ManagerPo managerPo) {

		if(!managerPo.getRolePo().getType().equals(RoleEnum.Type.System) && !managerPo.getRolePo().getType().equals(RoleEnum.Type.Product) || managerPo == null){
			logger.error("getUserCount ExceptionrequestId："+"requestId,"+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ ResourceName.MANAGER.toString());
			return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.MANAGER.toString()))).build();
		}
		BaseResponse baseResponse = new BaseResponse(requestId);  
		OnlineUserRespone onlineUserRespone = new OnlineUserRespone();
		Map<String, Object> mapCount = new  HashMap<String, Object>();
		String productUuid = null;
		String productId = null;
		Set<String> onlineSet = null;
		List<String> onlineList =  null;
		try {
			if(managerPo.getRolePo().getType().equals(RoleEnum.Type.System)){
				productUuid = onlineUserModel.getProductUuid();
				String equipment = onlineUserModel.getEquipment();
				//都是空-查询onlineUid_*
				if(StringUtils.isBlank(productUuid) && StringUtils.isBlank(equipment) ){
					onlineSet = redisClientTemplate.keys(MessageFormat.format(this.onlineUidKey, this.onlineXing));
				}else if(StringUtils.isBlank(productUuid) && StringUtils.isNotBlank(equipment)){
					onlineSet = redisClientTemplate.keys(this.changeOPK(null, equipment));
				}else{
					productId = this.getProductId(productUuid);
					if(StringUtils.isBlank(productId)){
						logger.error("getUserCount ExceptionrequestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.PRODUCT_ID.toString());
						return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PRODUCT_ID.toString()))).build();
					}
					onlineSet = redisClientTemplate.keys(this.changeOPK(productId, equipment));
				}
			}else if(managerPo.getRolePo().getType().equals(RoleEnum.Type.Product)){
				productId = String.valueOf(managerPo.getProductPo().getId());
				if(StringUtils.isBlank(productId)){
					logger.error("getUserCount ExceptionrequestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.PRODUCT_ID.toString());
					return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PRODUCT_ID.toString()))).build();
				}
				onlineSet = redisClientTemplate.keys(this.changeOPK(productId, onlineUserModel.getEquipment()));
			}
			onlineList = new ArrayList<String>(onlineSet);
			mapCount.put("totalCount", String.valueOf(onlineList.size() == 0 ? 0 : onlineList.size()));
//			logger.debug("totalCount的值为------>"+mapCount.get("totalCount"));
			onlineUserModel.setMap(mapCount);
			onlineUserModel.setProductUuid(productUuid);
			onlineUserRespone.setOnlineUserModel(onlineUserModel);
			baseResponse.setResult(onlineUserRespone);
			return Response.ok(gson.toJson(baseResponse)).build();
		} catch (Exception e) {
			logger.error("requestId：{},getUserCount Exception：", requestId, e);
			return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(),ResourceName.ONLINEUSERSIZE.toString()))).build();
		}
	}

	/**在线用户列表查询
	 * @author songwei
	 * Date 2016-03-24
	 */
	private Response getUserList(OnlineUserModel onlineUserModel , UserPo userPo, ManagerPo managerPo){

		if(!managerPo.getRolePo().getType().equals(RoleEnum.Type.System) && !managerPo.getRolePo().getType().equals(RoleEnum.Type.Product) || managerPo == null){
			logger.error("getUserList ExceptionrequestId："+"requestId,"+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ ResourceName.MANAGER.toString());
			return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.MANAGER.toString()))).build();
		}
		PageUtil pageUtil = new PageUtil();
		BaseResponse baseResponse = new BaseResponse(requestId);
		OnlineUserRespone onlineUseRespone = new OnlineUserRespone();
		List<UserPo> onlineUserPo = new ArrayList<UserPo>();
		List<UserModel> userModels = new ArrayList<UserModel>();
		String productUuid = null;
		String productId = null;
		Set<String> onlineSet = null;
		List<String> onlineList =  null;
		try {
			if(managerPo.getRolePo().getType().equals(RoleEnum.Type.System)){//系统管理员
				productUuid = onlineUserModel.getProductUuid();
				String equipment = onlineUserModel.getEquipment();
				if(StringUtils.isBlank(productUuid)){
					onlineSet = redisClientTemplate.keys(MessageFormat.format(this.onlineUidKey, this.onlineXing));
					if(StringUtils.isNotBlank(equipment)){
						onlineSet = redisClientTemplate.keys(this.changeOPK(null, equipment));
					}
				}else{
					productId = this.getProductId(productUuid);
					if(StringUtils.isBlank(productId)){
						logger.error("getUserList ExceptionrequestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.PRODUCT_ID.toString());
						return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PRODUCT_ID.toString()))).build();
					}
					onlineSet = redisClientTemplate.keys(this.changeOPK(productId, equipment));
				}
			}else if(managerPo.getRolePo().getType().equals(RoleEnum.Type.Product)){//产品管理员
				productId = String.valueOf(managerPo.getProductPo().getId());
				if(StringUtils.isBlank(productId)){
					logger.error("getUserList ExceptionrequestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.PRODUCT_ID.toString());
					return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PRODUCT_ID.toString()))).build();
				}
				onlineSet = redisClientTemplate.keys(this.changeOPK(productId, onlineUserModel.getEquipment()));
			}
			if(onlineSet.size() > 0){
				onlineList = new ArrayList<String>(onlineSet);
				onlineUserPo = this.getListPage(onlineUserModel, onlineList, pageUtil, productId);
				userModels = authConverter.fromUserPos(onlineUserPo, true,false);
			}
//			logger.debug("list的值为--------->"+userModels.size());
			onlineUserModel.setList(userModels);
			onlineUserModel.setProductUuid(productUuid);
			onlineUserModel.setCurrentPage(pageUtil.getCurrentPage());
			onlineUserModel.setTotalPage(pageUtil.getTotalPage());
			onlineUserModel.setTotalSize(pageUtil.getTotalCount());
			onlineUseRespone.setOnlineUserModel(onlineUserModel);
			baseResponse.setResult(onlineUseRespone);
			return Response.ok(gson.toJson(baseResponse)).build();
		} catch (NumberFormatException e) {
			logger.error("requestId：{},getUserList Exception：", requestId, e);
			return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(),ResourceName.ONLINEUSERLIST.toString()))).build();
		}
	}
	/**
	 * 在线列表全局数量概览
	 * */
	private Response getUserCountAll(OnlineUserModel onlineUserModel , UserPo userPo, ManagerPo managerPo) {

		if(!managerPo.getRolePo().getType().equals(RoleEnum.Type.System) && !managerPo.getRolePo().getType().equals(RoleEnum.Type.Product) || managerPo == null){
			logger.error("getUserCountAll ExceptionrequestId："+"requestId,"+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ ResourceName.MANAGER.toString());
			return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.MANAGER.toString()))).build();
		}
		BaseResponse baseResponse = new BaseResponse(requestId);  
		OnlineUserRespone onlineUserRespone = new OnlineUserRespone();
		OnlineUserAllModel onlineUserAll = new OnlineUserAllModel();
		List<OnlineUserAllModel> onlineUserAlls = new ArrayList<OnlineUserAllModel>();
		try {
			Set<String> onlineSetAll = redisClientTemplate.keys(this.changeOPK(null, null));
			List<String> equipments = new ArrayList<String>();
			for (EquipmentType it : SourceEnum.EquipmentType.values()) {
				equipments.add(it.toString());
			}
			if(managerPo.getRolePo().getType().equals(RoleEnum.Type.System)){//系统管理员
				onlineUserAll.setUserTatolCount(String.valueOf(onlineSetAll.size()));
				Set<String> openIds = this.getList(onlineSetAll);
				//重复数量 转set时截取标记去重
				onlineUserAll.setUserRepeatCount(String.valueOf((onlineSetAll.size()-openIds.size()) < 0 ? 0 : (onlineSetAll.size()-openIds.size())));
				onlineUserAll = setOnlineUserAllEMType(onlineUserAll, onlineSetAll, equipments,null);
				onlineUserAlls.add(onlineUserAll);
				List<ProductPo> productList = productService.findAll();
				if(productList.size() > 0){
					for (ProductPo pro : productList) {
						OnlineUserAllModel onlineUserAll2 = new OnlineUserAllModel();
						onlineUserAll2 = this.recombineOUAM(pro.getName(), pro.getId(), equipments);
						onlineUserAlls.add(onlineUserAll2);
					}
				}
			}else if(managerPo.getRolePo().getType().equals(RoleEnum.Type.Product)){//产品管理员
				ProductPo po = managerPo.getProductPo();
				if (po == null) {
					logger.error("getUserCountAll ExceptionrequestId："+"requestId,"+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ ResourceName.PRODUCT.toString());
					return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.PRODUCT.toString()))).build();
				}
				OnlineUserAllModel onlineUserAll3 = this.recombineOUAM(po.getName(), po.getId(), equipments);
				onlineUserAlls.add(onlineUserAll3);
			}
			onlineUserModel.setOnlineUserAlls(onlineUserAlls);
			onlineUserRespone.setOnlineUserModel(onlineUserModel);
			baseResponse.setResult(onlineUserRespone);
			return Response.ok(gson.toJson(baseResponse)).build();
		} catch (Exception e) {
			logger.error("requestId：{},getUserCountAll Exception：", requestId, e);
			return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(),ResourceName.ONLINEUSERSIZEALL.toString()))).build();
		}
	}
	/**
	 * 获取UUID集合的分页
	 * */
	private List<UserPo> getListPage(OnlineUserModel onlineUserModel ,List<String> onlineList ,PageUtil pageUtil ,String productId){
		List<String> onlineList2 = new ArrayList<String>();
		List<String> onlineListPage = new ArrayList<String>();
		List<UserPo> onlineUserPo = new ArrayList<UserPo>();
		if(onlineList.size() > 0){
			for (String str : onlineList) {
				int index = str.lastIndexOf(OnlineSourceType.UnderLine.getValue()) + 1;
				str = str.substring(index);
				onlineList2.add(str);
			}
			Set<String> set = new HashSet<String>(onlineList2);
			onlineList2 = new ArrayList<String>(set);
			pageUtil.setLineSize(10);
			pageUtil.setTotalCount(onlineList2.size());
			pageUtil.setTotalPage();
			pageUtil.setCurrentPage(onlineUserModel.getCurrentPage());
			onlineListPage = pageUtil.getPage(onlineList2);
			onlineList.clear();
			set.clear();
			onlineList2.clear();
			if(StringUtils.isNotBlank(productId)){
				onlineUserPo = userService.getByRegisterProductIdAndUuidIn(Long.valueOf(productId), onlineListPage);
			}else{
				for (String str : onlineListPage) {
					onlineUserPo.add(userService.findByUuid(str));
				}
			}
		}
		return onlineUserPo;
	}
	/**
	 * 通过ProductUuid获得ProductId
	 * */
	private String getProductId(String productUuid){
		String productId = null;
		if(StringUtils.isNotBlank(productUuid)){
			ProductPo productPo = productService.findByUuid(productUuid);
			if(productPo != null && productPo.getId() != null){
				productId = String.valueOf(productPo.getId());
			}
		}
		return productId;
	}
	/**
	 * 取得缓存中openId的列表
	 * */
	private Set<String> getList(Set<String> onlineList){
		Set<String> onlineList2 = new HashSet<String>();
		if(onlineList.size() > 0){
			for (String str : onlineList) {
				int index = str.lastIndexOf(OnlineSourceType.UnderLine.getValue()) + 1;
				str = str.substring(index);
				onlineList2.add(str);
			}
		}
		return onlineList2;
	}
	/**
	 * 根据equipment查询数量
	 * */
	private OnlineUserAllModel setOnlineUserAllEMType(OnlineUserAllModel onlineUserAll,Set<String> onlineSetAll,List<String> equipments,Long productId){
		OnlineUserAllModel onlineUserAllModel = new OnlineUserAllModel();
		onlineUserAllModel.setOnlineProductName(onlineUserAll.getOnlineProductName());
		onlineUserAllModel.setUserTatolCount(onlineUserAll.getUserTatolCount());
		onlineUserAllModel.setUserRepeatCount(onlineUserAll.getUserRepeatCount());
		for (String str : equipments) {
			if(productId == null){
				onlineSetAll = redisClientTemplate.keys(this.changeOPK(null, str));
			}else{
				onlineSetAll = redisClientTemplate.keys(this.changeOPK(String.valueOf(productId), str));
			}
			if(str.equals(String.valueOf(EquipmentType.Web))){
				onlineUserAllModel.setUserWebCount(String.valueOf(onlineSetAll.size()));
			}else if(str.equals(String.valueOf(EquipmentType.Ios))){
				onlineUserAllModel.setUserIosCount(String.valueOf(onlineSetAll.size()));
			}else if(str.equals(String.valueOf(EquipmentType.Android))){
				onlineUserAllModel.setUserAndroidCount(String.valueOf(onlineSetAll.size()));
			}else{
				onlineUserAllModel.setUserTestCount(String.valueOf(onlineSetAll.size()));
			}
		}
		return onlineUserAllModel;
	}
	/**
	 * 重组OnlineUserModel
	 * */
	private OnlineUserAllModel recombineOUAM(String productName,Long productId,List<String> equipments){
		OnlineUserAllModel onlineUserAll =new OnlineUserAllModel();
		onlineUserAll.setOnlineProductName(productName);
		Set<String> onlineSetAll = redisClientTemplate.keys(this.changeOPK(String.valueOf(productId), null));
		onlineUserAll.setUserTatolCount(String.valueOf(onlineSetAll.size()));
		Set<String> productEquipments = this.getList(onlineSetAll); 
		onlineUserAll.setUserRepeatCount(String.valueOf((onlineSetAll.size()-productEquipments.size()) < 0 
				? 0 : (onlineSetAll.size()-productEquipments.size())));
		onlineUserAll = setOnlineUserAllEMType(onlineUserAll, onlineSetAll, equipments,productId);
		if (onlineSetAll.size() > 0) {
			onlineSetAll.clear();
		}
		return onlineUserAll;
	}
	/**
	 * 从配置文件读取onlineKey重组查询语句
	 * radis - onlinePidKey
	 * productId（可选）
	 * equipment（可选）4种情况
	 * */
	private String changeOPK(String productId ,String equipment){
		String onlinePidKey = this.onlinePidKey;
		if(StringUtils.isBlank(productId)){
			if(StringUtils.isBlank(equipment)){
				//编辑查询的格式
				onlinePidKey = MessageFormat.format(this.onlinePidKey, this.onlineXing,this.onlineXing,this.onlineXing);
			}else{
				onlinePidKey = MessageFormat.format(this.onlinePidKey, this.onlineXing,equipment,this.onlineXing);
			}
		}else{
			if(StringUtils.isBlank(equipment)){
				onlinePidKey = MessageFormat.format(this.onlinePidKey, productId,this.onlineXing,this.onlineXing);
			}else{
				onlinePidKey = MessageFormat.format(this.onlinePidKey, productId,equipment,this.onlineXing);
			}
		}
//		logger.debug("重组后的onlineKey为：" + onlinePidKey + "配置文件的onlinePidKey为：" + this.onlinePidKey);
		return onlinePidKey;

	}
}
