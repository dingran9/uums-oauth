package com.eeduspace.uuims.oauth.persist.po;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Author: dingran
 * Date: 2015/10/22
 * Description:授权表（我方提供认证时使用）
 */
@Entity
@Table(name = "auth_code")
public class CodePo implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false)
    private Long id;

    //授权码
    @Column(nullable = false)
    private String code;

    //第三方合作企业ID
    @Column(name = "enterprise_id",nullable = false)
    private Long enterpriseId;

    //有效时间
    @Column(nullable = false)
    private Long expires;

    //创建时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false,name = "create_time")
    private Date createDate = new Date();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public Long getExpires() {
        return expires;
    }

    public void setExpires(Long expires) {
        this.expires = expires;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

/*    public enum ThirdPartyType {
        Tencent(0),
        WeChat(1),
        Sina(2);
        private final int value;

        public int getValue() {
            return value;
        }

        ThirdPartyType(int value) {
            this.value = value;
        }
    }
    */
}
