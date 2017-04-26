package com.eeduspace.uuims.oauth.persist.po;

import javax.persistence.*;
import java.util.Date;

/**
 * Author: dingran
 * Date: 2015/10/22
 * Description:第三方企业token （暂未使用）
 */
@Entity
@Table(name = "auth_enterprise_token")
public class EnterpriseTokenPo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false)
    private Long id;

    //令牌
    @Column(nullable = false,length = 1000)
    private String token;

    //刷新令牌
    @Column(nullable = false,name = "refresh_token",length = 1000)
    private String refreshToken;

    //唯一标识
    @Column(nullable = false,name = "open_id")
    private String openId;

    //有效时间
    @Column(nullable = false)
    private String expires;

    //第三方企业ID
    @Column(name = "enterprise_id")
    private Long enterpriseId;

    //用户授权作用域
    @Column(length = 500)
    private String scope;

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public Long getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
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
