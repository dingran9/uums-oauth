package com.eeduspace.uuims.oauth.persist.po;



import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;

import javax.persistence.*;
import java.util.Date;

/**
 * Author: dingran
 * Date: 2015/10/22
 * Description:第三方用户表(记录 微信、腾讯登录认证的用户信息)
 */
@Entity
@Table(name = "auth_enterprise_user")
public class EnterpriseUserPo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false)
    private Long id;

    //第三方用户的唯一标识
    @Column(nullable = false,name = "third_open_id")
    private String thirdOpenId;

    //取系统用户id
    @Column(name = "user_id")
    private Long userId;

    //手机号
    private String phone;

    //第三方企业账户Id
    @Column(nullable = false,name = "enterprise_id")
    private Long enterpriseId;

    //产品Id
    @Column(name = "product_id")
    private Long productId;

    //最后登录时间
    @Column(name = "last_login_time")
    private Date lastLoginTime;

    //最后登录Ip
    @Column(name = "last_login_ip")
    private String lastLoginIp;

    //创建时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false,name = "create_time")
    private Date createDate = new Date();

    //更新时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_time")
    private Date updateDate = new Date();

    //昵称
    @Column(name = "nick_name")
    private String nickName;
    //性别
    private UserEnum.Sex sex;
    // 国家
    private String country;
    // 省份
    private String province;
    // 城市
    private String city;
    // 用户头像链接
    @Column(name = "head_img_url")
    private String headImgUrl;
    // 用户授权作用域
    private String scope;
    //扩展字段
    private String extend_;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getThirdOpenId() {
        return thirdOpenId;
    }

    public void setThirdOpenId(String thirdOpenId) {
        this.thirdOpenId = thirdOpenId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public UserEnum.Sex getSex() {
        return sex;
    }

    public void setSex(UserEnum.Sex sex) {
        this.sex = sex;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getHeadImgUrl() {
        return headImgUrl;
    }

    public void setHeadImgUrl(String headImgUrl) {
        this.headImgUrl = headImgUrl;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getExtend_() {
        return extend_;
    }

    public void setExtend_(String extend_) {
        this.extend_ = extend_;
    }
}
