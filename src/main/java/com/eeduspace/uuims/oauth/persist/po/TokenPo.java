package com.eeduspace.uuims.oauth.persist.po;

import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.TokenEnum;

import javax.persistence.*;
import java.util.Date;

/**
 * Author: dingran
 * Date: 2015/10/22
 * Description:令牌表
 */
@Entity
@Table(name = "auth_token")
public class TokenPo {
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
    //用户标识
    @Column(nullable = false,name = "open_id",length = 1000)
    private String openId;
    //有效时间
    @Column(nullable = false)
    private String expires;
    //作用域
    private String scope;
    //类型 目前只有user类型
    private TokenEnum.Type Type;


    @Column(name = "equipment_type")
    private SourceEnum.EquipmentType equipmentType;
    @Column(name = "product_id")
    private Long productId;

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

    public TokenEnum.Type getType() {
        return Type;
    }

    public void setType(TokenEnum.Type type) {
        Type = type;
    }

    public SourceEnum.EquipmentType getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(SourceEnum.EquipmentType equipmentType) {
        this.equipmentType = equipmentType;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
