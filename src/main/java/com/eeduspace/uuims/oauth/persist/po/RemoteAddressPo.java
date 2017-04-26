package com.eeduspace.uuims.oauth.persist.po;

import com.eeduspace.uuims.oauth.util.UIDGenerator;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.Date;

/**
 * 远程地址
 * User: TIANLI
 * Date: 2014/7/6 15:40
 */
@Entity
@Table(name = "auth_remote_address")
public class RemoteAddressPo {
    //id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false)
    protected Long id;
    //uuid
    @Column(unique = true)
    protected String uuid = UIDGenerator.getUUID();
    @Column(nullable = true)
    protected String name;

    @Column(unique = true,nullable = true,name = "remote_address")
    protected String remoteAddress;
    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    @ForeignKey(name = "none")
    @NotFound(action = NotFoundAction.IGNORE)
    protected ProductPo productPo;
    //创建时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    protected Date createDate = new Date();
    //更新时间
    @Temporal(TemporalType.TIMESTAMP)
    protected Date updateDate = new Date();

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

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
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

    public ProductPo getProductPo() {
        return productPo;
    }

    public void setProductPo(ProductPo productPo) {
        this.productPo = productPo;
    }
}
