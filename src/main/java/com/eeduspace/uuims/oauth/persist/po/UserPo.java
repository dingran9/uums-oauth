package com.eeduspace.uuims.oauth.persist.po;




import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import com.eeduspace.uuims.oauth.util.UIDGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Author: dingran
 * Date: 2015/10/21
 * Description:用户实体类
 */

@Entity
@Table(name = "auth_user")
public class UserPo implements Serializable {
    //用户id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false)
    protected Long id;
    //用户uuid
    @Column(unique = true)
    private String uuid = UIDGenerator.getUUID();
    @Column( nullable = false,unique = true)
    private String phone;
    //用户登陆密码
    private String password;
    //用户登录名
    private String name;
    //邮箱
    private String email;
    //学籍号
    @Column(name = "school_number",unique = true)
    private String schoolNumber;
    //公钥
    @Column( unique = true,name = "access_key",nullable = false)
    private String accessKey;
    //秘钥
    @Column(name = "secret_key",nullable = false)
    private String secretKey;
    //状态
    @Column(nullable = false)
    private UserEnum.Status status;
    //第一次新增时的所属产品ID
    @Column(name = "register_product_id")
    private Long registerProductId;
    //扩展字段
    private String extend_;
    //最后登录时间
    @Column(name = "last_login_time")
    private Date lastLoginDate;
    //创建类型
//    @Column(name = "create_type")
//    private UserEnum.CreateType createType;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSchoolNumber() {
        return schoolNumber;
    }

    public void setSchoolNumber(String schoolNumber) {
        this.schoolNumber = schoolNumber;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public UserEnum.Status getStatus() {
        return status;
    }

    public void setStatus(UserEnum.Status status) {
        this.status = status;
    }

    public String getExtend_() {
        return extend_;
    }

    public void setExtend_(String extend_) {
        this.extend_ = extend_;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
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

    public Long getRegisterProductId() {
        return registerProductId;
    }

    public void setRegisterProductId(Long registerProductId) {
        this.registerProductId = registerProductId;
    }
}
