package com.eeduspace.uuims.oauth.ws;

import com.eeduspace.uuims.comm.util.base.DateUtils;
import com.eeduspace.uuims.comm.util.base.ValidateUtils;
import com.eeduspace.uuims.comm.util.base.encrypt.Digest;
import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.model.UserModel;
import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.UserInfoPo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import com.eeduspace.uuims.oauth.redis.RedisClientTemplate;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.response.UserResponse;
import com.eeduspace.uuims.oauth.service.*;
import com.eeduspace.uuims.oauth.util.RandomUtils;
import com.eeduspace.uuims.rescode.ParamName;
import com.eeduspace.uuims.rescode.ResourceName;
import com.eeduspace.uuims.rescode.ResponseCode;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: dingran Date: 2015/12/4 Description:
 */

@Component
@Path(value = "/batch")



/*
 * @Consumes({MediaType.APPLICATION_JSON})
 * 
 * @Produces({MediaType.APPLICATION_JSON})
 */
@CrossOriginResourceSharing(allowAllOrigins = true)
//extends BaseWs
public class BatchWs extends BaseWs {

    private final Logger logger = LoggerFactory.getLogger(BatchWs.class);
    private String requestId;
    private Gson gson = new Gson();

    @Inject
    private UserService userService;
    @Inject
    private ManagerService managerService;
    @Inject
    private AuthConverter authConverter;
    @Inject
    private TokenService tokenService;
    @Inject
    private RedisClientTemplate redisClientTemplate;
    @Inject
    private UserLogService userLogService;
    @Inject
    private ManagerLogService managerLogService;
    @Inject
    private AclService aclService;
    @Value("${uuims.user.excelTemplate.url}")
    private String excelTemplateUrl;

    HttpServletResponse response;


    // TODO 获取平台、终端等信息
    // 进行基本的验证

