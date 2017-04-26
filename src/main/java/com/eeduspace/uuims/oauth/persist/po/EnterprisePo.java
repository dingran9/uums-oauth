package com.eeduspace.uuims.oauth.persist.po;

import com.eeduspace.uuims.oauth.persist.enumeration.EnterpriseEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;

import javax.persistence.*;
import java.util.Date;

/**
 * Author: dingran
 * Date: 2015/10/22
 * Description:第三方企业
 */
@Entity
@Table(name = "auth_enterprise")
public class EnterprisePo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false)
    private Long id;

    //第三方工作平台 所申请的appId
    @Column(nullable = false,name = "app_id",length = 1000)
    private String appId;

    //第三方工作平台 所申请的appKey
    @Column(nullable = false,name = "app_key",length = 1000)
    private String appKey;

    //第三企业合作类型 目前统一为B型
    private EnterpriseEnum.Type type;

    //第三方企业类型
    @Column(name = "enterprise_type")
    private SourceEnum.EnterpriseType enterpriseType;

    //扩展字段
    private String extend_;

    //创建时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false,name = "create_time")
    private Date createDate = new Date();
    //更新时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_time")
    private Date updateDate = new Date();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public EnterpriseEnum.Type getType() {
        return type;
    }

    public void setType(EnterpriseEnum.Type type) {
        this.type = type;
    }

    public SourceEnum.EnterpriseType getEnterpriseType() {
        return enterpriseType;
    }

    public void setEnterpriseType(SourceEnum.EnterpriseType enterpriseType) {
        this.enterpriseType = enterpriseType;
    }

    public String getExtend_() {
        return extend_;
    }

    public void setExtend_(String extend_) {
        this.extend_ = extend_;
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
}
