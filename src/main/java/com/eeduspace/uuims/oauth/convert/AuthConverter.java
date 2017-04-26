package com.eeduspace.uuims.oauth.convert;


import com.eeduspace.uuims.comm.util.base.DateUtils;
import com.eeduspace.uuims.oauth.model.*;
import com.eeduspace.uuims.oauth.persist.enumeration.RoleEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import com.eeduspace.uuims.oauth.persist.po.*;
import com.eeduspace.uuims.oauth.service.ManagerService;
import com.eeduspace.uuims.oauth.service.ProductService;
import com.eeduspace.uuims.oauth.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: dingran
 * Date: 2015/10/21
 * Description:转换工具类：将实体转换为模型
 */
@Component
public class AuthConverter {
    private static final Logger logger = LoggerFactory.getLogger(AuthConverter.class);


    @Inject
    private UserService userService;
    @Inject
    private ManagerService managerService;
    @Inject
    private ProductService productService;
    public UserModel fromUserPo(UserPo userPo, Boolean isAll, Boolean create) {
        UserModel userModel = new UserModel();
        if(userPo!=null){
            userModel.setName(userPo.getName());
            userModel.setEmail(userPo.getEmail());
            userModel.setSchoolNumber(userPo.getSchoolNumber());
            userModel.setOpenId(userPo.getUuid());
            userModel.setStatus(userPo.getStatus().toString());
            userModel.setPhone(userPo.getPhone());
            userModel.setCreateDate(DateUtils.getTimeStampStr(userPo.getCreateDate()));
            if(userPo.getRegisterProductId()!=null){
              ProductPo productPo=  productService.findOne(userPo.getRegisterProductId());
                if(productPo!=null){
                    userModel.setProductType(productPo.getType());
                    userModel.setProductName(productPo.getName());
                }
            }

            //TODO 用户当前的登录状态

            if (create) {
                userModel.setAccessKey(userPo.getAccessKey());
                userModel.setSecretKey(userPo.getSecretKey());
            }
            if (isAll) {
                UserInfoPo userInfoPo = userService.findInfoByUserId(userPo.getId());
                if (userInfoPo != null) {
//                    userModel.setBandStatus(userInfoPo.getBandStatus().toString());
                    userModel.setNickName(userInfoPo.getNickName());
                    userModel.setSex(userInfoPo.getSex().toString());
                    userModel.setAddress(userInfoPo.getAddress());
                    userModel.setImagePath(userInfoPo.getImagePath());
                    userModel.setCardId(userInfoPo.getCardId());
                    userModel.setRealName(userInfoPo.getRealName());
                    userModel.setProvinceCode(userInfoPo.getProvinceCode());
                    userModel.setCityCode(userInfoPo.getCityCode());
                    userModel.setAreaCode(userInfoPo.getAreaCode());
                    userModel.setStageCode(userInfoPo.getStageCode());
                    userModel.setGradeCode(userInfoPo.getGradeCode());
                    userModel.setSchoolCode(userInfoPo.getSchoolCode());
                    userModel.setClassCode(userInfoPo.getClassCode());
                    userModel.setBandSina(userInfoPo.isBandSina());
                    userModel.setBandEmail(userInfoPo.isBandEmail());
                    userModel.setBandQQ(userInfoPo.isBandQQ());
                    userModel.setBandWX(userInfoPo.isBandWX());
                }
            }
        }
        return userModel;
    }

    public List<UserModel> fromUserPos(List<UserPo> userPos, Boolean isAll, Boolean create) {
        List<UserModel> list = new ArrayList<>();
        if (userPos != null && !userPos.isEmpty()) {
            for (UserPo po : userPos) {
                list.add(this.fromUserPo(po, isAll, create));
            }
        }
        return list;
    }

