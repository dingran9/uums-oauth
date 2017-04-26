package com.eeduspace.uuims.oauth.service;


import com.eeduspace.uuims.oauth.persist.po.ManagerLogPo;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.ProductPo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:管理员操作日志管理
 */
public interface ManagerLogService {
	/**
	 * 获取当天用户注册数据
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月22日 下午2:49:34
	 * @param productId
	 * @return
	 */
	List<ManagerLogPo> findAllTodayRegister(Long productId);
    /**
     * 查询所有
     * @return
     */
    List<ManagerLogPo> findAll();
    /**
     * 查找
     * @param logId
     * @return
     */
    ManagerLogPo findOne(Long logId);

    /**
     * 新增/更新
     * @param ManagerLogPo
     * @return
     */
    ManagerLogPo save(ManagerLogPo ManagerLogPo);
    /**
     * 新增
     * @return
     */
    public ManagerLogPo create(Long managerId, String action,String module, Boolean result,Long productId,String sourceIp,String sourceEquipment,String requestId) ;
    /**
     * 新增
     * @return
     */
    public ManagerLogPo create(ManagerPo managerPo,String resourceId, String action,String module, Boolean result,ProductPo productPo,String sourceIp,String sourceEquipment,String requestId) ;

    /**
     * 删除
     * @param id
     */
    void delete(Long id);

    /**
     * 分页获取代列表
     * @param pageable
     * @return
     */
    Page<ManagerLogPo> findPage(ManagerPo manager,String type, String keyword,String param,String sort, Pageable pageable);
    /**
     * 获取用户注册数据
     * Author： zhuchaowei
     * e-mail:zhuchaowei@e-eduspace.com
     * 2016年3月24日 上午9:09:37
     * @param startDate
     * @param endDate
     * @return
     */
    List<Object> findUserRegisterData(Date startDate,Date endDate);
    
    /**
     * 统计当天按小时分组注册数据
     * Author： zhuchaowei
     * e-mail:zhuchaowei@e-eduspace.com
     * 2016年3月25日 下午4:04:45
     * @param productId
     * @return
     */
    List<Object> findUserRegisterGroupByHour(Long productId);
    /**
     * 获取当天注册量根据产品id分组
     * Author： zhuchaowei
     * e-mail:zhuchaowei@e-eduspace.com
     * 2016年3月29日 下午3:23:47
     * @return
     */
    List<Object> findUserRegisterGroupByProductId(Long productId);
}
