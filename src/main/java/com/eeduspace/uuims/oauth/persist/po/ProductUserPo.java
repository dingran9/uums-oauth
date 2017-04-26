package com.eeduspace.uuims.oauth.persist.po;

import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;

import javax.persistence.*;
import java.util.Date;

/**
 * Author: dingran
 * Date: 2015/11/2
 * Description:
 */
@Entity
@Table(name = "auth_product_user")
public class ProductUserPo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false)
    private Long id;
    //产品ID
    @Column(name = "product_id")
    private Long productId;
    //用户ID
    @Column(name = "user_id")
    private Long userId;

    //登录状态
    @Column(name = "login_status",nullable = false)
    private UserEnum.LoginStatus loginStatus= UserEnum.LoginStatus.UnKnow;

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

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public UserEnum.LoginStatus getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(UserEnum.LoginStatus loginStatus) {
        this.loginStatus = loginStatus;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