    public UserModel toUserModel(Map map) {
        //TODO
        UserModel userModel = new UserModel();
        if(map!=null){
            if (null != map.get("uuid") && StringUtils.isNotBlank(map.get("uuid").toString())) {
                userModel.setOpenId(map.get("uuid").toString());
            }
            if (null != map.get("phone") && StringUtils.isNotBlank(map.get("phone").toString())) {
                userModel.setPhone(map.get("phone").toString());
            }
            if (null != map.get("name") && StringUtils.isNotBlank(map.get("name").toString())) {
                userModel.setName(map.get("name").toString());
            }
            if (null != map.get("email") && StringUtils.isNotBlank(map.get("email").toString())) {
                userModel.setEmail(map.get("email").toString());
            }
            if (null != map.get("status") && StringUtils.isNotBlank(map.get("status").toString())) {
                userModel.setStatus(map.get("status").toString());
            }
            if (null != map.get("createDate") && StringUtils.isNotBlank(map.get("createDate").toString())) {
                userModel.setCreateDate(map.get("createDate").toString().replace(" 00:00:00.0", ""));
            }
        }
        return userModel;

    }

    public ManagerModel fromManagerPo(ManagerPo managerPo, Boolean create) {
        ManagerModel managerModel = new ManagerModel();
        if(managerPo!=null){
            managerModel.setName(managerPo.getName());
            managerModel.setEmail(managerPo.getEmail());
            managerModel.setOpenId(managerPo.getUuid());
            managerModel.setStatus(managerPo.getStatus().toString());
            managerModel.setPhone(managerPo.getPhone());
            managerModel.setCreateDate(DateUtils.getTimeStampStr(managerPo.getCreateDate()));
            managerModel.setRoleType(managerPo.getRolePo().getType().toString());
            managerModel.setRoleName(managerPo.getRolePo().getName());
            managerModel.setRoleStatus(managerPo.getRolePo().getStatus().toString());
//            managerModel.setRoleId(managerPo.getRolePo().getUuid());

            if(managerPo.getProductPo()!=null){
//                managerModel.setProductId(managerPo.getProductPo().getUuid());
                managerModel.setProductType(managerPo.getProductPo().getType());
                managerModel.setProductName(managerPo.getProductPo().getName());
            }

            if (create) {
                managerModel.setAccessKey(managerPo.getAccessKey());
                managerModel.setSecretKey(managerPo.getSecretKey());
            }
        }
        return managerModel;
    }

    public List<ManagerModel> fromManagerPos(List<ManagerPo> managerPos, Boolean isAll) {
        List<ManagerModel> list = new ArrayList<>();
        if (managerPos != null && !managerPos.isEmpty()) {
            for (ManagerPo po : managerPos) {
                list.add(this.fromManagerPo(po, false));
            }
        }
        return list;
    }

    public RoleModel fromRolePo(RolePo rolePo) {
        RoleModel roleModel = new RoleModel();
        if (rolePo!=null){
            roleModel.setRoleId(rolePo.getUuid());
            roleModel.setName(rolePo.getName());
            roleModel.setStatus(rolePo.getStatus().toString());
            roleModel.setDescription(rolePo.getDescription());
            roleModel.setType(rolePo.getType().toString());
            roleModel.setCreateDate(DateUtils.getTimeStampStr(rolePo.getCreateDate()));
            roleModel.setUpdateDate(DateUtils.getTimeStampStr(rolePo.getUpdateDate()));
        }

        return roleModel;
    }

    public List<RoleModel> fromRolePos(List<RolePo> rolePos) {
        List<RoleModel> list = new ArrayList<>();
        if (rolePos != null && !rolePos.isEmpty()) {
            for (RolePo po : rolePos) {
                list.add(this.fromRolePo(po));
            }
        }
        return list;
    }
    public ProductModel fromProductPo(ProductPo productPo) {
        ProductModel productModel = new ProductModel();
        if(productPo!=null){
            productModel.setProductId(productPo.getUuid());
            productModel.setName(productPo.getName());
            productModel.setIsManyEquipmentLogin(productPo.getIsManyEquipmentLogin());
            productModel.setType(productPo.getType());
            productModel.setDomain(productPo.getDomain());
            productModel.setDescription(productPo.getDescription());
            productModel.setCreateDate(DateUtils.getTimeStampStr(productPo.getCreateDate()));
            productModel.setUpdateDate(DateUtils.getTimeStampStr(productPo.getUpdateDate()));
        }

        return productModel;
    }

