package com.eeduspace.uuims.oauth.util;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Author: dingran
 * Date: 2015/11/3
 * Description: 服务层帮助类
 */
public class ServiceHelper {

    public final static String SORT_ASC = "asc";
    public final static String SORT_DESC = "desc";

    /**
     * 排序查询
     * @param root
     * @param query
     * @param builder
     * @param param
     * @param sort
     */
    public static void sortQuery(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder builder,String param,String sort){
        if(SORT_ASC.equals(sort) && StringUtils.isNotBlank(param)){
            query.orderBy(builder.asc(root.get(param)));
        }else if(SORT_DESC.equals(sort) && StringUtils.isNotBlank(param)){
            query.orderBy(builder.desc(root.get(param)));
        }else{
            query.orderBy(builder.desc(root.get("createDate")));
        }
    }
    /**
     * 排序查询
     * @param root
     * @param query
     * @param builder
     * @param param
     * @param sort
     */
    public static void sortQuery(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder builder,String param,String param1,String sort){
        if(SORT_ASC.equals(sort) && StringUtils.isNotBlank(param)){
            if(StringUtils.isNotBlank(param1)){
                query.orderBy(builder.asc(root.get(param).get(param1)));
            }else {
                query.orderBy(builder.asc(root.get(param)));
            }
        }else if(SORT_DESC.equals(sort) && StringUtils.isNotBlank(param)){
            if(StringUtils.isNotBlank(param1)){
                query.orderBy(builder.desc(root.get(param).get(param1)));
            }else {
                query.orderBy(builder.desc(root.get(param)));
            }
        }/*else{
            query.orderBy(builder.desc(root.get("createTime")));
        }*/
    }
}
