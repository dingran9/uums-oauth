package com.eeduspace.uuims.oauth.persist.dao;


import com.eeduspace.uuims.oauth.persist.po.AuthPage;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

/**
 * Author: dingran
 * Date: 2015/10/22
 * Description:
 */
@Service
public class PageDao {

    @Resource(name = "entityManagerFactory")
    private EntityManagerFactory emf;

    /**
     * 分页
     * @param pageNo
     * @param pageSize
     * @param sql
     * @param params
     * @return
     * @throws Exception
     */
    public AuthPage findByPage(int pageNo, int pageSize,String sql,Object[] params) throws Exception {
        if (sql == null)
            throw new IllegalArgumentException("NULL is not a valid string");

        EntityManager em = emf.createEntityManager();

        String countSql = "select count(*) " + sql.substring(sql.indexOf("from"),sql.length());
        Query query = null;
        Query countQuery = null;
        if(params==null){
            query = em.createNativeQuery(sql);
            countQuery = em.createNativeQuery(countSql);
        }else{//增加参数
            query = em.createNativeQuery(sql);
            countQuery = em.createNativeQuery(countSql);
//            if((null != objs && objs.length > 0)){
//                for (int i = 0; i < objs.length; i++) {
//                    query.setParameter(i, objs[i]);
//                    countQuery.setParameter(i, objs[i]);
//                }
//            }

            int parameterIndex = 1;
            if (params != null && params.length > 0) {
                for (Object obj : params) {
                    query.setParameter(parameterIndex++, obj);
                }
            }
        }
        //将返回数据转换成map
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        query.setFirstResult(pageNo * pageSize).setMaxResults(pageSize);

        AuthPage page = new AuthPage();
        page.setList(query.getResultList());
        page.setTotalRecords(Integer.parseInt(countQuery.getResultList().get(0).toString()));
        return page;
    }


}
