package com.eeduspace.uuims.oauth.persist.po;

import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.Date;

/**
 * Author: dingran
 * Date: 2015/10/22
 * Description:
 */
@Entity
@Table(name = "auth_user_log")
public class UserLogPo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false)
    private Long id;
//    @Column(nullable = false,name = "user_id")

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @org.hibernate.annotations.ForeignKey(name = "none")
    @NotFound(action= NotFoundAction.IGNORE)
    private UserPo userPo;
    //产品ID
    @Column(name = "product_id")
    private Long productId;
    //操作的设备类型
    @Column(name = "source_equipment")
    private SourceEnum.EquipmentType sourceEquipment;
    //操作的来源IP
    @Column(name = "source_ip")
    private String sourceIp;
    //操作模块
    @Column(nullable = false)
    private String module;
    //操作动作
    private String action;
    //结果
    private Boolean result;
    //描述
    @Column(length = 1000)
    private String description;
    //请求ID
    @Column(name = "request_id")
    private String requestId;
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

    public UserPo getUserPo() {
        return userPo;
    }

    public void setUserPo(UserPo userPo) {
        this.userPo = userPo;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public SourceEnum.EquipmentType getSourceEquipment() {
        return sourceEquipment;
    }

    public void setSourceEquipment(SourceEnum.EquipmentType sourceEquipment) {
        this.sourceEquipment = sourceEquipment;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