    /**
     * 1.验证timestamp 2.验证bodyMD5 3.验证手机号
     *
     * @throws IOException
     */
    @Override
    public Response dispatch(String action, String token, String requestBody, HttpServletRequest request, ManagerPo managerPo, UserPo userPo) {

        logger.debug("dispatch----action=" + action + "," + "token=" + token + "," + "requestBody=" + requestBody + "," + "request=" + request + "," + "managerPo=" + managerPo);
        switch (ActionName.toEnum(action)) {
            case ADD_EXCEL_BY_TEMPLATE:
                return addEmployeeByExcel(requestBody, request, response, managerPo);
            default:
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.USER.toString()))).build();
        }
    }

    /**
     * 获取excle模板
     *
     * @author：zengzhe 2016年3月16日上午11:07:57
     */
    private Response getExcelTemplate(HttpServletResponse response) {

        try {
            response.setHeader("Content-Disposition", "attachment; filename=uuims-user-template.xls");
            response.setContentType("application/octet-stream; charset=utf-8");
            OutputStream outputStream = null;
            try {
                outputStream = response.getOutputStream();
            } catch (IOException e) {
                logger.error("获取用户excel模板失败!", e);
            }
            File file = new File(excelTemplateUrl);
            FileInputStream fileInputStream = new FileInputStream(file);
            IOUtils.copy(fileInputStream, outputStream);
            fileInputStream.close();
            outputStream.flush();
            outputStream.close();

            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SUCCESS.toString()))).build();
        } catch (Exception e) {
            logger.error("requestId：{},ldap Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }

    /**
     * @throws IOException
     * @author：zengzhe 2016年3月16日上午11:07:57
     */
    @Transactional
    private Response addEmployeeByExcel(String requestBody, HttpServletRequest request, HttpServletResponse response, ManagerPo managerPo) {

        try {
            //只有系统管理员及产品管理员有操作权限
            if (!aclService.isManager(managerPo)) {
                logger.error("requestId：" + requestId + "," + ResponseCode.FORBIDDEN_NOPERMISSION.toString() + ParamName.PHONE.toString());
            }

            // 处理excle字符串后存入数据库
            UserModel userModels = gson.fromJson(requestBody, UserModel.class);
            List<UserModel> excleList = userModels.getUserList();
            List<UserModel> userModelList = new ArrayList<UserModel>();
            BaseResponse baseResponse = null;
            UserResponse userResponse = null;
            // x:成功导入用户数,y:Excel中重复用户数,z:错误数据数,
            int successSize = 0, repeatSize = 0, invalidSize = 0,failSize=0;
            List<UserModel> results=new ArrayList<>();
            //List<UserModel> excleList = JSONUtils.jsonToList(requestBody,UserModel.class);
            for (UserModel userModel : excleList) {

                try {
                    if (StringUtils.isBlank(userModel.getPassword())) {
                        logger.error("requestId：" + requestId + "," + ResponseCode.PARAMETER_MISS.toString() + ParamName.PASSWORD.toString());
                        invalidSize++;
                        continue;
                    }
                    if (StringUtils.isBlank(userModel.getPhone())) {
                        logger.error("requestId：" + requestId + "," + ResponseCode.PARAMETER_MISS.toString() + ParamName.PHONE.toString());
                        invalidSize++;
                        continue;
                    }
                    // 验证数据格式
                    if (!ValidateUtils.isMobile(userModel.getPhone())) {
                        logger.error("requestId：" + requestId + "," + ResponseCode.PARAMETER_INVALID.toString() + ParamName.PHONE.toString());
                        invalidSize++;
                        continue;
                    }
                    // 手机号查重
                    if (userService.findByPhone(userModel.getPhone()) != null) {
                        logger.error("requestId：" + requestId + "," + ResponseCode.RESOURCE_DUPLICATE.toString() + ParamName.PHONE.toString());
                        repeatSize++;
                        continue;
                    }
                    //验证学籍号
                    if(StringUtils.isNotBlank(userModel.getSchoolNumber())){
                        if(userService.findBySchoolNumber(userModel.getSchoolNumber())!=null){
                            repeatSize++;
                            continue;
                        }
                    }
                    String accessKeyId = "CU" + Digest.md5Digest16(userModel.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
                    String secretKey = Digest.md5Digest(userModel.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6) + accessKeyId);
                    userModel.setAccessKey(accessKeyId);
                    userModel.setSecretKey(secretKey);
                    if (managerPo.getProductPo() != null) {
                        userModel.setRegisterProductId(managerPo.getProductPo().getId());
                    }
                    UserInfoPo userInfoPo = new UserInfoPo();
                    userInfoPo.setManagerId(managerPo.getId());
                    userInfoPo.setRegisterSource(SourceEnum.EquipmentType.Web);
                    userInfoPo.setCreateType(UserEnum.CreateType.TemplateAdd);

                    results.add(userModel);

                    successSize++;
                    logger.debug("----成功导入用户数" + successSize + "," + "Excel中重复用户数" + repeatSize + "," + "错误数据数" + invalidSize);

                } catch (Exception e) {
                    logger.error("requestId：{},batch add user Exception：{}", requestId, e);
                    failSize++;
                }
            }
            List<UserPo> userPos=  userService.createList(results,managerPo.getId());

            logger.debug("----成功导入用户数" + successSize + "," + "Excel中重复用户数" + repeatSize + "," + "错误数据数" + invalidSize+"," + "失败数" + failSize);
            logger.debug("----成功导入用户数" + results);
            baseResponse = new BaseResponse(requestId);
            userResponse = new UserResponse();
            userResponse.setUserList(authConverter.fromUserPos(userPos, false, true));
            userResponse.setBatchSuccessSize(successSize);
            userResponse.setBatchRepeatSize(repeatSize);
            userResponse.setBatchInvalidSize(invalidSize);
            userResponse.setBatchFailSize(failSize);
            baseResponse.setResult(userResponse);
            return Response.ok(gson.toJson(baseResponse)).build();

        } catch (Exception e) {
            logger.error("requestId：{},batch Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }


    /**
     * @author：zengzhe Description:获取AccessKeyId 2016年3月16日下午1:12:23
     */
    private String getAccessKeyId(UserModel userModel) {
        String accessKeyId = "CU" + Digest.md5Digest16(userModel.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
        // 验证 AccessKeyId是否已存在
        UserPo ven = userService.findByAccessKeyId(accessKeyId);
        if (ven != null) {
            accessKeyId = "CU" + Digest.md5Digest16(userModel.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
        }
        return accessKeyId;
    }

}
