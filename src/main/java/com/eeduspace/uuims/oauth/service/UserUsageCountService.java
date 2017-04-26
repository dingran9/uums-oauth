package com.eeduspace.uuims.oauth.service;

import java.util.Date;
import java.util.List;

import com.eeduspace.uuims.oauth.persist.enumeration.UserUsageCountEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.UserUsageCountEnum.CountType;
import com.eeduspace.uuims.oauth.persist.po.UserUsageCountPo;
/**
 * @author zhuchaowei
 * 2016年3月22日
 * Description  用户使用情况统计业务接口类
 */
public interface UserUsageCountService {
	/**
	 * 新增
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月21日 下午5:58:22
	 * @param usageCountPo
	 * @return
	 */
	UserUsageCountPo save(UserUsageCountPo usageCountPo);
	/**
	 * 根据时间查询所有数据
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月22日 上午9:46:40
	 * @param startDate 开始日期
	 * @param endDate   结束日期
	 * @param countType 统计类型
	 * @return
	 */
	List<UserUsageCountPo> findAll(Date startDate,Date endDate,UserUsageCountEnum.CountType countType);
	/**
	 * 根据产品ID获取数据
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月22日 上午9:48:10
	 * @param startDate 开始日期
	 * @param endDate   结束日期
	 * @param productId 产品id
	 * @param countType 统计类型
	 * @return
	 */
	List<UserUsageCountPo> findAllByProductId(Date startDate,Date endDate,Long productId,UserUsageCountEnum.CountType countType);
	
	/**
	 * 获取分组统计数据
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月25日 上午10:59:51
	 * @param startDate 开始时间
	 * @param endDate   结束时间
	 * @param productId 产品ID
	 * @param countType 统计类型
	 * @param groupType 分组类型
	 * @return
	 */
	List<Object> findGroupData(Date startDate,Date endDate,Long productId,UserUsageCountEnum.CountType countType,String groupType);
	
	/**
	 * 按产品分组统计
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月29日 下午2:41:06
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	List<Object> findGroupByProductId(Date startDate,Date endDate,UserUsageCountEnum.CountType countType,Long productId);
}
