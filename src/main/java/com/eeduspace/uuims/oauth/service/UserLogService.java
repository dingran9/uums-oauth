package com.eeduspace.uuims.oauth.service;

import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.UserLogPo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:用户操作日志管理
 */
public interface UserLogService {
	/**
	 * 获取当天所有的登录日志
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月22日 下午12:42:02
	 * @return
	 */
	List<UserLogPo> findTodayAllData(Long productId);
    /**
     * 查询所有
     * @return
     */
    List<UserLogPo> findAll();
    /**
     * 查找
     * @param logId
     * @return
     */
    UserLogPo findOne(Long logId);

    /**
     * 新增/更新
     * @param UserLogPo
     * @return
     */
    UserLogPo save(UserLogPo UserLogPo);

    /**
     * 新增/更新
     * @return
     */
    public void create(UserPo userPo, String action,String module, Boolean result,Long productId,String sourceIp,String sourceEquipment,String requestId) ;

    /**
     * 新增/更新
     * @return
     */
    public UserLogPo create(Long userId, String action, String module, Boolean result, Long productId, String sourceIp, String sourceEquipment,String requestId);
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
    Page<UserLogPo> findPage(String accountPhone, ManagerPo managerPo,String type, String keyword,String param,String sort, Pageable pageable);
    /**
     * 获取活跃用户数据
     * Author： zhuchaowei
     * e-mail:zhuchaowei@e-eduspace.com
     * 2016年3月24日 上午9:08:39
     * @param startDate
     * @param endDate
     * @return
     */
    public List<Object> findUserActiveData(Date startDate,Date endDate);
    /**
     * 根据时间段分组统计
     * Author： zhuchaowei
     * e-mail:zhuchaowei@e-eduspace.com
     * 2016年3月25日 下午3:18:01
     * @param productId 产品ID
     * @return
     */
    public List<Object> findUserActiveGroupByHour(Long productId);
    /**
     * 获取用户活跃量  根据产品ID分组
     * Author： zhuchaowei
     * e-mail:zhuchaowei@e-eduspace.com
     * 2016年3月29日 下午3:55:23
     * @return
     */
    public List<Object> findUserActeiceGroupByProductId(Long productId);
}
