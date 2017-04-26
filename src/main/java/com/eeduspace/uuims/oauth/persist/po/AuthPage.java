package com.eeduspace.uuims.oauth.persist.po;

import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/22
 * Description:
 */
public class AuthPage {

    private Integer totalRecords;
    private Integer totalshowRecords;

    private List list;

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Integer getTotalshowRecords() {
        return list.size();
    }

    public void setTotalshowRecords(Integer totalshowRecords) {
        this.totalshowRecords = totalshowRecords;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }
}
