package com.eeduspace.uuims.oauth.persist.po;

import com.eeduspace.uuims.oauth.persist.enumeration.RoleEnum;
import com.eeduspace.uuims.oauth.util.UIDGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * Author: dingran
 * Date: 2015/10/22
 * Description:
 */
@Entity
@Table(name = "auth_role")
public class RolePo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false)
    private Long id;
    //唯一标识
    @Column(unique = true)
    private String uuid = UIDGenerator.getUUID();
    //名称
    @Column(nullable = false)
    private String name;
    //类型
    @Column(nullable = false,unique = false)
    private RoleEnum.Type type;
    //状态
    @Column(nullable = false)
    private RoleEnum.Status status;
    //描述
    private String description;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public RoleEnum.Type getType() {
        return type;
    }

    public void setType(RoleEnum.Type type) {
        this.type = type;
    }

    public RoleEnum.Status getStatus() {
        return status;
    }

    public void setStatus(RoleEnum.Status status) {
        this.status = status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