    public List<ProductModel> fromProductPos(List<ProductPo> productPos) {
        List<ProductModel> list = new ArrayList<>();
        if (productPos != null && !productPos.isEmpty()) {
            for (ProductPo po : productPos) {
                list.add(this.fromProductPo(po));
            }
        }
        return list;
    }
    public EnterpriseModel fromEnterprisePo(EnterprisePo enterprisePo) {
        EnterpriseModel enterpriseModel = new EnterpriseModel();
        if(enterprisePo!=null){
            enterpriseModel.setEnterpriseId(enterprisePo.getId());
            enterpriseModel.setAppId(enterprisePo.getAppId());
            enterpriseModel.setAppKey(enterprisePo.getAppKey());
            enterpriseModel.setType(enterprisePo.getType().toString());
            enterpriseModel.setCreateDate(DateUtils.getTimeStampStr(enterprisePo.getCreateDate()));
            enterpriseModel.setUpdateDate(DateUtils.getTimeStampStr(enterprisePo.getUpdateDate()));
        }

        return enterpriseModel;
    }

    public List<EnterpriseModel> fromEnterprises(List<EnterprisePo> enterprisePos) {
        List<EnterpriseModel> list = new ArrayList<>();
        if (enterprisePos != null && !enterprisePos.isEmpty()) {
            for (EnterprisePo po : enterprisePos) {
                list.add(this.fromEnterprisePo(po));
            }
        }
        return list;
    }

    public TokenModel fromTokenPo(TokenPo tokenPo) {
        TokenModel tokenModel = new TokenModel();
        if(tokenPo!=null){
//        tokenModel.setType(tokenPo.getType().toString());
            tokenModel.setToken(tokenPo.getToken());
            tokenModel.setExpires(tokenPo.getExpires());
//        tokenModel.setOpenId(tokenPo.getOpenId());
            tokenModel.setScope(tokenPo.getScope());
            tokenModel.setRefreshToken(tokenPo.getRefreshToken());
        }

        return tokenModel;
    }
    public ManagerLogModel fromManagerLogPo(ManagerLogPo managerLogPo) {
        ManagerLogModel managerLogModel = new ManagerLogModel();
        if(managerLogPo!=null){
            managerLogModel.setAction(managerLogPo.getAction());
            managerLogModel.setLogId(managerLogPo.getId());
            managerLogModel.setDescription(managerLogPo.getDescription());
            ManagerPo managerPo=managerService.findOne(managerLogPo.getManagerId());
            if(managerPo!=null){
                managerLogModel.setManagerPhone(managerPo.getPhone());
            }
//            managerLogModel.setProductId(managerLogPo.getProductId());
            managerLogModel.setRequestId(managerLogPo.getRequestId());
            managerLogModel.setResourceId(managerLogPo.getResourceId());
            managerLogModel.setResult(managerLogPo.getResult());
            if(managerLogPo.getSourceEquipment()!=null){
                managerLogModel.setSourceEquipment(managerLogPo.getSourceEquipment().toString());
            }
            managerLogModel.setSourceIp(managerLogPo.getSourceIp());
            managerLogModel.setModule(managerLogPo.getModule());
            managerLogModel.setCreateDate(DateUtils.getTimeStampStr(managerLogPo.getCreateDate()));
        }

        return managerLogModel;
    }

