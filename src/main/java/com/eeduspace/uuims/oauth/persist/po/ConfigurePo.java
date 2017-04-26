package com.eeduspace.uuims.oauth.persist.po;

import javax.persistence.*;

/**
 * Author: dingran
 * Date: 2015/10/22
 * Description:系统配置表
 */
@Entity
@Table(name = "auth_configure")
public class ConfigurePo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false)
    private Long id;

    //名称
    @Column(nullable = false,length = 45)
    private String name;

    //值
    @Column(nullable = false)
    private String value;

    //描述
    private String description;

    public Long getId() {
        return id;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
