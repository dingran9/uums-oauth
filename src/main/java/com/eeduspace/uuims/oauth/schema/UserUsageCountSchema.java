package com.eeduspace.uuims.oauth.schema;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.xml.rpc.holders.LongHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.eeduspace.uuims.comm.util.base.DateUtils;
import com.eeduspace.uuims.oauth.persist.enumeration.UserUsageCountEnum.CountType;
import com.eeduspace.uuims.oauth.persist.po.ManagerLogPo;
import com.eeduspace.uuims.oauth.persist.po.UserLogPo;
import com.eeduspace.uuims.oauth.persist.po.UserUsageCountPo;
import com.eeduspace.uuims.oauth.service.ManagerLogService;
import com.eeduspace.uuims.oauth.service.ProductService;
import com.eeduspace.uuims.oauth.service.UserLogService;
import com.eeduspace.uuims.oauth.service.UserUsageCountService;
import com.eeduspace.uuims.oauth.ws.BaseWs;
/**
 * @author zhuchaowei
 * 2016年3月24日
 * Description 用户注册量和活跃量统计 定时器
 */
@Component
public class UserUsageCountSchema {
	@Inject
	private UserLogService userLogService;
	@Inject
	private ManagerLogService managerLogService;
	
	@Inject
	private ProductService productService;
	@Inject
	private UserUsageCountService usageCountService;
	private final Logger logger = LoggerFactory
			.getLogger(UserUsageCountSchema.class);

	// 每天00:10 执行定时任务
	@Scheduled(cron = "0 10 0 * * ? ")
	public void saveData() {
		Date nowDate = new Date();
		Date startDate = DateUtils.addDay(nowDate, -1);
		startDate.setHours(0);
		startDate.setMinutes(0);
		startDate.setSeconds(0);
		Date endDate = DateUtils.addDay(nowDate, -1);;
		endDate.setHours(23);
		endDate.setMinutes(59);
		endDate.setSeconds(59);
		try {
			saveUserActiveData(startDate, endDate);
		} catch (Exception e) {
			logger.error("定时任务执行保存用户活跃量报错------------》", e);
		}
		try {
			saveUserRegisterData(startDate, endDate);
		} catch (Exception e) {
			logger.error("定时任务执行保存注册量报错------------》", e);
		}
		logger.info("-----当天定时任务执行完毕-------");
	}
	/**
	 * 保存前一天的用户注册量
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月24日 上午11:14:28
	 * @param startDate
	 * @param endDate
	 */
	private void saveUserRegisterData(Date startDate, Date endDate) {
		List<Object> objects = managerLogService.findUserRegisterData(
				startDate, endDate);
		Date creDate = DateUtils.addDay(new Date(),-1);
		if(objects.size()==0){
			UserUsageCountPo usageCountPo = new UserUsageCountPo();
			usageCountPo.setCountTotal(0L);
			usageCountPo.setCountType(CountType.USERREGISTER);
			usageCountPo.setCreateDate(creDate);
			usageCountPo.setProductId(-1l);
			usageCountPo.setProductName("---");
			usageCountService.save(usageCountPo);
		}
		for (int i = 0; i < objects.size(); i++) {
			Object[] object = (Object[]) objects.get(i);
			UserUsageCountPo usageCountPo = new UserUsageCountPo();
			usageCountPo.setCountTotal((Long) object[0]);
			usageCountPo.setProductId((Long) object[1]);
			usageCountPo.setCreateDate(creDate);
			usageCountPo.setCountType(CountType.USERREGISTER);
			usageCountPo.setProductName(productService.findOne((Long) object[1])==null?"----":productService.findOne((Long) object[1]).getName());
			usageCountService.save(usageCountPo);
		}
		

	}
	/**
	 * 保存前一天的用户活跃量
	 * Author： zhuchaowei
	 * e-mail:zhuchaowei@e-eduspace.com
	 * 2016年3月24日 上午11:14:43
	 * @param startDate
	 * @param endDate
	 */
	private void saveUserActiveData(Date startDate, Date endDate) {
		List<Object> objects = userLogService.findUserActiveData(startDate,
				endDate);
		Date creDate = DateUtils.addDay(new Date(),-1);
		if(objects.size()==0){
			UserUsageCountPo usageCountPo = new UserUsageCountPo();
			usageCountPo.setCreateDate(creDate);
			usageCountPo.setCountTotal(0l);
			usageCountPo.setProductId(-1l);
			usageCountPo.setCountType(CountType.USERACTIVE);
			usageCountPo.setProductName("---");
			usageCountService.save(usageCountPo);
		}
		for (int i = 0; i < objects.size(); i++) {
			Object[] object = (Object[]) objects.get(i);
			UserUsageCountPo usageCountPo = new UserUsageCountPo();
			usageCountPo.setCountTotal((Long) object[0]);
			usageCountPo.setProductId((Long) object[1]);
			usageCountPo.setCreateDate(creDate);
			usageCountPo.setCountType(CountType.USERACTIVE);
			usageCountPo.setProductName(productService.findOne((Long) object[1])==null?"----":productService.findOne((Long) object[1]).getName());
			usageCountService.save(usageCountPo);
		}
	}
}