    public List<ManagerLogModel> fromManagerLogPos(List<ManagerLogPo> managerLogPos) {
        List<ManagerLogModel> list = new ArrayList<>();
        if (managerLogPos != null && !managerLogPos.isEmpty()) {
            for (ManagerLogPo po : managerLogPos) {
                list.add(this.fromManagerLogPo(po));
            }
        }
        return list;
    }
    public UserLogModel fromUserLogPo(UserLogPo userLogPo) {
        UserLogModel userLogModel = new UserLogModel();
        if(userLogPo!=null){
            userLogModel.setAction(userLogPo.getAction());
            userLogModel.setId(userLogPo.getId());
            userLogModel.setDescription(userLogPo.getDescription());
            if(userLogPo.getUserPo()!=null){
                UserPo userPo=userService.findOne(userLogPo.getUserPo().getId());
                if(userPo!=null){
                    userLogModel.setUserPhone(userPo.getPhone());
                }
            }
            userLogModel.setRequestId(userLogPo.getRequestId());
            userLogModel.setResult(userLogPo.getResult());
            if(userLogModel.getSourceEquipment()!=null){
                userLogModel.setSourceEquipment(userLogPo.getSourceEquipment().toString());
            }
            userLogModel.setSourceIp(userLogPo.getSourceIp());
            userLogModel.setModule(userLogPo.getModule());
            userLogModel.setCreateDate(DateUtils.getTimeStampStr(userLogPo.getCreateDate()));
        }

        return userLogModel;
    }

    public List<UserLogModel> fromUserLogPos(List<UserLogPo> userLogPos) {
        List<UserLogModel> list = new ArrayList<>();
        if (userLogPos != null && !userLogPos.isEmpty()) {
            for (UserLogPo po : userLogPos) {
                list.add(this.fromUserLogPo(po));
            }
        }
        return list;
    }

    public MessageModel fromMessageModel(ManagerPo managerPo, UserPo userPo) {
        MessageModel messageModel = new MessageModel();
        if(managerPo!=null){

            messageModel.setManagerId(managerPo.getUuid());

            if(managerPo.getProductPo()!=null){
                messageModel.setProductId(managerPo.getProductPo().getUuid());
                messageModel.setProductType(managerPo.getProductPo().getType());
                messageModel.setProductName(managerPo.getProductPo().getName());
            }

            if (userPo!=null) {
                messageModel.setUserId(userPo.getUuid());
                messageModel.setUserPhone(userPo.getPhone());
            }
        }
        return messageModel;
    }
    public RemoteAddressModel fromRemoteAddressPo(RemoteAddressPo remoteAddressPo) {
        RemoteAddressModel remoteAddressModel = new RemoteAddressModel();
        if(remoteAddressPo!=null) {
            remoteAddressModel.setName(remoteAddressPo.getName());
            remoteAddressModel.setRemoteAddressId(remoteAddressPo.getUuid());
            remoteAddressModel.setRemoteAddress(remoteAddressPo.getRemoteAddress());

            remoteAddressModel.setCreateDate(DateUtils.getTimeStampStr(remoteAddressPo.getCreateDate()));
            remoteAddressModel.setUpdateDate(DateUtils.getTimeStampStr(remoteAddressPo.getUpdateDate()));
            if(remoteAddressPo.getProductPo()!=null){
                remoteAddressModel.setProductType(remoteAddressPo.getProductPo().getType());
                remoteAddressModel.setProductId(remoteAddressPo.getProductPo().getUuid());
            }
        }

        return remoteAddressModel;
    }

    public List<RemoteAddressModel> fromRemoteAddressPos(List<RemoteAddressPo> remoteAddressPos) {
        List<RemoteAddressModel> list = new ArrayList<>();
        if (remoteAddressPos != null && !remoteAddressPos.isEmpty()) {
            for (RemoteAddressPo po : remoteAddressPos) {
                list.add(this.fromRemoteAddressPo(po));
            }
        }
        return list;
    }

