package com.eeduspace.uuims.oauth.persist.po;

import com.eeduspace.uuims.oauth.util.UIDGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * Author: dingran
 * Date: 2015/10/22
 * Description:
 */
@Entity
@Table(name = "auth_product")
public class ProductPo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false)
    private Long id;
    //唯一标识
    @Column(unique = true)
    private String uuid = UIDGenerator.getUUID();
    //名称
    @Column(nullable = false,unique = true)
    private String name;
//    private ProductEnum.Type type;
    //类型 唯一
//    @Column(unique = true)
    private String type;
    //是否允许同设备同时登录
    @Column(name = "is_many_equipment_login")//true表示可以 false 表示不允许
    private Boolean isManyEquipmentLogin;
    //描述
    private String description;
    //域名不能为空
    @Column(nullable = false)
    private String domain;

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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getIsManyEquipmentLogin() {
        return isManyEquipmentLogin;
    }

    public void setIsManyEquipmentLogin(Boolean isManyEquipmentLogin) {
        this.isManyEquipmentLogin = isManyEquipmentLogin;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
