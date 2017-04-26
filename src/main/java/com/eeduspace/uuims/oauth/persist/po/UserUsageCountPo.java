package com.eeduspace.uuims.oauth.persist.po;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.eeduspace.uuims.oauth.persist.enumeration.UserUsageCountEnum;

/**
 * 
 * @author zhuchaowei 2016年3月21日 Description 用户使用情况统计实体
 */
@Entity
@Table(name = "auth_user_usage_count")
public class UserUsageCountPo {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(updatable = false)
	protected Long id;
	/**
	 * 产品对应id
	 */
	@Column(name = "product_id")
	private Long productId;
	/**
	 * 统计类型  
	 */
	@Column(name = "count_type")
	private UserUsageCountEnum.CountType countType;
	/**创建时间*/
	@Temporal(TemporalType.TIMESTAMP)
	@Column(updatable = false, name = "create_time")
	private Date createDate = new Date();
	/**数量*/
	@Column(name="count_total")
	private Long countTotal;
	/**
	 * 产品名称
	 */
	@Column(name="product_name")
	private String productName;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public UserUsageCountEnum.CountType getCountType() {
		return countType;
	}
	public void setCountType(UserUsageCountEnum.CountType countType) {
		this.countType = countType;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Long getCountTotal() {
		return countTotal;
	}
	public void setCountTotal(Long countTotal) {
		this.countTotal = countTotal;
	}
}