    public UserEnum.Status converterStatus(String status) {
        switch (status) {
            case "Enable":
                return UserEnum.Status.Enable;
            case "Disable":
                return UserEnum.Status.Disable;
            case "IsDelete":
                return UserEnum.Status.IsDelete;
            case "NoActive":
                return UserEnum.Status.NoActive;
            default:
                return null;
        }
    }
    public RoleEnum.Status converterRoleStatus(String status) {
        switch (status) {
            case "Enable":
                return RoleEnum.Status.Enable;
            case "Disable":
                return RoleEnum.Status.Disable;
            case "IsDelete":
                return RoleEnum.Status.IsDelete;
            default:
                return null;
        }
    }
    public RoleEnum.Type converterRoleType(String type) {
        switch (type) {
            case "System":
                return RoleEnum.Type.System;
            case "Product":
                return RoleEnum.Type.Product;
            case "Test":
                return RoleEnum.Type.Test;
            default:
                return RoleEnum.Type.Ordinary;
        }
    }

    public SourceEnum.EquipmentType converterSourceEquipmentType(String type) {
        if(StringUtils.isBlank(type)){
            return null;
        }
        switch (type) {
            case "Test":
                return SourceEnum.EquipmentType.Test;
            case "Android":
                return SourceEnum.EquipmentType.Android;
            case "Ios":
                return SourceEnum.EquipmentType.Ios;
            case "Template":
                return SourceEnum.EquipmentType.Template;
            default:
                return SourceEnum.EquipmentType.Web;
        }
    }

    public SourceEnum.EnterpriseType converterEquipmentType(String type) {
        switch (type) {
            case "Tencent":
                return SourceEnum.EnterpriseType.Tencent;
            case "WeChat":
                return SourceEnum.EnterpriseType.WeChat;
            case "Sina":
                return SourceEnum.EnterpriseType.Sina;
            default:
                return null;
        }
    }
    public UserEnum.Sex converterSex(int sex) {
        switch (sex) {
            case 1:
                return UserEnum.Sex.Man;
            case 2:
                return UserEnum.Sex.Woman;
            default:
                return UserEnum.Sex.UnKnow;
        }
    }

    public UserEnum.Sex toQQSex(int sex){
        switch (sex){
            case 0:
                return UserEnum.Sex.Man;
            case 1:
                return UserEnum.Sex.Woman;
            case 2:
                return UserEnum.Sex.UnKnow;
            default:
                return UserEnum.Sex.UnKnow;
        }

    }
    public UserModel fromUserByPhone(UserPo userPo) {
        UserModel userModel = new UserModel();
        if(userPo!=null){
            if(StringUtils.isNotBlank(userPo.getName())){
                userModel.setName(userPo.getName());
            }
            if(StringUtils.isNotBlank(userPo.getEmail())) {
                userModel.setEmail(userPo.getEmail());
            }
            userModel.setOpenId(userPo.getUuid());
            userModel.setStatus(userPo.getStatus().toString());
            userModel.setPhone(userPo.getPhone());
            userModel.setCreateDate(DateUtils.getTimeStampStr(userPo.getCreateDate()));
            if(userPo.getRegisterProductId()!=null){
              userModel.setRegisterProductId(userPo.getRegisterProductId());
              ProductPo productPo=  productService.findOne(userPo.getRegisterProductId());
                if(productPo!=null){
                    userModel.setProductType(productPo.getType());
                    userModel.setProductName(productPo.getName());
                }
            }
            
           
          
            if(StringUtils.isNotBlank(userPo.getAccessKey())){
                userModel.setAccessKey(userPo.getAccessKey());
            }
            if(StringUtils.isNotBlank(userPo.getSecretKey())) {
                userModel.setSecretKey(userPo.getSecretKey());
            }
            userModel.setId(userPo.getId());
            if(StringUtils.isNotBlank(userPo.getPassword())) {
                userModel.setPassword(userPo.getPassword());;
            }
        }
		return userModel;
    }
}
