package com.eeduspace.uuims.oauth.service;




import com.eeduspace.uuims.oauth.model.UserModel;
import com.eeduspace.uuims.oauth.model.user.ImportExcelResults;
import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum.Status;
import com.eeduspace.uuims.oauth.persist.po.*;
import com.eeduspace.uuims.oauth.response.UserResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/21
 * Description:用户管理
 */
public interface UserService {
    /**
     * 查找用户
     * @param userId
     * @return
     */
    UserPo findOne(Long userId);
    /**
     * 查找用户
     * @param phone
     * @return
     */
    UserPo findByPhone(String phone);
    /**
     * 查找用户
     * @param schoolNumber
     * @return
     */
    UserPo findBySchoolNumber(String schoolNumber);
    /**
     * 查找用户
     * @param email
     * @return
     */
    UserPo findByEmail(String email);
    /**
     * 查找用户
     * @param AccessKeyId
     * @return
     */
    UserPo findByAccessKeyId(String AccessKeyId);

    /**
     * 查找用户
     * @param uuid
     * @return
     */
    UserPo findByUuid(String uuid);

    /**
     * 更新用户
     * @param userPo
     * @return
     */
    UserPo save(UserPo userPo);

    /**
     * 新增用户
     * @param userModel
     * @return
     */
    UserPo create(UserModel userModel , Long managerId);
    /**
     * 新增用户
     * @param userModels
     * @return
     */
    List<UserPo> createList(List<UserModel> userModels, Long managerId);
    /**
     * 更新用户
     * @param userPo
     * @return
     */
    UserPo save(UserPo userPo,UserInfoPo userInfoPo);
    /**
     * 第三方用户绑定手机
     * @param userPo
     * @return
     */
    UserPo save(UserPo userPo, EnterpriseUserPo enterpriseUserPo);
    /**
     * 删除用户
     * @param userPo
     */
    void delete(UserPo userPo);


    /**
     * 验证登录用户是否存在
     * @param phone 登陆用户
     * @param password MD5加密过的密码
     * @return
     */
    UserPo validateUser(String phone, String password);

    /**
     * 查询所有
     * @return
     */
    List<UserPo> findAll();

    /**
     * 按照管理员查询
     * @return
     */
     List<UserPo> findAllByManager(ManagerPo managerPo) ;
    /**
     * 按照管理员查询
     * @return
     */
    List<UserPo> findAllByManager(ManagerPo managerPo,UserEnum.Status status) ;

    /**
     * 获取分页
     * @param type
     * @param keyword
     * @param param
     * @param sort
     * @param request
     * @return
     */
    Page<UserPo> findPage(String type,String keyword,String param,String sort,PageRequest request);

    /**
     * 获取分页
     * @param managerPo
     * @param type
     * @param keyword
     * @param param
     * @param sort
     * @param request
     * @return
     */
    Page<UserPo> findPage(ManagerPo managerPo,String type,String keyword,String param,String sort,PageRequest request);


    AuthPage findPage(ManagerPo managerPo,String type,String keyword,String param,String sort,int pageNo ,int PageSize) throws Exception;
    
    /**
     * 添加根据productId和uuid集合来查询用户列表
     * */
    List<UserPo> getByRegisterProductIdAndUuidIn(Long productId,List<String> uuids);

/**************************用户详情**********************************/


    UserInfoPo findInfoByUserId(Long userId);

    ImportExcelResults addEmployeeByExcel(byte[] byteArray);

    List<UserPo> findByProductId(Long id);
	UserResponse deleteAllUser(UserModel userModel, ManagerPo managerPo, String requestId);
	UserResponse updateStatusUser(UserModel userModel, ManagerPo managerPo, String requestId, Status status);}



